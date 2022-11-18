package io.Adrestus.consensus;

import java.util.EnumMap;
import java.util.Map;

public class Supervisor implements ConsensusRole {

    private final Map<ConsensusType, SupervisorConsensusPhases> supervisor_map;
    private boolean DEBUG;
    public Supervisor(boolean DEBUG) {
        supervisor_map = new EnumMap<>(ConsensusType.class);
        supervisor_map.put(ConsensusType.VDF, new SupervisorConsensusPhases.ProposeVDF(DEBUG));
        supervisor_map.put(ConsensusType.VRF, new SupervisorConsensusPhases.ProposeVRF());
        supervisor_map.put(ConsensusType.COMMITTEE_BLOCK, new SupervisorConsensusPhases.ProposeCommitteeBlock(DEBUG));
    }

    @Override
    public BFTConsensusPhase manufacturePhases(ConsensusType consensusType) {
        return (BFTConsensusPhase) supervisor_map.get(consensusType);
    }
}
