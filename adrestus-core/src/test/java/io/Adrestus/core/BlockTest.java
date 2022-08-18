package io.Adrestus.core;

import org.junit.jupiter.api.Test;

public class BlockTest {

    @Test
    public void block_test() {
        AbstractBlock t = new TransactionBlock();
        t.setHash("hash");
        t.accept(new Genesis());
    }

    @Test
    public void block_test2() {
        DefaultFactory factory = new DefaultFactory(new TransactionBlock(), new CommitteeBlock());
        var genesis = (Genesis) factory.getBlock(BlockType.GENESIS);
        var regural_block = factory.getBlock(BlockType.REGULAR);
        factory.accept(genesis);
        factory.accept(regural_block);
    }
}
