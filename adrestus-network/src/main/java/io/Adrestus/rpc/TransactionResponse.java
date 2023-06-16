package io.Adrestus.rpc;

import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;

import java.util.Arrays;

public class TransactionResponse {
    private byte[] byte_data;

    public TransactionResponse(@Deserialize("byte_data") byte[] byte_data) {
        this.byte_data = byte_data;
    }

    @Serialize
    public byte[] getByte_data() {
        return byte_data;
    }

    public void setByte_data(byte[] byte_data) {
        this.byte_data = byte_data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransactionResponse that = (TransactionResponse) o;
        return Arrays.equals(byte_data, that.byte_data);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(byte_data);
    }

    @Override
    public String toString() {
        return "Response{" +
                "abstractBlock=" + byte_data +
                '}';
    }
}
