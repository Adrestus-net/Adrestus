package io.Adrestus.core.Resourses;

import io.Adrestus.core.Transaction;

import java.util.Optional;
import java.util.stream.Stream;

public interface MemoryPool {
    Stream<Transaction> getAll() throws Exception;

    Optional<Transaction> getById(int id) throws Exception;

    boolean add(Transaction transaction) throws Exception;

    boolean update(Transaction transaction) throws Exception;

    boolean delete(Transaction transaction) throws Exception;

    boolean checkHashExists(Transaction transaction) throws Exception;

    boolean checkTimestamp(Transaction transaction) throws Exception;
}
