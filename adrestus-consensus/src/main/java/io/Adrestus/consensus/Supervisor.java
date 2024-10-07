package io.Adrestus.consensus;

import io.Adrestus.consensus.ChangeView.ChangeViewConsensusPhase;
import io.Adrestus.consensus.ChangeView.ChangeViewSupervisorConsensusPhase;

import java.util.EnumMap;
import java.util.Map;

public class Supervisor implements ConsensusRole {
    private static volatile Supervisor instance;
    private final Map<ConsensusType, SupervisorConsensusPhases> supervisor_map;
    private final Map<ConsensusType, ChangeViewSupervisorConsensusPhase> change_view_map;

    private Supervisor(boolean DEBUG) {
        if (instance != null) {
            throw new IllegalStateException("Already initialized.");
        }
        this.supervisor_map = new EnumMap<>(ConsensusType.class);
        this.change_view_map = new EnumMap<>(ConsensusType.class);
        this.supervisor_map.put(ConsensusType.VDF, new SupervisorConsensusPhases.ProposeVDF(DEBUG));
        this.supervisor_map.put(ConsensusType.VRF, new SupervisorConsensusPhases.ProposeVRF(DEBUG));
        this.supervisor_map.put(ConsensusType.COMMITTEE_BLOCK, new SupervisorConsensusPhases.ProposeCommitteeBlock(DEBUG));
        this.change_view_map.put(ConsensusType.CHANGE_VIEW_COMMITTEE_BLOCK, new ChangeViewSupervisorConsensusPhase(DEBUG));
    }

    public static Supervisor getInstance(boolean DEBUG) {

        var result = instance;
        if (result == null) {
            synchronized (Supervisor.class) {
                result = instance;
                if (result == null) {
                    result = new Supervisor(DEBUG);
                    instance = result;
                }
            }
        }
        return result;
    }

    @Override
    public BFTConsensusPhase manufacturePhases(ConsensusType consensusType) {
        return (BFTConsensusPhase) supervisor_map.get(consensusType);
    }

    @Override
    public ChangeViewConsensusPhase manufacterChangeViewPhases(ConsensusType consensusType) {
        return (ChangeViewConsensusPhase) change_view_map.get(consensusType);
    }

    public synchronized void clear(boolean DEBUG) {
        instance = null;
        this.supervisor_map.clear();
        this.change_view_map.clear();
    }
}
