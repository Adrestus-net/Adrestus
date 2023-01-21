package io.Adrestus.consensus;

import io.Adrestus.consensus.ChangeView.ChangeViewConsensusPhase;
import io.Adrestus.consensus.ChangeView.ChangeViewOrganizerConsensusPhase;

import java.util.EnumMap;
import java.util.Map;

public class Organizer implements ConsensusRole {
    private final Map<ConsensusType, OrganizerConsensusPhases> organizer_map;
    private final Map<ConsensusType, ChangeViewOrganizerConsensusPhase> change_view_map;
    private boolean DEBUG;

    public Organizer(boolean DEBUG) {
        this.DEBUG = DEBUG;
        this.organizer_map = new EnumMap<>(ConsensusType.class);
        this.change_view_map = new EnumMap<>(ConsensusType.class);
        this.organizer_map.put(ConsensusType.TRANSACTION_BLOCK, new OrganizerConsensusPhases.ProposeTransactionBlock(this.DEBUG));
        this.change_view_map.put(ConsensusType.CHANGE_VIEW_TRANSACTION_BLOCK, new ChangeViewOrganizerConsensusPhase(this.DEBUG));
    }

    @Override
    public BFTConsensusPhase manufacturePhases(ConsensusType consensusType) {
        return (BFTConsensusPhase) organizer_map.get(consensusType);
    }

    @Override
    public ChangeViewConsensusPhase manufacterChangeViewPhases(ConsensusType consensusType) {
        return (ChangeViewConsensusPhase) change_view_map.get(consensusType);
    }

}
