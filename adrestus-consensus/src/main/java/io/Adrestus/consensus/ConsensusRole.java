package io.Adrestus.consensus;

public interface ConsensusRole {
    BFTConsensusPhase manufacturePhases(ConsensusType consensusType);
}
