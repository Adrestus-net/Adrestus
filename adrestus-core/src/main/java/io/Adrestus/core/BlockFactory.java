package io.Adrestus.core;

public interface BlockFactory {
    void accept(BlockForge visitor) throws Exception;

    void accept(BlockInvent invent) throws Exception;
}
