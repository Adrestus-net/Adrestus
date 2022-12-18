package io.Adrestus.core;


import io.Adrestus.TreeFactory;
import io.Adrestus.Trie.PatriciaTreeNode;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.Optional;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MemoryTreePoolTest {


    @Test
    public void store_mempool() throws Exception {
        String address = "ADR-ADL3-VDZK-ZU7H-2BX5-M2H4-S7LF-5SR4-ECQA-EIUJ-CBFK";
        PatriciaTreeNode treeNode = new PatriciaTreeNode(10, 1);
        TreeFactory.getMemoryTree(0).store(address, treeNode);

        TreeFactory.getMemoryTree(0).store("updated_address", treeNode);
        System.out.println(TreeFactory.getMemoryTree(0).getRootHash());
        treeNode.setAmount(100);
        treeNode.setNonce(2);


        TreeFactory.getMemoryTree(0).deposit(address, treeNode);
        Optional<PatriciaTreeNode> copy = TreeFactory.getMemoryTree(0).getByaddress(address);
        System.out.println(copy.get().toString());

        if (copy.isPresent())
            System.out.println(copy.get().toString());
        System.out.println(TreeFactory.getMemoryTree(0).getRootHash());
    }

    @Test
    public void mempool_get_value() throws Exception {
        String address = "ADR-ADL3-VDZK-ZU7H-2BX5-M2H4-S7LF-5SR4-ECQA-EIUJ-CBFK";
        PatriciaTreeNode treeNode = new PatriciaTreeNode(10, 1);
        TreeFactory.getMemoryTree(0).store(address, treeNode);


        Optional<PatriciaTreeNode> copy = TreeFactory.getMemoryTree(0).getByaddress(address);

        if (copy.isPresent())
            System.out.println(copy.get().toString());

    }

    @Test
    public void mempool_stress_test() throws Exception {
        PatriciaTreeNode treeNode = new PatriciaTreeNode(10, 1);
        int size = 10000;
        for (int i = 0; i < size; i++) {
            TreeFactory.getMemoryTree(0).store(String.valueOf(i), treeNode);
        }

        for (int i = 0; i < size; i++) {
            Optional<PatriciaTreeNode> copy = TreeFactory.getMemoryTree(0).getByaddress(String.valueOf(i));

            if (!copy.isPresent())
                System.out.println("error");
        }
    }
}
