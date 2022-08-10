package io.Adrestus.consensus;

public interface VRFConsensusPhase<T> extends BFTConsensusPhase<T>{

    public void init();
}
