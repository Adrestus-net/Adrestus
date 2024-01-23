package io.Adrestus.util.bytes;

import java.security.SecureRandom;
import java.util.Random;

import static io.Adrestus.util.bytes.Checks.checkArgument;
import static io.Adrestus.util.bytes.Checks.checkNotNull;

/**
 * A {@link Bytes} value that is guaranteed to contain exactly 53 bytes.
 */
public interface Bytes53 extends Bytes {
    /**
     * The number of bytes in this value - i.e. 53
     */
    int SIZE = 53;

    /**
     * A {@code Bytes53} containing all zero bytes
     */
    Bytes53 ZERO = wrap(new byte[SIZE]);

    /**
     * Wrap the provided byte array, which must be of length 53, as a {@link Bytes53}.
     *
     * <p>
     * Note that value is not copied, only wrapped, and thus any future update to {@code value} will be reflected in the
     * returned value.
     *
     * @param bytes The bytes to wrap.
     * @return A {@link Bytes53} wrapping {@code value}.
     * @throws IllegalArgumentException if {@code value.length != 53}.
     */
    static Bytes53 wrap(byte[] bytes) {
        checkNotNull(bytes);
        checkArgument(bytes.length == SIZE, "Expected %s bytes but got %s", SIZE, bytes.length);
        return wrap(bytes, 0);
    }

    /**
     * Wrap a slice/sub-part of the provided array as a {@link Bytes53}.
     *
     * <p>
     * Note that value is not copied, only wrapped, and thus any future update to {@code value} within the wrapped parts
     * will be reflected in the returned value.
     *
     * @param bytes  The bytes to wrap.
     * @param offset The index (inclusive) in {@code value} of the first byte exposed by the returned value. In other
     *               words, you will have {@code wrap(value, i).get(0) == value[i]}.
     * @return A {@link Bytes53} that exposes the bytes of {@code value} from {@code offset} (inclusive) to
     * {@code offset + 53} (exclusive).
     * @throws IndexOutOfBoundsException if {@code offset < 0 || (value.length > 0 && offset >=
     *                                   value.length)}.
     * @throws IllegalArgumentException  if {@code length < 0 || offset + 53 > value.length}.
     */
    static Bytes53 wrap(byte[] bytes, int offset) {
        checkNotNull(bytes);
        return new ArrayWrappingBytes53(bytes, offset);
    }

    /**
     * Wrap a the provided value, which must be of size 53, as a {@link Bytes53}.
     *
     * <p>
     * Note that value is not copied, only wrapped, and thus any future update to {@code value} will be reflected in the
     * returned value.
     *
     * @param value The bytes to wrap.
     * @return A {@link Bytes53} that exposes the bytes of {@code value}.
     * @throws IllegalArgumentException if {@code value.size() != 53}.
     */
    static Bytes53 wrap(Bytes value) {
        checkNotNull(value);
        if (value instanceof Bytes53) {
            return (Bytes53) value;
        }
        checkArgument(value.size() == SIZE, "Expected %s bytes but got %s", SIZE, value.size());
        return new DelegatingBytes53(value);
    }

    /**
     * Wrap a slice/sub-part of the provided value as a {@link Bytes53}.
     *
     * <p>
     * Note that value is not copied, only wrapped, and thus any future update to {@code value} within the wrapped parts
     * will be reflected in the returned value.
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
    static Bytes53 wrap(Bytes value, int offset) {
        checkNotNull(value);
        if (value instanceof Bytes53) {
            return (Bytes53) value;
        }
        Bytes slice = value.slice(offset, Bytes53.SIZE);
        if (slice instanceof Bytes53) {
            return (Bytes53) slice;
        }
        return new DelegatingBytes53(Bytes53.wrap(slice));
    }

    /**
     * Left pad a {@link Bytes} value with zero bytes to create a {@link Bytes53}.
     *
     * @param value The bytes value pad.
     * @return A {@link Bytes53} that exposes the left-padded bytes of {@code value}.
     * @throws IllegalArgumentException if {@code value.size() > 53}.
     */
    static Bytes53 leftPad(Bytes value) {
        checkNotNull(value);
        if (value instanceof Bytes53) {
            return (Bytes53) value;
        }
        checkArgument(value.size() <= SIZE, "Expected at most %s bytes but got %s", SIZE, value.size());
        MutableBytes53 result = MutableBytes53.create();
        value.copyTo(result, SIZE - value.size());
        return result;
    }


