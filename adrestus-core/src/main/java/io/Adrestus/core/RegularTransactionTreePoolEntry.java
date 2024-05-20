package io.Adrestus.core;

import io.Adrestus.IMemoryTreePool;
import io.Adrestus.Trie.PatriciaTreeTransactionType;
import io.Adrestus.core.Resourses.CachedZoneIndex;

import java.util.ArrayList;

public class RegularTransactionTreePoolEntry implements TransactionTreePoolEntries<RegularTransaction> {

    private ArrayList<RegularTransaction> transactionList;

    @Override
    public void ForgeEntriesBuilder(IMemoryTreePool memoryTreePool) {
        transactionList.forEach(transaction -> {
            if ((transaction.getZoneFrom() == CachedZoneIndex.getInstance().getZoneIndex()) && (transaction.getZoneTo() == CachedZoneIndex.getInstance().getZoneIndex())) {
                memoryTreePool.withdraw(PatriciaTreeTransactionType.REGULAR, transaction.getFrom(), transaction.getAmount(), transaction.getAmountWithTransactionFee());
                memoryTreePool.deposit(PatriciaTreeTransactionType.REGULAR, transaction.getTo(), transaction.getAmount(), transaction.getAmountWithTransactionFee());
            } else {
                memoryTreePool.withdraw(PatriciaTreeTransactionType.REGULAR, transaction.getFrom(), transaction.getAmount(), transaction.getAmountWithTransactionFee());
            }
        });
    }

    @Override
    public void InventEntriesBuilder(IMemoryTreePool memoryTreePool, int blockHeight) {
        for (int i = 0; i < transactionList.size(); i++) {
            Transaction transaction = transactionList.get(i);
            memoryTreePool.getByaddress(transaction.getFrom()).get().addTransactionPosition(PatriciaTreeTransactionType.valueOf(transaction.getType().toString()), transaction.getHash(), CachedZoneIndex.getInstance().getZoneIndex(), blockHeight, i);
            memoryTreePool.getByaddress(transaction.getTo()).get().addTransactionPosition(PatriciaTreeTransactionType.valueOf(transaction.getType().toString()), transaction.getHash(), CachedZoneIndex.getInstance().getZoneIndex(), blockHeight, i);
            if ((transaction.getZoneFrom() == CachedZoneIndex.getInstance().getZoneIndex()) && (transaction.getZoneTo() == CachedZoneIndex.getInstance().getZoneIndex())) {
                memoryTreePool.withdraw(PatriciaTreeTransactionType.REGULAR, transaction.getFrom(), transaction.getAmount(), transaction.getAmountWithTransactionFee());
                memoryTreePool.deposit(PatriciaTreeTransactionType.REGULAR, transaction.getTo(), transaction.getAmount(), transaction.getAmountWithTransactionFee());
            } else {
                memoryTreePool.withdraw(PatriciaTreeTransactionType.REGULAR, transaction.getFrom(), transaction.getAmount(), transaction.getAmountWithTransactionFee());
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
