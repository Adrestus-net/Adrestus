package io.Adrestus.core;

import java.io.Serializable;
import java.util.Objects;

public class ReceiptBlock implements Serializable {
    private StatusType statusType;
    private Receipt receipt;
    private TransactionBlock transactionBlock;

    private Transaction transaction;

    public ReceiptBlock() {
    }

    public ReceiptBlock(StatusType statusType, Receipt receipt, TransactionBlock transactionBlock, Transaction transaction) {
        this.statusType = statusType;
        this.receipt = receipt;
        this.transactionBlock = transactionBlock;
        this.transaction = transaction;
    }

    public StatusType getStatusType() {
        return statusType;
    }

    public void setStatusType(StatusType statusType) {
        this.statusType = statusType;
    }

    public Receipt getReceipt() {
        return receipt;
    }

    public void setReceipt(Receipt receipt) {
        this.receipt = receipt;
    }

    public TransactionBlock getTransactionBlock() {
        return transactionBlock;
    }

    public void setTransactionBlock(TransactionBlock transactionBlock) {
        this.transactionBlock = transactionBlock;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReceiptBlock that = (ReceiptBlock) o;
        return statusType == that.statusType && Objects.equals(receipt, that.receipt) && Objects.equals(transactionBlock, that.transactionBlock) && Objects.equals(transaction, that.transaction);
    }

    @Override
    public int hashCode() {
        return Objects.hash(statusType, receipt, transactionBlock, transaction);
    }


    public void clear(){
        this.statusType=null;
        this.receipt=null;
        this.transactionBlock=null;
        this.transaction=null;
    }
}
