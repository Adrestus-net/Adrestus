package io.Adrestus.rpc;

public interface IChunksService<V> {

    V downloadErasureChunks() throws Exception;

    byte[] downloadConsensusChunks(int position) throws Exception;
}
