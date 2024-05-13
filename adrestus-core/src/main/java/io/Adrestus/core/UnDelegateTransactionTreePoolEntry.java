package io.Adrestus.core;

import io.Adrestus.IMemoryTreePool;
import io.Adrestus.Trie.PatriciaTreeTransactionType;
import io.Adrestus.core.Resourses.CachedZoneIndex;

import java.util.ArrayList;

public class UnDelegateTransactionTreePoolEntry implements TransactionTreePoolEntries<UnDelegateTransaction> {

    private ArrayList<UnDelegateTransaction> undelegateList;

    @Override
    public void ForgeEntriesBuilder(IMemoryTreePool memoryTreePool) {
        undelegateList.forEach(transaction -> {
            memoryTreePool.withdraw(PatriciaTreeTransactionType.UNDELEGATE,transaction.getValidatorAddress(), transaction.getAmount(),transaction.getAmountWithTransactionFee());
            memoryTreePool.deposit(PatriciaTreeTransactionType.UNDELEGATE,transaction.getDelegatorAddress(),  transaction.getAmount(),transaction.getAmountWithTransactionFee());
        });
    }

    @Override
    public void InventEntriesBuilder(IMemoryTreePool memoryTreePool, int blockHeight) {
        for (int i = 0; i < undelegateList.size(); i++) {
            UnDelegateTransaction transaction = undelegateList.get(i);
            memoryTreePool.getByaddress(transaction.getDelegatorAddress()).get().addTransactionPosition(PatriciaTreeTransactionType.valueOf(transaction.getType().toString()), transaction.getHash(), CachedZoneIndex.getInstance().getZoneIndex(), blockHeight, i);
            memoryTreePool.getByaddress(transaction.getValidatorAddress()).get().addTransactionPosition(PatriciaTreeTransactionType.valueOf(transaction.getType().toString()), transaction.getHash(), CachedZoneIndex.getInstance().getZoneIndex(), blockHeight, i);
            memoryTreePool.withdraw(PatriciaTreeTransactionType.UNDELEGATE,transaction.getValidatorAddress(), transaction.getAmount(),transaction.getAmountWithTransactionFee());
            memoryTreePool.deposit(PatriciaTreeTransactionType.UNDELEGATE,transaction.getDelegatorAddress(), transaction.getAmount(),transaction.getAmountWithTransactionFee());
        }
    }

    @Override
    public void SetArrayList(ArrayList<UnDelegateTransaction> undelegateList) {
        this.undelegateList = new ArrayList<>(undelegateList);
    }

    @Override
    public void Clear() {
        this.undelegateList.clear();
        this.undelegateList = null;
    }
}
