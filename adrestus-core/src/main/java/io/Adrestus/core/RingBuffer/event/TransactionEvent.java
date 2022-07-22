package io.Adrestus.core.RingBuffer.event;

import io.Adrestus.core.Transaction;

import java.io.Serializable;

public class TransactionEvent {
    private Transaction transaction;

    public TransactionEvent(Transaction transaction) {
        this.transaction = transaction;
    }

    public TransactionEvent() {
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

}
