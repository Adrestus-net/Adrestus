package io.Adrestus.core;


import com.google.common.reflect.TypeToken;
import io.Adrestus.MemoryTreePool;
import io.Adrestus.TreeFactory;
import io.Adrestus.Trie.PatriciaTreeNode;
import io.Adrestus.Trie.PatriciaTreeTransactionType;
import io.Adrestus.Trie.StakingInfo;
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
import static org.junit.jupiter.api.Assertions.assertNotEquals;

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


        TreeFactory.getMemoryTree(1).deposit(PatriciaTreeTransactionType.REGULAR, address, 50, 1);
        TreeFactory.getMemoryTree(1).deposit(PatriciaTreeTransactionType.REGULAR, "updated_address", 20, 1);
        Option<PatriciaTreeNode> copy = TreeFactory.getMemoryTree(1).getByaddress(address);
        Option<PatriciaTreeNode> copy2 = TreeFactory.getMemoryTree(1).getByaddress("updated_address");
        assertEquals(149, copy.get().getAmount());
        assertEquals(119, copy2.get().getAmount());

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

        TreeFactory.getMemoryTree(1).withdraw(PatriciaTreeTransactionType.REGULAR, address, 1, 1);
        System.out.println(TreeFactory.getMemoryTree(1).getByaddress(address).get().getAmount());
        assertEquals(8, TreeFactory.getMemoryTree(1).getByaddress(address).get().getAmount());
        Option<PatriciaTreeNode> copy = TreeFactory.getMemoryTree(0).getByaddress(address);

        if (copy.isDefined())
            System.out.println(copy.get().toString());

    }

    @Test
    public void deposit_withdraw_unclaimed() throws Exception {
        String address = "ADR-ADL3-VDZK-ZU7H-2BX5-M2H4-S7LF-5SR4-ECQA-EIUJ-CBFK";
        PatriciaTreeNode treeNode = new PatriciaTreeNode(10, 1);
        TreeFactory.getMemoryTree(1).store(address, treeNode);

        TreeFactory.getMemoryTree(1).deposit(PatriciaTreeTransactionType.UNCLAIMED_FEE_REWARD, address, 10, 1);
        assertEquals(10, TreeFactory.getMemoryTree(1).getByaddress(address).get().getUnclaimed_reward());
        TreeFactory.getMemoryTree(1).withdraw(PatriciaTreeTransactionType.UNCLAIMED_FEE_REWARD, address, 5, 1);
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
        replica2.withdraw(PatriciaTreeTransactionType.UNCLAIMED_FEE_REWARD, address, 10, 1);
        replica2.withdraw(PatriciaTreeTransactionType.REGULAR, address, 2, 1);
        replica2.deposit(PatriciaTreeTransactionType.REGULAR, address, 20, 1);
        replica.withdraw(PatriciaTreeTransactionType.UNCLAIMED_FEE_REWARD, address, 10, 1);
        replica.withdraw(PatriciaTreeTransactionType.REGULAR, address, 2, 1);
        replica.deposit(PatriciaTreeTransactionType.REGULAR, address, 20, 1);
        assertEquals(replica, replica2);
    }

    @Test
    public void MultiPatriciaTrees2() throws Exception {
        String address = "ADR-ADL3-VDZK-ZU7H-2BX5-M2H4-S7LF-5SR4-ECQA-EIUJ-CBFK";
        PatriciaTreeNode treeNode = new PatriciaTreeNode(200, 112, 0, 456, 456);
        TreeFactory.getMemoryTree(1).store(address, treeNode);
        MemoryTreePool replica = new MemoryTreePool(((MemoryTreePool) TreeFactory.getMemoryTree(1)));
        MemoryTreePool replica2 = new MemoryTreePool(((MemoryTreePool) TreeFactory.getMemoryTree(1)));
        replica.deposit(PatriciaTreeTransactionType.UNCLAIMED_FEE_REWARD, address, 10, 1);
        replica.withdraw(PatriciaTreeTransactionType.UNCLAIMED_FEE_REWARD, address, 2, 1);
        replica.deposit(PatriciaTreeTransactionType.REGULAR, address, 10, 1);
        replica.withdraw(PatriciaTreeTransactionType.REGULAR, address, 2, 1);
        replica2.deposit(PatriciaTreeTransactionType.UNCLAIMED_FEE_REWARD, address, 10, 1);
        replica2.withdraw(PatriciaTreeTransactionType.UNCLAIMED_FEE_REWARD, address, 2, 1);
        replica2.deposit(PatriciaTreeTransactionType.REGULAR, address, 10, 1);
        replica2.withdraw(PatriciaTreeTransactionType.REGULAR, address, 2, 1);
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
        replica.deposit(PatriciaTreeTransactionType.REGULAR, address1, 10, 1);
        replica.deposit(PatriciaTreeTransactionType.REGULAR, address2, 20, 1);
        replica.deposit(PatriciaTreeTransactionType.REGULAR, address3, 30, 1);

        replica2.deposit(PatriciaTreeTransactionType.REGULAR, address3, 30, 1);
        replica2.deposit(PatriciaTreeTransactionType.REGULAR, address2, 20, 1);
        replica2.deposit(PatriciaTreeTransactionType.REGULAR, address1, 10, 1);
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

    //Cloned trie and TreeFactory.getMemoryTree(1) must not have same patriciatreenode make sure its different
    //SUPER IMPORTANT YOU NEED FIRST GET PATRICIA_TREE_NODE CHANGE IT AND THEN STORE AGAIN
//    PatriciaTreeNode patriciaTreeNode3= (PatriciaTreeNode) TreeFactory.getMemoryTree(1).getByaddress(address).get();
//    patriciaTreeNode3.setStakingInfo(new StakingInfo("name",10,"identity","website","details"));
//    patriciaTreeNode3.setAmount(21312);
//    TreeFactory.getMemoryTree(1).store(address, patriciaTreeNode3);
    @Test
    public void StorageStakingInfos() throws Exception {
        String address = "ADR-ADL3-VDZK-ZU7H-2BX5-M2H4-S7LF-5SR4-ECQA-EIUJ-CBFK";
        PatriciaTreeNode treeNode = new PatriciaTreeNode(2, 112);
        PatriciaTreeNode treeNode2 = new PatriciaTreeNode(2, 1112);
        MemoryTreePool m = (MemoryTreePool) TreeFactory.getMemoryTree(1).clone();
        TreeFactory.getMemoryTree(1).store(address, treeNode);
        m.store(address, treeNode2);
        assertNotEquals(m.getRootHash(), TreeFactory.getMemoryTree(1).getRootHash());
        PatriciaTreeNode patriciaTreeNode = (PatriciaTreeNode) m.getByaddress(address).get().clone();
        patriciaTreeNode.setNonce(112);
        m.store(address, patriciaTreeNode);
        assertEquals(m.getRootHash(), TreeFactory.getMemoryTree(1).getRootHash());
        //m.store(address,treeNode);
        PatriciaTreeNode patriciaTreeNode3 = (PatriciaTreeNode) TreeFactory.getMemoryTree(1).getByaddress(address).get();
        patriciaTreeNode3.setStakingInfo(new StakingInfo("name", 10, "identity", "website", "details"));
        patriciaTreeNode3.setAmount(21312);
        TreeFactory.getMemoryTree(1).store(address, patriciaTreeNode3);
        assertNotEquals(m.getRootHash(), TreeFactory.getMemoryTree(1).getRootHash());
        PatriciaTreeNode patriciaTreeNode2 = (PatriciaTreeNode) m.getByaddress(address).get().clone();
        patriciaTreeNode2.setStakingInfo(new StakingInfo("name", 10, "identity", "website", "details"));
        patriciaTreeNode2.setAmount(21312);
        m.store(address, patriciaTreeNode2);
        assertEquals(m.getRootHash(), TreeFactory.getMemoryTree(1).getRootHash());
        PatriciaTreeNode copy = m.getByaddress(address).get();
        assertEquals(treeNode, copy);
        assertEquals(m.getRootHash(), TreeFactory.getMemoryTree(1).getRootHash());

    }

    @Test
    public void RetrieveKeys() throws Exception {
        String Address = "ADR-ADL3-VDZK-ZU7H-2BX5-M2H4-S7LF-5SR4-ECQA-EIUJ-CBFK";
        String Address2 = "ADR-GBIV-HG2J-27P5-BNVN-MLN6-DL5V-M3YZ-PKEJ-CFFG-FK4L";
        String Address3 = "ADR-GBIV-HG2J-27P5-BNVN-MLN6-DL5V-M3YZ-PKEJ-CFFG-FK4R";
        String Address4 = "ADR-GBIV-HG2J-27P5-BNVN-MLN6-DL5V-M3YZ-PKEJ-CFFG-FK4Y";
        Bytes key1 = Bytes.wrap(Address.getBytes(StandardCharsets.UTF_8));
        Bytes53 key53 = Bytes53.wrap(Address.getBytes(StandardCharsets.UTF_8));
        assertEquals(key1.toString(), key53.toString());
        String hexString = key1.toUnprefixedHexString();
        byte[] bytes = Hex.decodeHex(hexString.toCharArray());
        String copies = new String(bytes, "UTF-8");
        assertEquals(Address, copies);

        PatriciaTreeNode treeNode1 = new PatriciaTreeNode(1, 112);
        PatriciaTreeNode treeNode2 = new PatriciaTreeNode(2, 212);
        PatriciaTreeNode treeNode3 = new PatriciaTreeNode(3, 112);
        PatriciaTreeNode treeNode4 = new PatriciaTreeNode(4, 112);
        TreeFactory.getMemoryTree(1).store(Address, treeNode1);
        TreeFactory.getMemoryTree(1).store(Address2, treeNode2);
        TreeFactory.getMemoryTree(1).store(Address3, treeNode3);
        TreeFactory.getMemoryTree(1).store(Address4, treeNode4);
        Set<String> fg = TreeFactory.getMemoryTree(1).Keyset(Bytes53.ZERO, Integer.MAX_VALUE);
        int n = fg.size();
        assertEquals(4, n);
        String arr[] = new String[n];
        arr = fg.toArray(arr);
        assertEquals(Address3, arr[0]);
        assertEquals(Address, arr[1]);
    }
}
