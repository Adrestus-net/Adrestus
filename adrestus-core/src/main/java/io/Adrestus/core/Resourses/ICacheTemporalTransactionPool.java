package io.Adrestus.core.Resourses;

import io.Adrestus.core.Transaction;

import java.util.Optional;

public interface ICacheTemporalTransactionPool {
    void add(Transaction transaction);

    Optional<Transaction> get(String key);

    boolean isExist(String key);

    void remove(String key, Transaction transaction);

    void clear();

    long size();
}
