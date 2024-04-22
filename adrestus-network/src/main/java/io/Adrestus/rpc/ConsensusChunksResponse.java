package io.Adrestus.rpc;

import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;

import java.util.Arrays;

public class ConsensusChunksResponse {
    private byte[] consensus_data;

    public ConsensusChunksResponse(@Deserialize("consensus_data") byte[] consensus_data) {
        this.consensus_data = consensus_data;
    }


    @Serialize
    public byte[] getConsensus_data() {
        return consensus_data;
    }

    public void setConsensus_data(byte[] consensus_data) {
        this.consensus_data = consensus_data;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        ConsensusChunksResponse that = (ConsensusChunksResponse) object;
        return Arrays.equals(consensus_data, that.consensus_data);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(consensus_data);
    }

    @Override
    public String toString() {
        return "ConsensusChunksResponse{" +
                "consensus_data=" + Arrays.toString(consensus_data) +
                '}';
    }
}
