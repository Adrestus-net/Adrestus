package io.Adrestus.core.RingBuffer.handler.blocks;

import io.Adrestus.core.CommitteeBlock;
import io.Adrestus.core.RingBuffer.event.AbstractBlockEvent;
import io.Adrestus.core.StatusType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class SortedStakingEventHandler implements BlockEventHandler<AbstractBlockEvent> {

    private static Logger LOG = LoggerFactory.getLogger(SortedStakingEventHandler.class);

    @Override
    public void onEvent(AbstractBlockEvent blockEvent, long l, boolean b) throws Exception {
        try {
            CommitteeBlock block = (CommitteeBlock) blockEvent.getBlock();

            Set<Double> values = block.getStakingMap().keySet();
            SortedSet<Double> keys = new TreeSet<Double>(Collections.reverseOrder());
            keys.addAll(values);
            if (!Objects.equals(values.size(), keys.size())) {
                LOG.info("Staking Map does not have valid size");
                block.setStatustype(StatusType.ABORT);
                return;
            }
            List<Double> old = new ArrayList<>(values);
            List<Double> neww = new ArrayList<>(keys);
            for (int i = 0; i < old.size(); i++) {
                if (old.get(i).compareTo(neww.get(i)) != 0) {
                    LOG.info("Staking Map elements is not sorted or equal");
                    block.setStatustype(StatusType.ABORT);
                    return;
                }
            }
        } catch (NullPointerException ex) {
            LOG.info("Block is empty");
        }
    }
}
