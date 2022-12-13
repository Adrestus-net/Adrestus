package io.Adrestus.rpc;

import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;

public class BlockRequest {
    @Serialize
    public final String hash;

    public BlockRequest(@Deserialize("hash") String name) {
        this.hash = name;
    }
}
