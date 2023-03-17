package io.Adrestus.core;

public interface BlockForge {

    void forgeTransactionBlock(TransactionBlock transactionBlock) throws Exception;

    void forgeCommitteBlock(CommitteeBlock committeeBlock);
}
