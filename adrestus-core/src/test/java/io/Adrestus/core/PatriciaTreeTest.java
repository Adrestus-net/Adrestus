package io.Adrestus.core;


import io.Adrestus.core.Trie.MerklePatriciaTreeImp;
import io.Adrestus.core.Trie.optimized.MerklePatriciaTrie;
import io.Adrestus.core.Trie.optimized.SimpleMerklePatriciaTrie;
import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.spongycastle.util.encoders.Hex;

import java.nio.charset.StandardCharsets;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PatriciaTreeTest {

    private MerklePatriciaTreeImp trie;
    protected MerklePatriciaTrie<Bytes, String> optimized;
    private Function<String, Bytes> valueSerializer;
    @BeforeEach
    void setUp() throws Exception {
        trie = new MerklePatriciaTreeImp();
        valueSerializer = value -> (value != null) ? Bytes.wrap(value.getBytes(StandardCharsets.UTF_8)) : null;
        optimized=new SimpleMerklePatriciaTrie<Bytes, String>(valueSerializer);
    }

    @Test
    public void patricia_tree_insert() {
        for (int i = 0; i < 10000; i++) {
            trie.put(String.valueOf(i).getBytes(StandardCharsets.UTF_8), String.valueOf(i).getBytes(StandardCharsets.UTF_8));
        }
    }

    @Test
    public void patricia_tree_get() {
        for (int i = 0; i < 10000; i++) {
            trie.put(String.valueOf(i).getBytes(StandardCharsets.UTF_8), String.valueOf(i).getBytes(StandardCharsets.UTF_8));
        }
        for (int i = 0; i < 10000; i++) {
            assertEquals(String.valueOf(i), new String(trie.get(String.valueOf(i).getBytes(StandardCharsets.UTF_8))));
        }
        /*trie.put("dog".getBytes(StandardCharsets.UTF_8),"value".getBytes(StandardCharsets.UTF_8));
        assertEquals("value", new String(trie.get("dog".getBytes(StandardCharsets.UTF_8))));*/
    }

    @Test
    public void optimized_patricia_tree(){
        final Bytes key1 = Bytes.of(1, 5, 8, 9);
        final Bytes key2 = Bytes.of(1, 6, 1, 2);
        final Bytes key3 = Bytes.of(1, 6, 1, 3);

        final String value1 = "value1";
        optimized.put(key1, value1);
        final String hash1 = optimized.getRootHash().toHexString();

        assertEquals("value1",optimized.get(key1).get());

        final String value2 = "value2";
        optimized.put(key2, value2);
        final String value3 = "value3";
        optimized.put(key3, value3);
        final String hash2 = optimized.getRootHash().toHexString();

        System.out.println(hash1);
        System.out.println(hash2);
    }
}