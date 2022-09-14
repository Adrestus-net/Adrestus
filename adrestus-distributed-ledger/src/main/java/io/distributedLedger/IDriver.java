package io.distributedLedger;

@FunctionalInterface
public interface IDriver<T> {
    T get();
}
