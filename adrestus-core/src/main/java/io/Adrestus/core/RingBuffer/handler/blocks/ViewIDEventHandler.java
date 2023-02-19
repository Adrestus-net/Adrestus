package io.Adrestus.core.RingBuffer.handler.blocks;

import io.Adrestus.core.AbstractBlock;
import io.Adrestus.core.CommitteeBlock;
import io.Adrestus.core.Resourses.CachedLatestBlocks;
import io.Adrestus.core.RingBuffer.event.AbstractBlockEvent;
import io.Adrestus.core.TransactionBlock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ViewIDEventHandler implements BlockEventHandler<AbstractBlockEvent>, DisruptorBlockVisitor {
    private static Logger LOG = LoggerFactory.getLogger(ViewIDEventHandler.class);

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
        if (committeeBlock.getViewID() != CachedLatestBlocks.getInstance().getCommitteeBlock().getViewID() + 1)
            LOG.info("CommitteeBlock ViewID is not valid");
    }

    @Override
    public void visit(TransactionBlock transactionBlock) {
        if (transactionBlock.getViewID() != CachedLatestBlocks.getInstance().getTransactionBlock().getViewID() + 1)
            LOG.info("TransactionBlock ViewID is not valid");
    }
}
