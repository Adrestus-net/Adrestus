package io.Adrestus.rpc;

public class ErasureService<V> implements IErasureService<V> {
    @Override
    public V downloadChunks() throws Exception {
        return (V) CachedSerializableErasureObject.getInstance().getSerializableErasureObject();
    }
}
