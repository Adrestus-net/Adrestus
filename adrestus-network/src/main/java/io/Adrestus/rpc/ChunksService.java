package io.Adrestus.rpc;

public class ChunksService<V> implements IChunksService<V> {
    @Override
    public V downloadErasureChunks() throws Exception {
        return (V) CachedSerializableErasureObject.getInstance().getSerializableErasureObject();
    }

    @Override
    public byte[] downloadConsensusChunks(int position) throws Exception {
        return CachedConsensusPublisherData.getInstance().getDataAtPosition(position);
    }
}
