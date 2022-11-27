package io.Adrestus.consensus;

import java.util.EnumMap;
import java.util.Map;

public class Validator implements ConsensusRole {
    private final Map<ConsensusType, ValidatorConsensusPhases> validator_map;
    private final boolean DEBUG;

    public Validator(boolean DEBUG) {
        this.DEBUG = DEBUG;
        validator_map = new EnumMap<>(ConsensusType.class);
        validator_map.put(ConsensusType.VDF, new ValidatorConsensusPhases.VerifyVDF(this.DEBUG));
        validator_map.put(ConsensusType.VRF, new ValidatorConsensusPhases.VerifyVRF(this.DEBUG));
        validator_map.put(ConsensusType.TRANSACTION_BLOCK, new ValidatorConsensusPhases.VerifyTransactionBlock(this.DEBUG));
        validator_map.put(ConsensusType.COMMITTEE_BLOCK, new ValidatorConsensusPhases.VerifyCommitteeBlock(this.DEBUG));
    }

    @Override
    public BFTConsensusPhase manufacturePhases(ConsensusType consensusType) {
        return (BFTConsensusPhase) validator_map.get(consensusType);
    }
}
