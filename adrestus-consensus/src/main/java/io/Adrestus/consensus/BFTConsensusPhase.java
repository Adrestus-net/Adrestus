package io.Adrestus.consensus;

public interface BFTConsensusPhase<T> {
    void AnnouncePhase(ConsensusMessage<T> data) throws Exception;

    void PreparePhase(ConsensusMessage<T> data);

    void CommitPhase(ConsensusMessage<T> data);
}