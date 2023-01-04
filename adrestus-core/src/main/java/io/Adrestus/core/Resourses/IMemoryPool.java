package io.Adrestus.core.Resourses;

import java.util.List;
import io.vavr.control.Option;

import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.stream.Stream;

public interface IMemoryPool<T> {
    Stream<T> getAllStream() throws Exception;

    List<T> getAll() throws Exception;

    Lock getR();

    Lock getW();

    Optional<T> getObjectByHash(String hash) throws Exception;

    boolean add(T Object) throws Exception;

    void delete(List<T> list_Object);

    void delete(T Object);


    boolean checkAdressExists(T Object) throws Exception;

    boolean checkHashExists(T Object) throws Exception;

    boolean checkTimestamp(T Object) throws Exception;

    void printAll() throws Exception;

    void clear();
}
