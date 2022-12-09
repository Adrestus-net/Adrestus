package io.Adrestus.rpc;

import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;

import java.util.ArrayList;
import java.util.List;

public class Response2<T> {

    private List<T> data;

    public Response2(@Deserialize("data")List<T> data) {
        this.data = data;
    }

    @Serialize
    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }
}
