package io.Adrestus.core;

import io.Adrestus.IMemoryTreePool;
import io.Adrestus.Trie.PatriciaTreeTransactionType;
import io.Adrestus.core.Resourses.CachedZoneIndex;

import java.util.ArrayList;

public class DelegateTransactionTreePoolEntry implements TransactionTreePoolEntries<DelegateTransaction> {

    private ArrayList<DelegateTransaction> delegateList;

    @Override
    public void ForgeEntriesBuilder(IMemoryTreePool memoryTreePool) {
        delegateList.forEach(transaction -> {
            memoryTreePool.withdraw(PatriciaTreeTransactionType.DELEGATE, transaction.getDelegatorAddress(), transaction.getAmount(), transaction.getAmountWithTransactionFee());
            memoryTreePool.deposit(PatriciaTreeTransactionType.DELEGATE, transaction.getValidatorAddress(), transaction.getAmount(), transaction.getAmountWithTransactionFee());
        });
    }

    @Override
    public void InventEntriesBuilder(IMemoryTreePool memoryTreePool, int blockHeight) {
        for (int i = 0; i < delegateList.size(); i++) {
            DelegateTransaction transaction = delegateList.get(i);
            memoryTreePool.getByaddress(transaction.getDelegatorAddress()).get().addTransactionPosition(PatriciaTreeTransactionType.valueOf(transaction.getType().toString()), transaction.getHash(), CachedZoneIndex.getInstance().getZoneIndex(), blockHeight, i);
            memoryTreePool.getByaddress(transaction.getValidatorAddress()).get().addTransactionPosition(PatriciaTreeTransactionType.valueOf(transaction.getType().toString()), transaction.getHash(), CachedZoneIndex.getInstance().getZoneIndex(), blockHeight, i);
            memoryTreePool.withdraw(PatriciaTreeTransactionType.DELEGATE, transaction.getDelegatorAddress(), transaction.getAmount(), transaction.getAmountWithTransactionFee());
            memoryTreePool.deposit(PatriciaTreeTransactionType.DELEGATE, transaction.getValidatorAddress(), transaction.getAmount(), transaction.getAmountWithTransactionFee());
        }
    }

    @Override
    public void SetArrayList(ArrayList<DelegateTransaction> delegateList) {
        this.delegateList = new ArrayList<>(delegateList);
    }

    @Override
    public void Clear() {
        this.delegateList.clear();
        this.delegateList = null;
    }
}
