package io.Adrestus.crypto;

import com.google.common.base.Objects;
import io.activej.serializer.annotations.Serialize;

import java.util.Arrays;

public final class SecurityHeader {
    private byte[] pRnd;
    private byte[] Rnd;

    public SecurityHeader() {
        this.pRnd = new byte[0];
        this.Rnd = new byte[0];
    }

    public SecurityHeader(byte[] pRnd, byte[] rnd) {
        this.pRnd = pRnd;
        this.Rnd = rnd;
    }


    @Serialize
    public byte[] getPRnd() {
        return pRnd;
    }


    public void setPRnd(byte[] pRnd) {
        this.pRnd = pRnd;
    }

    public void setRnd(byte[] rnd) {
        this.Rnd = rnd;
    }

    @Serialize
    public byte[] getRnd() {
        return Rnd;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SecurityHeader that = (SecurityHeader) o;
        return Arrays.equals(pRnd, that.pRnd) && Arrays.equals(Rnd, that.Rnd);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(pRnd, Rnd);
    }

    @Override
    public String toString() {
        return "SecurityHeader{" +
                "pRnd=" + Arrays.toString(pRnd) +
                ", Rnd=" + Arrays.toString(Rnd) +
                '}';
    }
}
