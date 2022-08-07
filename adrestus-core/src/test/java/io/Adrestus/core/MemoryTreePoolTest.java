package io.Adrestus.core;


import io.Adrestus.core.Resourses.InMemoryTreePoolmp;
import io.Adrestus.core.Resourses.MemoryTreePool;
import io.Adrestus.core.Trie.PatriciaTreeNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

public class MemoryTreePoolTest {

    private MemoryTreePool tree_pool;

    @BeforeEach
    void setUp() throws Exception {
        tree_pool = new InMemoryTreePoolmp();
    }

    @Test
    public void store_mempool() throws Exception {
        String address = "ADR-ADL3-VDZK-ZU7H-2BX5-M2H4-S7LF-5SR4-ECQA-EIUJ-CBFK";
        PatriciaTreeNode treeNode = new PatriciaTreeNode(10, 1);
        tree_pool.store(address, treeNode);
        tree_pool.store("updated_address", treeNode);
        System.out.println(tree_pool.getRootHash());
        treeNode.setAmount(100);
        treeNode.setNonce(2);


        tree_pool.update(address, treeNode);
        Optional<PatriciaTreeNode> copy = tree_pool.getByaddress(address);

        if (copy.isPresent())
            System.out.println(copy.get().toString());
        System.out.println(tree_pool.getRootHash());
    }

    @Test
    public void mempool_get_value() throws Exception {
        String address = "ADR-ADL3-VDZK-ZU7H-2BX5-M2H4-S7LF-5SR4-ECQA-EIUJ-CBFK";
        PatriciaTreeNode treeNode = new PatriciaTreeNode(10, 1);
        tree_pool.store(address, treeNode);


        Optional<PatriciaTreeNode> copy = tree_pool.getByaddress(address);

        if (copy.isPresent())
            System.out.println(copy.get().toString());

    }

    @Test
    public void mempool_stress_test() throws Exception {
        PatriciaTreeNode treeNode = new PatriciaTreeNode(10, 1);

        for (int i = 0; i < 1000000; i++) {
            tree_pool.store(String.valueOf(i), treeNode);
        }

        for (int i = 0; i < 1000000; i++) {
            Optional<PatriciaTreeNode> copy = tree_pool.getByaddress(String.valueOf(i));

            if (!copy.isPresent())
                System.out.println("error");
        }
    }
}
