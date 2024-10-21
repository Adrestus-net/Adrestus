package io.Adrestus.Trie;

import io.Adrestus.Trie.optimize64_trie.ProfKey;
import io.Adrestus.crypto.HashUtil;
import io.Adrestus.util.ByteUtil;
import io.activej.serializer.annotations.Serialize;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.*;

public class MerkleTreeOptimizedImp implements Serializable, MerkleTree {
    private static Logger LOG = LoggerFactory.getLogger(MerkleTreeOptimizedImp.class);
    private static final String DUPLICATE = "_DUPLICATE";
    private static int LOWEST_INDEX_LEFT = 201;
    private static int LOWEST_INDEX_RIGHT = 202;

    @Setter
    @Getter
    private MerkleProofs MerkleProofs;
    @Setter
    @Getter
    private ArrayList<Map<String, MerkleNode>> merkleNodeHashMapByHash;
    @Setter
    @Getter
    private ArrayList<Map<Integer, MerkleNode>> merkleNodeHashMapByIndex;
    @Setter
    @Getter
    private MerkleNode root;
    @Setter
    @Getter
    private int capacity;

    public MerkleTreeOptimizedImp() {
        this.root = new MerkleNode("");
        this.MerkleProofs = new MerkleProofs();
    }

    public MerkleTreeOptimizedImp(MerkleNode root) {
        this.root = root;
        this.MerkleProofs = new MerkleProofs();
    }

    @Override
    public void constructTree(List<MerkleNode> dataBlocks) {
        if (dataBlocks.isEmpty()) {
            return;
        }

        if (dataBlocks.size() == 1) {
            this.capacity = 1;
            this.merkleNodeHashMapByHash = new ArrayList<>(this.capacity);
            this.merkleNodeHashMapByIndex = new ArrayList<>(this.capacity);
            for (int i = 0; i < this.capacity; i++) {
                this.merkleNodeHashMapByHash.add(new HashMap<>());
                this.merkleNodeHashMapByIndex.add(new HashMap<>());
            }
            String left = HashUtil.XXH3(dataBlocks.get(0).getTransactionHash());
            String right = HashUtil.XXH3(dataBlocks.get(0).getTransactionHash());
            String hash = HashUtil.XXH3(left + right);
            MerkleNode leftMerkleNode = new MerkleNode(left);
            MerkleNode rightMerkleNode = new MerkleNode(right);
            leftMerkleNode.setRoot(this.root);
            rightMerkleNode.setRoot(this.root);
            this.root.setTransactionHash(hash);
            this.root.setLeft(leftMerkleNode);
            this.root.setRight(rightMerkleNode);

            this.merkleNodeHashMapByHash.get(0).put(leftMerkleNode.getTransactionHash(), leftMerkleNode);
            this.merkleNodeHashMapByIndex.get(0).put(0, leftMerkleNode);
            this.merkleNodeHashMapByHash.get(0).put(rightMerkleNode.getTransactionHash() + DUPLICATE, rightMerkleNode);
            this.merkleNodeHashMapByIndex.get(0).put(1, rightMerkleNode);

            return;
        } else if (dataBlocks.size() == 2) {
            this.capacity = 1;
            this.merkleNodeHashMapByHash = new ArrayList<>(this.capacity);
            this.merkleNodeHashMapByIndex = new ArrayList<>(this.capacity);
            for (int i = 0; i < this.capacity; i++) {
                this.merkleNodeHashMapByHash.add(new HashMap<>());
                this.merkleNodeHashMapByIndex.add(new HashMap<>());
            }
            String left = HashUtil.XXH3(dataBlocks.get(0).getTransactionHash());
            String right = HashUtil.XXH3(dataBlocks.get(1).getTransactionHash());
            String hash = HashUtil.XXH3(left + right);
            MerkleNode leftMerkleNode = new MerkleNode(left);
            MerkleNode rightMerkleNode = new MerkleNode(right);
            leftMerkleNode.setRoot(this.root);
            rightMerkleNode.setRoot(this.root);
            this.root.setTransactionHash(hash);
            this.root.setLeft(leftMerkleNode);
            this.root.setRight(rightMerkleNode);

            this.merkleNodeHashMapByHash.get(0).put(leftMerkleNode.getTransactionHash(), leftMerkleNode);
            this.merkleNodeHashMapByIndex.get(0).put(0, leftMerkleNode);
            this.merkleNodeHashMapByHash.get(0).put(rightMerkleNode.getTransactionHash(), rightMerkleNode);
            this.merkleNodeHashMapByIndex.get(0).put(1, rightMerkleNode);

            return;
        }

        this.capacity = ByteUtil.log2(ByteUtil.nextPowerOfTwo((dataBlocks.size())));
        this.merkleNodeHashMapByHash = new ArrayList<>(this.capacity);
        this.merkleNodeHashMapByIndex = new ArrayList<>(this.capacity);
        for (int i = 0; i < this.capacity; i++) {
            this.merkleNodeHashMapByHash.add(new HashMap<>());
            this.merkleNodeHashMapByIndex.add(new HashMap<>());
        }

        for (int i = 0; i < dataBlocks.size(); i++) {
            MerkleNode node = dataBlocks.get(i).clone();
            node.setTransactionHash(HashUtil.XXH3(node.getTransactionHash()));
            this.merkleNodeHashMapByIndex.get(this.capacity - 1).put(i, node);
            this.merkleNodeHashMapByHash.get(this.capacity - 1).put(node.getTransactionHash(), node);
        }

        int looper = ByteUtil.nextPowerOfTwo(dataBlocks.size()) - dataBlocks.size();
        for (int i = 0; i < looper; i++) {
            MerkleNode node = new MerkleNode(DUPLICATE + "_" + String.valueOf(i));
            merkleNodeHashMapByHash.get(this.capacity - 1).put(node.getTransactionHash(), node);
            merkleNodeHashMapByIndex.get(this.capacity - 1).put(merkleNodeHashMapByIndex.get(this.capacity - 1).size(), node);
        }
        ConstructMap(this.capacity - 1).result();
    }

