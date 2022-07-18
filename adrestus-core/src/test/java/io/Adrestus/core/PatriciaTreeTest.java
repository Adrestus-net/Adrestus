package io.Adrestus.core;


import io.Adrestus.core.Trie.MerklePatriciaTreeImp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PatriciaTreeTest {

    private MerklePatriciaTreeImp trie;

    @BeforeEach
    void setUp() throws Exception {
        trie = new MerklePatriciaTreeImp();
    }

    @Test
    public void patricia_tree_insert() {
        for (int i = 0; i < 1000000; i++) {
            trie.put(String.valueOf(i).getBytes(StandardCharsets.UTF_8), String.valueOf(i).getBytes(StandardCharsets.UTF_8));
        }
    }

    @Test
    public void patricia_tree_get() {
        for (int i = 0; i < 1000000; i++) {
            trie.put(String.valueOf(i).getBytes(StandardCharsets.UTF_8), String.valueOf(i).getBytes(StandardCharsets.UTF_8));
        }
        for (int i = 0; i < 1000000; i++) {
            assertEquals(String.valueOf(i), new String(trie.get(String.valueOf(i).getBytes(StandardCharsets.UTF_8))));
        }
        /*trie.put("dog".getBytes(StandardCharsets.UTF_8),"value".getBytes(StandardCharsets.UTF_8));
        assertEquals("value", new String(trie.get("dog".getBytes(StandardCharsets.UTF_8))));*/
    }
}