package io.Adrestus.core.Util;

import io.Adrestus.core.CommitteeBlock;
import io.Adrestus.core.TransactionBlock;

public abstract class AbstractBlockSizeCalculator {

    protected TransactionBlock transactionBlock;
    protected CommitteeBlock committeeBlock;

    public AbstractBlockSizeCalculator(TransactionBlock abstractBlock) {
        this.transactionBlock = abstractBlock;
    }

    public AbstractBlockSizeCalculator(CommitteeBlock committeeBlock) {
        this.committeeBlock = committeeBlock;
    }


    public AbstractBlockSizeCalculator() {
    }

    public int AbstractTransactionBlockSizeCalculator() {
        return transactionBlock.getSignatureData().size() == 0 ? 1024 : 1024 + (transactionBlock.getSignatureData().size() * 1024);
    }

    public int AbstractCommitteeBlockSizeCalculator() {
        return committeeBlock.getSignatureData().size() == 0 ? 1024 : 1024 + (committeeBlock.getSignatureData().size() * 1024);
    }

    public TransactionBlock getTransactionBlock() {
        return transactionBlock;
    }

    public void setTransactionBlock(TransactionBlock transactionBlock) {
        this.transactionBlock = transactionBlock;
    }

    public CommitteeBlock getCommitteeBlock() {
        return committeeBlock;
    }

    public void setCommitteeBlock(CommitteeBlock committeeBlock) {
        this.committeeBlock = committeeBlock;
    }
}