    public RecursiveOptimizer<Integer> ConstructMap(int iterate) {
        if (iterate == 0) {
            String hash = HashUtil.XXH3(this.merkleNodeHashMapByIndex.get(iterate).get(0).getTransactionHash() + this.merkleNodeHashMapByIndex.get(iterate).get(1).getTransactionHash());
            this.root.setTransactionHash(hash);
            this.root.setLeft(this.merkleNodeHashMapByIndex.get(iterate).get(0));
            this.root.setRight(this.merkleNodeHashMapByIndex.get(iterate).get(1));
            return RecursiveOptimizer.done(0);
        }

        int looper = 0;
        for (int i = 0; i < this.merkleNodeHashMapByIndex.get(iterate).size(); i += 2) {
            MerkleNode left = this.merkleNodeHashMapByIndex.get(iterate).get(i);
            MerkleNode right = this.merkleNodeHashMapByIndex.get(iterate).get(i + 1);
            String hash = HashUtil.XXH3(left.getTransactionHash() + right.getTransactionHash());
            MerkleNode root = new MerkleNode(hash);
            root.setTransactionHash(hash);
            root.setLeft(left);
            root.setRight(right);
            left.setRoot(root);
            right.setRoot(root);
            this.merkleNodeHashMapByIndex.get(iterate - 1).put(looper, root);
            this.merkleNodeHashMapByHash.get(iterate - 1).put(root.getTransactionHash(), root);
            looper++;
        }
        iterate--;
        int finalIterate = iterate;
        return RecursiveOptimizer.more(() -> ConstructMap(finalIterate));
    }

