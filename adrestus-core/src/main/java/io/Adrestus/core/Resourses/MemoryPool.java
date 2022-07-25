package io.Adrestus.core.Resourses;

import io.Adrestus.core.Transaction;

import java.util.Optional;
import java.util.stream.Stream;

public interface MemoryPool {
    Stream<Transaction> getAll() throws Exception;

    Optional<Transaction> getById(int id) throws Exception;

    boolean add(Transaction customer) throws Exception;

    boolean update(Transaction customer) throws Exception;

    boolean delete(Transaction customer) throws Exception;
}
