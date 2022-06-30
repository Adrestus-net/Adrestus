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
    public void patricia_tree() {
        trie.put("dog".getBytes(StandardCharsets.UTF_8),"value".getBytes(StandardCharsets.UTF_8));
        assertEquals("value", new String(trie.get("dog".getBytes(StandardCharsets.UTF_8))));
    }
}