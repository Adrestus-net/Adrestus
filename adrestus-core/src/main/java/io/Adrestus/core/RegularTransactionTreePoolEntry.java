package io.Adrestus.core;

import io.Adrestus.IMemoryTreePool;
import io.Adrestus.core.Resourses.CachedZoneIndex;

import java.util.ArrayList;

public class RegularTransactionTreePoolEntry implements TransactionTreePoolEntries<RegularTransaction> {

    private ArrayList<RegularTransaction> transactionList;

    @Override
    public void ForgeEntriesBuilder(IMemoryTreePool memoryTreePool) {
        transactionList.forEach(transaction -> {
            if ((transaction.getZoneFrom() == CachedZoneIndex.getInstance().getZoneIndex()) && (transaction.getZoneTo() == CachedZoneIndex.getInstance().getZoneIndex())) {
                memoryTreePool.withdraw(transaction.getFrom(), transaction.getAmount());
                memoryTreePool.deposit(transaction.getTo(), transaction.getAmount());
            } else {
                memoryTreePool.withdraw(transaction.getFrom(), transaction.getAmount());
            }
        });
    }

    @Override
    public void InventEntriesBuilder(IMemoryTreePool memoryTreePool, int blockHeight) {
        for (int i = 0; i < transactionList.size(); i++) {
            memoryTreePool.getByaddress(transactionList.get(i).getFrom()).get().addTransactionPosition(transactionList.get(i).getHash(), CachedZoneIndex.getInstance().getZoneIndex(), blockHeight, i);
            memoryTreePool.getByaddress(transactionList.get(i).getTo()).get().addTransactionPosition(transactionList.get(i).getHash(), CachedZoneIndex.getInstance().getZoneIndex(), blockHeight, i);
            if ((transactionList.get(i).getZoneFrom() == CachedZoneIndex.getInstance().getZoneIndex()) && (transactionList.get(i).getZoneTo() == CachedZoneIndex.getInstance().getZoneIndex())) {
                memoryTreePool.withdraw(transactionList.get(i).getFrom(), transactionList.get(i).getAmount());
                memoryTreePool.deposit(transactionList.get(i).getTo(), transactionList.get(i).getAmount());
            } else {
                memoryTreePool.withdraw(transactionList.get(i).getFrom(), transactionList.get(i).getAmount());
            }
        }
    }

    @Override
    public void SetArrayList(ArrayList<RegularTransaction> transactionList) {
        this.transactionList = new ArrayList<>(transactionList);
    }

    @Override
    public void Clear() {
        this.transactionList.clear();
        this.transactionList = null;
    }
}
