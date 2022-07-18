package io.Adrestus.core;

public class BlockFactory {

    public static BlockForge getCoin(BlockType type) {
        return type.getConstructor().get();
    }
}
