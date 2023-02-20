package io.Adrestus.consensus.ChangeView;

import io.Adrestus.consensus.*;
import io.Adrestus.core.BlockIndex;
import io.Adrestus.core.IBlockIndex;
import io.Adrestus.core.Resourses.CachedZoneIndex;
import io.Adrestus.crypto.bls.model.BLSPublicKey;
import io.Adrestus.crypto.bls.model.CachedBLSKeyPair;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChangeViewCommitteeState extends AbstractState {
    private static Logger LOG = LoggerFactory.getLogger(ChangeViewCommitteeState.class);

    private IBlockIndex blockIndex;
    private int target;
    private int current;
    private ConsensusManager consensusManager;

    public ChangeViewCommitteeState() {
        this.blockIndex = new BlockIndex();
        this.consensusManager = new ConsensusManager(false);
    }

    @Override
    public void onEnterState(BLSPublicKey blsPublicKey) {
        target = blockIndex.getPublicKeyIndex(CachedZoneIndex.getInstance().getZoneIndex(), CachedBLSKeyPair.getInstance().getPublicKey());
        current = blockIndex.getPublicKeyIndex(CachedZoneIndex.getInstance().getZoneIndex(), blsPublicKey);
    }

    @SneakyThrows
    @Override
    public boolean onActiveState() {
        ConsensusMessage<ChangeViewData> consensusMessage = new ConsensusMessage<ChangeViewData>(new ChangeViewData());
        try {
            if (target == current) {
                LOG.info("Change View Committee Block Supervisor State");
                consensusManager.changeStateTo(ConsensusRoleType.SUPERVISOR);
                var organizerphase = consensusManager.getRole().manufacterChangeViewPhases(ConsensusType.CHANGE_VIEW_COMMITTEE_BLOCK);
                organizerphase.InitialSetup();
                organizerphase.AnnouncePhase(consensusMessage);
                organizerphase.PreparePhase(consensusMessage);
            } else {
                LOG.info("Change View Committee Block Validator State");
                consensusManager.changeStateTo(ConsensusRoleType.VALIDATOR);
                var validatorphase = consensusManager.getRole().manufacterChangeViewPhases(ConsensusType.CHANGE_VIEW_COMMITTEE_BLOCK);
                validatorphase.InitialSetup();
                validatorphase.AnnouncePhase(consensusMessage);
                validatorphase.PreparePhase(consensusMessage);
            }
        } catch (Exception e) {
            LOG.info("Exception caught " + e.getMessage());
            return false;
        }

        if (consensusMessage.getStatusType().equals(ConsensusStatusType.ABORT))
            return false;
        return true;
    }
}
