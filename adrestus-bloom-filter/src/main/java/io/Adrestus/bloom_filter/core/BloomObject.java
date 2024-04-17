package io.Adrestus.bloom_filter.core;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

public class BloomObject implements Cloneable, Serializable {
    private int array[];
    private int HashFunctionNum;

    private int numBitsRequired;

    public BloomObject() {
    }

    public BloomObject(int[] array, int hashFunctionNum, int numBitsRequired) {
        this.array = array;
        this.HashFunctionNum = hashFunctionNum;
        this.numBitsRequired = numBitsRequired;
    }

    public int[] getArray() {
        return array;
    }

    public void setArray(int[] array) {
        this.array = array;
    }

    public int getHashFunctionNum() {
        return HashFunctionNum;
    }

    public void setHashFunctionNum(int hashFunctionNum) {
        HashFunctionNum = hashFunctionNum;
    }

    public int getNumBitsRequired() {
        return numBitsRequired;
    }

    public void setNumBitsRequired(int numBitsRequired) {
        this.numBitsRequired = numBitsRequired;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BloomObject that = (BloomObject) o;
        return HashFunctionNum == that.HashFunctionNum && numBitsRequired == that.numBitsRequired && Arrays.equals(array, that.array);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(HashFunctionNum, numBitsRequired);
        result = 31 * result + Arrays.hashCode(array);
        return result;
    }
}
