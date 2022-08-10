package io.Adrestus.consensus;

import java.util.EnumMap;
import java.util.Map;

public class Organizer implements ConsensusRole {
    private final Map<ConsensusType, OrganizerConsensusPhases> organizer_map;

    public Organizer() {
        organizer_map = new EnumMap<>(ConsensusType.class);
        organizer_map.put(ConsensusType.VDF, new OrganizerConsensusPhases.ProposeVDF());
        organizer_map.put(ConsensusType.VRF, new OrganizerConsensusPhases.ProposeVRF());
        organizer_map.put(ConsensusType.COMMITTEE_BLOCK, new OrganizerConsensusPhases.ProposeCommitteeBlock());
    }

    @Override
    public BFTConsensusPhase manufacturePhases(ConsensusType consensusType) {
        return (BFTConsensusPhase) organizer_map.get(consensusType);
    }

}
