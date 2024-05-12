package io.Adrestus.core;

import io.Adrestus.MemoryTreePool;
import io.Adrestus.TreeFactory;
import io.Adrestus.Trie.PatriciaTreeNode;
import io.Adrestus.core.Resourses.CachedZoneIndex;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TreePoolConstructBlockTest {




    @SneakyThrows
    @Test
    public void test() {
        CachedZoneIndex.getInstance().setZoneIndex(0);
        String address="1";
        String address2="2";
        TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).store(address,new PatriciaTreeNode(1000,0));
        TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).store(address2,new PatriciaTreeNode(1000,0));
        ArrayList<Transaction> list = new ArrayList<>();
        Transaction transaction1=new RegularTransaction();
        transaction1.setFrom(address);
        transaction1.setType(TransactionType.REGULAR);
        transaction1.setTo(address2);
        transaction1.setAmount(10);
        RewardsTransaction rewardsTransaction=new RewardsTransaction();
        rewardsTransaction.setRecipientAddress(address);
        rewardsTransaction.setType(TransactionType.REWARDS);
        transaction1.setAmount(10);
        list.add(transaction1);
        list.add(rewardsTransaction);
        TransactionBlock transactionBlock=new TransactionBlock();
        transactionBlock.setHash("hash");
        transactionBlock.setTransactionList(list);
        transactionBlock.setHeight(1);
        MemoryTreePool replica = new MemoryTreePool(((MemoryTreePool) TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex())));
        TreePoolConstructBlock.getInstance().visitForgeTreePool(transactionBlock, replica);
        TreePoolConstructBlock.getInstance().visitInventTreePool(transactionBlock,TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()));
        assertEquals(replica.getRootHash(),TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getRootHash());
    }
}
