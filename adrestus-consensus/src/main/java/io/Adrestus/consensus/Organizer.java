package io.Adrestus.consensus;

import java.util.EnumMap;
import java.util.Map;

public class Organizer implements ConsensusRole {
    private final Map<ConsensusType, OrganizerConsensusPhases> organizer_map;
    private boolean DEBUG;

    public Organizer(boolean DEBUG) {
        this.DEBUG = DEBUG;
        organizer_map = new EnumMap<>(ConsensusType.class);
        organizer_map.put(ConsensusType.TRANSACTION_BLOCK, new OrganizerConsensusPhases.ProposeTransactionBlock(this.DEBUG));
    }

    @Override
    public BFTConsensusPhase manufacturePhases(ConsensusType consensusType) {
        return (BFTConsensusPhase) organizer_map.get(consensusType);
    }

}
