package io.Adrestus.core;

import io.Adrestus.core.Trie.MerkleNode;
import io.Adrestus.core.Trie.MerkleProofs;
import io.Adrestus.core.Trie.MerkleTreeImp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class MerkleTreeTest {
    private MerkleTreeImp tree;

    @BeforeEach
    void setUp() throws Exception {
        tree = new MerkleTreeImp();
    }

    @Test
    public void merklee_tree() {
        List<MerkleNode> list1 = new ArrayList<MerkleNode>();
        MerkleNode node1 = new MerkleNode("E");
        for (int i = 0; i < 1000000; i++) {
            list1.add(new MerkleNode(String.valueOf(i)));
        }
        list1.add(node1);
        tree.my_generate(list1);
        assertEquals("72ad9ce53d35639b8576626e1365fd55221b544dfdd8d1fbf3faa86b034344be", tree.getRootHash());
    }

    @Test
    public void merklee_proofs_add() {
        List<MerkleNode> list = new ArrayList<MerkleNode>();
        for (int i = 0; i < 100000; i++) {
            MerkleNode node = new MerkleNode(String.valueOf(i));
            list.add(node);

        }
        tree.my_generate(list);
    }

    @Test
    public void merklee_proofs_search() {
        List<MerkleNode> list = new ArrayList<MerkleNode>();
        for (int i = 0; i < 100000; i++) {
            MerkleNode node = new MerkleNode(String.valueOf(i));
            list.add(node);

        }
        tree.my_generate(list);
        MerkleNode node = new MerkleNode(String.valueOf(1000));
        tree.build_proofs(list, node);
        MerkleProofs proofs = tree.getMerkleeproofs();
        assertEquals(tree.getRootHash(), tree.GenerateRoot(proofs));
    }

    @Test
    public void merklee_proofs_add_with_optimization() {
        List<MerkleNode> list = new ArrayList<MerkleNode>();
        for (int i = 0; i < 100000; i++) {
            MerkleNode node = new MerkleNode(String.valueOf(i));
            list.add(node);

        }
        tree.my_generate2(list);
    }

    @Test
    public void merklee_proofs_search_with_optimization() {
        List<MerkleNode> list = new ArrayList<MerkleNode>();
        for (int i = 0; i < 100000; i++) {
            MerkleNode node = new MerkleNode(String.valueOf(i));
            list.add(node);

        }
        tree.my_generate2(list);
        MerkleNode node = new MerkleNode(String.valueOf(1000));
        tree.build_proofs(list, node);
        MerkleProofs proofs = tree.getMerkleeproofs();
        assertEquals(tree.getRootHash(), tree.GenerateRoot(proofs));
    }

}
