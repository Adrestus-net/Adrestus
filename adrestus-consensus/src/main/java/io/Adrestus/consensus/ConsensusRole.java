package io.Adrestus.consensus;

import io.Adrestus.consensus.ChangeView.ChangeViewConsensusPhase;

public interface ConsensusRole {
    BFTConsensusPhase manufacturePhases(ConsensusType consensusType);

    ChangeViewConsensusPhase manufacterChangeViewPhases(ConsensusType consensusType);
}