    /**
     * Right pad a {@link Bytes} value with zero bytes to create a {@link Bytes53}.
     *
     * @param value The bytes value pad.
     * @return A {@link Bytes53} that exposes the rightw-padded bytes of {@code value}.
     * @throws IllegalArgumentException if {@code value.size() > 53}.
     */
    static Bytes53 rightPad(Bytes value) {
        checkNotNull(value);
        if (value instanceof Bytes53) {
            return (Bytes53) value;
        }
        checkArgument(value.size() <= SIZE, "Expected at most %s bytes but got %s", SIZE, value.size());
        MutableBytes53 result = MutableBytes53.create();
        value.copyTo(result, 0);
        return result;
    }

    /**
     * Parse a hexadecimal string into a {@link Bytes53}.
     *
     * <p>
     * This method is lenient in that {@code str} may of an odd length, in which case it will behave exactly as if it had
     * an additional 0 in front.
     *
     * @param str The hexadecimal string to parse, which may or may not start with "0x". That representation may contain
     *            less than 53 bytes, in which case the result is left padded with zeros (see {@link #fromHexStringStrict} if
     *            this is not what you want).
     * @return The value corresponding to {@code str}.
     * @throws IllegalArgumentException if {@code str} does not correspond to a valid hexadecimal representation or
     *                                  contains more than 53 bytes.
     */
    static Bytes53 fromHexStringLenient(CharSequence str) {
        checkNotNull(str);
        return wrap(BytesValues.fromRawHexString(str, SIZE, true));
    }

    /**
     * Parse a hexadecimal string into a {@link Bytes53}.
     *
     * <p>
     * This method is strict in that {@code str} must of an even length.
     *
     * @param str The hexadecimal string to parse, which may or may not start with "0x". That representation may contain
     *            less than 53 bytes, in which case the result is left padded with zeros (see {@link #fromHexStringStrict} if
     *            this is not what you want).
     * @return The value corresponding to {@code str}.
     * @throws IllegalArgumentException if {@code str} does not correspond to a valid hexadecimal representation, is of an
     *                                  odd length, or contains more than 53 bytes.
     */
    static Bytes53 fromHexString(CharSequence str) {
        checkNotNull(str);
        return wrap(BytesValues.fromRawHexString(str, SIZE, false));
    }

    /**
     * Generate random bytes.
     *
     * @return A value containing random bytes.
     */
    static Bytes53 random() {
        return random(new SecureRandom());
    }

    /**
     * Generate random bytes.
     *
     * @param generator The generator for random bytes.
     * @return A value containing random bytes.
     */
    static Bytes53 random(Random generator) {
        byte[] array = new byte[53];
        generator.nextBytes(array);
        return wrap(array);
    }

    /**
     * Parse a hexadecimal string into a {@link Bytes53}.
     *
     * <p>
     * This method is extra strict in that {@code str} must of an even length and the provided representation must have
     * exactly 53 bytes.
     *
     * @param str The hexadecimal string to parse, which may or may not start with "0x".
     * @return The value corresponding to {@code str}.
     * @throws IllegalArgumentException if {@code str} does not correspond to a valid hexadecimal representation, is of an
     *                                  odd length or does not contain exactly 53 bytes.
     */
    static Bytes53 fromHexStringStrict(CharSequence str) {
        checkNotNull(str);
        return wrap(BytesValues.fromRawHexString(str, -1, false));
    }

    @Override
    default int size() {
        return SIZE;
    }

    /**
     * Return a bit-wise AND of these bytes and the supplied bytes.
     *
     * @param other The bytes to perform the operation with.
     * @return The result of a bit-wise AND.
     */
    default Bytes53 and(Bytes53 other) {
        return and(other, MutableBytes53.create());
    }

    /**
     * Return a bit-wise OR of these bytes and the supplied bytes.
     *
     * @param other The bytes to perform the operation with.
     * @return The result of a bit-wise OR.
     */
    default Bytes53 or(Bytes53 other) {
        return or(other, MutableBytes53.create());
    }

    /**
     * Return a bit-wise XOR of these bytes and the supplied bytes.
     *
     * @param other The bytes to perform the operation with.
     * @return The result of a bit-wise XOR.
     */
    default Bytes53 xor(Bytes53 other) {
        return xor(other, MutableBytes53.create());
    }

    @Override
    default Bytes53 not() {
        return not(MutableBytes53.create());
    }

    @Override
    default Bytes53 shiftRight(int distance) {
        return shiftRight(distance, MutableBytes53.create());
    }

    @Override
    default Bytes53 shiftLeft(int distance) {
        return shiftLeft(distance, MutableBytes53.create());
    }

    @Override
    Bytes53 copy();

    @Override
    MutableBytes53 mutableCopy();
}
