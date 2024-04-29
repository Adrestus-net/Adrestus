package io.Adrestus.rpc;

import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;

import java.util.Objects;

public class ConsensusChunksRequest4 {

    @Serialize
    public final String number;

    public ConsensusChunksRequest4(@Deserialize("number") String number) {
        this.number = number;
    }


    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        ConsensusChunksRequest4 that = (ConsensusChunksRequest4) object;
        return Objects.equals(number, that.number);
    }

    @Override
    public int hashCode() {
        return Objects.hash(number);
    }


    @Override
    public String toString() {
        return "ConsensusChunksRequest4{" +
                "number='" + number + '\'' +
                '}';
    }
}
