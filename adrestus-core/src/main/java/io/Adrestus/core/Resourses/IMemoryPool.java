package io.Adrestus.core.Resourses;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.stream.Stream;

public interface IMemoryPool<T> {
    Stream<T> getAllStream() throws Exception;

    List<T> getAll() throws Exception;

    Lock getR();

    Lock getW();

    Optional<T> getTransactionByHash(String hash) throws Exception;

    boolean add(T transaction) throws Exception;

    void delete(List<T> list_transaction);

    void delete(T transaction);

    boolean checkAdressExists(T transaction) throws Exception;

    boolean checkHashExists(T transaction) throws Exception;

    boolean checkTimestamp(T transaction) throws Exception;

    void printAll() throws Exception;

    void clear();
}
