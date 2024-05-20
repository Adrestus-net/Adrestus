package io.Adrestus;

import com.google.common.reflect.TypeToken;
import io.Adrestus.Trie.PatriciaTreeNode;
import io.Adrestus.Trie.PatriciaTreeTransactionType;
import io.Adrestus.Trie.StorageInfo;
import io.Adrestus.bloom_filter.BloomFilter;
import io.Adrestus.bloom_filter.mapper.BloomFilterSerializer;
import io.Adrestus.util.SerializationUtil;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;

public class PatriciaTreeNodeSerializeTest {

    @Test
    public void test() {
        Type fluentType = new TypeToken<PatriciaTreeNode>() {
        }.getType();
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(BloomFilter.class, ctx -> new BloomFilterSerializer()));
        SerializationUtil<PatriciaTreeNode> ser = new SerializationUtil<PatriciaTreeNode>(fluentType, list);
        PatriciaTreeNode patriciaTreeNode = new PatriciaTreeNode(23, 2, 31);
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
}
