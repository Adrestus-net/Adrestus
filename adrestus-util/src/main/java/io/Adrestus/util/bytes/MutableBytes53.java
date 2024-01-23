package io.Adrestus.util.bytes;

import static io.Adrestus.util.bytes.Checks.checkNotNull;

public interface MutableBytes53 extends MutableBytes, Bytes53 {

    /**
     * Create a new mutable 53 bytes value.
     *
     * @return A newly allocated {@link MutableBytes} value.
     */
    static MutableBytes53 create() {
        return new MutableArrayWrappingBytes53(new byte[SIZE]);
    }

    /**
     * Wrap a 53 bytes array as a mutable 53 bytes value.
     *
     * @param value The value to wrap.
     * @return A {@link MutableBytes53} wrapping {@code value}.
     * @throws IllegalArgumentException if {@code value.length != 53}.
     */
    static MutableBytes53 wrap(byte[] value) {
        checkNotNull(value);
        return new MutableArrayWrappingBytes53(value);
    }

    /**
     * Wrap a the provided array as a {@link MutableBytes53}.
     *
     * <p>
     * Note that value is not copied, only wrapped, and thus any future update to {@code value} within the wrapped parts
     * will be reflected in the returned value.
     *
     * @param value  The bytes to wrap.
     * @param offset The index (inclusive) in {@code value} of the first byte exposed by the returned value. In other
     *               words, you will have {@code wrap(value, i).get(0) == value[i]}.
     * @return A {@link MutableBytes53} that exposes the bytes of {@code value} from {@code offset} (inclusive) to
     * {@code offset + 53} (exclusive).
     * @throws IndexOutOfBoundsException if {@code offset < 0 || (value.length > 0 && offset >=
     *                                   value.length)}.
     * @throws IllegalArgumentException  if {@code length < 0 || offset + 53 > value.length}.
     */
    static MutableBytes53 wrap(byte[] value, int offset) {
        checkNotNull(value);
        return new MutableArrayWrappingBytes53(value, offset);
    }

    /**
     * Wrap a the provided value, which must be of size 53, as a {@link MutableBytes53}.
     *
     * <p>
     * Note that value is not copied, only wrapped, and thus any future update to {@code value} will be reflected in the
     * returned value.
     *
     * @param value The bytes to wrap.
     * @return A {@link MutableBytes53} that exposes the bytes of {@code value}.
     * @throws IllegalArgumentException if {@code value.size() != 53}.
     */
    static MutableBytes53 wrap(MutableBytes value) {
        checkNotNull(value);
        if (value instanceof MutableBytes53) {
            return (MutableBytes53) value;
        }
        return DelegatingMutableBytes53.delegateTo(value);
    }

    /**
     * Wrap a slice/sub-part of the provided value as a {@link MutableBytes53}.
     *
     * <p>
     * Note that the value is not copied, and thus any future update to {@code value} within the wrapped parts will be
     * reflected in the returned value.
     *
     * @param value  The bytes to wrap.
     * @param offset The index (inclusive) in {@code value} of the first byte exposed by the returned value. In other
     *               words, you will have {@code wrap(value, i).get(0) == value.get(i)}.
     * @return A {@link Bytes53} that exposes the bytes of {@code value} from {@code offset} (inclusive) to
     * {@code offset + 53} (exclusive).
     * @throws IndexOutOfBoundsException if {@code offset < 0 || (value.size() > 0 && offset >=
     *                                   value.size())}.
     * @throws IllegalArgumentException  if {@code length < 0 || offset + 53 > value.size()}.
     */
    static MutableBytes53 wrap(MutableBytes value, int offset) {
        checkNotNull(value);
        if (value instanceof MutableBytes53) {
            return (MutableBytes53) value;
        }
        MutableBytes slice = value.mutableSlice(offset, Bytes53.SIZE);
        if (slice instanceof MutableBytes53) {
            return (MutableBytes53) slice;
        }
        return DelegatingMutableBytes53.delegateTo(slice);
    }
}
