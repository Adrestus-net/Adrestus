package io.Adrestus.core;


public class Genesis implements BlockForge {


    public void init(){

    }
    @Override
    public void forgeTransactionBlock(TransactionBlock transactionBlock) {
        System.out.println("edw"+transactionBlock.var);
    }

    @Override
    public void forgeCommitteBlock(CommitteeBlock committeeBlock) {
        System.out.println("commite"+committeeBlock.var);
    }
}
