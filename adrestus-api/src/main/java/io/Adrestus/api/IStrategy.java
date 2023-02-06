package io.Adrestus.api;

public interface IStrategy {
    void execute();
    void block_until_send();
    void terminate();
}
