package io.Adrestus.core.RingBuffer.handler.blocks;

import io.Adrestus.core.CommitteeBlock;
import io.Adrestus.core.Resourses.CachedSecurityHeaders;
import io.Adrestus.core.RingBuffer.event.AbstractBlockEvent;
import io.Adrestus.core.StatusType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

public class VRFEventHandler implements BlockEventHandler<AbstractBlockEvent> {
    private static Logger LOG = LoggerFactory.getLogger(VRFEventHandler.class);

    @Override
    public void onEvent(AbstractBlockEvent blockEvent, long l, boolean b) throws Exception {
        CommitteeBlock block = (CommitteeBlock) blockEvent.getBlock();
        String cachedVRF = "EMPTY";
        try {
            cachedVRF = Hex.toHexString(CachedSecurityHeaders.getInstance().getSecurityHeader().getPRnd());
        } catch (NullPointerException e) {
            LOG.info("VRF Null Pointer exception caught " + e.toString());
            block.setStatustype(StatusType.ABORT);
            return;
        }
        if (!block.getVRF().equals(cachedVRF)) {
            LOG.info("VRF Security Header is not valid");
            block.setStatustype(StatusType.ABORT);
            return;
        }
    }
}
