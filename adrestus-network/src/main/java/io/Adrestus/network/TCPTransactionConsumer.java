package io.Adrestus.network;

@FunctionalInterface
public interface TCPTransactionConsumer<T> {
    void accept(T t) throws Exception;
}
