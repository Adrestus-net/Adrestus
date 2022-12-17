package io.Adrestus.core;

public interface BlockInvent {
    void InventTransactionBlock(TransactionBlock transactionBlock) throws Exception;

    void InventCommitteBlock(CommitteeBlock committeeBlock);
}
