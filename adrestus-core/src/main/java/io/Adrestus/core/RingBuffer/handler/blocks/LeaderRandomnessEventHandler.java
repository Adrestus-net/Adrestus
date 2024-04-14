package io.Adrestus.core.RingBuffer.handler.blocks;

import com.google.common.primitives.Ints;
import io.Adrestus.config.AdrestusConfiguration;
import io.Adrestus.core.CommitteeBlock;
import io.Adrestus.core.RingBuffer.event.AbstractBlockEvent;
import io.Adrestus.core.StatusType;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;

public class LeaderRandomnessEventHandler implements BlockEventHandler<AbstractBlockEvent> {
    private static Logger LOG = LoggerFactory.getLogger(LeaderRandomnessEventHandler.class);

    @SneakyThrows
    public LeaderRandomnessEventHandler() {
    }

    @Override
    public void onEvent(AbstractBlockEvent blockEvent, long l, boolean b) throws Exception {
        CommitteeBlock block = (CommitteeBlock) blockEvent.getBlock();
        SecureRandom secureRandom = SecureRandom.getInstance(AdrestusConfiguration.ALGORITHM, AdrestusConfiguration.PROVIDER);
        secureRandom.setSeed(Hex.decode(block.getVDF()));

        ArrayList<Integer> replica = new ArrayList<>();
        int iteration = 0;
        while (iteration < block.getStakingMap().size()) {
            int nextInt = secureRandom.nextInt(block.getStakingMap().size());
            if (!replica.contains(nextInt)) {
                replica.add(nextInt);
                iteration++;
            }
        }
        int[] array = Ints.toArray(replica);
        if (!Arrays.equals(block.getCommitteeProposer(), array)) {
            LOG.info("Randomness is incorrect");
            block.setStatustype(StatusType.ABORT);
            return;
        }

    }
}
