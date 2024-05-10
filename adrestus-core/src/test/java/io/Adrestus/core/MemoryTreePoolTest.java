package io.Adrestus.core;


import com.google.common.reflect.TypeToken;
import io.Adrestus.MemoryTreePool;
import io.Adrestus.TreeFactory;
import io.Adrestus.Trie.PatriciaTreeNode;
import io.Adrestus.mapper.MemoryTreePoolSerializer;
import io.Adrestus.util.SerializationUtil;
import io.Adrestus.util.bytes.Bytes53;
import io.vavr.control.Option;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.tuweni.bytes.Bytes;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MemoryTreePoolTest {


    @Test
    public void store_mempool() throws Exception {
        String address = "ADR-ADL3-VDZK-ZU7H-2BX5-M2H4-S7LF-5SR4-ECQA-EIUJ-CBFK";
        PatriciaTreeNode treeNode = new PatriciaTreeNode(10, 1);
        treeNode.setAmount(100);
        treeNode.setNonce(2);
        PatriciaTreeNode treeNode2 = new PatriciaTreeNode(10, 1);
        treeNode2.setAmount(100);
        treeNode2.setNonce(2);
        TreeFactory.getMemoryTree(1).store(address, treeNode);

        TreeFactory.getMemoryTree(1).store("updated_address", treeNode2);
        System.out.println(TreeFactory.getMemoryTree(0).getRootHash());


        TreeFactory.getMemoryTree(1).deposit(address, 50);
        TreeFactory.getMemoryTree(1).deposit("updated_address", 20);
        Option<PatriciaTreeNode> copy = TreeFactory.getMemoryTree(1).getByaddress(address);
        Option<PatriciaTreeNode> copy2 = TreeFactory.getMemoryTree(1).getByaddress("updated_address");
        assertEquals(150, copy.get().getAmount());
        assertEquals(120, copy2.get().getAmount());

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

        TreeFactory.getMemoryTree(1).withdraw(address, 1);
        System.out.println(TreeFactory.getMemoryTree(1).getByaddress(address).get().getAmount());
        assertEquals(9, TreeFactory.getMemoryTree(1).getByaddress(address).get().getAmount());
        Option<PatriciaTreeNode> copy = TreeFactory.getMemoryTree(0).getByaddress(address);

        if (copy.isDefined())
            System.out.println(copy.get().toString());

    }

    @Test
    public void deposit_withdraw_unclaimed() throws Exception {
        String address = "ADR-ADL3-VDZK-ZU7H-2BX5-M2H4-S7LF-5SR4-ECQA-EIUJ-CBFK";
        PatriciaTreeNode treeNode = new PatriciaTreeNode(10, 1);
        TreeFactory.getMemoryTree(1).store(address, treeNode);

        TreeFactory.getMemoryTree(1).depositUnclaimedReward(address, 10);
        assertEquals(10, TreeFactory.getMemoryTree(1).getByaddress(address).get().getUnclaimed_reward());
        TreeFactory.getMemoryTree(1).withdrawUnclaimedReward(address, 5);
        assertEquals(5, TreeFactory.getMemoryTree(1).getByaddress(address).get().getUnclaimed_reward());
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

        assertEquals(m, copy);

    }

    @Test
    public void MultiPatriciaTrees() throws Exception {
        String address = "ADR-ADL3-VDZK-ZU7H-2BX5-M2H4-S7LF-5SR4-ECQA-EIUJ-CBFK";
        PatriciaTreeNode treeNode = new PatriciaTreeNode(200, 112, 0, 456);
        TreeFactory.getMemoryTree(1).store(address, treeNode);
        String hash = TreeFactory.getMemoryTree(1).getRootHash();
        MemoryTreePool replica = new MemoryTreePool(((MemoryTreePool) TreeFactory.getMemoryTree(1)));
        MemoryTreePool replica2 = new MemoryTreePool(((MemoryTreePool) TreeFactory.getMemoryTree(1)));
        assertEquals(hash, hash);
        assertEquals(hash, replica.getRootHash());
        assertEquals(hash, replica2.getRootHash());
        replica2.withdrawUnclaimedReward(address, 10);
        replica2.withdraw(address, 2);
        replica2.deposit(address, 20);
        replica.withdrawUnclaimedReward(address, 10);
        replica.withdraw(address, 2);
        replica.deposit(address, 20);
        assertEquals(replica, replica2);
    }

    @Test
    public void MultiPatriciaTrees2() throws Exception {
        String address = "ADR-ADL3-VDZK-ZU7H-2BX5-M2H4-S7LF-5SR4-ECQA-EIUJ-CBFK";
        PatriciaTreeNode treeNode = new PatriciaTreeNode(200, 112, 0, 456);
        TreeFactory.getMemoryTree(1).store(address, treeNode);
        MemoryTreePool replica = new MemoryTreePool(((MemoryTreePool) TreeFactory.getMemoryTree(1)));
        MemoryTreePool replica2 = new MemoryTreePool(((MemoryTreePool) TreeFactory.getMemoryTree(1)));
        replica.depositUnclaimedReward(address, 10);
        replica.withdrawUnclaimedReward(address, 2);
        replica.deposit(address, 10);
        replica.withdraw(address, 2);
        replica2.depositUnclaimedReward(address, 10);
        replica2.withdrawUnclaimedReward(address, 2);
        replica2.deposit(address, 10);
        replica2.withdraw(address, 2);
        assertEquals(456, TreeFactory.getMemoryTree(1).getByaddress(address).get().getUnclaimed_reward());
        assertEquals(464, replica.getByaddress(address).get().getUnclaimed_reward());
        assertEquals(464, replica2.getByaddress(address).get().getUnclaimed_reward());
        assertEquals(replica, replica2);
    }

    @Test
    public void MultiPatriciaTrees3() throws Exception {
        String address1 = "1";
        String address2 = "2";
        String address3 = "2";
        PatriciaTreeNode treeNode1 = new PatriciaTreeNode(200, 112, 0, 456);
        PatriciaTreeNode treeNode2 = new PatriciaTreeNode(200, 112, 0, 456);
        PatriciaTreeNode treeNode3 = new PatriciaTreeNode(200, 112, 0, 456);
        TreeFactory.getMemoryTree(1).store(address1, treeNode1);
        TreeFactory.getMemoryTree(2).store(address2, treeNode2);
        TreeFactory.getMemoryTree(3).store(address3, treeNode3);
        MemoryTreePool replica = new MemoryTreePool(((MemoryTreePool) TreeFactory.getMemoryTree(1)));
        MemoryTreePool replica2 = new MemoryTreePool(((MemoryTreePool) TreeFactory.getMemoryTree(1)));
        replica.deposit(address1, 10);
        replica.deposit(address2, 20);
        replica.deposit(address3, 30);

        replica2.deposit(address3, 30);
        replica2.deposit(address2, 20);
        replica2.deposit(address1, 10);
        assertEquals(replica, replica2);


    }

    @Test
    public void serialization_tree2() throws Exception {
        String address = "ADR-ADL3-VDZK-ZU7H-2BX5-M2H4-S7LF-5SR4-ECQA-EIUJ-CBFK";
        PatriciaTreeNode treeNode = new PatriciaTreeNode(2, 112);
        TreeFactory.getMemoryTree(1).store(address, treeNode);
        System.out.println(TreeFactory.getMemoryTree(1).getRootHash());


        TreeFactory.getMemoryTree(1).store(address, new PatriciaTreeNode(1, 3));
        System.out.println(TreeFactory.getMemoryTree(1).getRootHash());
        Option<PatriciaTreeNode> pats = TreeFactory.getMemoryTree(1).getByaddress(address);
        MemoryTreePool m = (MemoryTreePool) TreeFactory.getMemoryTree(1);

        byte[] bt = SerializationUtils.serialize(m);
        MemoryTreePool copy = (MemoryTreePool) SerializationUtils.deserialize(bt);

        Option<PatriciaTreeNode> pat = copy.getByaddress(address);

        if (!pat.isDefined())
            System.out.println("error");
        int g = 3;

        assertEquals(m, copy);
    }

    @Test
    public void RetrieveKeys() throws Exception {
        String Address = "ADR-ADL3-VDZK-ZU7H-2BX5-M2H4-S7LF-5SR4-ECQA-EIUJ-CBFK";
        String Address2 = "ADR-GBIV-HG2J-27P5-BNVN-MLN6-DL5V-M3YZ-PKEJ-CFFG-FK4L";
        Bytes key1 = Bytes.wrap(Address.getBytes(StandardCharsets.UTF_8));
        Bytes53 key53 = Bytes53.wrap(Address.getBytes(StandardCharsets.UTF_8));
        assertEquals(key1.toString(), key53.toString());
        String hexString = key1.toUnprefixedHexString();
        byte[] bytes = Hex.decodeHex(hexString.toCharArray());
        String copies = new String(bytes, "UTF-8");
        assertEquals(Address, copies);

        PatriciaTreeNode treeNode1 = new PatriciaTreeNode(1, 112);
        PatriciaTreeNode treeNode2 = new PatriciaTreeNode(2, 212);
        TreeFactory.getMemoryTree(1).store(Address, treeNode1);
        TreeFactory.getMemoryTree(1).store(Address2, treeNode2);
        Set<String> fg = TreeFactory.getMemoryTree(1).Keyset(Bytes53.ZERO, Integer.MAX_VALUE);
        int n = fg.size();
        String arr[] = new String[n];
        arr = fg.toArray(arr);
        assertEquals(Address, arr[0]);
        assertEquals(Address2, arr[1]);
    }
}
