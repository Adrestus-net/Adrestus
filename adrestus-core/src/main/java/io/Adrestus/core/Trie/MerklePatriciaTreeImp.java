package io.Adrestus.core.Trie;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.Adrestus.crypto.ByteUtil;
import io.Adrestus.crypto.FastByteComparisons;
import io.Adrestus.crypto.HashUtil;
import io.Adrestus.util.RLP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static io.Adrestus.crypto.EncodeUtil.encodeElement;
import static io.Adrestus.crypto.EncodeUtil.encodeList;
import static io.Adrestus.util.RLP.EMPTY_ELEMENT_RLP;
import static org.apache.commons.lang3.concurrent.ConcurrentUtils.constantFuture;

public class MerklePatriciaTreeImp implements PatriciaTree<byte[], byte[]> {

    private final static Object NULL_NODE = new Object();
    private final static int MIN_BRANCHES_CONCURRENTLY = 3;
    private static ExecutorService executor;

    private static final Logger logger = LoggerFactory.getLogger("state");

    public static ExecutorService getExecutor() {
        if (executor == null) {
            executor = Executors.newFixedThreadPool(4,
                    new ThreadFactoryBuilder().setNameFormat("trie-calc-thread-%d").build());
        }
        return executor;
    }

    public enum NodeType {
        BranchNode,
        KVNodeValue,
        KVNodeNode
    }

    public final class Node {
        private byte[] hash = null;
        private byte[] rlp = null;
        private RLP.LList parsedRlp = null;
        protected boolean dirty = false;

        private Object[] children = null;

        // new empty BranchNode
        public Node() {
            children = new Object[17];
            dirty = true;
        }

        // new KVNode with key and (value or node)
        public Node(TrieKey key, Object valueOrNode) {
            this(new Object[]{key, valueOrNode});
            dirty = true;
        }

        // new Node with hash or RLP
        public Node(byte[] hashOrRlp) {
            if (hashOrRlp.length == 32) {
                this.hash = hashOrRlp;
            } else {
                this.rlp = hashOrRlp;
            }
        }

        private Node(RLP.LList parsedRlp) {
            this.parsedRlp = parsedRlp;
            this.rlp = parsedRlp.getEncoded();
        }

        private Node(Object[] children) {
            this.children = children;
        }

        public boolean resolveCheck() {
            if (rlp != null || parsedRlp != null || hash == null) return true;
            rlp = getHash(hash);
            return rlp != null;
        }

        private void resolve() {
            if (!resolveCheck()) {
                logger.error("Invalid Trie state, can't resolve hash " + ByteUtil.toHexString(hash));
                throw new RuntimeException("Invalid Trie state, can't resolve hash " + ByteUtil.toHexString(hash));
            }
        }

        public byte[] encode() {
            return encode(1, true);
        }

