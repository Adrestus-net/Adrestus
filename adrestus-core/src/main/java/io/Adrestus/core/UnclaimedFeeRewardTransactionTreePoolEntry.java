package io.Adrestus.core;

import io.Adrestus.IMemoryTreePool;
import io.Adrestus.Trie.PatriciaTreeTransactionType;

import java.util.ArrayList;

public class UnclaimedFeeRewardTransactionTreePoolEntry implements TransactionTreePoolEntries<UnclaimedFeeRewardTransaction> {
    private ArrayList<UnclaimedFeeRewardTransaction> transactionList;

    @Override
    public void ForgeEntriesBuilder(IMemoryTreePool memoryTreePool) {
        try {
            UnclaimedFeeRewardTransaction unclaimedFeeRewardTransaction = (UnclaimedFeeRewardTransaction) transactionList.get(0);
            memoryTreePool.deposit(PatriciaTreeTransactionType.UNCLAIMED_FEE_REWARD,unclaimedFeeRewardTransaction.getRecipientAddress(), unclaimedFeeRewardTransaction.getAmount());
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void InventEntriesBuilder(IMemoryTreePool memoryTreePool, int blockHeight) {
        try {
            UnclaimedFeeRewardTransaction unclaimedFeeRewardTransaction = (UnclaimedFeeRewardTransaction) transactionList.get(0);
            memoryTreePool.deposit(PatriciaTreeTransactionType.UNCLAIMED_FEE_REWARD,unclaimedFeeRewardTransaction.getRecipientAddress(), unclaimedFeeRewardTransaction.getAmount());
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void SetArrayList(ArrayList<UnclaimedFeeRewardTransaction> transactionList) {
        this.transactionList = new ArrayList<>(transactionList);
    }

    @Override
    public void Clear() {
        this.transactionList.clear();
        this.transactionList = null;
    }
}
