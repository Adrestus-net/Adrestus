package io.distributedLedger;

public interface IDriver<T, V> {

    void close(T options);

    V getDB();

}