        private byte[] encode(final int depth, boolean forceHash) {
            if (!dirty) {
                return hash != null ? encodeElement(hash) : rlp;
            } else {
                MerklePatriciaTreeImp.NodeType type = getType();
                byte[] ret;
                if (type == MerklePatriciaTreeImp.NodeType.BranchNode) {
                    if (depth == 1 && async) {
                        // parallelize encode() on the first trie level only and if there are at least
                        // MIN_BRANCHES_CONCURRENTLY branches are modified
                        final Object[] encoded = new Object[17];
                        int encodeCnt = 0;
                        for (int i = 0; i < 16; i++) {
                            final MerklePatriciaTreeImp.Node child = branchNodeGetChild(i);
                            if (child == null) {
                                encoded[i] = EMPTY_ELEMENT_RLP;
                            } else if (!child.dirty) {
                                encoded[i] = child.encode(depth + 1, false);
                            } else {
                                encodeCnt++;
                            }
                        }
                        for (int i = 0; i < 16; i++) {
                            if (encoded[i] == null) {
                                final MerklePatriciaTreeImp.Node child = branchNodeGetChild(i);
                                if (encodeCnt >= MIN_BRANCHES_CONCURRENTLY) {
                                    encoded[i] = getExecutor().submit(() -> child.encode(depth + 1, false));
                                } else {
                                    encoded[i] = child.encode(depth + 1, false);
                                }
                            }
                        }
                        byte[] value = branchNodeGetValue();
                        encoded[16] = constantFuture(encodeElement(value));
                        try {
                            ret = encodeRlpListFutures(encoded);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        byte[][] encoded = new byte[17][];
                        for (int i = 0; i < 16; i++) {
                            MerklePatriciaTreeImp.Node child = branchNodeGetChild(i);
                            encoded[i] = child == null ? EMPTY_ELEMENT_RLP : child.encode(depth + 1, false);
                        }
                        byte[] value = branchNodeGetValue();
                        encoded[16] = encodeElement(value);
                        ret = encodeList(encoded);
                    }
                } else if (type == MerklePatriciaTreeImp.NodeType.KVNodeNode) {
                    ret = encodeList(encodeElement(kvNodeGetKey().toPacked()), kvNodeGetChildNode().encode(depth + 1, false));
                } else {
                    byte[] value = kvNodeGetValue();
                    ret = encodeList(encodeElement(kvNodeGetKey().toPacked()),
                            encodeElement(value == null ? ByteUtil.EMPTY_BYTE_ARRAY : value));
                }
                if (hash != null) {
                    deleteHash(hash);
                }
                dirty = false;
                if (ret.length < 32 && !forceHash) {
                    rlp = ret;
                    return ret;
                } else {
                    hash = HashUtil.sha3(ret);
                    addHash(hash, ret);
                    return encodeElement(hash);
                }
            }
        }

        @SafeVarargs
        private final byte[] encodeRlpListFutures(Object... list) throws ExecutionException, InterruptedException {
            byte[][] vals = new byte[list.length][];
            for (int i = 0; i < list.length; i++) {
                if (list[i] instanceof Future) {
                    vals[i] = ((Future<byte[]>) list[i]).get();
                } else {
                    vals[i] = (byte[]) list[i];
                }
            }
            return encodeList(vals);
        }

        private void parse() {
            if (children != null) return;
            resolve();

            RLP.LList list = parsedRlp == null ? RLP.decodeLazyList(rlp) : parsedRlp;

            if (list.size() == 2) {
                children = new Object[2];
                TrieKey key = TrieKey.fromPacked(list.getBytes(0));
                children[0] = key;
                if (key.isTerminal()) {
                    children[1] = list.getBytes(1);
                } else {
                    children[1] = list.isList(1) ? new MerklePatriciaTreeImp.Node(list.getList(1)) : new MerklePatriciaTreeImp.Node(list.getBytes(1));
                }
            } else {
                children = new Object[17];
                parsedRlp = list;
            }
        }

        public MerklePatriciaTreeImp.Node branchNodeGetChild(int hex) {
            parse();
            assert getType() == MerklePatriciaTreeImp.NodeType.BranchNode;
            Object n = children[hex];
            if (n == null && parsedRlp != null) {
                if (parsedRlp.isList(hex)) {
                    n = new MerklePatriciaTreeImp.Node(parsedRlp.getList(hex));
                } else {
                    byte[] bytes = parsedRlp.getBytes(hex);
                    if (bytes.length == 0) {
                        n = NULL_NODE;
                    } else {
                        n = new MerklePatriciaTreeImp.Node(bytes);
                    }
                }
                children[hex] = n;
            }
            return n == NULL_NODE ? null : (MerklePatriciaTreeImp.Node) n;
        }

        public MerklePatriciaTreeImp.Node branchNodeSetChild(int hex, MerklePatriciaTreeImp.Node node) {
            parse();
            assert getType() == MerklePatriciaTreeImp.NodeType.BranchNode;
            children[hex] = node == null ? NULL_NODE : node;
            dirty = true;
            return this;
        }

        public byte[] branchNodeGetValue() {
            parse();
            assert getType() == MerklePatriciaTreeImp.NodeType.BranchNode;
            Object n = children[16];
            if (n == null && parsedRlp != null) {
                byte[] bytes = parsedRlp.getBytes(16);
                if (bytes.length == 0) {
                    n = NULL_NODE;
                } else {
                    n = bytes;
                }
                children[16] = n;
            }
            return n == NULL_NODE ? null : (byte[]) n;
        }

        public MerklePatriciaTreeImp.Node branchNodeSetValue(byte[] val) {
            parse();
            assert getType() == MerklePatriciaTreeImp.NodeType.BranchNode;
            children[16] = val == null ? NULL_NODE : val;
            dirty = true;
            return this;
        }

        public int branchNodeCompactIdx() {
            parse();
            assert getType() == MerklePatriciaTreeImp.NodeType.BranchNode;
            int cnt = 0;
            int idx = -1;
            for (int i = 0; i < 16; i++) {
                if (branchNodeGetChild(i) != null) {
                    cnt++;
                    idx = i;
                    if (cnt > 1) return -1;
                }
            }
            return cnt > 0 ? idx : (branchNodeGetValue() == null ? -1 : 16);
        }

        public boolean branchNodeCanCompact() {
            parse();
            assert getType() == MerklePatriciaTreeImp.NodeType.BranchNode;
            int cnt = 0;
            for (int i = 0; i < 16; i++) {
                cnt += branchNodeGetChild(i) == null ? 0 : 1;
                if (cnt > 1) return false;
            }
            return cnt == 0 || branchNodeGetValue() == null;
        }

        public TrieKey kvNodeGetKey() {
            parse();
            assert getType() != MerklePatriciaTreeImp.NodeType.BranchNode;
            return (TrieKey) children[0];
        }

        public MerklePatriciaTreeImp.Node kvNodeGetChildNode() {
            parse();
            assert getType() == MerklePatriciaTreeImp.NodeType.KVNodeNode;
            return (MerklePatriciaTreeImp.Node) children[1];
        }

        public byte[] kvNodeGetValue() {
            parse();
            assert getType() == MerklePatriciaTreeImp.NodeType.KVNodeValue;
            return (byte[]) children[1];
        }

        public MerklePatriciaTreeImp.Node kvNodeSetValue(byte[] value) {
            parse();
            assert getType() == MerklePatriciaTreeImp.NodeType.KVNodeValue;
            children[1] = value;
            dirty = true;
            return this;
        }

        public Object kvNodeGetValueOrNode() {
            parse();
            assert getType() != MerklePatriciaTreeImp.NodeType.BranchNode;
            return children[1];
        }

        public MerklePatriciaTreeImp.Node kvNodeSetValueOrNode(Object valueOrNode) {
            parse();
            assert getType() != MerklePatriciaTreeImp.NodeType.BranchNode;
            children[1] = valueOrNode;
            dirty = true;
            return this;
        }

        public MerklePatriciaTreeImp.NodeType getType() {
            parse();

            return children.length == 17 ? MerklePatriciaTreeImp.NodeType.BranchNode :
                    (children[1] instanceof MerklePatriciaTreeImp.Node ? MerklePatriciaTreeImp.NodeType.KVNodeNode : MerklePatriciaTreeImp.NodeType.KVNodeValue);
        }

        public void dispose() {
            if (hash != null) {
                deleteHash(hash);
            }
        }

        public MerklePatriciaTreeImp.Node invalidate() {
            dirty = true;
            return this;
        }

        /***********  Dump methods  ************/

        public String dumpStruct(String indent, String prefix) {
            String ret = indent + prefix + getType() + (dirty ? " *" : "") +
                    (hash == null ? "" : "(hash: " + Hex.toHexString(hash).substring(0, 6) + ")");
            if (getType() == MerklePatriciaTreeImp.NodeType.BranchNode) {
                byte[] value = branchNodeGetValue();
                ret += (value == null ? "" : " [T] = " + Hex.toHexString(value)) + "\n";
                for (int i = 0; i < 16; i++) {
                    MerklePatriciaTreeImp.Node child = branchNodeGetChild(i);
                    if (child != null) {
                        ret += child.dumpStruct(indent + "  ", "[" + i + "] ");
                    }
                }

            } else if (getType() == MerklePatriciaTreeImp.NodeType.KVNodeNode) {
                ret += " [" + kvNodeGetKey() + "]\n";
                ret += kvNodeGetChildNode().dumpStruct(indent + "  ", "");
            } else {
                ret += " [" + kvNodeGetKey() + "] = " + Hex.toHexString(kvNodeGetValue()) + "\n";
            }
            return ret;
        }

      /*  public List<String> dumpTrieNode(boolean compact) {
            List<String> ret = new ArrayList<>();
            if (hash != null) {
                ret.add(hash2str(hash, compact) + " ==> " + dumpContent(false, compact));
            }

            if (getType() == MerkleePatriciaTree.NodeType.BranchNode) {
                for (int i = 0; i < 16; i++) {
                    MerkleePatriciaTree.Node child = branchNodeGetChild(i);
                    if (child != null) ret.addAll(child.dumpTrieNode(compact));
                }
            } else if (getType() == MerkleePatriciaTree.NodeType.KVNodeNode) {
                ret.addAll(kvNodeGetChildNode().dumpTrieNode(compact));
            }
            return ret;
        }*/

        private String dumpContent(boolean recursion, boolean compact) {
            if (recursion && hash != null) return hash2str(hash, compact);
            String ret;
            if (getType() == MerklePatriciaTreeImp.NodeType.BranchNode) {
                ret = "[";
                for (int i = 0; i < 16; i++) {
                    MerklePatriciaTreeImp.Node child = branchNodeGetChild(i);
                    ret += i == 0 ? "" : ",";
                    ret += child == null ? "" : child.dumpContent(true, compact);
                }
                byte[] value = branchNodeGetValue();
                ret += value == null ? "" : ", " + val2str(value, compact);
                ret += "]";
            } else if (getType() == MerklePatriciaTreeImp.NodeType.KVNodeNode) {
                ret = "[<" + kvNodeGetKey() + ">, " + kvNodeGetChildNode().dumpContent(true, compact) + "]";
            } else {
                ret = "[<" + kvNodeGetKey() + ">, " + val2str(kvNodeGetValue(), compact) + "]";
            }
            return ret;
        }

        @Override
        public String toString() {
            return getType() + (dirty ? " *" : "") + (hash == null ? "" : "(hash: " + ByteUtil.toHexString(hash) + " )");
        }
    }

