package io.Adrestus.core;


import com.google.common.reflect.TypeToken;
import io.Adrestus.MemoryTreePool;
import io.Adrestus.TreeFactory;
import io.Adrestus.Trie.PatriciaTreeNode;
import io.Adrestus.mapper.MemoryTreePoolSerializer;
import io.Adrestus.util.SerializationUtil;
import io.vavr.control.Option;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

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

        TreeFactory.getMemoryTree(1).withdraw(address, 1, TreeFactory.getMemoryTree(1));
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

    @Test
    public void serialization_tree() throws Exception {
        Type fluentType = new TypeToken<MemoryTreePool>() {
        }.getType();
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        //list.add(new SerializationUtil.Mapping(Bytes.class, ctx->new BytesSerializer()));
        //list.add(new SerializationUtil.Mapping(Bytes32.class, ctx->new Bytes32Serializer()));
        //list.add(new SerializationUtil.Mapping(MutableBytes.class, ctx->new MutableBytesSerializer()));
        //list.add(new SerializationUtil.Mapping(Option.class,ctx->new OptionSerializer()));
        list.add(new SerializationUtil.Mapping(MemoryTreePool.class, ctx -> new MemoryTreePoolSerializer()));
        SerializationUtil valueMapper = new SerializationUtil<>(fluentType, list);

        String address = "ADR-ADL3-VDZK-ZU7H-2BX5-M2H4-S7LF-5SR4-ECQA-EIUJ-CBFK";
        PatriciaTreeNode treeNode = new PatriciaTreeNode(2, 1);
        TreeFactory.getMemoryTree(1).store(address, treeNode);
        MemoryTreePool m = (MemoryTreePool) TreeFactory.getMemoryTree(1);

        //m.getByaddress(address);
        //use only special
        byte[] bt = valueMapper.encode_special(m, SerializationUtils.serialize(m).length);
        MemoryTreePool copy = (MemoryTreePool) valueMapper.decode(bt);


        //copy.store(address, treeNode);
        Option<PatriciaTreeNode> pat = copy.getByaddress(address);

        if (!pat.isDefined())
            System.out.println("error");

        assertEquals(m,copy);

    }

    @Test
    public void serialization_tree2() throws Exception {
        String address = "ADR-ADL3-VDZK-ZU7H-2BX5-M2H4-S7LF-5SR4-ECQA-EIUJ-CBFK";
        PatriciaTreeNode treeNode = new PatriciaTreeNode(2, 112);
        TreeFactory.getMemoryTree(1).store(address, treeNode);
        System.out.println(TreeFactory.getMemoryTree(1).getRootHash());


        TreeFactory.getMemoryTree(1).store(address, new PatriciaTreeNode(1, 3));
        System.out.println(TreeFactory.getMemoryTree(1).getRootHash());
        Option<PatriciaTreeNode> pats=TreeFactory.getMemoryTree(1).getByaddress(address);
        MemoryTreePool m = (MemoryTreePool) TreeFactory.getMemoryTree(1);

        byte[] bt = SerializationUtils.serialize(m);
        MemoryTreePool copy = (MemoryTreePool)SerializationUtils.deserialize(bt);

        Option<PatriciaTreeNode> pat = copy.getByaddress(address);

        if (!pat.isDefined())
            System.out.println("error");
        int g = 3;

      assertEquals(m,copy);
    }
}
