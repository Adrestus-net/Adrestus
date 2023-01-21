package io.Adrestus.consensus.ChangeView;

import io.Adrestus.consensus.*;
import io.Adrestus.core.BlockIndex;
import io.Adrestus.core.IBlockIndex;
import io.Adrestus.core.Resourses.CachedLatestBlocks;
import io.Adrestus.core.Resourses.CachedLeaderIndex;
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
        if (current == CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(CachedZoneIndex.getInstance().getZoneIndex()).size() - 1)
            current=0;
        else {
            current= CachedLeaderIndex.getInstance().getCommitteePositionLeader()+1;
        }
    }

    @SneakyThrows
    @Override
    public boolean onActiveState() {
        ConsensusMessage<ChangeViewData> consensusMessage = new ConsensusMessage<ChangeViewData>(new ChangeViewData());

        if (target == current + 1 || (target == 0 && current == CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(1).size() - 1)) {
            LOG.info("Change View Transaction Block Organizer State");
            consensusManager.changeStateTo(ConsensusRoleType.ORGANIZER);
            var organizerphase = consensusManager.getRole().manufacterChangeViewPhases(ConsensusType.CHANGE_VIEW_COMMITTEE_BLOCK);
            organizerphase.InitialSetup();
            organizerphase.AnnouncePhase(consensusMessage);
            organizerphase.PreparePhase(consensusMessage);
        } else {
            LOG.info("Transaction Block Validator State");
            consensusManager.changeStateTo(ConsensusRoleType.VALIDATOR);
            var validatorphase = consensusManager.getRole().manufacterChangeViewPhases(ConsensusType.CHANGE_VIEW_COMMITTEE_BLOCK);
            validatorphase.InitialSetup();
            validatorphase.AnnouncePhase(consensusMessage);
            validatorphase.PreparePhase(consensusMessage);
        }

        if (consensusMessage.getStatusType().equals(ConsensusStatusType.ABORT))
            return false;
        return true;
    }
}