    public interface ScanAction {

        void doOnNode(byte[] hash, MerklePatriciaTreeImp.Node node);

        void doOnValue(byte[] nodeHash, MerklePatriciaTreeImp.Node node, byte[] key, byte[] value);
    }

    private final HashMapDB cache;
    private MerklePatriciaTreeImp.Node root;
    private boolean async = true;

    public MerklePatriciaTreeImp() {
        this(null);
    }

    public MerklePatriciaTreeImp(byte[] root) {
        this(new HashMapDB<byte[]>(), root);
    }


    public MerklePatriciaTreeImp(HashMapDB cache, byte[] root) {
        this.cache = cache;
        setRoot(root);
    }

    public void setAsync(boolean async) {
        this.async = async;
    }

    private void encode() {
        if (root != null) {
            root.encode();
        }
    }

    public void setRoot(byte[] root) {
        if (root != null && !FastByteComparisons.equal(root, HashUtil.EMPTY_TRIE_HASH)) {
            this.root = new MerklePatriciaTreeImp.Node(root);
        } else {
            this.root = null;
        }

    }

    private boolean hasRoot() {
        return root != null && root.resolveCheck();
    }

    public HashMapDB getCache() {
        return cache;
    }

    private byte[] getHash(byte[] hash) {
        return cache.get(hash);
    }

