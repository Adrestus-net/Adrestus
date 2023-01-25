package io.Adrestus.consensus;

import io.Adrestus.consensus.ChangeView.ChangeViewConsensusPhase;
import io.Adrestus.consensus.ChangeView.ChangeViewValidatorsConsensusPhase;

import java.util.EnumMap;
import java.util.Map;

public class Validator implements ConsensusRole {
    private final Map<ConsensusType, ValidatorConsensusPhases> validator_map;
    private final Map<ConsensusType, ChangeViewValidatorsConsensusPhase> change_view_map;
    private final boolean DEBUG;

    public Validator(boolean DEBUG) {
        this.DEBUG = DEBUG;
        this.validator_map = new EnumMap<>(ConsensusType.class);
        this.change_view_map = new EnumMap<>(ConsensusType.class);
        this.validator_map.put(ConsensusType.VDF, new ValidatorConsensusPhases.VerifyVDF(this.DEBUG));
        this.validator_map.put(ConsensusType.VRF, new ValidatorConsensusPhases.VerifyVRF(this.DEBUG));
        this.validator_map.put(ConsensusType.TRANSACTION_BLOCK, new ValidatorConsensusPhases.VerifyTransactionBlock(this.DEBUG));
        this.validator_map.put(ConsensusType.COMMITTEE_BLOCK, new ValidatorConsensusPhases.VerifyCommitteeBlock(this.DEBUG));

        this.change_view_map.put(ConsensusType.CHANGE_VIEW_TRANSACTION_BLOCK, new ChangeViewValidatorsConsensusPhase.ChangeViewTransactionBlock(this.DEBUG));
        this.change_view_map.put(ConsensusType.CHANGE_VIEW_COMMITTEE_BLOCK, new ChangeViewValidatorsConsensusPhase.ChangeCommiteeBlockView(this.DEBUG));
    }

    @Override
    public BFTConsensusPhase manufacturePhases(ConsensusType consensusType) {
        return (BFTConsensusPhase) validator_map.get(consensusType);
    }

    @Override
    public ChangeViewConsensusPhase manufacterChangeViewPhases(ConsensusType consensusType) {
        return (ChangeViewConsensusPhase) change_view_map.get(consensusType);
    }
}
