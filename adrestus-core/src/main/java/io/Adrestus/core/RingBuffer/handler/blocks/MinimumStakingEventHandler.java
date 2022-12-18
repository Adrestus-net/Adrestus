package io.Adrestus.core.RingBuffer.handler.blocks;

import io.Adrestus.TreeFactory;
import io.Adrestus.config.StakingConfiguration;
import io.Adrestus.core.CommitteeBlock;
import io.Adrestus.core.RingBuffer.event.AbstractBlockEvent;
import io.Adrestus.core.StatusType;
import io.Adrestus.p2p.kademlia.repository.KademliaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

public class MinimumStakingEventHandler implements BlockEventHandler<AbstractBlockEvent> {

    private static Logger LOG = LoggerFactory.getLogger(MinimumStakingEventHandler.class);

    @Override
    public void onEvent(AbstractBlockEvent blockEvent, long l, boolean b) throws Exception {
        CommitteeBlock block = (CommitteeBlock) blockEvent.getBlock();

        List<KademliaData> validatorAddressDatalist = block
                .getStakingMap()
                .values()
                .stream()
                .filter(this::hasOverMinimumPoints)
                .collect(Collectors.toList());

        if (validatorAddressDatalist.size() != block.getStakingMap().size()) {
            LOG.info("Some validators do not meet minimum staking requirements");
            block.setStatustype(StatusType.ABORT);
            return;
        }

    }

    private boolean hasOverMinimumPoints(KademliaData securityAuditProofs) {
        try {
            if (TreeFactory.getMemoryTree(0)
                    .getByaddress(securityAuditProofs.getAddressData().getAddress())
                    .get()
                    .getAmount() >= StakingConfiguration.MINIMUM_STAKING)
                return true;
            else
                return false;
        } catch (Exception e) {
            LOG.info("Address not found on PatriciaTree");
            return true;
        }
    }

}