    @Override
    public void build_proofs(MerkleNode current) {
        if (current == null) {
            throw new IllegalArgumentException("MerkleNode is null");
        }
        if (merkleNodeHashMapByHash.get(this.capacity - 1).isEmpty()) {
            LOG.info("MerkleNodeHashMap is empty");
            return;
        }

        current.setTransactionHash(HashUtil.XXH3(current.getTransactionHash()));
        if (merkleNodeHashMapByHash.get(this.capacity - 1).get(current.getTransactionHash()) == null) {
            LOG.info("MerkleNode is not found with hash: " + current.getTransactionHash());
            throw new IllegalArgumentException("MerkleNode is not found with hash: " + current.getTransactionHash());
        }

        if (merkleNodeHashMapByHash.get(this.capacity - 1).size() == 1) {
            MerkleProofs.getProofs().put(new ProfKey(LOWEST_INDEX_LEFT, true), current);
            return;
        }

        this.MerkleProofs = new MerkleProofs();
        int iteration = this.capacity - 1;
        MerkleNode low_root = this.merkleNodeHashMapByHash.get(iteration).get(current.getTransactionHash()).getRoot();
        MerkleProofs.getProofs().put(new ProfKey(LOWEST_INDEX_LEFT, true), new MerkleNode(low_root.getLeft().getTransactionHash()));
        MerkleProofs.getProofs().put(new ProfKey(LOWEST_INDEX_RIGHT, false), new MerkleNode(low_root.getRight().getTransactionHash()));
        if (this.capacity < 2)
            return;
        iteration--;
        MerkleNode search = low_root;
        while (search.getRoot() != null) {
            MerkleNode root = this.merkleNodeHashMapByHash.get(iteration).get(search.getTransactionHash()).getRoot();
            if (root.getLeft().equals(search)) {
                MerkleProofs.getProofs().put(new ProfKey(iteration, false), new MerkleNode(root.getRight().getTransactionHash()));
            } else {
                MerkleProofs.getProofs().put(new ProfKey(iteration, true), new MerkleNode(root.getLeft().getTransactionHash()));
            }
            iteration--;
            search = root;
        }
        if (root.getLeft().equals(search)) {
            MerkleProofs.getProofs().put(new ProfKey(iteration, false), new MerkleNode(root.getRight().getTransactionHash()));
        } else {
            MerkleProofs.getProofs().put(new ProfKey(iteration, true), new MerkleNode(root.getLeft().getTransactionHash()));
        }
    }


    @Override
    public void clear() {
        this.MerkleProofs.clear();
        this.merkleNodeHashMapByHash.forEach(Map::clear);
        this.merkleNodeHashMapByHash.clear();
        this.merkleNodeHashMapByIndex.forEach(Map::clear);
        this.merkleNodeHashMapByIndex.clear();
        this.clearMerkleNode(this.root);
    }

    private void clearMerkleNode(MerkleNode node) {
        if (node == null) {
            return;
        }
        clearMerkleNode(node.getLeft());
        clearMerkleNode(node.getRight());
        node.setLeft(null);
        node.setRight(null);
        node.setTransactionHash(null);
    }

    @Override
    public String generateRoot(MerkleProofs proofs) {
        if (proofs.getProofs().isEmpty()) {
            throw new IllegalArgumentException("Proofs is empty");
        }
        if (proofs.getProofs().size() == 1) {
            return proofs.getProofs().get(new ProfKey(LOWEST_INDEX_LEFT, true)).getTransactionHash();
        }
        String hash = HashUtil.XXH3(proofs.getProofs().get(new ProfKey(LOWEST_INDEX_LEFT, true)).getTransactionHash() + proofs.getProofs().get(new ProfKey(LOWEST_INDEX_RIGHT, false)).getTransactionHash());

        if (proofs.getProofs().size() < 2) {
            return hash;
        }

        int skip = 0;
        for (Map.Entry<ProfKey, MerkleNode> entry : proofs.getProofs().entrySet()) {
            if (skip < 2) {
                skip++;
                continue;
            }
            ProfKey key = entry.getKey();
            MerkleNode node = entry.getValue();
            if (key.isLeft()) {
                hash = HashUtil.XXH3(node.getTransactionHash() + hash);
            } else {
                hash = HashUtil.XXH3(hash + node.getTransactionHash());
            }
        }
        return hash;
    }

