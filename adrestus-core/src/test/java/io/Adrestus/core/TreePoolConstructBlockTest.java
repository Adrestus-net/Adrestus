package io.Adrestus.core;

import io.Adrestus.MemoryTreePool;
import io.Adrestus.TreeFactory;
import io.Adrestus.Trie.PatriciaTreeNode;
import io.Adrestus.core.Resourses.CachedZoneIndex;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TreePoolConstructBlockTest {

    private String address = "1";
    private String address2 = "2";
    private String address3 = "3";
    private String address4 = "4";
    private String address5 = "5";

    @BeforeAll
    public static void setup() {
        CachedZoneIndex.getInstance().setZoneIndex(0);
    }

    @SneakyThrows
    @Test
    public void regular_transaction() {
        TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).store(address, new PatriciaTreeNode(BigDecimal.valueOf(1000), 0));
        TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).store(address2, new PatriciaTreeNode(BigDecimal.valueOf(1000), 0));
        ArrayList<Transaction> list = new ArrayList<>();
        Transaction transaction1 = new RegularTransaction();
        transaction1.setFrom(address);
        transaction1.setType(TransactionType.REGULAR);
        transaction1.setTo(address2);
        transaction1.setAmount(BigDecimal.valueOf(10));
        transaction1.setAmountWithTransactionFee(BigDecimal.valueOf(1));
        RewardsTransaction rewardsTransaction = new RewardsTransaction();
        rewardsTransaction.setRecipientAddress(address);
        rewardsTransaction.setType(TransactionType.REWARDS);
        list.add(transaction1);
        list.add(rewardsTransaction);
        TransactionBlock transactionBlock = new TransactionBlock();
        transactionBlock.setHash("hash");
        transactionBlock.setTransactionList(list);
        transactionBlock.setHeight(1);
        MemoryTreePool replica = new MemoryTreePool(((MemoryTreePool) TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex())));
        TreePoolConstructBlock.getInstance().visitForgeTreePool(transactionBlock, replica);
        TreePoolConstructBlock.getInstance().visitInventTreePool(transactionBlock, TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()));
        assertEquals(replica.getRootHash(), TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getRootHash());
        TreeFactory.ClearMemoryTree(CachedZoneIndex.getInstance().getZoneIndex());
    }

    @SneakyThrows
    @Test
    public void regular_transaction1a() {
        TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).store(address, new PatriciaTreeNode(BigDecimal.valueOf(1000), 0, BigDecimal.valueOf(34), BigDecimal.valueOf(4), BigDecimal.valueOf(0)));
        TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).store(address2, new PatriciaTreeNode(BigDecimal.valueOf(1000), 0, BigDecimal.valueOf(45), BigDecimal.valueOf(5), BigDecimal.valueOf(0)));
        ArrayList<Transaction> list = new ArrayList<>();
        Transaction transaction1 = new RegularTransaction();
        transaction1.setFrom(address);
        transaction1.setType(TransactionType.REGULAR);
        transaction1.setTo(address2);
        transaction1.setAmount(BigDecimal.valueOf(10));
        transaction1.setAmountWithTransactionFee(BigDecimal.valueOf(1));
        RewardsTransaction rewardsTransaction = new RewardsTransaction();
        rewardsTransaction.setRecipientAddress(address);
        rewardsTransaction.setType(TransactionType.REWARDS);
        list.add(transaction1);
        list.add(rewardsTransaction);
        TransactionBlock transactionBlock = new TransactionBlock();
        transactionBlock.setHash("hash");
        transactionBlock.setTransactionList(list);
        transactionBlock.setHeight(1);
        MemoryTreePool replica = new MemoryTreePool(((MemoryTreePool) TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex())));
        TreePoolConstructBlock.getInstance().visitForgeTreePool(transactionBlock, replica);
        TreePoolConstructBlock.getInstance().visitInventTreePool(transactionBlock, TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()));
        assertEquals(replica.getRootHash(), TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getRootHash());
        TreeFactory.ClearMemoryTree(CachedZoneIndex.getInstance().getZoneIndex());
    }

    @SneakyThrows
    @Test
    public void regular_transaction2() {
        TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).store(address, new PatriciaTreeNode(BigDecimal.valueOf(1000), 0));
        TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).store(address2, new PatriciaTreeNode(BigDecimal.valueOf(1000), 0));
        ArrayList<Transaction> list = new ArrayList<>();
        Transaction transaction1 = new RegularTransaction();
        transaction1.setFrom(address);
        transaction1.setType(TransactionType.REGULAR);
        transaction1.setTo(address2);
        transaction1.setAmount(BigDecimal.valueOf(10));
        transaction1.setAmountWithTransactionFee(BigDecimal.valueOf(1));
        Transaction transaction2 = new RegularTransaction();
        transaction2.setFrom(address);
        transaction2.setType(TransactionType.REGULAR);
        transaction2.setTo(address2);
        transaction2.setAmount(BigDecimal.valueOf(10));
        transaction2.setAmountWithTransactionFee(BigDecimal.valueOf(1));
        Transaction transaction3 = new RegularTransaction();
        transaction3.setFrom(address);
        transaction3.setType(TransactionType.REGULAR);
        transaction3.setTo(address2);
        transaction3.setAmount(BigDecimal.valueOf(10));
        transaction3.setAmountWithTransactionFee(BigDecimal.valueOf(1));
        list.add(transaction1);
        list.add(transaction2);
        list.add(transaction3);
        TransactionBlock transactionBlock = new TransactionBlock();
        transactionBlock.setHash("hash");
        transactionBlock.setTransactionList(list);
        transactionBlock.setHeight(1);
        MemoryTreePool replica = new MemoryTreePool(((MemoryTreePool) TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex())));
        TreePoolConstructBlock.getInstance().visitForgeTreePool(transactionBlock, replica);
        TreePoolConstructBlock.getInstance().visitInventTreePool(transactionBlock, TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()));
        assertEquals(replica.getRootHash(), TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getRootHash());
        assertEquals(1027, TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(address2).get().getAmount().doubleValue());
        assertEquals(967, TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(address).get().getAmount().doubleValue());
        TreeFactory.ClearMemoryTree(CachedZoneIndex.getInstance().getZoneIndex());
    }

    @SneakyThrows
    @Test
    public void staking_transaction2() {
        TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).store(address5, new PatriciaTreeNode(BigDecimal.valueOf(1000), 0));
        ArrayList<Transaction> list = new ArrayList<>();
        StakingTransaction transaction1 = new StakingTransaction();
        transaction1.setValidatorAddress(address5);
        transaction1.setType(TransactionType.STAKING);
        transaction1.setWebsite("Website");
        transaction1.setAmount(BigDecimal.valueOf(10));
        transaction1.setAmountWithTransactionFee(BigDecimal.valueOf(1));
        StakingTransaction transaction2 = new StakingTransaction();
        transaction2.setValidatorAddress(address5);
        transaction2.setType(TransactionType.STAKING);
        transaction2.setAmount(BigDecimal.valueOf(10));
        transaction2.setAmountWithTransactionFee(BigDecimal.valueOf(1));
        StakingTransaction transaction3 = new StakingTransaction();
        transaction3.setValidatorAddress(address5);
        transaction3.setType(TransactionType.STAKING);
        transaction3.setAmount(BigDecimal.valueOf(10));
        transaction3.setAmountWithTransactionFee(BigDecimal.valueOf(1));
        list.add(transaction1);
        list.add(transaction2);
        list.add(transaction3);
        TransactionBlock transactionBlock = new TransactionBlock();
        transactionBlock.setHash("hash");
        transactionBlock.setTransactionList(list);
        transactionBlock.setHeight(1);
        MemoryTreePool replica = (MemoryTreePool) TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).clone();
        TreePoolConstructBlock.getInstance().visitForgeTreePool(transactionBlock, replica);
        TreePoolConstructBlock.getInstance().visitInventTreePool(transactionBlock, TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()));
        assertEquals(replica.getRootHash(), TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getRootHash());
        assertEquals(27, TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(address5).get().getStaking_amount().doubleValue());
        assertEquals(967, TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(address5).get().getAmount().doubleValue());
        TreeFactory.ClearMemoryTree(CachedZoneIndex.getInstance().getZoneIndex());
    }

    @SneakyThrows
    @Test
    public void delegate_transaction2() {
        TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).store(address, new PatriciaTreeNode(BigDecimal.valueOf(1000), 0));
        TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).store(address2, new PatriciaTreeNode(BigDecimal.valueOf(1000), 0));
        TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).store(address3, new PatriciaTreeNode(BigDecimal.valueOf(1000), 0));
        TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).store(address4, new PatriciaTreeNode(BigDecimal.valueOf(1000), 0));
        ArrayList<Transaction> list = new ArrayList<>();
        DelegateTransaction transaction1 = new DelegateTransaction();
        transaction1.setDelegatorAddress(address);
        transaction1.setValidatorAddress(address4);
        transaction1.setType(TransactionType.DELEGATE);
        transaction1.setAmount(BigDecimal.valueOf(10));
        transaction1.setAmountWithTransactionFee(BigDecimal.valueOf(1));
        DelegateTransaction transaction2 = new DelegateTransaction();
        transaction2.setDelegatorAddress(address2);
        transaction2.setValidatorAddress(address4);
        transaction2.setType(TransactionType.DELEGATE);
        transaction2.setAmount(BigDecimal.valueOf(10));
        transaction2.setAmountWithTransactionFee(BigDecimal.valueOf(1));
        DelegateTransaction transaction3 = new DelegateTransaction();
        transaction3.setDelegatorAddress(address3);
        transaction3.setValidatorAddress(address4);
        transaction3.setType(TransactionType.DELEGATE);
        transaction3.setAmount(BigDecimal.valueOf(10));
        transaction3.setAmountWithTransactionFee(BigDecimal.valueOf(1));
        list.add(transaction1);
        list.add(transaction2);
        list.add(transaction3);
        TransactionBlock transactionBlock = new TransactionBlock();
        transactionBlock.setHash("hash");
        transactionBlock.setTransactionList(list);
        transactionBlock.setHeight(1);
        MemoryTreePool replica = new MemoryTreePool(((MemoryTreePool) TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex())));
        TreePoolConstructBlock.getInstance().visitForgeTreePool(transactionBlock, replica);
        TreePoolConstructBlock.getInstance().visitInventTreePool(transactionBlock, TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()));
        assertEquals(replica.getRootHash(), TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getRootHash());
        assertEquals(27, TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(address4).get().getStaking_amount().doubleValue());
        assertEquals(989, TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(address).get().getAmount().doubleValue());
        assertEquals(989, TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(address2).get().getAmount().doubleValue());
        assertEquals(989, TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(address3).get().getAmount().doubleValue());
        TreeFactory.ClearMemoryTree(CachedZoneIndex.getInstance().getZoneIndex());
    }

    @SneakyThrows
    @Test
    public void Unstaking_transaction2() {
        TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).store(address, new PatriciaTreeNode(BigDecimal.ZERO, 1000, BigDecimal.valueOf(1000)));
        ArrayList<Transaction> list = new ArrayList<>();
        UnstakingTransaction transaction1 = new UnstakingTransaction();
        transaction1.setValidatorAddress(address);
        transaction1.setType(TransactionType.UNSTAKING);
        transaction1.setAmount(BigDecimal.valueOf(10));
        transaction1.setAmountWithTransactionFee(BigDecimal.valueOf(1));
        UnstakingTransaction transaction2 = new UnstakingTransaction();
        transaction2.setValidatorAddress(address);
        transaction2.setType(TransactionType.UNSTAKING);
        transaction2.setAmount(BigDecimal.valueOf(10));
        transaction2.setAmountWithTransactionFee(BigDecimal.valueOf(1));
        UnstakingTransaction transaction3 = new UnstakingTransaction();
        transaction3.setValidatorAddress(address);
        transaction3.setType(TransactionType.UNSTAKING);
        transaction3.setAmount(BigDecimal.valueOf(10));
        transaction3.setAmountWithTransactionFee(BigDecimal.valueOf(1));
        list.add(transaction1);
        list.add(transaction2);
        list.add(transaction3);
        TransactionBlock transactionBlock = new TransactionBlock();
        transactionBlock.setHash("hash");
        transactionBlock.setTransactionList(list);
        transactionBlock.setHeight(1);
        MemoryTreePool replica = new MemoryTreePool(((MemoryTreePool) TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex())));
        TreePoolConstructBlock.getInstance().visitForgeTreePool(transactionBlock, replica);
        TreePoolConstructBlock.getInstance().visitInventTreePool(transactionBlock, TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()));
        assertEquals(replica.getRootHash(), TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getRootHash());
        assertEquals(967, TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(address).get().getStaking_amount().doubleValue());
        assertEquals(27, TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(address).get().getAmount().doubleValue());
        TreeFactory.ClearMemoryTree(CachedZoneIndex.getInstance().getZoneIndex());
    }

    @SneakyThrows
    @Test
    public void Undelegate_transaction2() {
        TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).store(address, new PatriciaTreeNode(BigDecimal.ZERO, 0));
        TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).store(address2, new PatriciaTreeNode(BigDecimal.ZERO, 0));
        TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).store(address3, new PatriciaTreeNode(BigDecimal.ZERO, 0));
        TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).store(address4, new PatriciaTreeNode(BigDecimal.ZERO, 0, BigDecimal.valueOf(1000), BigDecimal.ZERO));
        ArrayList<Transaction> list = new ArrayList<>();
        UnDelegateTransaction transaction1 = new UnDelegateTransaction();
        transaction1.setDelegatorAddress(address);
        transaction1.setValidatorAddress(address4);
        transaction1.setType(TransactionType.UNDELEGATE);
        transaction1.setAmount(BigDecimal.valueOf(10));
        transaction1.setAmountWithTransactionFee(BigDecimal.valueOf(1));
        UnDelegateTransaction transaction2 = new UnDelegateTransaction();
        transaction2.setDelegatorAddress(address2);
        transaction2.setValidatorAddress(address4);
        transaction2.setType(TransactionType.UNDELEGATE);
        transaction2.setAmount(BigDecimal.valueOf(10));
        transaction2.setAmountWithTransactionFee(BigDecimal.valueOf(1));
        UnDelegateTransaction transaction3 = new UnDelegateTransaction();
        transaction3.setDelegatorAddress(address3);
        transaction3.setValidatorAddress(address4);
        transaction3.setType(TransactionType.UNDELEGATE);
        transaction3.setAmount(BigDecimal.valueOf(10));
        transaction3.setAmountWithTransactionFee(BigDecimal.valueOf(1));
        list.add(transaction1);
        list.add(transaction2);
        list.add(transaction3);
        TransactionBlock transactionBlock = new TransactionBlock();
        transactionBlock.setHash("hash");
        transactionBlock.setTransactionList(list);
        transactionBlock.setHeight(1);
        MemoryTreePool replica = new MemoryTreePool(((MemoryTreePool) TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex())));
        TreePoolConstructBlock.getInstance().visitForgeTreePool(transactionBlock, replica);
        TreePoolConstructBlock.getInstance().visitInventTreePool(transactionBlock, TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()));
        assertEquals(replica.getRootHash(), TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getRootHash());
        assertEquals(970, TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(address4).get().getStaking_amount().doubleValue());
        assertEquals(9, TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(address).get().getAmount().doubleValue());
        assertEquals(9, TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(address2).get().getAmount().doubleValue());
        assertEquals(9, TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(address3).get().getAmount().doubleValue());
        TreeFactory.ClearMemoryTree(CachedZoneIndex.getInstance().getZoneIndex());
    }

    @SneakyThrows
    @Test
    public void UnClaimedReward_transaction() {
        TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).store(address, new PatriciaTreeNode(BigDecimal.ZERO, 0, BigDecimal.ZERO, BigDecimal.valueOf(1000), BigDecimal.valueOf(1000)));
        ArrayList<Transaction> list = new ArrayList<>();
        UnclaimedFeeRewardTransaction transaction1 = new UnclaimedFeeRewardTransaction();
        transaction1.setRecipientAddress(address);
        transaction1.setType(TransactionType.UNCLAIMED_FEE_REWARD);
        transaction1.setAmount(BigDecimal.valueOf(10));
        list.add(transaction1);
        TransactionBlock transactionBlock = new TransactionBlock();
        transactionBlock.setHash("hash");
        transactionBlock.setTransactionList(list);
        transactionBlock.setHeight(1);
        MemoryTreePool replica = new MemoryTreePool(((MemoryTreePool) TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex())));
        TreePoolConstructBlock.getInstance().visitForgeTreePool(transactionBlock, replica);
        TreePoolConstructBlock.getInstance().visitInventTreePool(transactionBlock, TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()));
        assertEquals(replica.getRootHash(), TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getRootHash());
        assertEquals(1010, TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(address).get().getUnclaimed_reward().doubleValue());
        TreeFactory.ClearMemoryTree(CachedZoneIndex.getInstance().getZoneIndex());
    }

    @SneakyThrows
    @Test
    public void reward_transaction() {
        TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).store(address, new PatriciaTreeNode(BigDecimal.ZERO, 0, BigDecimal.ZERO, BigDecimal.valueOf(1000), BigDecimal.valueOf(1000)));
        ArrayList<Transaction> list = new ArrayList<>();
        RewardsTransaction reward = new RewardsTransaction();
        reward.setRecipientAddress(address);
        reward.setType(TransactionType.REWARDS);
        reward.setAmount(BigDecimal.valueOf(10));
        list.add(reward);
        TransactionBlock transactionBlock = new TransactionBlock();
        transactionBlock.setHash("hash");
        transactionBlock.setTransactionList(list);
        transactionBlock.setHeight(1);
        MemoryTreePool replica = new MemoryTreePool(((MemoryTreePool) TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex())));
        TreePoolConstructBlock.getInstance().visitForgeTreePool(transactionBlock, replica);
        TreePoolConstructBlock.getInstance().visitInventTreePool(transactionBlock, TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()));
        assertEquals(replica.getRootHash(), TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getRootHash());
        assertEquals(990, TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(address).get().getUnclaimed_reward().doubleValue());
        assertEquals(10, TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(address).get().getAmount().doubleValue());
        TreeFactory.ClearMemoryTree(CachedZoneIndex.getInstance().getZoneIndex());
    }

}
