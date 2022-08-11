package io.Adrestus.consensus;

import java.util.EnumMap;
import java.util.Map;

public class Validator implements ConsensusRole {
    private final Map<ConsensusType, ValidatorConsensusPhases> validator_map;


    public Validator() {
        validator_map = new EnumMap<>(ConsensusType.class);
        validator_map.put(ConsensusType.VDF, new ValidatorConsensusPhases.VerifyVDF());
        validator_map.put(ConsensusType.VRF, new ValidatorConsensusPhases.VerifyVRF());
        validator_map.put(ConsensusType.TRANSACTION_BLOCK, new ValidatorConsensusPhases.VerifyTransactionBlock());
        validator_map.put(ConsensusType.COMMITTEE_BLOCK, new ValidatorConsensusPhases.VerifyCommitteeBlock());
    }

    @Override
    public BFTConsensusPhase manufacturePhases(ConsensusType consensusType) {
        return (BFTConsensusPhase) validator_map.get(consensusType);
    }
}
