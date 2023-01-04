package io.Adrestus.core;


import io.Adrestus.TreeFactory;
import io.Adrestus.Trie.PatriciaTreeNode;
import io.vavr.control.Option;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;


import static org.junit.jupiter.api.Assertions.assertEquals;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MemoryTreePoolTest {


    @Test
    public void store_mempool() throws Exception {
        String address = "ADR-ADL3-VDZK-ZU7H-2BX5-M2H4-S7LF-5SR4-ECQA-EIUJ-CBFK";
        PatriciaTreeNode treeNode = new PatriciaTreeNode(10, 1);
        TreeFactory.getMemoryTree(1).store(address, treeNode);

        TreeFactory.getMemoryTree(1).store("updated_address", treeNode);
        System.out.println(TreeFactory.getMemoryTree(0).getRootHash());
        treeNode.setAmount(100);
        treeNode.setNonce(2);


        TreeFactory.getMemoryTree(1).deposit(address, treeNode.getAmount(), TreeFactory.getMemoryTree(1));
        Option<PatriciaTreeNode> copy = TreeFactory.getMemoryTree(1).getByaddress(address);
        System.out.println(copy.get().toString());

        if (copy.isDefined())
            System.out.println(copy.get().toString());
        System.out.println(TreeFactory.getMemoryTree(0).getRootHash());
    }

    @Test
    public void mempool_get_value() throws Exception {
        String address = "ADR-ADL3-VDZK-ZU7H-2BX5-M2H4-S7LF-5SR4-ECQA-EIUJ-CBFK";
        PatriciaTreeNode treeNode = new PatriciaTreeNode(10, 1);
        TreeFactory.getMemoryTree(1).store(address, treeNode);


        Option<PatriciaTreeNode> copy = TreeFactory.getMemoryTree(1).getByaddress(address);

        if (copy.isDefined())
            System.out.println(copy.get().toString());

    }

    @Test
    public void deposit_withdraw() throws Exception {
        String address = "ADR-ADL3-VDZK-ZU7H-2BX5-M2H4-S7LF-5SR4-ECQA-EIUJ-CBFK";
        PatriciaTreeNode treeNode = new PatriciaTreeNode(10, 1);
        TreeFactory.getMemoryTree(1).store(address, treeNode);

        TreeFactory.getMemoryTree(1).withdraw(address, 1,TreeFactory.getMemoryTree(1));
        System.out.println(TreeFactory.getMemoryTree(1).getByaddress(address).get().getAmount());
        assertEquals(9, TreeFactory.getMemoryTree(1).getByaddress(address).get().getAmount());
        Option<PatriciaTreeNode> copy = TreeFactory.getMemoryTree(0).getByaddress(address);

        if (copy.isDefined())
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
            Option<PatriciaTreeNode> copy = TreeFactory.getMemoryTree(0).getByaddress(String.valueOf(i));

            if (!copy.isDefined())
                System.out.println("error");
        }
    }
}
