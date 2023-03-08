package io.Adrestus.rpc;

import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;

public class PatriciaTreeRequest {

    @Serialize
    public final String hash;

    public PatriciaTreeRequest(@Deserialize("hash") String name) {
        this.hash = name;
    }
}
