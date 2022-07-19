package io.Adrestus.core;

import org.junit.jupiter.api.Test;

public class BlockTest {

    @Test
    public void block_test() {
     TransactionBlock t=new TransactionBlock(2);
     t.accept(new Genesis());
    }

    @Test
    public void block_test2() {
       DefaultFactory factory=new DefaultFactory(new TransactionBlock(3),new CommitteeBlock(6));
       var genesis= (Genesis) factory.getBlock(BlockType.GENESIS);
       var regural_block=factory.getBlock(BlockType.REGULAR);
       factory.accept(genesis);
       factory.accept(regural_block);
    }
}
