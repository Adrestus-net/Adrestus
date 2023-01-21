package io.Adrestus.consensus;

import io.Adrestus.consensus.ChangeView.ChangeViewConsensusPhase;
import io.Adrestus.consensus.ChangeView.ChangeViewSupervisorConsensusPhase;

import java.util.EnumMap;
import java.util.Map;

public class Supervisor implements ConsensusRole {

    private final Map<ConsensusType, SupervisorConsensusPhases> supervisor_map;
    private final Map<ConsensusType, ChangeViewSupervisorConsensusPhase> change_view_map;

    public Supervisor(boolean DEBUG) {
        this.supervisor_map = new EnumMap<>(ConsensusType.class);
        this.change_view_map = new EnumMap<>(ConsensusType.class);
        this.supervisor_map.put(ConsensusType.VDF, new SupervisorConsensusPhases.ProposeVDF(DEBUG));
        this.supervisor_map.put(ConsensusType.VRF, new SupervisorConsensusPhases.ProposeVRF(DEBUG));
        this.supervisor_map.put(ConsensusType.COMMITTEE_BLOCK, new SupervisorConsensusPhases.ProposeCommitteeBlock(DEBUG));
        this.change_view_map.put(ConsensusType.CHANGE_VIEW_COMMITTEE_BLOCK, new ChangeViewSupervisorConsensusPhase(DEBUG));
    }

    @Override
    public BFTConsensusPhase manufacturePhases(ConsensusType consensusType) {
        return (BFTConsensusPhase) supervisor_map.get(consensusType);
    }

    @Override
    public ChangeViewConsensusPhase manufacterChangeViewPhases(ConsensusType consensusType) {
        return (ChangeViewConsensusPhase) change_view_map.get(consensusType);
    }
}
