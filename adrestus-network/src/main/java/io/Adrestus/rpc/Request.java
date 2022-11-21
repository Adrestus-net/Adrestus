package io.Adrestus.rpc;

import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;

public class Request {
    @Serialize
    public final String hash;

    public Request(@Deserialize("hash") String name) {
        this.hash = name;
    }
}
