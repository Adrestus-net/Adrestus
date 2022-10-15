package io.Adrestus.core.Resourses;

import io.Adrestus.core.Transaction;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.stream.Stream;

public interface IMemoryPool {
    Stream<Transaction> getAllStream() throws Exception;

    List<Transaction> getAll() throws Exception;

    Lock getR();

    Lock getW();

    Optional<Transaction> getTransactionByHash(String hash) throws Exception;

    boolean add(Transaction transaction) throws Exception;

    void delete(List<Transaction> list_transaction) throws Exception;

    void delete(Transaction transaction) throws Exception;

    boolean checkHashExists(Transaction transaction) throws Exception;

    boolean checkTimestamp(Transaction transaction) throws Exception;

    void printAll() throws Exception;

    void clear();
}
