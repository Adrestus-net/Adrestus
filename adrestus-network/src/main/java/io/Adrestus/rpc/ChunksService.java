package io.Adrestus.rpc;

import java.util.Map;

public class ChunksService implements IChunksService {
    @Override
    public Map<String, byte[]> downloadErasureChunks() {
        return CachedSerializableErasureObject.getSerializableErasureObject();
    }
}
