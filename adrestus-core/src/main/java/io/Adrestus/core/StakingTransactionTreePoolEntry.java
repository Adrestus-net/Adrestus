package io.Adrestus.core;

import io.Adrestus.IMemoryTreePool;
import io.Adrestus.Trie.PatriciaTreeTransactionType;
import io.Adrestus.Trie.StakingInfo;
import io.Adrestus.core.Resourses.CachedZoneIndex;

import java.util.ArrayList;

public class StakingTransactionTreePoolEntry implements TransactionTreePoolEntries<StakingTransaction> {

    private ArrayList<StakingTransaction> stakingList;

    @Override
    public void ForgeEntriesBuilder(IMemoryTreePool memoryTreePool) {
        stakingList.forEach(transaction -> {
            memoryTreePool.setStakingInfos(transaction.getValidatorAddress(), new StakingInfo(transaction.getName(), transaction.getCommissionRate(), transaction.getIdentity(), transaction.getWebsite(), transaction.getDetails()));
            memoryTreePool.withdraw(PatriciaTreeTransactionType.STAKING, transaction.getValidatorAddress(), transaction.getAmount(), transaction.getAmountWithTransactionFee());
            memoryTreePool.deposit(PatriciaTreeTransactionType.STAKING, transaction.getValidatorAddress(), transaction.getAmount(), transaction.getAmountWithTransactionFee());
        });
    }

    @Override
    public void InventEntriesBuilder(IMemoryTreePool memoryTreePool, int blockHeight) {
        for (int i = 0; i < stakingList.size(); i++) {
            StakingTransaction transaction = stakingList.get(i);
            memoryTreePool.setStakingInfos(transaction.getValidatorAddress(), new StakingInfo(transaction.getName(), transaction.getCommissionRate(), transaction.getIdentity(), transaction.getWebsite(), transaction.getDetails()));
            memoryTreePool.getByaddress(transaction.getValidatorAddress()).get().addTransactionPosition(PatriciaTreeTransactionType.valueOf(transaction.getType().toString()), transaction.getHash(), CachedZoneIndex.getInstance().getZoneIndex(), blockHeight, i);
            memoryTreePool.withdraw(PatriciaTreeTransactionType.STAKING, transaction.getValidatorAddress(), transaction.getAmount(), transaction.getAmountWithTransactionFee());
            memoryTreePool.deposit(PatriciaTreeTransactionType.STAKING, transaction.getValidatorAddress(), transaction.getAmount(), transaction.getAmountWithTransactionFee());
        }
    }

    @Override
    public void SetArrayList(ArrayList<StakingTransaction> stakingList) {
        this.stakingList = new ArrayList<>(stakingList);
    }

    @Override
    public void Clear() {
        this.stakingList.clear();
        this.stakingList = null;
    }
}
