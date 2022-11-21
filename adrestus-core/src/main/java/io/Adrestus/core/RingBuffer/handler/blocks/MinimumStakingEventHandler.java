package io.Adrestus.core.RingBuffer.handler.blocks;

import io.Adrestus.config.StakingConfiguration;
import io.Adrestus.core.CommitteeBlock;
import io.Adrestus.core.Resourses.MemoryTreePool;
import io.Adrestus.core.RingBuffer.event.AbstractBlockEvent;
import io.Adrestus.core.StatusType;
import io.Adrestus.crypto.SecurityAuditProofs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

public class MinimumStakingEventHandler implements BlockEventHandler<AbstractBlockEvent> {

    private static Logger LOG = LoggerFactory.getLogger(MinimumStakingEventHandler.class);

    @Override
    public void onEvent(AbstractBlockEvent blockEvent, long l, boolean b) throws Exception {
        CommitteeBlock block = (CommitteeBlock) blockEvent.getBlock();

        List<SecurityAuditProofs> validatorAddressDatalist = block
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

    private boolean hasOverMinimumPoints(SecurityAuditProofs securityAuditProofs) {
        try {
            if (MemoryTreePool.getInstance()
                    .getByaddress(securityAuditProofs.getAddress())
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
