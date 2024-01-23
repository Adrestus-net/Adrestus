package io.Adrestus.util.bytes;

import static io.Adrestus.util.bytes.Checks.checkArgument;

public class ArrayWrappingBytes53 extends ArrayWrappingBytes implements Bytes53 {

    ArrayWrappingBytes53(byte[] bytes) {
        this(checkLength(bytes), 0);
    }

    ArrayWrappingBytes53(byte[] bytes, int offset) {
        super(checkLength(bytes, offset), offset, SIZE);
    }

    // Ensures a proper error message.
    private static byte[] checkLength(byte[] bytes) {
        checkArgument(bytes.length == SIZE, "Expected %s bytes but got %s", SIZE, bytes.length);
        return bytes;
    }

    // Ensures a proper error message.
    private static byte[] checkLength(byte[] bytes, int offset) {
        checkArgument(
                bytes.length - offset >= SIZE,
                "Expected at least %s bytes from offset %s but got only %s",
                SIZE,
                offset,
                bytes.length - offset);
        return bytes;
    }

    @Override
    public Bytes53 copy() {
        if (offset == 0 && length == bytes.length) {
            return this;
        }
        return new ArrayWrappingBytes53(toArray());
    }

    @Override
    public MutableBytes53 mutableCopy() {
        return new MutableArrayWrappingBytes53(toArray());
    }
}
