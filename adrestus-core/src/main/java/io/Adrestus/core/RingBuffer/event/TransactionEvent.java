package io.Adrestus.core.RingBuffer.event;

import io.Adrestus.core.Transaction;

public class TransactionEvent implements Cloneable {
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

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
