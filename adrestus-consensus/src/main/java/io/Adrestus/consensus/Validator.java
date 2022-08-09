package io.Adrestus.consensus;

import java.util.EnumMap;
import java.util.Map;

public class Validator implements ConsensusRole{
    private final Map<ConsensusMessageType, ValidatorPhases> validator_map;


    public Validator() {
        validator_map = new EnumMap<>(ConsensusMessageType.class);
        validator_map.put(ConsensusMessageType.VDF,new ValidatorPhases.VerifyVDF());
        validator_map.put(ConsensusMessageType.VRF,new ValidatorPhases.VerifyVRF());
        validator_map.put(ConsensusMessageType.TRANSACTION_BLOCK,new ValidatorPhases.VerifyTransactionBlock());
        validator_map.put(ConsensusMessageType.COMMITTEE_BLOCK,new ValidatorPhases.VerifyCommitteeBlock());
    }

    @Override
    public BFTConsensusPhase manufacturePhases(ConsensusMessageType consensusType) {
        return (BFTConsensusPhase) validator_map.get(consensusType);
    }
}
