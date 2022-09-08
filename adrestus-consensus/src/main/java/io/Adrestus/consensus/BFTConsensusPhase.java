package io.Adrestus.consensus;

public interface BFTConsensusPhase<T> {
    void AnnouncePhase(ConsensusMessage<T> data) throws Exception;

    void PreparePhase(ConsensusMessage<T> data) throws InterruptedException;

    void CommitPhase(ConsensusMessage<T> data) throws InterruptedException;
}
