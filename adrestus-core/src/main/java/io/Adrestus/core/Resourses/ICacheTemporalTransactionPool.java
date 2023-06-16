package io.Adrestus.core.Resourses;

import io.Adrestus.core.Transaction;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;

public interface ICacheTemporalTransactionPool {
    void add(Transaction transaction);

    Optional<Transaction> get(String key);

    boolean isExist(String key);

    void remove(String key, Transaction transaction);

    ConcurrentMap<String, List<Transaction>> getAsMap();

    void clear();

    long size();
}
