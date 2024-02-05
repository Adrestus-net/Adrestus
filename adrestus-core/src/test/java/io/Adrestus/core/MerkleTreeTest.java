package io.Adrestus.core;

import io.Adrestus.Trie.MerkleNode;
import io.Adrestus.Trie.MerkleProofs;
import io.Adrestus.Trie.MerkleTreeImp;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class MerkleTreeTest {
    private static MerkleTreeImp tree;

    @BeforeAll
    private static void setUp() throws Exception {
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
        MerkleTreeImp tree2=new MerkleTreeImp();
        for (int i = 0; i < 100000; i++) {
            MerkleNode node = new MerkleNode(String.valueOf(i));
            list.add(node);

        }
        tree.my_generate(list);
        MerkleNode node = new MerkleNode(String.valueOf(1000));
        tree.build_proofs(list, node);
        MerkleProofs proofs = tree.getMerkleeproofs();
        assertEquals(tree.getRootHash(), tree.GenerateRoot(proofs));
        assertEquals(tree.getRootHash(), tree2.GenerateRoot(proofs));
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


    //faster approach
    @Test
    public void merklee_proofs_search_with_optimization() {
        tree = new MerkleTreeImp();
        List<MerkleNode> list = new ArrayList<MerkleNode>();
        for (int i = 0; i < 100000; i++) {
            MerkleNode node = new MerkleNode(String.valueOf(i));
            list.add(node);

        }
        tree.my_generate2(list);
        String hash = tree.getRootHash();
        MerkleNode node = new MerkleNode(String.valueOf(1000));
        tree.build_proofs(list, node);
        MerkleProofs proofs = tree.getMerkleeproofs();
        assertEquals(tree.getRootHash(), tree.GenerateRoot(proofs));
        System.out.println(tree.getRootHash());
        System.out.println(tree.GenerateRoot(proofs));
        MerkleNode node1 = new MerkleNode(String.valueOf(1000));
        tree.setRoot();
        tree.my_generate2(list);
        tree.build_proofs(list, node1);
        MerkleProofs proofs1 = tree.getMerkleeproofs();
        assertEquals(hash, tree.GenerateRoot(proofs1));
    }

    @Test
    public void merklee_proofs_search_with_optimization2() {
        tree = new MerkleTreeImp();
        List<MerkleNode> list = new ArrayList<MerkleNode>();
        for (int i = 0; i < 100000; i++) {
            MerkleNode node = new MerkleNode(String.valueOf(i));
            list.add(node);

        }
        tree.my_generate2(list);
        MerkleNode node = new MerkleNode(String.valueOf(2));
        tree.build_proofs2(list, node);
        String roothash = tree.getRootHash();
        MerkleProofs proofs = tree.getMerkleeproofs();
        tree = new MerkleTreeImp();
        assertEquals(roothash, tree.GenerateRoot(proofs));
    }

    @Test
    public void merklee_proofs3() {
        tree = new MerkleTreeImp();
        List<MerkleNode> list = new ArrayList<MerkleNode>();
        for (int i = 0; i < 100000; i++) {
            MerkleNode node = new MerkleNode(String.valueOf(i));
            list.add(node);

        }

        tree.my_generate2(list);
        String roothash3 = tree.getRootHash();
        List<MerkleNode> cloned_list = new ArrayList<MerkleNode>(list);
        MerkleNode node = new MerkleNode(String.valueOf(2));
        MerkleNode node2 = new MerkleNode(String.valueOf(2));
        tree.build_proofs2(list, node);
        String roothash = tree.getRootHash();
        MerkleProofs proofs = tree.getMerkleeproofs();
        assertEquals(roothash, tree.GenerateRoot(proofs));
        tree.build_proofs2(list, node2);
        String roothash1 = tree.getRootHash();
        MerkleProofs proofs1 = tree.getMerkleeproofs();
        assertEquals(roothash, roothash1);
        assertEquals(roothash, roothash3);
        assertEquals(roothash, tree.GenerateRoot(proofs1));
    }

}
