package io.Adrestus.core;


public class Genesis implements BlockForge {


    public void init() {

    }

    @Override
    public void forgeTransactionBlock(TransactionBlock transactionBlock) {
        System.out.println(transactionBlock.getHash());
    }

    @Override
    public void forgeCommitteBlock(CommitteeBlock committeeBlock) {

    }

}
