package io.Adrestus.rpc;

import java.util.Map;

public interface IChunksService {

    Map<String, byte[]> downloadErasureChunks();

}