    @Override
    public boolean isMekleeNodeExisted(List<MerkleNode> list, String roothash, MerkleNode node) {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    @Serialize
    public static Logger getLOG() {
        return LOG;
    }

    public static void setLOG(Logger LOG) {
        MerkleTreeOptimizedImp.LOG = LOG;
    }

    @Serialize
    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    @Serialize
    public MerkleNode getRoot() {
        return root;
    }

    public void setRoot(MerkleNode root) {
        this.root = root;
    }

    @Serialize
    public ArrayList<Map<Integer, MerkleNode>> getMerkleNodeHashMapByIndex() {
        return merkleNodeHashMapByIndex;
    }

    public void setMerkleNodeHashMapByIndex(ArrayList<Map<Integer, MerkleNode>> merkleNodeHashMapByIndex) {
        this.merkleNodeHashMapByIndex = merkleNodeHashMapByIndex;
    }

    @Serialize
    public ArrayList<Map<String, MerkleNode>> getMerkleNodeHashMapByHash() {
        return merkleNodeHashMapByHash;
    }

    public void setMerkleNodeHashMapByHash(ArrayList<Map<String, MerkleNode>> merkleNodeHashMapByHash) {
        this.merkleNodeHashMapByHash = merkleNodeHashMapByHash;
    }

    @Serialize
    public MerkleProofs getMerkleProofs() {
        return MerkleProofs;
    }

    public void setMerkleProofs(io.Adrestus.Trie.MerkleProofs merkleProofs) {
        MerkleProofs = merkleProofs;
    }

    @Serialize
    public static int getLowestIndexRight() {
        return LOWEST_INDEX_RIGHT;
    }

    public static void setLowestIndexRight(int lowestIndexRight) {
        LOWEST_INDEX_RIGHT = lowestIndexRight;
    }

    @Serialize
    public static int getLowestIndexLeft() {
        return LOWEST_INDEX_LEFT;
    }

    public static void setLowestIndexLeft(int lowestIndexLeft) {
        LOWEST_INDEX_LEFT = lowestIndexLeft;
    }

    @Serialize
    @Override
    public MerkleProofs getMerkleeproofs() {
        return this.MerkleProofs;
    }

    @Serialize
    @Override
    public String getRootHash() {
        return this.root.getTransactionHash();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MerkleTreeOptimizedImp that = (MerkleTreeOptimizedImp) o;
        return capacity == that.capacity && Objects.equals(MerkleProofs, that.MerkleProofs) && Objects.equals(merkleNodeHashMapByHash, that.merkleNodeHashMapByHash) && Objects.equals(merkleNodeHashMapByIndex, that.merkleNodeHashMapByIndex) && Objects.equals(root, that.root);
    }

    @Override
    public int hashCode() {
        return Objects.hash(MerkleProofs, merkleNodeHashMapByHash, merkleNodeHashMapByIndex, root, capacity);
    }

    //Never Delete it
    @Override
    public String toString() {
        return "MerkleTreePlainImp{" +
                "MerkleProofs=" + (MerkleProofs != null ? MerkleProofs.hashCode() : "null") +
                ", merkleNodeHashMapByHash size=" + (merkleNodeHashMapByHash != null ? merkleNodeHashMapByHash.size() : "null") +
                ", merkleNodeHashMapByIndex size=" + (merkleNodeHashMapByIndex != null ? merkleNodeHashMapByIndex.size() : "null") +
                ", root transactionHash=" + (root != null ? root.getTransactionHash() : "null") +
                ", capacity=" + capacity +
                '}';
    }
}