    private void addHash(byte[] hash, byte[] ret) {
        cache.put(hash, ret);
    }

    private void deleteHash(byte[] hash) {
        cache.delete(hash);
    }


    public byte[] get(byte[] key) {
        if (!hasRoot()) return null; // treating unknown root hash as empty trie
        TrieKey k = TrieKey.fromNormal(key);
        return get(root, k);
    }

    private byte[] get(MerklePatriciaTreeImp.Node n, TrieKey k) {
        if (n == null) return null;

        MerklePatriciaTreeImp.NodeType type = n.getType();
        if (type == MerklePatriciaTreeImp.NodeType.BranchNode) {
            if (k.isEmpty()) return n.branchNodeGetValue();
            MerklePatriciaTreeImp.Node childNode = n.branchNodeGetChild(k.getHex(0));
            return get(childNode, k.shift(1));
        } else {
            TrieKey k1 = k.matchAndShift(n.kvNodeGetKey());
            if (k1 == null) return null;
            if (type == MerklePatriciaTreeImp.NodeType.KVNodeValue) {
                return k1.isEmpty() ? n.kvNodeGetValue() : null;
            } else {
                return get(n.kvNodeGetChildNode(), k1);
            }
        }
    }

    public void put(byte[] key, byte[] value) {
        TrieKey k = TrieKey.fromNormal(key);
        if (root == null) {
            if (value != null && value.length > 0) {
                root = new MerklePatriciaTreeImp.Node(k, value);
            }
        } else {
            if (value == null || value.length == 0) {
                root = delete(root, k);
            } else {
                root = insert(root, k, value);
            }
        }
    }

