package io.Adrestus.core;


import java.util.Arrays;

public class DefaultFactory implements BlockFactory {

    private final BlockFactory[] children;

    public DefaultFactory(BlockFactory... children) {
        this.children = children;
    }

    public void accept(BlockForge visitor) {
        Arrays.stream(children).forEach(child -> {
            try {
                child.accept(visitor);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public BlockForge getBlock(BlockType type) {
        return type.getConstructor().get();
    }
}
