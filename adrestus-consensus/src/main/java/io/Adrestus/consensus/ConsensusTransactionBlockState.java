package io.Adrestus.consensus;

import io.Adrestus.core.BlockIndex;
import io.Adrestus.core.IBlockIndex;
import io.Adrestus.core.Resourses.CachedLatestBlocks;
import io.Adrestus.core.Resourses.CachedZoneIndex;
import io.Adrestus.core.StatusType;
import io.Adrestus.core.TransactionBlock;
import io.Adrestus.crypto.bls.model.BLSPublicKey;
import io.Adrestus.crypto.bls.model.CachedBLSKeyPair;
import lombok.SneakyThrows;
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
            current = blockIndex.getPublicKeyIndex(CachedZoneIndex.getInstance().getZoneIndex(), CachedLatestBlocks.getInstance().getTransactionBlock().getLeaderPublicKey());
        else
            current = blockIndex.getPublicKeyIndex(CachedZoneIndex.getInstance().getZoneIndex(), blsPublicKey);
    }

    @SneakyThrows
    @Override
    public boolean onActiveState() {
        ConsensusMessage<TransactionBlock> consensusMessage = new ConsensusMessage<>(new TransactionBlock());

        if (target == current + 1 || (target == 0 && current == CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(1).size() - 1)) {
            LOG.info("Transaction Block Organizer State");
            consensusManager.changeStateTo(ConsensusRoleType.ORGANIZER);
            var organizerphase = consensusManager.getRole().manufacturePhases(ConsensusType.TRANSACTION_BLOCK);
            organizerphase.InitialSetup();
            organizerphase.AnnouncePhase(consensusMessage);
            organizerphase.PreparePhase(consensusMessage);
            organizerphase.CommitPhase(consensusMessage);
        } else {
            LOG.info("Transaction Block Validator State");
            consensusManager.changeStateTo(ConsensusRoleType.VALIDATOR);
            var validatorphase = consensusManager.getRole().manufacturePhases(ConsensusType.TRANSACTION_BLOCK);
            validatorphase.InitialSetup();
            validatorphase.AnnouncePhase(consensusMessage);
            validatorphase.PreparePhase(consensusMessage);
            validatorphase.CommitPhase(consensusMessage);
        }

        if (consensusMessage.getData().getStatustype().equals(StatusType.ABORT))
            return false;
        return true;
    }

}
