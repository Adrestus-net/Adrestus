package io.Adrestus.core.RingBuffer.handler.blocks;

import io.Adrestus.core.CommitteeBlock;
import io.Adrestus.core.RingBuffer.event.AbstractBlockEvent;
import io.Adrestus.core.StatusType;
import io.Adrestus.crypto.vdf.engine.VdfEngine;
import io.Adrestus.crypto.vdf.engine.VdfEnginePietrzak;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

public class VDFVerifyEventHandler implements BlockEventHandler<AbstractBlockEvent> {

    private static Logger LOG = LoggerFactory.getLogger(VDFVerifyEventHandler.class);
    private final VdfEngine vdf;

    public VDFVerifyEventHandler() {
        this.vdf = new VdfEnginePietrzak(2048);
    }

    @Override
    public void onEvent(AbstractBlockEvent blockEvent, long l, boolean b) throws Exception {
        CommitteeBlock block = (CommitteeBlock) blockEvent.getBlock();
        boolean verify = this.vdf.verify(Hex.decode(block.getVRF()), block.getDifficulty(), Hex.decode(block.getVDF()));

        if (!verify) {
            LOG.info("VDF Verification failed");
            block.setStatustype(StatusType.ABORT);
            return;
        }

    }
}
