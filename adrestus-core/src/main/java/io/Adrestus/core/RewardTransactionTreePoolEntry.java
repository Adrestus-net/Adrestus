package io.Adrestus.core;

import io.Adrestus.IMemoryTreePool;
import io.Adrestus.core.Resourses.CachedZoneIndex;

import java.util.ArrayList;

public class RewardTransactionTreePoolEntry implements TransactionTreePoolEntries<RewardsTransaction> {

    private ArrayList<RewardsTransaction> transactionList;

    @Override
    public void ForgeEntriesBuilder(IMemoryTreePool memoryTreePool) {
        transactionList.forEach(transaction -> {
            if ((transaction.getZoneFrom() == CachedZoneIndex.getInstance().getZoneIndex()) && (transaction.getZoneTo() == CachedZoneIndex.getInstance().getZoneIndex()) && CachedZoneIndex.getInstance().getZoneIndex() == 0) {
                memoryTreePool.withdrawUnclaimedReward(((RewardsTransaction) transaction).getRecipientAddress(), transaction.getAmount());
                memoryTreePool.deposit(((RewardsTransaction) transaction).getRecipientAddress(), transaction.getAmount());
            } else
                throw new IllegalArgumentException("Reward Forge transaction error");
        });
    }

    @Override
    public void InventEntriesBuilder(IMemoryTreePool memoryTreePool, int blockHeight) {
        for (int i = 0; i < transactionList.size(); i++) {
            memoryTreePool.getByaddress(transactionList.get(i).getRecipientAddress()).get().addTransactionPosition(transactionList.get(i).getHash(), CachedZoneIndex.getInstance().getZoneIndex(), blockHeight, i);
            if ((transactionList.get(i).getZoneFrom() == transactionList.get(i).getZoneTo()) && (CachedZoneIndex.getInstance().getZoneIndex() == 0)) {
                memoryTreePool.withdrawUnclaimedReward(transactionList.get(i).getRecipientAddress(), transactionList.get(i).getAmount());
                memoryTreePool.deposit(transactionList.get(i).getRecipientAddress(), transactionList.get(i).getAmount());
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
