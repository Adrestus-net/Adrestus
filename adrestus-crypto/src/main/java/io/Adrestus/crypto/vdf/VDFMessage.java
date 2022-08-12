package io.Adrestus.crypto.vdf;

import com.google.common.base.Objects;
import io.activej.serializer.annotations.Serialize;

import java.util.Arrays;

public class VDFMessage {
    private byte [] VDFSolution;

    public VDFMessage(byte[] buffer) {
        this.VDFSolution = buffer;
    }

    public VDFMessage() {
    }

    @Serialize
    public byte[] getVDFSolution() {
        return VDFSolution;
    }

    public void setVDFSolution(byte[] VDFSolution) {
        this.VDFSolution = VDFSolution;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VDFMessage that = (VDFMessage) o;
        return Objects.equal(VDFSolution, that.VDFSolution);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(VDFSolution);
    }

    @Override
    public String toString() {
        return "VDFMessage{" +
                "buffer=" + Arrays.toString(VDFSolution) +
                '}';
    }
}
