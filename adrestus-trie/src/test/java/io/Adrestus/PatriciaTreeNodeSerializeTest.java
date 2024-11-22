package io.Adrestus;

import com.google.common.reflect.TypeToken;
import io.Adrestus.Trie.PatriciaTreeNode;
import io.Adrestus.Trie.PatriciaTreeTransactionType;
import io.Adrestus.Trie.StorageInfo;
import io.Adrestus.Trie.optimize64_trie.MerklePatriciaTrie;
import io.Adrestus.bloom_filter.BloomFilter;
import io.Adrestus.bloom_filter.mapper.BloomFilterSerializer;
import io.Adrestus.crypto.elliptic.mapper.BigDecimalSerializer;
import io.Adrestus.crypto.elliptic.mapper.CustomFurySerializer;
import io.Adrestus.util.SerializationUtil;
import io.Adrestus.util.bytes.Bytes;
import org.apache.fury.Fury;
import org.apache.fury.config.CompatibleMode;
import org.apache.fury.config.Language;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class PatriciaTreeNodeSerializeTest implements Serializable {

    @Test
    public void test() {
        Type fluentType = new TypeToken<PatriciaTreeNode>() {
        }.getType();
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(BigDecimal.class, ctx -> new BigDecimalSerializer()));
        list.add(new SerializationUtil.Mapping(BloomFilter.class, ctx -> new BloomFilterSerializer()));
        SerializationUtil<PatriciaTreeNode> ser = new SerializationUtil<PatriciaTreeNode>(fluentType, list);
        PatriciaTreeNode patriciaTreeNode = new PatriciaTreeNode(BigDecimal.valueOf(23), 2, BigDecimal.valueOf(31));
        patriciaTreeNode.addTransactionPosition(PatriciaTreeTransactionType.REGULAR, "1", 0, 1, 2);
        patriciaTreeNode.addTransactionPosition(PatriciaTreeTransactionType.REGULAR, "2", 0, 1, 3);
        patriciaTreeNode.addTransactionPosition(PatriciaTreeTransactionType.REGULAR, "3", 0, 1, 4);

        patriciaTreeNode.addTransactionPosition(PatriciaTreeTransactionType.REGULAR, "4", 0, 2, 1);
        patriciaTreeNode.addTransactionPosition(PatriciaTreeTransactionType.REGULAR, "5", 0, 2, 2);
        patriciaTreeNode.addTransactionPosition(PatriciaTreeTransactionType.REGULAR, "6", 0, 2, 3);


        patriciaTreeNode.addReceiptPosition("1", 0, 1, 1, 2, 0);
        patriciaTreeNode.addReceiptPosition("2", 0, 1, 1, 3, 0);
        patriciaTreeNode.addReceiptPosition("3", 0, 1, 1, 4, 0);

        patriciaTreeNode.addReceiptPosition("4", 0, 2, 2, 1, 0);
        patriciaTreeNode.addReceiptPosition("5", 0, 2, 2, 2, 1);
        patriciaTreeNode.addReceiptPosition("6", 0, 2, 2, 3, 0);

        byte[] data = ser.encode(patriciaTreeNode, 1024 * 6);
        PatriciaTreeNode copy = ser.decode(data);
        assertEquals(patriciaTreeNode, copy);

        StorageInfo trx = new StorageInfo(0, 2, 3);
        StorageInfo rcp = new StorageInfo(0, 2, 2, 1, 0);

        StorageInfo trx1 = copy.retrieveTransactionInfoByHash(PatriciaTreeTransactionType.REGULAR, "6").get(0);
        StorageInfo rcp1 = copy.retrieveReceiptInfoByHash("4").get(0);

        assertEquals(trx, trx1);
        assertEquals(rcp, rcp1);
    }


    //Run this with Jprofiler and check speed fury must be fasterx10 than serializationutil commons
    @Test
    public void FurySerializationPatriciaTreeTest() {
        String address1 = "1";
        Fury fury = Fury.builder()
                .withLanguage(Language.JAVA)
                .withRefTracking(false)
                .withClassVersionCheck(true)
                .withCompatibleMode(CompatibleMode.SCHEMA_CONSISTENT)
                .withAsyncCompilation(true)
                .withCodegen(false)
                .requireClassRegistration(false)
                .build();
        MerklePatriciaTrie<Bytes, PatriciaTreeNode> patriciaTreeImp = null;
        for (int i = 0; i < 10000; i++) {
            PatriciaTreeNode treeNode = new PatriciaTreeNode(BigDecimal.valueOf(i), i, BigDecimal.valueOf(i));
            SerializableFunction<PatriciaTreeNode, Bytes> valueSerializer = (SerializableFunction<PatriciaTreeNode, Bytes> & Serializable) value -> (value != null) ? Bytes.wrap(CustomFurySerializer.getInstance().getFury().serialize(value)) : null;
            patriciaTreeImp = new MerklePatriciaTrie<Bytes, PatriciaTreeNode>(valueSerializer);
            patriciaTreeImp.put(Bytes.wrap(String.valueOf(i).getBytes(StandardCharsets.UTF_8)), treeNode);
        }
        for (int i = 0; i < 10000; i++) {
            byte[] data = fury.serialize(patriciaTreeImp);//59 ms
            MerklePatriciaTrie<Bytes, PatriciaTreeNode> cloned_data = (MerklePatriciaTrie<Bytes, PatriciaTreeNode>) fury.deserialize(data);//131 ms
            assertEquals(patriciaTreeImp, cloned_data);
        }
        for (int i = 0; i < 10000; i++) {
            byte[] data = CustomFurySerializer.getInstance().getFury().serialize(patriciaTreeImp);//132 ms
            MerklePatriciaTrie<Bytes, PatriciaTreeNode> cloned_data = (MerklePatriciaTrie<Bytes, PatriciaTreeNode>) CustomFurySerializer.getInstance().getFury().deserialize(data);//927 ms
            assertEquals(patriciaTreeImp, cloned_data);
        }
    }

    @Test
    public void FurySerializationPatriciaTreeTest1() {
        String address1 = "1";
        String address2 = "2";
        String rootHash = "";
        PatriciaTreeNode treeNode1 = new PatriciaTreeNode(BigDecimal.valueOf(23), 2, BigDecimal.valueOf(31));
        SerializableFunction<PatriciaTreeNode, Bytes> valueSerializer = (SerializableFunction<PatriciaTreeNode, Bytes> & Serializable) value -> (value != null) ? Bytes.wrap(CustomFurySerializer.getInstance().getFury().serialize(value)) : null;
        MerklePatriciaTrie<Bytes, PatriciaTreeNode> patriciaTreeImp = new MerklePatriciaTrie<Bytes, PatriciaTreeNode>(valueSerializer);
        patriciaTreeImp.put(Bytes.wrap(address1.getBytes(StandardCharsets.UTF_8)), treeNode1);
        rootHash = String.valueOf(patriciaTreeImp.getRootHash());
        byte[] data = CustomFurySerializer.getInstance().getFury().serialize(patriciaTreeImp);
        MerklePatriciaTrie<Bytes, PatriciaTreeNode> cloned_data = (MerklePatriciaTrie<Bytes, PatriciaTreeNode>) CustomFurySerializer.getInstance().getFury().deserialize(data);
        assertEquals(patriciaTreeImp, cloned_data);
        assertEquals(BigDecimal.valueOf(23), cloned_data.get(Bytes.wrap(address1.getBytes(StandardCharsets.UTF_8))).get().getAmount());
        assertEquals(BigDecimal.valueOf(31), cloned_data.get(Bytes.wrap(address1.getBytes(StandardCharsets.UTF_8))).get().getStaking_amount());
        assertEquals(rootHash, String.valueOf(cloned_data.getRootHash()));
        cloned_data.put(Bytes.wrap(address2.getBytes(StandardCharsets.UTF_8)), new PatriciaTreeNode(BigDecimal.valueOf(3), 2, BigDecimal.valueOf(1)));
        Assertions.assertNotEquals(cloned_data, patriciaTreeImp);

        assertEquals(BigDecimal.valueOf(23), patriciaTreeImp.get(Bytes.wrap(address1.getBytes(StandardCharsets.UTF_8))).get().getAmount());
        assertEquals(BigDecimal.valueOf(31), patriciaTreeImp.get(Bytes.wrap(address1.getBytes(StandardCharsets.UTF_8))).get().getStaking_amount());
        assertEquals(BigDecimal.valueOf(3), cloned_data.get(Bytes.wrap(address2.getBytes(StandardCharsets.UTF_8))).get().getAmount());
        assertEquals(BigDecimal.valueOf(1), cloned_data.get(Bytes.wrap(address2.getBytes(StandardCharsets.UTF_8))).get().getStaking_amount());
    }
}
