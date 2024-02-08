package io.Adrestus.rpc;

public interface IErasureService<V> {

    V downloadChunks() throws Exception;
}
