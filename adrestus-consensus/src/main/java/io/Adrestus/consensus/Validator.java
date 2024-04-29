package io.Adrestus.consensus;

import io.Adrestus.consensus.ChangeView.ChangeViewConsensusPhase;
import io.Adrestus.consensus.ChangeView.ChangeViewValidatorsConsensusPhase;

import java.util.EnumMap;
import java.util.Map;

public class Validator implements ConsensusRole {

    private static volatile Validator instance;
    private final Map<ConsensusType, ValidatorConsensusPhases> validator_map;
    private final Map<ConsensusType, ChangeViewValidatorsConsensusPhase> change_view_map;
    private final boolean DEBUG;

    private Validator(boolean DEBUG) {
        if (instance != null) {
            throw new IllegalStateException("Already initialized.");
        }
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

    public static Validator getInstance(boolean DEBUG) {
        var result = instance;
        if (result == null) {
            synchronized (Validator.class) {
                result = instance;
                if (result == null) {
                    result = new Validator(DEBUG);
                    instance = result;
                }
            }
        }
        return result;
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
