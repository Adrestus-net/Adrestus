package io.Adrestus.consensus;

import io.Adrestus.crypto.vrf.VRFMessage;

public interface VRFConsensusPhase<T> extends BFTConsensusPhase<T> {

    public void Initialize(VRFMessage message) throws Exception;

    public void AggregateVRF(VRFMessage message) throws Exception;
}
