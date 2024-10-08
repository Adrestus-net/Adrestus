package io.Adrestus.core.RingBuffer.handler.blocks;

import io.Adrestus.core.CommitteeBlock;
import io.Adrestus.core.RingBuffer.event.AbstractBlockEvent;
import io.Adrestus.core.StatusType;
import io.Adrestus.crypto.elliptic.mapper.StakingData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.*;

public class SortedStakingEventHandler implements BlockEventHandler<AbstractBlockEvent> {

    private static Logger LOG = LoggerFactory.getLogger(SortedStakingEventHandler.class);

    @Override
    public void onEvent(AbstractBlockEvent blockEvent, long l, boolean b) throws Exception {
        try {
            CommitteeBlock block = (CommitteeBlock) blockEvent.getBlock();

            Set<StakingData> values = block.getStakingMap().keySet();
            SortedSet<StakingData> keys = new TreeSet<StakingData>(new StakingValueComparator());
            keys.addAll(values);
            if (!Objects.equals(values.size(), keys.size())) {
                LOG.info("Staking Map does not have valid size");
                block.setStatustype(StatusType.ABORT);
                return;
            }
            List<StakingData> old = new ArrayList<>(values);
            List<StakingData> neww = new ArrayList<>(keys);
            for (int i = 0; i < old.size(); i++) {
                if (old.get(i).getStake().compareTo(neww.get(i).getStake()) != 0) {
                    LOG.info("Staking Map elements is not sorted or equal");
                    block.setStatustype(StatusType.ABORT);
                    return;
                }
            }
        } catch (NullPointerException ex) {
            LOG.info("Block is empty");
        }
    }

    private static final class StakingValueComparator implements Comparator<StakingData>, Serializable {
        @Override
        public int compare(StakingData a, StakingData b) {
            if (a.getStake().compareTo(b.getStake())>=0) {
                return -1;
            } else {
                return 1;
            } // returning 0 would merge keys
        }
    }
}
