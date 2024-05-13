package io.Adrestus.core;

import io.Adrestus.IMemoryTreePool;
import io.Adrestus.Trie.PatriciaTreeTransactionType;
import io.Adrestus.core.Resourses.CachedZoneIndex;

import java.util.ArrayList;

public class RewardTransactionTreePoolEntry implements TransactionTreePoolEntries<RewardsTransaction> {

    private ArrayList<RewardsTransaction> transactionList;

    @Override
    public void ForgeEntriesBuilder(IMemoryTreePool memoryTreePool) {
        transactionList.forEach(transaction -> {
            if ((transaction.getZoneFrom() == CachedZoneIndex.getInstance().getZoneIndex()) && (transaction.getZoneTo() == CachedZoneIndex.getInstance().getZoneIndex()) && CachedZoneIndex.getInstance().getZoneIndex() == 0) {
                memoryTreePool.withdraw(PatriciaTreeTransactionType.REWARDS,transaction.getRecipientAddress(), transaction.getAmount(),transaction.getAmountWithTransactionFee());
                memoryTreePool.deposit(PatriciaTreeTransactionType.REWARDS,transaction.getRecipientAddress(), transaction.getAmount(),transaction.getAmountWithTransactionFee());
            } else
                throw new IllegalArgumentException("Reward Forge transaction error");
        });
    }

    @Override
    public void InventEntriesBuilder(IMemoryTreePool memoryTreePool, int blockHeight) {
        for (int i = 0; i < transactionList.size(); i++) {
            RewardsTransaction transaction = transactionList.get(i);
            memoryTreePool.getByaddress(transaction.getRecipientAddress()).get().addTransactionPosition(PatriciaTreeTransactionType.REWARDS,transaction.getHash(), CachedZoneIndex.getInstance().getZoneIndex(), blockHeight, i);
            if ((transaction.getZoneFrom() == transaction.getZoneTo()) && (CachedZoneIndex.getInstance().getZoneIndex() == 0)) {
                memoryTreePool.withdraw(PatriciaTreeTransactionType.REWARDS,transaction.getRecipientAddress(), transaction.getAmount(),transaction.getAmountWithTransactionFee());
                memoryTreePool.deposit(PatriciaTreeTransactionType.REWARDS,transaction.getRecipientAddress(), transaction.getAmount(),transaction.getAmountWithTransactionFee());
            } else
                throw new IllegalArgumentException("Reward Invent transaction error");
        }
    }

    @Override
    public void SetArrayList(ArrayList<RewardsTransaction> transactionList) {
        this.transactionList = new ArrayList<>(transactionList);
    }

    @Override
    public void Clear() {
        this.transactionList.clear();
        this.transactionList = null;
    }
}
