package io.Adrestus.rpc;

import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;

public class TransactionRequest {
    @Serialize
    public final String hash;

    public TransactionRequest(@Deserialize("hash") String hash) {
        this.hash = hash;
    }
}
