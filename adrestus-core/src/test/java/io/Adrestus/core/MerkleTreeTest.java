package io.Adrestus.core;

import io.Adrestus.core.Trie.MekleNode;
import io.Adrestus.core.Trie.MerkleTree;
import io.Adrestus.core.Trie.MerkleTreeImp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MerkleTreeTest {
    private MerkleTree tree;
    @BeforeEach
    void setUp() throws Exception {
        tree = new MerkleTreeImp();
    }

    @Test
    public void merklee_tree() {
        List<MekleNode> list1 = new ArrayList<MekleNode>();
        MekleNode node1 = new MekleNode("E");
        for (int i = 0; i < 1000000; i++) {
            list1.add(new MekleNode(String.valueOf(i)));
        }
        list1.add(node1);
        tree.my_generate(list1);
        assertEquals("72ad9ce53d35639b8576626e1365fd55221b544dfdd8d1fbf3faa86b034344be",tree.getRootHash());
    }
}
