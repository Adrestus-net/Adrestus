package io.Adrestus.core.RingBuffer.handler.blocks;

import io.Adrestus.core.AbstractBlock;
import io.Adrestus.core.CommitteeBlock;
import io.Adrestus.core.Resourses.CachedLatestBlocks;
import io.Adrestus.core.RingBuffer.event.AbstractBlockEvent;
import io.Adrestus.core.StatusType;
import io.Adrestus.core.TransactionBlock;
import io.Adrestus.crypto.HashUtil;
import io.Adrestus.util.SerializationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HashEventHandler implements BlockEventHandler<AbstractBlockEvent>,DisruptorBlockVisitor {

    private static Logger LOG = LoggerFactory.getLogger(HashEventHandler.class);
    private final SerializationUtil<AbstractBlock> wrapper;

    public HashEventHandler() {
        wrapper = new SerializationUtil<AbstractBlock>(AbstractBlock.class);
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
        try {
            if (committeeBlock.getHash().length() != 64) {
                LOG.info("Block hashes length is not valid");
                committeeBlock.setStatustype(StatusType.ABORT);
            }
            CommitteeBlock cloneable = (CommitteeBlock) committeeBlock.clone();
            cloneable.setHash("");
            byte[] buffer = wrapper.encode(cloneable);
            String result_hash = HashUtil.sha256_bytetoString(buffer);
            if (!result_hash.equals(committeeBlock.getHash())) {
                LOG.info("Block hash is manipulated");
                committeeBlock.setStatustype(StatusType.ABORT);
            }
        } catch (NullPointerException ex) {
            LOG.info("Block is empty");
            committeeBlock.setStatustype(StatusType.ABORT);
        } catch (CloneNotSupportedException e) {
            LOG.info("Block clone error ");
            committeeBlock.setStatustype(StatusType.ABORT);
        }
    }

    @Override
    public void visit(TransactionBlock transactionBlock) {
        try {
            if (transactionBlock.getHash().length() != 64) {
                LOG.info("Block hashes length is not valid");
                transactionBlock.setStatustype(StatusType.ABORT);
            }
            TransactionBlock cloneable = (TransactionBlock) transactionBlock.clone();
            cloneable.setHash("");
            byte[] buffer = wrapper.encode(cloneable);
            String result_hash = HashUtil.sha256_bytetoString(buffer);
            if (!result_hash.equals(transactionBlock.getHash())) {
                LOG.info("Block hash is manipulated");
                transactionBlock.setStatustype(StatusType.ABORT);
            }
        } catch (NullPointerException ex) {
            LOG.info("Block is empty");
            transactionBlock.setStatustype(StatusType.ABORT);
        } catch (CloneNotSupportedException e) {
            LOG.info("Block clone error ");
            transactionBlock.setStatustype(StatusType.ABORT);
        }
    }

}
