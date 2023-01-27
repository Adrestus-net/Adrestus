package io.Adrestus.consensus;

import io.Adrestus.core.BlockIndex;
import io.Adrestus.core.CommitteeBlock;
import io.Adrestus.core.IBlockIndex;
import io.Adrestus.core.Resourses.CachedLatestBlocks;
import io.Adrestus.core.Resourses.CachedSecurityHeaders;
import io.Adrestus.core.Resourses.CachedZoneIndex;
import io.Adrestus.crypto.bls.model.BLSPublicKey;
import io.Adrestus.crypto.bls.model.CachedBLSKeyPair;
import io.Adrestus.crypto.vdf.engine.VdfEngine;
import io.Adrestus.crypto.vdf.engine.VdfEnginePietrzak;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsensusCommitteeBlockState extends AbstractState {
    private static Logger LOG = LoggerFactory.getLogger(ConsensusCommitteeBlockState.class);

    private IBlockIndex blockIndex;
    private int target;
    private int current;
    private ConsensusManager consensusManager;
    private VdfEngine vdf;

    public ConsensusCommitteeBlockState() {
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
        ConsensusMessage<CommitteeBlock> consensusMessage = new ConsensusMessage<>(new CommitteeBlock());

        try {
            if (target == current) {
                LOG.info("Committee Block Supervisor State");
                consensusManager.changeStateTo(ConsensusRoleType.SUPERVISOR);
                var supervisorphase = consensusManager.getRole().manufacturePhases(ConsensusType.COMMITTEE_BLOCK);
                supervisorphase.InitialSetup();
                supervisorphase.AnnouncePhase(consensusMessage);
                supervisorphase.PreparePhase(consensusMessage);
                supervisorphase.CommitPhase(consensusMessage);
            } else {
                LOG.info("Committee Block Validator State");
                consensusManager.changeStateTo(ConsensusRoleType.VALIDATOR);
                var validatorphase = consensusManager.getRole().manufacturePhases(ConsensusType.COMMITTEE_BLOCK);
                validatorphase.InitialSetup();
                validatorphase.AnnouncePhase(consensusMessage);
                validatorphase.PreparePhase(consensusMessage);
                validatorphase.CommitPhase(consensusMessage);
            }
            if (consensusMessage.getStatusType().equals(ConsensusStatusType.ABORT))
                return false;
            CachedSecurityHeaders.getInstance().getSecurityHeader().setRnd(vdf.solve(CachedSecurityHeaders.getInstance().getSecurityHeader().getpRnd(), CachedLatestBlocks.getInstance().getCommitteeBlock().getDifficulty()));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            LOG.info("Exception caught " + e.getMessage());
            return false;
        }
    }
}
