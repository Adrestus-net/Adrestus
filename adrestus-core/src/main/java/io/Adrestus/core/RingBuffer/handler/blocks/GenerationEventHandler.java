package io.Adrestus.core.RingBuffer.handler.blocks;

import io.Adrestus.core.AbstractBlock;
import io.Adrestus.core.CommitteeBlock;
import io.Adrestus.core.Resourses.CachedLatestBlocks;
import io.Adrestus.core.RingBuffer.event.AbstractBlockEvent;
import io.Adrestus.core.TransactionBlock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenerationEventHandler implements BlockEventHandler<AbstractBlockEvent>, DisruptorBlockVisitor {
    private static Logger LOG = LoggerFactory.getLogger(GenerationEventHandler.class);

    @Override
    public void onEvent(AbstractBlockEvent blockEvent, long l, boolean b) throws Exception {
        try {
            AbstractBlock block = blockEvent.getBlock();
            block.accept(this);
        } catch (NullPointerException ex) {
            LOG.info("Block is empty");
        }
    }

    @Override
    public void visit(CommitteeBlock committeeBlock) {
        if (committeeBlock.getGeneration() != CachedLatestBlocks.getInstance().getCommitteeBlock().getGeneration() + 1)
            LOG.info("CommitteeBlock Generation is not valid");
    }

    @Override
    public void visit(TransactionBlock transactionBlock) {
        if (transactionBlock.getGeneration() != CachedLatestBlocks.getInstance().getCommitteeBlock().getGeneration())
            LOG.info("TransactionBlock Generation is not valid");
    }
}
