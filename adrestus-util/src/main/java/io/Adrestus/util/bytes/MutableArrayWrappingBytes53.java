package io.Adrestus.util.bytes;

public class MutableArrayWrappingBytes53 extends MutableArrayWrappingBytes implements MutableBytes53 {

    MutableArrayWrappingBytes53(byte[] bytes) {
        this(bytes, 0);
    }

    MutableArrayWrappingBytes53(byte[] bytes, int offset) {
        super(bytes, offset, SIZE);
    }

    @Override
    public Bytes53 copy() {
        return new ArrayWrappingBytes53(toArray());
    }

    @Override
    public MutableBytes53 mutableCopy() {
        return new MutableArrayWrappingBytes53(toArray());
    }
}
