package io.Adrestus.erasure.code.parameters;

import io.activej.serializer.annotations.Serialize;

import java.util.Objects;

public class FECParameterObject {
    private long dataLen;
    private int numberOfSymbols;

    private int symbolSize;

    private int symbolOverhead;

    public FECParameterObject(long dataLen, int numberOfSymbols, int symbolSize, int symbolOverhead) {
        this.dataLen = dataLen;
        this.numberOfSymbols = numberOfSymbols;
        this.symbolSize = symbolSize;
        this.symbolOverhead = symbolOverhead;
    }


    public FECParameterObject() {
    }


    @Serialize
    public long getDataLen() {
        return dataLen;
    }

    public void setDataLen(long dataLen) {
        this.dataLen = dataLen;
    }

    @Serialize
    public int getNumberOfSymbols() {
        return numberOfSymbols;
    }

    public void setNumberOfSymbols(int numberOfSymbols) {
        this.numberOfSymbols = numberOfSymbols;
    }

    @Serialize
    public int getSymbolSize() {
        return symbolSize;
    }

    public void setSymbolSize(int symbolSize) {
        this.symbolSize = symbolSize;
    }

    @Serialize
    public int getSymbolOverhead() {
        return symbolOverhead;
    }

    public void setSymbolOverhead(int symbolOverhead) {
        this.symbolOverhead = symbolOverhead;
    }

    public int getSize() {
        return (4 * 3) + 8;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FECParameterObject object = (FECParameterObject) o;
        return dataLen == object.dataLen && numberOfSymbols == object.numberOfSymbols && symbolSize == object.symbolSize && symbolOverhead == object.symbolOverhead;
    }

    @Override
    public int hashCode() {
        return Objects.hash(dataLen, numberOfSymbols, symbolSize, symbolOverhead);
    }
}
