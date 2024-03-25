package io.Adrestus.consensus;

import io.Adrestus.core.BlockIndex;
import io.Adrestus.core.IBlockIndex;
import io.Adrestus.core.Resourses.CachedLeaderIndex;
import io.Adrestus.core.Resourses.CachedZoneIndex;
import io.Adrestus.core.TransactionBlock;
import io.Adrestus.crypto.bls.model.BLSPublicKey;
import io.Adrestus.crypto.bls.model.CachedBLSKeyPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsensusTransactionBlockState extends AbstractState {
    private static Logger LOG = LoggerFactory.getLogger(ConsensusTransactionBlockState.class);

    private IBlockIndex blockIndex;
    private int target;
    private int current;
    private ConsensusManager consensusManager;

    public ConsensusTransactionBlockState() {
        this.blockIndex = new BlockIndex();
        this.consensusManager = new ConsensusManager(false);
    }

    @Override
    public void onEnterState(BLSPublicKey blsPublicKey) {
        target = blockIndex.getPublicKeyIndex(CachedZoneIndex.getInstance().getZoneIndex(), CachedBLSKeyPair.getInstance().getPublicKey());
        if (blsPublicKey == null)
            current = CachedLeaderIndex.getInstance().getTransactionPositionLeader();
        else
            current = blockIndex.getPublicKeyIndex(CachedZoneIndex.getInstance().getZoneIndex(), blsPublicKey);
    }

    @Override
    public boolean onActiveState() {
        ConsensusMessage<TransactionBlock> consensusMessage = new ConsensusMessage<>(new TransactionBlock());

        try {
            if (target == current) {
                LOG.info("Transaction Block Organizer State");
                consensusManager.changeStateTo(ConsensusRoleType.ORGANIZER);
                var organizerphase = consensusManager.getRole().manufacturePhases(ConsensusType.TRANSACTION_BLOCK);
                organizerphase.InitialSetup();
                organizerphase.DispersePhase(consensusMessage);
                organizerphase.AnnouncePhase(consensusMessage);
                organizerphase.PreparePhase(consensusMessage);
                organizerphase.CommitPhase(consensusMessage);
            } else {
                LOG.info("Transaction Block Validator State");
                consensusManager.changeStateTo(ConsensusRoleType.VALIDATOR);
                var validatorphase = consensusManager.getRole().manufacturePhases(ConsensusType.TRANSACTION_BLOCK);
                validatorphase.InitialSetup();
                validatorphase.DispersePhase(consensusMessage);
                validatorphase.AnnouncePhase(consensusMessage);
                validatorphase.PreparePhase(consensusMessage);
                validatorphase.CommitPhase(consensusMessage);
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOG.info("Exception caught " + e.getMessage());
            return false;
        }

        if (consensusMessage.getStatusType().equals(ConsensusStatusType.ABORT))
            return false;
        return true;
    }

}
