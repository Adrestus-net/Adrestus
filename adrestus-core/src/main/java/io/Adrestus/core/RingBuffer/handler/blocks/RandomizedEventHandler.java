package io.Adrestus.core.RingBuffer.handler.blocks;

import io.Adrestus.config.AdrestusConfiguration;
import io.Adrestus.core.CommitteeBlock;
import io.Adrestus.core.RingBuffer.event.AbstractBlockEvent;
import io.Adrestus.core.StatusType;
import io.Adrestus.crypto.SecurityAuditProofs;
import io.Adrestus.crypto.bls.model.BLSPublicKey;
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
    private final SecureRandom secureRandom;

    @SneakyThrows
    public RandomizedEventHandler() {
        this.secureRandom = SecureRandom.getInstance(AdrestusConfiguration.ALGORITHM, AdrestusConfiguration.PROVIDER);
    }

    @Override
    public void onEvent(AbstractBlockEvent blockEvent, long l, boolean b) throws Exception {
        CommitteeBlock block = (CommitteeBlock) blockEvent.getBlock();
        CommitteeBlock committeeBlock = (CommitteeBlock) block.clone();
        committeeBlock.createStructureMap();
        secureRandom.setSeed(Hex.decode(block.getVDF()));


        ArrayList<Integer> exclude = new ArrayList<Integer>();
        ArrayList<Integer> order = new ArrayList<Integer>();
        for (Map.Entry<Double, SecurityAuditProofs> entry : committeeBlock.getStakingMap().entrySet()) {
            int nextInt = generateRandom(0, committeeBlock.getStakingMap().size() - 1, exclude);
            if (!exclude.contains(nextInt)) {
                exclude.add(nextInt);
            }
            order.add(nextInt);
        }
        int zone_count = 0;
        List<Map.Entry<Double, SecurityAuditProofs>> entryList = committeeBlock.getStakingMap().entrySet().stream().collect(Collectors.toList());
        int MAX_ZONE_SIZE = committeeBlock.getStakingMap().size() / 4;

        int j = 0;
        while (zone_count < 4) {
            int index_count = 0;
            if (committeeBlock.getStakingMap().size() % 4 != 0 && zone_count == 0) {
                while (index_count < committeeBlock.getStakingMap().size() - 3) {
                    committeeBlock
                            .getStructureMap()
                            .get(zone_count)
                            .put(entryList.get(order.get(j)).getValue().getValidatorBlSPublicKey(), entryList.get(order.get(j)).getValue().getIp());
                    index_count++;
                    j++;
                }
                zone_count++;
            }
            index_count = 0;
            while (index_count < MAX_ZONE_SIZE) {
                committeeBlock
                        .getStructureMap()
                        .get(zone_count)
                        .put(entryList.get(order.get(j)).getValue().getValidatorBlSPublicKey(), entryList.get(order.get(j)).getValue().getIp());
                index_count++;
                j++;
            }
            zone_count++;
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

    public int generateRandom(int start, int end, ArrayList<Integer> excludeRows) {
        int range = end - start + 1;
        int random = secureRandom.nextInt(range);
        while (excludeRows.contains(random)) {
            random = secureRandom.nextInt(range);
        }

        return random;
    }
}
