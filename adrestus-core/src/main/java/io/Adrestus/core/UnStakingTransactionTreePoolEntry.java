package io.Adrestus.core;

import io.Adrestus.IMemoryTreePool;
import io.Adrestus.Trie.PatriciaTreeTransactionType;
import io.Adrestus.core.Resourses.CachedZoneIndex;

import java.util.ArrayList;

public class UnStakingTransactionTreePoolEntry implements TransactionTreePoolEntries<UnstakingTransaction> {

    private ArrayList<UnstakingTransaction> stakingList;

    @Override
    public void ForgeEntriesBuilder(IMemoryTreePool memoryTreePool) {
        stakingList.forEach(transaction -> {
            memoryTreePool.withdraw(PatriciaTreeTransactionType.UNSTAKING,transaction.getValidatorAddress(), transaction.getAmount(),transaction.getAmountWithTransactionFee());
            memoryTreePool.deposit(PatriciaTreeTransactionType.UNSTAKING,transaction.getValidatorAddress(), transaction.getAmount(),transaction.getAmountWithTransactionFee());
        });
    }

    @Override
    public void InventEntriesBuilder(IMemoryTreePool memoryTreePool, int blockHeight) {
        for (int i = 0; i < stakingList.size(); i++) {
            StakingTransaction transaction = stakingList.get(i);
            memoryTreePool.getByaddress(transaction.getValidatorAddress()).get().addTransactionPosition(PatriciaTreeTransactionType.valueOf(transaction.getType().toString()), transaction.getHash(), CachedZoneIndex.getInstance().getZoneIndex(), blockHeight, i);
            memoryTreePool.withdraw(PatriciaTreeTransactionType.UNSTAKING,transaction.getValidatorAddress(), transaction.getAmount(),transaction.getAmountWithTransactionFee());
            memoryTreePool.deposit(PatriciaTreeTransactionType.UNSTAKING,transaction.getValidatorAddress(), transaction.getAmount(),transaction.getAmountWithTransactionFee());
        }
    }

    @Override
    public void SetArrayList(ArrayList<UnstakingTransaction> stakingList) {
        this.stakingList = new ArrayList<>(stakingList);
    }

    @Override
    public void Clear() {
        this.stakingList.clear();
        this.stakingList = null;
    }
}
