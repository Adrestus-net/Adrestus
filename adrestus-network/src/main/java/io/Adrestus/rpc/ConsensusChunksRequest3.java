package io.Adrestus.rpc;

import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;

import java.util.Objects;

public class ConsensusChunksRequest3 {
    @Serialize
    public final String number;

    public ConsensusChunksRequest3(@Deserialize("number") String number) {
        this.number = number;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        ConsensusChunksRequest3 that = (ConsensusChunksRequest3) object;
        return Objects.equals(number, that.number);
    }

    @Override
    public int hashCode() {
        return Objects.hash(number);
    }

    @Override
    public String toString() {
        return "ConsensusChunksRequest3{" +
                "number='" + number + '\'' +
                '}';
    }
}
