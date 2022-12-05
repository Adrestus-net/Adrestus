package io.Adrestus.rpc;

import com.google.common.base.Objects;
import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;

public class Response {
    private byte[] byte_data;

    public Response(@Deserialize("byte_data") byte[] byte_data) {
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
        Response response = (Response) o;
        return Objects.equal(byte_data, response.byte_data);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(byte_data);
    }

    @Override
    public String toString() {
        return "Response{" +
                "abstractBlock=" + byte_data +
                '}';
    }
}
