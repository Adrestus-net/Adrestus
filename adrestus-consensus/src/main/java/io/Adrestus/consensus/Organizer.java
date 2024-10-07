package io.Adrestus.consensus;

import io.Adrestus.consensus.ChangeView.ChangeViewConsensusPhase;
import io.Adrestus.consensus.ChangeView.ChangeViewOrganizerConsensusPhase;

import java.util.EnumMap;
import java.util.Map;

public class Organizer implements ConsensusRole {

    private static volatile Organizer instance;
    private final Map<ConsensusType, OrganizerConsensusPhases> organizer_map;
    private final Map<ConsensusType, ChangeViewOrganizerConsensusPhase> change_view_map;
    private boolean DEBUG;

    private Organizer(boolean DEBUG) {
        if (instance != null) {
            throw new IllegalStateException("Already initialized.");
        }
        this.DEBUG = DEBUG;
        this.organizer_map = new EnumMap<>(ConsensusType.class);
        this.change_view_map = new EnumMap<>(ConsensusType.class);
        this.organizer_map.put(ConsensusType.TRANSACTION_BLOCK, new OrganizerConsensusPhases.ProposeTransactionBlock(this.DEBUG));
        this.change_view_map.put(ConsensusType.CHANGE_VIEW_TRANSACTION_BLOCK, new ChangeViewOrganizerConsensusPhase(this.DEBUG));
    }

    public static Organizer getInstance(boolean DEBUG) {
        var result = instance;
        if (result == null) {
            synchronized (Organizer.class) {
                result = instance;
                if (result == null) {
                    result = new Organizer(DEBUG);
                    instance = result;
                }
            }
        }
        return result;
    }

    @Override
    public BFTConsensusPhase manufacturePhases(ConsensusType consensusType) {
        return (BFTConsensusPhase) organizer_map.get(consensusType);
    }

    @Override
    public ChangeViewConsensusPhase manufacterChangeViewPhases(ConsensusType consensusType) {
        return (ChangeViewConsensusPhase) change_view_map.get(consensusType);
    }

    public synchronized void clear(boolean DEBUG) {
        instance = null;
        this.organizer_map.clear();
        this.change_view_map.clear();
    }

}
