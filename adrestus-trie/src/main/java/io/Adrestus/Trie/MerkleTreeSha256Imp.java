package io.Adrestus.Trie;

import io.Adrestus.crypto.HashUtil;
import io.Adrestus.util.ByteUtil;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class MerkleTreeSha256Imp implements MerkleTree {
    private static Logger LOG = LoggerFactory.getLogger(MerkleTreeSha256Imp.class);
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

    public MerkleTreeSha256Imp() {
        this.root = new MerkleNode("");
        this.MerkleProofs = new MerkleProofs();
    }

    public MerkleTreeSha256Imp(MerkleNode root) {
        this.root = root;
        this.MerkleProofs = new MerkleProofs();
    }

    @Override
    public void constructTree(List<MerkleNode> dataBlocks) {
        if (dataBlocks.isEmpty()) {
            return;
        }

        if (dataBlocks.size() % 2 == 0) {
            this.capacity = ByteUtil.log2(dataBlocks.size());
        } else {
            this.capacity = ByteUtil.log2(dataBlocks.size() + 1);
        }
        this.merkleNodeHashMapByHash = new ArrayList<>(this.capacity);
        this.merkleNodeHashMapByIndex = new ArrayList<>(this.capacity);
        for (int i = 0; i < this.capacity; i++) {
            this.merkleNodeHashMapByHash.add(new HashMap<>());
            this.merkleNodeHashMapByIndex.add(new HashMap<>());
        }

        AtomicInteger counter = new AtomicInteger(0);
        dataBlocks
                .stream()
                .peek(node -> node.setTransactionHash(HashUtil.sha256(node.getTransactionHash())))
                .collect(Collectors.teeing(
                        Collectors.toMap(
                                item -> counter.getAndIncrement(),
                                item -> item,
                                (oldValue, newValue) -> oldValue,
                                () -> merkleNodeHashMapByIndex.get(this.capacity - 1)
                        ),
                        Collectors.toMap(
                                item -> item.getTransactionHash(),
                                item -> item,
                                (oldValue, newValue) -> oldValue,
                                () -> merkleNodeHashMapByHash.get(this.capacity - 1)
                        ),
                        (m1, m2) -> {
                            merkleNodeHashMapByIndex.get(this.capacity - 1).putAll(m1);
                            merkleNodeHashMapByHash.get(this.capacity - 1).putAll(m2);
                            return null;
                        }
                ));

        ConstructMap(this.capacity - 1).result();
    }

    public RecursiveOptimizer<Integer> ConstructMap(int iterate) {
        if (iterate == 0) {
            String hash = HashUtil.sha256(this.merkleNodeHashMapByIndex.get(iterate).get(0).getTransactionHash() + this.merkleNodeHashMapByIndex.get(iterate).get(1).getTransactionHash());
            this.root.setTransactionHash(hash);
            this.root.setLeft(this.merkleNodeHashMapByIndex.get(iterate).get(0));
            this.root.setRight(this.merkleNodeHashMapByIndex.get(iterate).get(1));
            return RecursiveOptimizer.done(0);
        }

        int looper = 0;
        for (int i = 0; i < this.merkleNodeHashMapByIndex.get(iterate).size(); i += 2) {
            String hash = HashUtil.sha256(this.merkleNodeHashMapByIndex.get(iterate).get(i).getTransactionHash() + this.merkleNodeHashMapByIndex.get(iterate).get(i + 1).getTransactionHash());
            MerkleNode root = new MerkleNode(hash);
            MerkleNode left = this.merkleNodeHashMapByIndex.get(iterate).get(i);
            MerkleNode right = this.merkleNodeHashMapByIndex.get(iterate).get(i + 1);
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
        current.setTransactionHash(HashUtil.sha256(current.getTransactionHash()));
        if (merkleNodeHashMapByHash.get(this.capacity - 1).isEmpty()) {
            LOG.info("MerkleNodeHashMap is empty");
            return;
        }

        if (merkleNodeHashMapByHash.get(this.capacity - 1).get(current.getTransactionHash()) == null) {
            LOG.info("MerkleNode is not found with hash: " + current.getTransactionHash());
            return;
        }

        if (merkleNodeHashMapByHash.get(this.capacity - 1).size() == 1) {
            MerkleProofs.getProofs().put(LOWEST_INDEX_LEFT, current);
            return;
        }

        this.MerkleProofs = new MerkleProofs();
        int iteration = this.capacity - 1;
        MerkleNode low_root = this.merkleNodeHashMapByHash.get(iteration).get(current.getTransactionHash()).getRoot();
        MerkleProofs.getProofs().put(LOWEST_INDEX_LEFT, low_root.getLeft());
        MerkleProofs.getProofs().put(LOWEST_INDEX_RIGHT, low_root.getRight());
        iteration--;
        MerkleNode search = low_root;
        while (search.getRoot() != null) {
            MerkleNode root = this.merkleNodeHashMapByHash.get(iteration).get(search.getTransactionHash()).getRoot();
            if (root.getLeft().equals(search)) {
                MerkleProofs.getProofs().put(iteration, root.getRight());
            } else {
                MerkleProofs.getProofs().put(iteration, root.getLeft());
            }
            iteration--;
            search = root;
        }
        if (root.getLeft().equals(search)) {
            MerkleProofs.getProofs().put(iteration, root.getRight());
        } else {
            MerkleProofs.getProofs().put(iteration, root.getLeft());
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
            return proofs.getProofs().get(LOWEST_INDEX_LEFT).getTransactionHash();
        }
        String hash = HashUtil.sha256(proofs.getProofs().get(LOWEST_INDEX_LEFT).getTransactionHash() + proofs.getProofs().get(LOWEST_INDEX_RIGHT).getTransactionHash());

        if (proofs.getProofs().size() == 2) {
            return hash;
        }
        MerkleNode root = proofs.getProofs().get(LOWEST_INDEX_LEFT).getRoot().getRoot();
        for (int i = proofs.getProofs().size() - 3; i > 0; i--) {
            if (proofs.getProofs().get(i).equals(root.getLeft())) {
                hash = HashUtil.sha256(proofs.getProofs().get(i).getTransactionHash() + hash);
            } else {
                hash = HashUtil.sha256(hash + proofs.getProofs().get(i).getTransactionHash());
            }
            root = root.getRoot();
        }
        if (this.root.getLeft().equals(proofs.getProofs().get(0))) {
            hash = HashUtil.sha256(proofs.getProofs().get(0).getTransactionHash() + hash);
        } else {
            hash = HashUtil.sha256(hash + proofs.getProofs().get(0).getTransactionHash());
        }
        return hash;
    }

    @Override
    public boolean isMekleeNodeExisted(List<MerkleNode> list, String roothash, MerkleNode node) {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    @Override
    public MerkleProofs getMerkleeproofs() {
        return this.MerkleProofs;
    }

    @Override
    public String getRootHash() {
        return this.root.getTransactionHash();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MerkleTreeSha256Imp that = (MerkleTreeSha256Imp) o;
        return capacity == that.capacity && Objects.equals(MerkleProofs, that.MerkleProofs) && Objects.equals(merkleNodeHashMapByHash, that.merkleNodeHashMapByHash) && Objects.equals(merkleNodeHashMapByIndex, that.merkleNodeHashMapByIndex) && Objects.equals(root, that.root);
    }

    @Override
    public int hashCode() {
        return Objects.hash(MerkleProofs, merkleNodeHashMapByHash, merkleNodeHashMapByIndex, root, capacity);
    }

    @Override
    public String toString() {
        return "MerkleTreeSha256Imp{" +
                "MerkleProofs=" + MerkleProofs +
                ", merkleNodeHashMapByHash=" + merkleNodeHashMapByHash +
                ", merkleNodeHashMapByIndex=" + merkleNodeHashMapByIndex +
                ", root=" + root +
                ", capacity=" + capacity +
                '}';
    }
}
