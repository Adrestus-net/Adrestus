package io.Adrestus.consensus;

import io.Adrestus.core.BlockIndex;
import io.Adrestus.core.IBlockIndex;
import io.Adrestus.core.Resourses.CachedZoneIndex;
import io.Adrestus.crypto.bls.model.BLSPublicKey;
import io.Adrestus.crypto.bls.model.CachedBLSKeyPair;
import io.Adrestus.crypto.vdf.VDFMessage;
import io.Adrestus.crypto.vdf.engine.VdfEngine;
import io.Adrestus.crypto.vdf.engine.VdfEnginePietrzak;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsensusVDFState extends AbstractState {
    private static Logger LOG = LoggerFactory.getLogger(ConsensusVDFState.class);

    private IBlockIndex blockIndex;
    private int target;
    private int current;
    private ConsensusManager consensusManager;
    private VdfEngine vdf;

    public ConsensusVDFState() {
        this.blockIndex = new BlockIndex();
        this.consensusManager = new ConsensusManager(false);
        this.vdf = new VdfEnginePietrzak(2048);
    }

    @Override
    public void onEnterState(BLSPublicKey blsPublicKey) {
        target = blockIndex.getPublicKeyIndex(CachedZoneIndex.getInstance().getZoneIndex(), CachedBLSKeyPair.getInstance().getPublicKey());
        current = blockIndex.getPublicKeyIndex(CachedZoneIndex.getInstance().getZoneIndex(), blsPublicKey);
    }

    @SneakyThrows
    @Override
    public boolean onActiveState() {
        ConsensusMessage<VDFMessage> consensusMessage = new ConsensusMessage<>(new VDFMessage());

        try {
            if (target == current) {
                LOG.info("VDF Committee Block Supervisor State");
                consensusManager.changeStateTo(ConsensusRoleType.SUPERVISOR);
                var organizerphase = consensusManager.getRole().manufacturePhases(ConsensusType.VDF);
                organizerphase.InitialSetup();
                organizerphase.AnnouncePhase(consensusMessage);
                organizerphase.PreparePhase(consensusMessage);
                organizerphase.CommitPhase(consensusMessage);
            } else {
                LOG.info("VDF Committee Block Validator State");
                consensusManager.changeStateTo(ConsensusRoleType.VALIDATOR);
                var validatorphase = consensusManager.getRole().manufacturePhases(ConsensusType.VDF);
                validatorphase.InitialSetup();
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