    private MerklePatriciaTreeImp.Node insert(MerklePatriciaTreeImp.Node n, TrieKey k, Object nodeOrValue) {
        MerklePatriciaTreeImp.NodeType type = n.getType();
        if (type == MerklePatriciaTreeImp.NodeType.BranchNode) {
            if (k.isEmpty()) return n.branchNodeSetValue((byte[]) nodeOrValue);
            MerklePatriciaTreeImp.Node childNode = n.branchNodeGetChild(k.getHex(0));
            if (childNode != null) {
                return n.branchNodeSetChild(k.getHex(0), insert(childNode, k.shift(1), nodeOrValue));
            } else {
                TrieKey childKey = k.shift(1);
                MerklePatriciaTreeImp.Node newChildNode;
                if (!childKey.isEmpty()) {
                    newChildNode = new MerklePatriciaTreeImp.Node(childKey, nodeOrValue);
                } else {
                    newChildNode = nodeOrValue instanceof MerklePatriciaTreeImp.Node ?
                            (MerklePatriciaTreeImp.Node) nodeOrValue : new MerklePatriciaTreeImp.Node(childKey, nodeOrValue);
                }
                return n.branchNodeSetChild(k.getHex(0), newChildNode);
            }
        } else {
            TrieKey currentNodeKey = n.kvNodeGetKey();
            TrieKey commonPrefix = k.getCommonPrefix(currentNodeKey);
            if (commonPrefix.isEmpty()) {
                MerklePatriciaTreeImp.Node newBranchNode = new MerklePatriciaTreeImp.Node();
                insert(newBranchNode, currentNodeKey, n.kvNodeGetValueOrNode());
                insert(newBranchNode, k, nodeOrValue);
                n.dispose();
                return newBranchNode;
            } else if (commonPrefix.equals(k)) {
                return n.kvNodeSetValueOrNode(nodeOrValue);
            } else if (commonPrefix.equals(currentNodeKey)) {
                insert(n.kvNodeGetChildNode(), k.shift(commonPrefix.getLength()), nodeOrValue);
                return n.invalidate();
            } else {
                MerklePatriciaTreeImp.Node newBranchNode = new MerklePatriciaTreeImp.Node();
                MerklePatriciaTreeImp.Node newKvNode = new MerklePatriciaTreeImp.Node(commonPrefix, newBranchNode);
                // TODO can be optimized
                insert(newKvNode, currentNodeKey, n.kvNodeGetValueOrNode());
                insert(newKvNode, k, nodeOrValue);
                n.dispose();
                return newKvNode;
            }
        }
    }

    @Override
    public void delete(byte[] key) {
        TrieKey k = TrieKey.fromNormal(key);
        if (root != null) {
            root = delete(root, k);
        }
    }

