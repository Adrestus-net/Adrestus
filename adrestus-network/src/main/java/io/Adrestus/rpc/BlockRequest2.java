package io.Adrestus.rpc;

import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;

import java.util.ArrayList;
import java.util.List;

public class BlockRequest2 {
    private final List<String> hash;

    public BlockRequest2(@Deserialize("hash") List<String> hash) {
        this.hash = hash;
    }

    @Serialize
    public ArrayList<String> getHash() {
        return (ArrayList<String>) hash;
    }
}
