package io.Adrestus.core;


import io.Adrestus.Trie.MerklePatriciaTreeImp;
import io.Adrestus.Trie.PatriciaTreeNode;
import io.Adrestus.Trie.optimize64_trie.IMerklePatriciaTrie;
import io.Adrestus.Trie.optimize64_trie.MerklePatriciaTrie;
import org.apache.tuweni.bytes.Bytes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PatriciaTreeTest {

    private MerklePatriciaTreeImp trie;
    protected IMerklePatriciaTrie<Bytes, String> optimized;
    protected IMerklePatriciaTrie<Bytes, PatriciaTreeNode> optimized2;
    private Function<String, Bytes> valueSerializer;
    private Function<PatriciaTreeNode, Bytes> valueSerializer2;

    @BeforeEach
    void setUp() throws Exception {
        trie = new MerklePatriciaTreeImp();
        valueSerializer = value -> (value != null) ? Bytes.wrap(value.getBytes(StandardCharsets.UTF_8)) : null;
        valueSerializer2 = value -> (value != null) ? Bytes.wrap("".getBytes(StandardCharsets.UTF_8)) : null;
        optimized = new MerklePatriciaTrie<Bytes, String>(valueSerializer);
        optimized2 = new MerklePatriciaTrie<Bytes, PatriciaTreeNode>(valueSerializer2);
    }

    @Test
    public void patricia_tree_insert() {
        int size = 1000;
        for (int i = 0; i < size; i++) {
            trie.put(String.valueOf(i).getBytes(StandardCharsets.UTF_8), String.valueOf(i).getBytes(StandardCharsets.UTF_8));
        }
    }

    @Test
    public void patricia_tree_get() {
        int size = 1000;
        for (int i = 0; i < size; i++) {
            trie.put(String.valueOf(i).getBytes(StandardCharsets.UTF_8), String.valueOf(i).getBytes(StandardCharsets.UTF_8));
        }
        for (int i = 0; i < size; i++) {
            assertEquals(String.valueOf(i), new String(trie.get(String.valueOf(i).getBytes(StandardCharsets.UTF_8))));
        }
        /*trie.put("dog".getBytes(StandardCharsets.UTF_8),"value".getBytes(StandardCharsets.UTF_8));
        assertEquals("value", new String(trie.get("dog".getBytes(StandardCharsets.UTF_8))));*/
    }

    @Test
    public void optimized_patricia_tree() {
        final Bytes key1 = Bytes.of(1, 5, 8, 9);
        final Bytes key2 = Bytes.of(1, 6, 1, 2);
        final Bytes key3 = Bytes.of(1, 6, 1, 3);

        final String value1 = "value1";
        optimized.put(key1, value1);
        final String hash1 = optimized.getRootHash().toHexString();

        assertEquals("value1", optimized.get(key1).get());

        final String value2 = "value2";
        optimized.put(key2, value2);
        final String value3 = "value3";
        optimized.put(key3, value3);
        final String hash2 = optimized.getRootHash().toHexString();

        System.out.println(hash1);
        System.out.println(hash2);

    }

    @Test
    public void optimized_patricia_tree2() {
        final Bytes key1 = Bytes.of(1, 5, 8, 9);


        PatriciaTreeNode node = new PatriciaTreeNode(2, 3);
        final String hash = optimized2.getRootHash().toHexString();
        assertEquals("0x56e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421", hash);
        optimized2.put(key1, node);
        final String hash1 = optimized2.getRootHash().toHexString();
        assertEquals("0x1ba4b3ad7d229d2b852cc7277a1f79395383ab60fd6b257ff6dbd723047ed382", hash1);
        assertEquals(node, optimized2.get(key1).get());
    }

    @Test
    public void stress_test_optimized_patricia_tree() {
        String addres = "ADR-AB2C-ARNW-4BYP-7CGJ-K6AD-OSNM-NC6Q-ET2C-6DEW-AAWY";
        StringBuilder stringBuilder = new StringBuilder();
        int size = 1000;
        for (int i = 0; i < size; i++) {
            stringBuilder.append(addres);
            stringBuilder.append(String.valueOf(i));
            optimized2.put(Bytes.wrap(stringBuilder.toString().getBytes(StandardCharsets.UTF_8)), new PatriciaTreeNode(i, i));
            stringBuilder.setLength(0);
            optimized2.getRootHash();
        }
        stringBuilder = new StringBuilder();
        for (int i = 0; i < size; i++) {
            stringBuilder.append(addres);
            stringBuilder.append(String.valueOf(i));
            assertEquals(new PatriciaTreeNode(i, i), optimized2.get(Bytes.wrap(stringBuilder.toString().getBytes(StandardCharsets.UTF_8))).get());
            stringBuilder.setLength(0);
        }
    }
}