    private MerklePatriciaTreeImp.Node delete(MerklePatriciaTreeImp.Node n, TrieKey k) {
        MerklePatriciaTreeImp.NodeType type = n.getType();
        MerklePatriciaTreeImp.Node newKvNode;
        if (type == MerklePatriciaTreeImp.NodeType.BranchNode) {
            if (k.isEmpty()) {
                n.branchNodeSetValue(null);
            } else {
                int idx = k.getHex(0);
                MerklePatriciaTreeImp.Node child = n.branchNodeGetChild(idx);
                if (child == null) return n; // no key found

                MerklePatriciaTreeImp.Node newNode = delete(child, k.shift(1));
                n.branchNodeSetChild(idx, newNode);
                if (newNode != null) return n; // newNode != null thus number of children didn't decrease
            }

            // child node or value was deleted and the branch node may need to be compacted
            int compactIdx = n.branchNodeCompactIdx();
            if (compactIdx < 0) return n; // no compaction is required

            // only value or a single child left - compact branch node to kvNode
            n.dispose();
            if (compactIdx == 16) { // only value left
                return new MerklePatriciaTreeImp.Node(TrieKey.empty(true), n.branchNodeGetValue());
            } else { // only single child left
                newKvNode = new MerklePatriciaTreeImp.Node(TrieKey.singleHex(compactIdx), n.branchNodeGetChild(compactIdx));
            }
        } else { // n - kvNode
            TrieKey k1 = k.matchAndShift(n.kvNodeGetKey());
            if (k1 == null) {
                // no key found
                return n;
            } else if (type == MerklePatriciaTreeImp.NodeType.KVNodeValue) {
                if (k1.isEmpty()) {
                    // delete this kvNode
                    n.dispose();
                    return null;
                } else {
                    // else no key found
                    return n;
                }
            } else {
                MerklePatriciaTreeImp.Node newChild = delete(n.kvNodeGetChildNode(), k1);
                if (newChild == null) throw new RuntimeException("Shouldn't happen");
                newKvNode = n.kvNodeSetValueOrNode(newChild);
            }
        }

        // if we get here a new kvNode was created, now need to check
        // if it should be compacted with child kvNode
        MerklePatriciaTreeImp.Node newChild = newKvNode.kvNodeGetChildNode();
        if (newChild.getType() != MerklePatriciaTreeImp.NodeType.BranchNode) {
            // two kvNodes should be compacted into a single one
            TrieKey newKey = newKvNode.kvNodeGetKey().concat(newChild.kvNodeGetKey());
            MerklePatriciaTreeImp.Node newNode = new MerklePatriciaTreeImp.Node(newKey, newChild.kvNodeGetValueOrNode());
            newChild.dispose();
            newKvNode.dispose();
            return newNode;
        } else {
            // no compaction needed
            return newKvNode;
        }
    }

    @Override
    public byte[] getRootHash() {
        encode();
        return root != null ? root.hash : HashUtil.EMPTY_TRIE_HASH;
    }

    @Override
    public void clear() {
        throw new RuntimeException("Not implemented yet");
    }

    @Override
    public boolean flush() {
        if (root != null && root.dirty) {
            // persist all dirty nodes to underlying Source
            encode();
            // release all Trie Node instances for GC
            root = new MerklePatriciaTreeImp.Node(root.hash);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MerklePatriciaTreeImp merkleePatriciaTree1 = (MerklePatriciaTreeImp) o;

        return FastByteComparisons.equal(getRootHash(), merkleePatriciaTree1.getRootHash());

    }

    public String dumpStructure() {
        return root == null ? "<empty>" : root.dumpStruct("", "");
    }


    public void scanTree(MerklePatriciaTreeImp.ScanAction scanAction) {
        scanTree(root, TrieKey.empty(false), scanAction);
    }

    public void scanTree(MerklePatriciaTreeImp.Node node, TrieKey k, MerklePatriciaTreeImp.ScanAction scanAction) {
        if (node == null) return;
        if (node.hash != null) {
            scanAction.doOnNode(node.hash, node);
        }
        if (node.getType() == MerklePatriciaTreeImp.NodeType.BranchNode) {
            if (node.branchNodeGetValue() != null)
                scanAction.doOnValue(node.hash, node, k.toNormal(), node.branchNodeGetValue());
            for (int i = 0; i < 16; i++) {
                scanTree(node.branchNodeGetChild(i), k.concat(TrieKey.singleHex(i)), scanAction);
            }
        } else if (node.getType() == MerklePatriciaTreeImp.NodeType.KVNodeNode) {
            scanTree(node.kvNodeGetChildNode(), k.concat(node.kvNodeGetKey()), scanAction);
        } else {
            scanAction.doOnValue(node.hash, node, k.concat(node.kvNodeGetKey()).toNormal(), node.kvNodeGetValue());
        }
    }


    private static String hash2str(byte[] hash, boolean shortHash) {
        String ret = Hex.toHexString(hash);
        return "0x" + (shortHash ? ret.substring(0, 8) : ret);
    }

    private static String val2str(byte[] val, boolean shortHash) {
        String ret = Hex.toHexString(val);
        if (val.length > 16) {
            ret = ret.substring(0, 10) + "... len " + val.length;
        }
        return "\"" + ret + "\"";
    }
}
