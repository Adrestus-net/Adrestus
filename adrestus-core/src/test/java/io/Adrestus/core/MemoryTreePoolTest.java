package io.Adrestus.core;


import io.Adrestus.core.Resourses.MemoryTreePool;
import io.Adrestus.core.Trie.PatriciaTreeNode;
import org.junit.jupiter.api.Test;

import java.util.Optional;

public class MemoryTreePoolTest {


    @Test
    public void store_mempool() throws Exception {
        String address = "ADR-ADL3-VDZK-ZU7H-2BX5-M2H4-S7LF-5SR4-ECQA-EIUJ-CBFK";
        PatriciaTreeNode treeNode = new PatriciaTreeNode(10, 1);
        MemoryTreePool.getInstance().store(address, treeNode);
        MemoryTreePool.getInstance().store("updated_address", treeNode);
        System.out.println(MemoryTreePool.getInstance().getRootHash());
        treeNode.setAmount(100);
        treeNode.setNonce(2);


        MemoryTreePool.getInstance().update(address, treeNode);
        Optional<PatriciaTreeNode> copy = MemoryTreePool.getInstance().getByaddress(address);
        System.out.println(copy.get().toString());

        if (copy.isPresent())
            System.out.println(copy.get().toString());
        System.out.println(MemoryTreePool.getInstance().getRootHash());
    }

    @Test
    public void mempool_get_value() throws Exception {
        String address = "ADR-ADL3-VDZK-ZU7H-2BX5-M2H4-S7LF-5SR4-ECQA-EIUJ-CBFK";
        PatriciaTreeNode treeNode = new PatriciaTreeNode(10, 1);
        MemoryTreePool.getInstance().store(address, treeNode);


        Optional<PatriciaTreeNode> copy = MemoryTreePool.getInstance().getByaddress(address);

        if (copy.isPresent())
            System.out.println(copy.get().toString());

    }

    @Test
    public void mempool_stress_test() throws Exception {
        PatriciaTreeNode treeNode = new PatriciaTreeNode(10, 1);
        int size = 10000;
        for (int i = 0; i < size; i++) {
            MemoryTreePool.getInstance().store(String.valueOf(i), treeNode);
        }

        for (int i = 0; i < size; i++) {
            Optional<PatriciaTreeNode> copy = MemoryTreePool.getInstance().getByaddress(String.valueOf(i));

            if (!copy.isPresent())
                System.out.println("error");
        }
    }
}
