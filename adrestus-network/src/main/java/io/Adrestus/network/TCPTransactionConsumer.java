package io.Adrestus.network;

@FunctionalInterface
public interface TCPTransactionConsumer<T> {
    String accept(T t) throws Exception;
}
