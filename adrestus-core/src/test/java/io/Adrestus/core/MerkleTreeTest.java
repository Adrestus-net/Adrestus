package io.Adrestus.core;

import io.Adrestus.Trie.*;
import io.Adrestus.crypto.HashUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class MerkleTreeTest {
    private static MerkleTreeOldImp tree;
    private static MerkleTree tree_new;

    @BeforeAll
    public static void setUp() throws Exception {
        tree = new MerkleTreeOldImp();
        tree_new = new MerkleTreePlainImp();
    }


    @Test
    public void merklee_new_construct_clear() throws CloneNotSupportedException {
        MerkleTree tree_new = new MerkleTreeSha256Imp();
        List<MerkleNode> list1 = new ArrayList<MerkleNode>();
        MerkleNode node1 = new MerkleNode("A");
        list1.add(node1);
        tree_new.constructTree(new ArrayList<>(list1));
        tree_new.build_proofs(node1);
        MerkleProofs proofs = (MerkleProofs) tree_new.getMerkleeproofs().clone();
        tree_new.clear();
        assertEquals(1,proofs.getProofs().size());

    }
    @Test
    public void merklee_hash256_four_nodes_simple() {
        MerkleTree tree_new = new MerkleTreeSha256Imp();
        MerkleTreeOldImp tree = new MerkleTreeOldImp();
        List<MerkleNode> list1 = new ArrayList<MerkleNode>();
        MerkleNode node1 = new MerkleNode("A");
        MerkleNode node2 = new MerkleNode("B");
        MerkleNode node3 = new MerkleNode("C");
        MerkleNode node4 = new MerkleNode("D");
        list1.add(node1);
        list1.add(node2);
        list1.add(node3);
        list1.add(node4);
        tree.my_generate2(new ArrayList<>(list1));
        tree_new.constructTree(new ArrayList<>(list1));
        assertEquals("50a504831bd50fee3581d287168a85a8dcdd6aa777ffd0fe35e37290268a0153", tree_new.getRootHash());
        assertEquals("50a504831bd50fee3581d287168a85a8dcdd6aa777ffd0fe35e37290268a0153", tree.getRootHash());
        tree_new.build_proofs(new MerkleNode("C"));
        MerkleProofs proofs = tree_new.getMerkleeproofs();
        assertEquals(tree_new.getRootHash(), tree_new.generateRoot(proofs));
    }

    @Test
    public void merklee_just_simple4Tests() {
        List<MerkleNode> list1 = new ArrayList<MerkleNode>();
        MerkleNode node1 = new MerkleNode("A");
        MerkleNode node2 = new MerkleNode("B");
        MerkleNode node3 = new MerkleNode("C");
        MerkleNode node4 = new MerkleNode("D");
        MerkleNode node5 = new MerkleNode("E");
        MerkleNode node6 = new MerkleNode("F");
        MerkleNode node7 = new MerkleNode("G");
        MerkleNode node8 = new MerkleNode("H");
        list1.add(node1);
        list1.add(node2);
        list1.add(node3);
        list1.add(node4);
        list1.add(node5);
        list1.add(node6);
        list1.add(node7);
        list1.add(node8);
        tree_new.constructTree(list1);
        tree_new.build_proofs(new MerkleNode("E"));
        MerkleProofs proofs2 = tree_new.getMerkleeproofs();
        assertEquals(tree_new.getRootHash(), tree_new.generateRoot(proofs2));
        tree_new.clear();
        tree_new = new MerkleTreePlainImp();
    }

    @Test
    public void merklee_just_simple1() {
        List<MerkleNode> list1 = new ArrayList<MerkleNode>();
        MerkleNode node1 = new MerkleNode("A");
        MerkleNode node2 = new MerkleNode("B");
        MerkleNode node3 = new MerkleNode("C");
        MerkleNode node4 = new MerkleNode("D");
        MerkleNode node5 = new MerkleNode("E");
        MerkleNode node6 = new MerkleNode("F");
        MerkleNode node7 = new MerkleNode("G");
        MerkleNode node8 = new MerkleNode("H");
        list1.add(node1);
        list1.add(node2);
        list1.add(node3);
        list1.add(node4);
        list1.add(node5);
        list1.add(node6);
        list1.add(node7);
        list1.add(node8);
        tree_new.constructTree(list1);
        for (int i = 0; i < list1.size(); i++) {
            tree_new.build_proofs(list1.get(i));
            MerkleProofs proofs2 = tree_new.getMerkleeproofs();
            assertEquals(tree_new.getRootHash(), tree_new.generateRoot(proofs2));
        }
        tree_new.clear();
        tree_new = new MerkleTreePlainImp();
    }

    @Test
    public void merklee_just_simple2() {
        MerkleTree tree_new_hash = new MerkleTreeSha256Imp();
        MerkleTreeOldImp tree = new MerkleTreeOldImp();
        List<MerkleNode> list1 = new ArrayList<MerkleNode>();
        List<MerkleNode> list2 = new ArrayList<MerkleNode>();
        List<MerkleNode> list3 = new ArrayList<MerkleNode>();
        for (int i = 0; i < 256; i++) {
            list1.add(new MerkleNode(String.valueOf(i)));
            list2.add(new MerkleNode(String.valueOf(i)));
            list3.add(new MerkleNode(String.valueOf(i)));
        }
        tree.my_generate2(list1);
        tree_new_hash.constructTree(list2);
        tree_new.constructTree(list3);
        for (int i = 0; i < list1.size(); i++) {
            tree_new.build_proofs(list3.get(3));
            tree_new_hash.build_proofs(new MerkleNode(String.valueOf(i)));
            tree.build_proofs2(list1, new MerkleNode(String.valueOf(i)));
            MerkleProofsCached cached = tree.getMerkleeproofs();
            MerkleProofs proofs = tree_new.getMerkleeproofs();
            MerkleProofs proofs_hash = tree_new_hash.getMerkleeproofs();
            assertEquals(tree_new.getRootHash(), tree_new.generateRoot(proofs));
            assertEquals(tree.getRootHash(), tree.GenerateRoot(cached));
            assertEquals(tree_new_hash.getRootHash(), tree_new_hash.generateRoot(proofs_hash));
            assertEquals(tree.getRootHash(), tree_new_hash.getRootHash());
        }
        tree_new.clear();
        tree_new = new MerkleTreePlainImp();
    }

    //553
    @Test
    public void sha256() {
        for (int i = 0; i < 1000000; i++) {
            HashUtil.sha256(String.valueOf(i).getBytes(StandardCharsets.UTF_8));
        }
    }

    //25ms
    @Test
    public void xx3() {
        for (int i = 0; i < 1000000; i++) {
            HashUtil.XXH3(String.valueOf(i).getBytes(StandardCharsets.UTF_8));
        }
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
        MerkleTreeOldImp tree2 = new MerkleTreeOldImp();
        for (int i = 0; i < 100000; i++) {
            MerkleNode node = new MerkleNode(String.valueOf(i));
            list.add(node);

        }
        tree.my_generate(list);
        MerkleNode node = new MerkleNode(String.valueOf(1000));
        tree.build_proofs(list, node);
        MerkleProofsCached proofs = tree.getMerkleeproofs();
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
        tree = new MerkleTreeOldImp();
        List<MerkleNode> list = new ArrayList<MerkleNode>();
        for (int i = 0; i < 100000; i++) {
            MerkleNode node = new MerkleNode(String.valueOf(i));
            list.add(node);

        }
        tree.my_generate2(list);
        String hash = tree.getRootHash();
        MerkleNode node = new MerkleNode(String.valueOf(1000));
        tree.build_proofs(list, node);
        MerkleProofsCached proofs = tree.getMerkleeproofs();
        assertEquals(tree.getRootHash(), tree.GenerateRoot(proofs));
        System.out.println(tree.getRootHash());
        System.out.println(tree.GenerateRoot(proofs));
        MerkleNode node1 = new MerkleNode(String.valueOf(1000));
        tree.setRoot();
        tree.my_generate2(list);
        tree.build_proofs(list, node1);
        MerkleProofsCached proofs1 = tree.getMerkleeproofs();
        assertEquals(hash, tree.GenerateRoot(proofs1));
    }

    @Test
    public void merklee_proofs_search_with_optimization2() {
        tree = new MerkleTreeOldImp();
        List<MerkleNode> list = new ArrayList<MerkleNode>();
        for (int i = 0; i < 100000; i++) {
            MerkleNode node = new MerkleNode(String.valueOf(i));
            list.add(node);

        }
        tree.my_generate2(list);
        MerkleNode node = new MerkleNode(String.valueOf(2));
        tree.build_proofs2(list, node);
        String roothash = tree.getRootHash();
        MerkleProofsCached proofs = tree.getMerkleeproofs();
        tree = new MerkleTreeOldImp();
        assertEquals(roothash, tree.GenerateRoot(proofs));
    }

    @Test
    public void merklee_proofs3() {
        tree = new MerkleTreeOldImp();
        List<MerkleNode> list = new ArrayList<MerkleNode>();
        for (int i = 0; i < 100000; i++) {
            MerkleNode node = new MerkleNode(String.valueOf(i));
            list.add(node);

        }

        tree.my_generate2(list);
        String roothash3 = tree.getRootHash();
        List<MerkleNode> cloned_list = new ArrayList<MerkleNode>(list);
        MerkleNode node = new MerkleNode(String.valueOf(2));
        MerkleNode node2 = new MerkleNode(String.valueOf(3));
        tree.build_proofs2(list, node);
        String roothash = tree.getRootHash();
        MerkleProofsCached proofs = tree.getMerkleeproofs();
        assertEquals(roothash, tree.GenerateRoot(proofs));
        tree.build_proofs2(list, node2);
        String roothash1 = tree.getRootHash();
        MerkleProofsCached proofs1 = tree.getMerkleeproofs();
        assertEquals(roothash, roothash1);
        assertEquals(roothash, roothash3);
        assertEquals(roothash, tree.GenerateRoot(proofs1));
    }

    @Test
    public void merklee_proofs4() {
        tree = new MerkleTreeOldImp();
        List<MerkleNode> list = new ArrayList<MerkleNode>();
        for (int i = 0; i < 100000; i++) {
            MerkleNode node = new MerkleNode(String.valueOf(i));
            list.add(node);

        }

        tree.my_generate2(list);
        String roothash3 = tree.getRootHash();
        List<MerkleNode> cloned_list = new ArrayList<MerkleNode>(list);
        MerkleNode node = new MerkleNode(String.valueOf(2));
        MerkleNode node2 = new MerkleNode(String.valueOf(3));
        tree.build_proofs2(list, node);
        String roothash = tree.getRootHash();
        MerkleProofsCached proofs = tree.getMerkleeproofs();
        assertEquals(roothash, tree.GenerateRoot(proofs));
        tree.build_proofs2(list, node2);
        String roothash1 = tree.getRootHash();
        MerkleProofsCached proofs1 = tree.getMerkleeproofs();
        assertEquals(roothash, roothash1);
        assertEquals(roothash, roothash3);
        assertEquals(roothash, tree.GenerateRoot(proofs1));
    }

    @Test
    public void metricPerformanceWithOldTreeSha256() {
        MerkleTreeOldImp tree = new MerkleTreeOldImp();
        List<MerkleNode> list1 = new ArrayList<MerkleNode>();
        for (int i = 0; i < 256; i++) {
            list1.add(new MerkleNode(String.valueOf(i)));
        }
        tree.my_generate2(list1);
        for (int i = 0; i < list1.size(); i++) {
            tree.build_proofs2(list1, new MerkleNode(String.valueOf(i)));
            MerkleProofsCached cached = tree.getMerkleeproofs();
            assertEquals(tree.getRootHash(), tree.GenerateRoot(cached));
        }
        assertEquals("ca5034d539d0939709b61df50a67f3a4271601eb46102b902805e918c29fe1da", tree.getRootHash());
        assertEquals(256, list1.size());
    }

    @Test
    public void metricPerformanceWithNewTreeSha256() {
        MerkleTree tree_new_hash = new MerkleTreeSha256Imp();
        List<MerkleNode> list1 = new ArrayList<MerkleNode>();
        for (int i = 0; i < 256; i++) {
            list1.add(new MerkleNode(String.valueOf(i)));
        }
        tree_new_hash.constructTree(list1);
        for (int i = 0; i < list1.size(); i++) {
            tree_new_hash.build_proofs(new MerkleNode(String.valueOf(i)));
            MerkleProofs proofs_hash = tree_new_hash.getMerkleeproofs();
            assertEquals(tree_new_hash.getRootHash(), tree_new_hash.generateRoot(proofs_hash));
        }
        assertEquals("ca5034d539d0939709b61df50a67f3a4271601eb46102b902805e918c29fe1da", tree_new_hash.getRootHash());
        assertEquals(256, list1.size());
    }

    // This is the best performance use this
    @Test
    public void metricPerformanceWithNewTreeOptimized() {
        MerkleTree tree_new_hash = new MerkleTreeOptimizedImp();
        List<MerkleNode> list1 = new ArrayList<MerkleNode>();
        for (int i = 0; i < 256; i++) {
            list1.add(new MerkleNode(String.valueOf(i)));
        }
        tree_new_hash.constructTree(list1);
        for (int i = 0; i < list1.size(); i++) {
            tree_new_hash.build_proofs(new MerkleNode(String.valueOf(i)));
            MerkleProofs proofs_hash = tree_new_hash.getMerkleeproofs();
            assertEquals(tree_new_hash.getRootHash(), tree_new_hash.generateRoot(proofs_hash));
        }
        System.out.println(tree_new_hash.getRootHash());
        assertEquals(256, list1.size());
    }
}
