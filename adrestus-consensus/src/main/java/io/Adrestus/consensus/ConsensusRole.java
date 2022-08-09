package io.Adrestus.consensus;

public interface ConsensusRole {
    BFTConsensusPhase manufacturePhases(ConsensusMessageType consensusType);
}
