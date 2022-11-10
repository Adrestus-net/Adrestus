package io.Adrestus.core.RingBuffer.handler.blocks;

import io.Adrestus.core.AbstractBlock;
import io.Adrestus.core.RingBuffer.event.AbstractBlockEvent;
import io.Adrestus.core.StatusType;
import io.Adrestus.crypto.HashUtil;
import io.Adrestus.util.SerializationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HashEventHandler implements BlockEventHandler<AbstractBlockEvent> {

    private static Logger LOG = LoggerFactory.getLogger(HashEventHandler.class);
    private final SerializationUtil<AbstractBlock> wrapper;

    public HashEventHandler() {
        wrapper = new SerializationUtil<AbstractBlock>(AbstractBlock.class);
    }

    @Override
    public void onEvent(AbstractBlockEvent blockEvent, long l, boolean b) throws Exception {
        try {
            AbstractBlock block = blockEvent.getBlock();
            if (block.getHash().length() != 64) {
                LOG.info("Block hashes length is not valid");
                block.setStatustype(StatusType.ABORT);
            }
            AbstractBlock cloneable = (AbstractBlock) block.clone();
            cloneable.setHash("");
            byte[] buffer = wrapper.encode(cloneable);
            String result_hash = HashUtil.sha256_bytetoString(buffer);
            if (!result_hash.equals(block.getHash())) {
                LOG.info("Block hash is manipulated");
                block.setStatustype(StatusType.ABORT);
            }
        } catch (NullPointerException ex) {
            LOG.info("Block is empty");
        }
    }

}
