package io.Adrestus.core.RingBuffer.handler.blocks;

import io.Adrestus.core.AbstractBlock;
import io.Adrestus.core.CommitteeBlock;
import io.Adrestus.core.Resourses.CachedLatestBlocks;
import io.Adrestus.core.Resourses.CachedZoneIndex;
import io.Adrestus.core.RingBuffer.event.AbstractBlockEvent;
import io.Adrestus.core.StatusType;
import io.Adrestus.core.TransactionBlock;
import io.distributedLedger.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class HeaderEventHandler implements BlockEventHandler<AbstractBlockEvent>, DisruptorBlockVisitor {
    private static Logger LOG = LoggerFactory.getLogger(HeaderEventHandler.class);
    private final IDatabase<String, TransactionBlock> transactionBlockIDatabase;
    private final IDatabase<String, CommitteeBlock> committeeBlockIDatabase;

    public HeaderEventHandler() {
        this.committeeBlockIDatabase = new DatabaseFactory(String.class, CommitteeBlock.class).getDatabase(DatabaseType.ROCKS_DB, DatabaseInstance.COMMITTEE_BLOCK);
        this.transactionBlockIDatabase = new DatabaseFactory(String.class, TransactionBlock.class).getDatabase(DatabaseType.ROCKS_DB, ZoneDatabaseFactory.getZoneInstance(CachedZoneIndex.getInstance().getZoneIndex()));
    }

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
        if (!committeeBlock.getHeaderData().getPreviousHash().equals(CachedLatestBlocks.getInstance().getCommitteeBlock().getHash())) {
            LOG.info("CommitteeBlock previous hashes does not match");
            committeeBlock.setStatustype(StatusType.ABORT);
        }
        int finish = this.committeeBlockIDatabase.findDBsize();
        if (finish == 0)
            return;

        Optional<CommitteeBlock> committeeBlockDBentry = this.committeeBlockIDatabase.seekLast();

        if (committeeBlockDBentry.isEmpty()) {
            LOG.info("committeeBlock hashes is empty");
            committeeBlock.setStatustype(StatusType.ABORT);
            return;
        }

        if (!committeeBlock.getHeaderData().getPreviousHash().equals(committeeBlockDBentry.get().getHash())) {
            LOG.info("Database committeeBlock previous hashes does not match");
            committeeBlock.setStatustype(StatusType.ABORT);
        }
    }

    @Override
    public void visit(TransactionBlock transactionBlock) {
        if (!transactionBlock.getHeaderData().getPreviousHash().equals(CachedLatestBlocks.getInstance().getTransactionBlock().getHash())) {
            LOG.info("TransactionBlock previous hashes does not match");
            transactionBlock.setStatustype(StatusType.ABORT);
        }
        int finish = this.transactionBlockIDatabase.findDBsize();
        if (finish == 0)
            return;

        Optional<TransactionBlock> transactionBlockDBEntry = this.transactionBlockIDatabase.seekLast();

        if (transactionBlockDBEntry.isEmpty()) {
            LOG.info("TransactionBlock hashes is empty");
            transactionBlock.setStatustype(StatusType.ABORT);
            return;
        }

        if (!transactionBlock.getHeaderData().getPreviousHash().equals(transactionBlockDBEntry.get().getHash())) {
            LOG.info("Database TransactionBlock previous hashes does not match");
            transactionBlock.setStatustype(StatusType.ABORT);
        }

    }
}
