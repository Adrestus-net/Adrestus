package io.Adrestus.core.RingBuffer.handler.blocks;

import io.Adrestus.config.AdrestusConfiguration;
import io.Adrestus.core.CommitteeBlock;
import io.Adrestus.core.RingBuffer.event.AbstractBlockEvent;
import io.Adrestus.core.StatusType;
import io.Adrestus.crypto.bls.model.BLSPublicKey;
import io.Adrestus.crypto.elliptic.mapper.StakingData;
import io.Adrestus.p2p.kademlia.repository.KademliaData;
import io.Adrestus.util.MathOperationUtil;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RandomizedEventHandler implements BlockEventHandler<AbstractBlockEvent> {

    private static Logger LOG = LoggerFactory.getLogger(RandomizedEventHandler.class);


    @SneakyThrows
    public RandomizedEventHandler() {
    }

    @Override
    public void onEvent(AbstractBlockEvent blockEvent, long l, boolean b) throws Exception {
        SecureRandom secureRandom = SecureRandom.getInstance(AdrestusConfiguration.ALGORITHM, AdrestusConfiguration.PROVIDER);
        CommitteeBlock block = (CommitteeBlock) blockEvent.getBlock();
        CommitteeBlock committeeBlock = (CommitteeBlock) block.clone();
        committeeBlock.createStructureMap();
        secureRandom.setSeed(Hex.decode(block.getVDF()));


        ArrayList<Integer> exclude = new ArrayList<Integer>();
        ArrayList<Integer> order = new ArrayList<Integer>();
        for (Map.Entry<StakingData, KademliaData> entry : committeeBlock.getStakingMap().entrySet()) {
            int nextInt = generateRandom(secureRandom, 0, committeeBlock.getStakingMap().size() - 1, exclude);
            if (!exclude.contains(nextInt)) {
                exclude.add(nextInt);
            }
            order.add(nextInt);
        }
        int zone_count = 0;
        List<Map.Entry<StakingData, KademliaData>> entryList = committeeBlock.getStakingMap().entrySet().stream().collect(Collectors.toList());
        int MAX_ZONE_SIZE = committeeBlock.getStakingMap().size() / 4;

        int j = 0;
        if (MAX_ZONE_SIZE >= 2) {
            int addition = committeeBlock.getStakingMap().size() - MathOperationUtil.closestNumber(committeeBlock.getStakingMap().size(), 4);
            while (zone_count < 4) {
                if (zone_count == 0 && addition != 0) {
                    while (j < MAX_ZONE_SIZE + addition) {
                        committeeBlock
                                .getStructureMap()
                                .get(zone_count)
                                .put(entryList.get(order.get(j)).getValue().getAddressData().getValidatorBlSPublicKey(), entryList.get(order.get(j)).getValue().getNettyConnectionInfo().getHost());
                        j++;
                    }
                } else {
                    int index_count = 0;
                    while (index_count < MAX_ZONE_SIZE) {
                        committeeBlock
                                .getStructureMap()
                                .get(zone_count)
                                .put(entryList.get(order.get(j)).getValue().getAddressData().getValidatorBlSPublicKey(), entryList.get(order.get(j)).getValue().getNettyConnectionInfo().getHost());
                        index_count++;
                        j++;
                    }
                }
                zone_count++;
            }
        } else {
            for (int i = 0; i < order.size(); i++) {
                committeeBlock
                        .getStructureMap()
                        .get(0)
                        .put(entryList.get(order.get(i)).getValue().getAddressData().getValidatorBlSPublicKey(), entryList.get(order.get(i)).getValue().getNettyConnectionInfo().getHost());
            }
        }
        boolean zone0 = areEqual(block.getStructureMap().get(0), committeeBlock.getStructureMap().get(0));
        boolean zone1 = areEqual(block.getStructureMap().get(1), committeeBlock.getStructureMap().get(1));
        boolean zone2 = areEqual(block.getStructureMap().get(2), committeeBlock.getStructureMap().get(2));
        boolean zone3 = areEqual(block.getStructureMap().get(3), committeeBlock.getStructureMap().get(3));

        if (!zone0 || !zone1 || !zone2 || !zone3) {
            LOG.info("Validator not placed correctly");
            block.setStatustype(StatusType.ABORT);
            return;
        }

    }

    private boolean areEqual(Map<BLSPublicKey, String> first, Map<BLSPublicKey, String> second) {
        if (first.size() != second.size()) {
            return false;
        }

        return first.entrySet().stream()
                .allMatch(e -> e.getValue().equals(second.get(e.getKey())));
    }

    public int generateRandom(SecureRandom secureRandom, int start, int end, ArrayList<Integer> excludeRows) {
        int range = end - start + 1;
        int random = secureRandom.nextInt(range);
        while (excludeRows.contains(random)) {
            random = secureRandom.nextInt(range);
        }

        return random;
    }
}
