package io.Adrestus.core;

import org.junit.jupiter.api.Test;

public class BlockTest {

    @Test
    public void block_test() {
        var genesis= BlockFactory.getCoin(BlockType.GENESIS);
        var regular= BlockFactory.getCoin(BlockType.REGULAR);

        genesis.forgeBlock(new TransactionBlock(3));
    }
}
