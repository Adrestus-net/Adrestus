package io.Adrestus.rpc;

import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;
import lombok.Setter;

import java.util.Map;
import java.util.Objects;

@Setter
public class ErasureResponse {
    private Map<String, byte[]> erasureData;

    public ErasureResponse(@Deserialize("erasureData") Map<String, byte[]> erasureData) {
        this.erasureData = erasureData;
    }

    @Serialize
    public Map<String, byte[]> getErasureData() {
        return erasureData;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ErasureResponse that = (ErasureResponse) o;
        return Objects.equals(erasureData, that.erasureData);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(erasureData);
    }
}
