package io.Adrestus.rpc;

import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;

import java.util.List;
import java.util.Objects;


public class ErasureRequest {
    private final List<String> validators;

    public ErasureRequest(@Deserialize("validators") List<String> validators) {
        this.validators = validators;
    }

    @Serialize
    public List<String> getValidators() {
        return validators;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ErasureRequest that = (ErasureRequest) o;
        return Objects.equals(validators, that.validators);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(validators);
    }
}
