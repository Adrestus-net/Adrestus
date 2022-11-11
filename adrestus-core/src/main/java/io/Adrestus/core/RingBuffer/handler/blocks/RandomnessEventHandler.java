package io.Adrestus.core.RingBuffer.handler.blocks;

import com.google.common.primitives.Ints;
import io.Adrestus.config.AdrestusConfiguration;
import io.Adrestus.core.CommitteeBlock;
import io.Adrestus.core.RingBuffer.event.AbstractBlockEvent;
import io.Adrestus.core.StatusType;
import io.Adrestus.core.ValidatorAddressData;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public class RandomnessEventHandler implements BlockEventHandler<AbstractBlockEvent> {
    private static Logger LOG = LoggerFactory.getLogger(RandomnessEventHandler.class);
    private final SecureRandom secureRandom;

    @SneakyThrows
    public RandomnessEventHandler() {
        this.secureRandom = SecureRandom.getInstance(AdrestusConfiguration.ALGORITHM, AdrestusConfiguration.PROVIDER);
    }

    @Override
    public void onEvent(AbstractBlockEvent blockEvent, long l, boolean b) throws Exception {
        CommitteeBlock block = (CommitteeBlock) blockEvent.getBlock();
        secureRandom.setSeed(Hex.decode(block.getVDF()));

        for (Map.Entry<Double, ValidatorAddressData> entry : block.getStakingMap().entrySet()) {
            int nextInt=secureRandom.nextInt(AdrestusConfiguration.MAX_ZONES);
            if(!block.getStructureMap().get(nextInt).containsKey(entry.getValue().getValidatorBlSPublicKey())){
                LOG.info("Validator not placed correctly");
                block.setStatustype(StatusType.ABORT);
                return;
            }
        }
        ArrayList<Integer>replica=new ArrayList<>();
        int iteration=0;
        while(iteration<block.getStakingMap().size()) {
            int nextInt = secureRandom.nextInt(block.getStakingMap().size());
            if(!replica.contains(nextInt)) {
                replica.add(nextInt);
                iteration++;
            }
        }
        int[] array = Ints.toArray(replica);
        if(!Arrays.equals(block.getCommitteeProposer(),array)){
            LOG.info("Randomness is incorrect");
            block.setStatustype(StatusType.ABORT);
            return;
        }
    }
}
