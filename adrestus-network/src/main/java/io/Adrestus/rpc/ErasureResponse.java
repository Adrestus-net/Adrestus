package io.Adrestus.rpc;

import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;

import java.util.Arrays;

public class ErasureResponse {
    private byte[] erasure_data;

    public ErasureResponse(@Deserialize("erasure_data") byte[] erasure_data) {
        this.erasure_data = erasure_data;
    }


    @Serialize
    public byte[] getErasure_data() {
        return erasure_data;
    }

    public void setErasure_data(byte[] erasure_data) {
        this.erasure_data = erasure_data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ErasureResponse that = (ErasureResponse) o;
        return Arrays.equals(erasure_data, that.erasure_data);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(erasure_data);
    }
}
