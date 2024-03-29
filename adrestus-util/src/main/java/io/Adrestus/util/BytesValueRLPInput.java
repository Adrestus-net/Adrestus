package io.Adrestus.util;

import io.Adrestus.util.bytes.Bytes;
import io.Adrestus.util.bytes.Bytes32;

import java.math.BigInteger;


public class BytesValueRLPInput extends AbstractRLPInput {

    // The RLP encoded data.
    private final Bytes value;

    public BytesValueRLPInput(final Bytes value, final boolean lenient) {
        this(value, lenient, true);
    }

    public BytesValueRLPInput(
            final Bytes value, final boolean lenient, final boolean shouldFitExactly) {
        super(lenient);
        this.value = value;
        init(value.size(), shouldFitExactly);
    }

    @Override
    protected byte inputByte(final long offset) {
        return value.get((int) offset);
    }

    @Override
    protected Bytes inputSlice(final long offset, final int length) {
        return value.slice(Math.toIntExact(offset), length);
    }

    @Override
    protected Bytes32 inputSlice32(final long offset) {
        return Bytes32.wrap(inputSlice(offset, 32));
    }

    @Override
    protected String inputHex(final long offset, final int length) {
        return value.slice(Math.toIntExact(offset), length).toString().substring(2);
    }

    @Override
    protected BigInteger getUnsignedBigInteger(final long offset, final int length) {
        return value.slice(Math.toIntExact(offset), length).toUnsignedBigInteger();
    }

    @Override
    protected int getInt(final long offset) {
        return value.getInt(Math.toIntExact(offset));
    }

    @Override
    protected long getLong(final long offset) {
        return value.getLong(Math.toIntExact(offset));
    }

    @Override
    public Bytes raw() {
        return value;
    }
}
