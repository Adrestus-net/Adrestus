package io.Adrestus.rpc;

import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;

public class BlockRequest2 {
    @Serialize
    public final String hash;

    public BlockRequest2(@Deserialize("hash") String name) {
        this.hash = name;
    }

}
