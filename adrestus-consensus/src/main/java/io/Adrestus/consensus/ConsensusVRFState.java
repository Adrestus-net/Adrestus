package io.Adrestus.consensus;

import io.Adrestus.core.BlockIndex;
import io.Adrestus.core.IBlockIndex;
import io.Adrestus.core.Resourses.CachedZoneIndex;
import io.Adrestus.crypto.bls.model.BLSPublicKey;
import io.Adrestus.crypto.bls.model.CachedBLSKeyPair;
import io.Adrestus.crypto.vrf.VRFMessage;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsensusVRFState extends AbstractState {
    private static Logger LOG = LoggerFactory.getLogger(ConsensusVDFState.class);

    private IBlockIndex blockIndex;
    private int target;
    private int current;
    private ConsensusManager consensusManager;

    public ConsensusVRFState() {
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
        VRFMessage vrfMessage = new VRFMessage();
        ConsensusMessage<VRFMessage> consensusMessage = new ConsensusMessage<>(vrfMessage);

        try {
            if (target == current) {
                LOG.info("VRF Committee Block Supervisor State");
                consensusManager.changeStateTo(ConsensusRoleType.SUPERVISOR);
                var supervisorphase = (VRFConsensusPhase) consensusManager.getRole().manufacturePhases(ConsensusType.VRF);
                supervisorphase.InitialSetup();
                supervisorphase.Initialize(vrfMessage);
                supervisorphase.AggregateVRF(vrfMessage);
                if (vrfMessage.getType().equals(VRFMessage.vrfMessageType.ABORT))
                    return false;
                supervisorphase.AnnouncePhase(consensusMessage);
                supervisorphase.PreparePhase(consensusMessage);
                supervisorphase.CommitPhase(consensusMessage);
            } else {
                LOG.info("VRF Committee Block Validator State");
                consensusManager.changeStateTo(ConsensusRoleType.VALIDATOR);
                var validatorphase = (VRFConsensusPhase) consensusManager.getRole().manufacturePhases(ConsensusType.VRF);
                validatorphase.InitialSetup();
                validatorphase.Initialize(vrfMessage);
                if (vrfMessage.getType().equals(VRFMessage.vrfMessageType.ABORT))
                    return false;
                validatorphase.AnnouncePhase(consensusMessage);
                validatorphase.PreparePhase(consensusMessage);
                validatorphase.CommitPhase(consensusMessage);
            }
            if (consensusMessage.getStatusType().equals(ConsensusStatusType.ABORT)) return false;

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            LOG.info("Exception caught " + e.getMessage());
            return false;
        }
    }
}
