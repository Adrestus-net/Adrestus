package io.Adrestus.consensus;

import java.util.EnumMap;
import java.util.Map;

public class Supervisor implements ConsensusRole {
    private final Map<ConsensusType,SupervisorConsensusPhases> supervisor_map;


    public Supervisor() {
        supervisor_map = new EnumMap<>(ConsensusType.class);
        supervisor_map.put(ConsensusType.TRANSACTION_BLOCK, new SupervisorConsensusPhases.ProposeTransactionBlock());
    }

    @Override
    public BFTConsensusPhase manufacturePhases(ConsensusType consensusType) {
        return (BFTConsensusPhase) supervisor_map.get(consensusType);
    }
}
