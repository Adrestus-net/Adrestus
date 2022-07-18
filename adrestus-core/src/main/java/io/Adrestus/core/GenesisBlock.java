package io.Adrestus.core;

public class GenesisBlock implements BlockForge<TransactionBlock> {

    @Override
    public void forgeBlock(TransactionBlock transactionBlock) {
        System.out.println(transactionBlock.var);
    }
}
