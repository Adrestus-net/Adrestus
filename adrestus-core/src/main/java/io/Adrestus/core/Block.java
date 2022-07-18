package io.Adrestus.core;

public class Block implements BlockForge<TransactionBlock,TransactionBlock> {

    @Override
    public TransactionBlock forgeCommiteBlock(TransactionBlock transactionBlock) {
        return null;
    }

    @Override
    public TransactionBlock forgeTransactionBlock(TransactionBlock transactionBlock) {
        return null;
    }
}
