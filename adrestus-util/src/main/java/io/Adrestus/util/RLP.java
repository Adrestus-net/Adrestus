package io.Adrestus.util;


import io.Adrestus.crypto.ByteArrayWrapper;
import io.Adrestus.crypto.ByteUtil;
import io.Adrestus.util.bytes.Bytes;
import io.Adrestus.util.bytes.MutableBytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import static io.Adrestus.crypto.EncodeUtil.*;
import static java.lang.String.format;
import static java.util.Arrays.copyOfRange;
import static org.spongycastle.util.Arrays.concatenate;
import static org.spongycastle.util.BigIntegers.asUnsignedByteArray;

/**
 * Recursive Length Prefix (RLP) encoding.
 * <p>
 * The purpose of RLP is to encode arbitrarily nested arrays of binary data, and
 * RLP is the main encoding method used to serialize objects in Ethereum. The
 * only purpose of RLP is to encode structure; encoding specific atomic data
 * types (eg. strings, integers, floats) is left up to higher-order protocols; in
 * Ethereum the standard is that integers are represented in big endian binary
 * form. If one wishes to use RLP to encode a dictionary, the two suggested
 * canonical forms are to either use [[k1,v1],[k2,v2]...] with keys in
 * lexicographic order or to use the higher-level Patricia Tree encoding as
 * Ethereum does.
 * <p>
 * The RLP encoding function takes in an item. An item is defined as follows:
 * <p>
 * - A string (ie. byte array) is an item - A list of items is an item
 * <p>
 * For example, an empty string is an item, as is the string containing the word
 * "cat", a list containing any number of strings, as well as more complex data
 * structures like ["cat",["puppy","cow"],"horse",[[]],"pig",[""],"sheep"]. Note
 * that in the context of the rest of this article, "string" will be used as a
 * synonym for "a certain number of bytes of binary data"; no special encodings
 * are used and no knowledge about the content of the strings is implied.
 * <p>
 * See: https://github.com/ethereum/wiki/wiki/%5BEnglish%5D-RLP
 *
 * @author Roman Mandeleil
 * @since 01.04.2014
 */
public class RLP {

    private static final Logger logger = LoggerFactory.getLogger("rlp");

    public static final byte[] EMPTY_ELEMENT_RLP = encodeElement(new byte[0]);

    private static final int MAX_DEPTH = 16;

    /**
     * Allow for content up to size of 2^64 bytes *
     */
    private static final double MAX_ITEM_LENGTH = Math.pow(256, 8);

    private static final int SIZE_THRESHOLD = 56;
    private static final int OFFSET_SHORT_LIST = 0xc0;
    private static final int OFFSET_LONG_LIST = 0xf7;
    private static final int OFFSET_SHORT_ITEM = 0x80;
    private static final int OFFSET_LONG_ITEM = 0xb7;

    /**
     * The RLP encoding of a single empty value, also known as RLP null.
     */
    public static final Bytes NULL = encodeOne(Bytes.EMPTY);

    public static final Bytes EMPTY_LIST;

    // RLP encoding requires payloads to be less thatn 2^64 bytes in length
    // As a result, the longest RLP strings will have a prefix composed of 1 byte encoding the type
    // of string followed by at most 8 bytes describing the length of the string
    public static final int MAX_PREFIX_SIZE = 9;

    static {
        final BytesValueRLPOutput out = new BytesValueRLPOutput();
        out.startList();
        out.endList();
        EMPTY_LIST = out.encoded();
    }

    /**
     * Creates a new {@link RLPInput} suitable for decoding the provided RLP encoded value.
     *
     * <p>The created input is strict, in that exceptions will be thrown for any malformed input,
     * either by this method or by future reads from the returned input.
     *
     * @param encoded The RLP encoded data for which to create a {@link RLPInput}.
     * @return A newly created {@link RLPInput} to decode {@code encoded}.
     * @throws MalformedRLPInputException if {@code encoded} doesn't contain a single RLP encoded item
     *                                    (item that can be a list itself). Note that more deeply nested corruption/malformation of
     *                                    the input will not be detected by this method call, but will be later when the input is
     *                                    read.
     */
    public static RLPInput input(final Bytes encoded) {
        return input(encoded, false);
    }

    public static RLPInput input(final Bytes encoded, final boolean lenient) {
        return new BytesValueRLPInput(encoded, lenient);
    }

    /**
     * Creates a {@link RLPOutput}, pass it to the provided consumer for writing, and then return the
     * RLP encoded result of that writing.
     *
     * <p>This method is a convenience method that is mostly meant for use with class that have a
     * method to write to an {@link RLPOutput}. For instance:
     *
     * <pre>{@code
     * class Foo {
     *   public void writeTo(RLPOutput out) {
     *     //... write some data to out ...
     *   }
     * }
     *
     * Foo f = ...;
     * // RLP encode f
     * Bytes encoded = RLPs.encode(f::writeTo);
     * }</pre>
     *
     * @param writer A method that given an {@link RLPOutput}, writes some data to it.
     * @return The RLP encoding of the data written by {@code writer}.
     */
    public static Bytes encode(final Consumer<RLPOutput> writer) {
        final BytesValueRLPOutput out = new BytesValueRLPOutput();
        writer.accept(out);
        return out.encoded();
    }

    /**
     * Encodes a single binary value into RLP.
     *
     * <p>This is equivalent (but possibly more efficient) to:
     *
     * <pre>
     * {
     *   &#64;code
     *   BytesValueRLPOutput out = new BytesValueRLPOutput();
     *   out.writeBytes(value);
     *   return out.encoded();
     * }
     * </pre>
     *
     * <p>So note in particular that the value is encoded as is (and so not as a scalar in
     * particular).
     *
     * @param value The value to encode.
     * @return The RLP encoding containing only {@code value}.
     */
    public static Bytes encodeOne(final Bytes value) {
        if (RLPEncodingHelpers.isSingleRLPByte(value)) return value;

        final MutableBytes res = MutableBytes.create(RLPEncodingHelpers.elementSize(value));
        RLPEncodingHelpers.writeElement(value, res, 0);
        return res;
    }

    /**
     * Decodes an RLP-encoded value assuming it contains a single non-list item.
     *
     * <p>This is equivalent (but possibly more efficient) to:
     *
     * <pre>{@code
     * return input(value).readBytes();
     * }</pre>
     *
     * <p>So note in particular that the value is decoded as is (and so not as a scalar in
     * particular).
     *
     * @param encodedValue The encoded RLP value.
     * @return The single value encoded in {@code encodedValue}.
     * @throws RLPException if {@code encodedValue} is not a valid RLP encoding or if it does not
     *                      contains a single non-list item.
     */
    public static Bytes decodeOne(final Bytes encodedValue) {
        if (encodedValue.size() == 0) {
            throw new RLPException("Invalid empty input for RLP decoding");
        }

        final int prefix = encodedValue.get(0) & 0xFF;
        final RLPDecodingHelpers.Kind kind = RLPDecodingHelpers.Kind.of(prefix);
        if (kind.isList()) {
            throw new RLPException(format("Invalid input: value %s is an RLP list", encodedValue));
        }

        if (kind == RLPDecodingHelpers.Kind.BYTE_ELEMENT) {
            return encodedValue;
        }

        final int offset;
        final int size;
        if (kind == RLPDecodingHelpers.Kind.SHORT_ELEMENT) {
            offset = 1;
            size = prefix - 0x80;
        } else {
            final int sizeLength = prefix - 0xb7;
            if (1 + sizeLength > encodedValue.size()) {
                throw new RLPException(
                        format(
                                "Malformed RLP input: not enough bytes to read size of "
                                        + "long item in %s: expected %d bytes but only %d",
                                encodedValue, sizeLength + 1, encodedValue.size()));
            }
            offset = 1 + sizeLength;
            size = RLPDecodingHelpers.extractSize((index) -> encodedValue.get(index), 1, sizeLength);
        }
        if (offset + size != encodedValue.size()) {
            throw new RLPException(
                    format(
                            "Malformed RLP input: %s should be of size %d according to "
                                    + "prefix byte but of size %d",
                            encodedValue, offset + size, encodedValue.size()));
        }
        return encodedValue.slice(offset, size);
    }

    /**
     * Validates that the provided value is a valid RLP encoding.
     *
     * @param encodedValue The value to check.
     * @throws RLPException if {@code encodedValue} is not a valid RLP encoding.
     */
    public static void validate(final Bytes encodedValue) {
        final RLPInput in = input(encodedValue);
        while (!in.isDone()) {
            if (in.nextIsList()) {
                in.enterList();
            } else if (in.isEndOfCurrentList()) {
                in.leaveList();
            } else {
                // Skip does as much validation as can be done in general, without allocating anything.
                in.skipNext();
            }
        }
    }

    /**
     * Given a {@link Bytes} containing rlp-encoded data, determines the full length of the encoded
     * value (including the prefix) by inspecting the prefixed metadata.
     *
     * @param value the rlp-encoded byte string
     * @return the length of the encoded data, according to the prefixed metadata
     */
    public static int calculateSize(final Bytes value) {
        return RLPDecodingHelpers.rlpElementMetadata((index) -> value.get((int) index), value.size(), 0)
                .getEncodedSize();
    }


    private static byte decodeOneByteItem(byte[] data, int index) {
        // null item
        if ((data[index] & 0xFF) == OFFSET_SHORT_ITEM) {
            return (byte) (data[index] - OFFSET_SHORT_ITEM);
        }
        // single byte item
        if ((data[index] & 0xFF) < OFFSET_SHORT_ITEM) {
            return data[index];
        }
        // single byte item
        if ((data[index] & 0xFF) == OFFSET_SHORT_ITEM + 1) {
            return data[index + 1];
        }
        return 0;
    }

    public static int decodeInt(byte[] data, int index) {

        int value = 0;
        // NOTE: From RLP doc:
        // Ethereum integers must be represented in big endian binary form
        // with no leading zeroes (thus making the integer value zero be
        // equivalent to the empty byte array)

        if (data[index] == 0x00) {
            throw new RuntimeException("not a number");
        } else if ((data[index] & 0xFF) < OFFSET_SHORT_ITEM) {

            return data[index];

        } else if ((data[index] & 0xFF) <= OFFSET_SHORT_ITEM + Integer.BYTES) {

            byte length = (byte) (data[index] - OFFSET_SHORT_ITEM);
            byte pow = (byte) (length - 1);
            for (int i = 1; i <= length; ++i) {
                // << (8 * pow) == bit shift to 0 (*1), 8 (*256) , 16 (*65..)..
                value += (data[index + i] & 0xFF) << (8 * pow);
                pow--;
            }
        } else {

            // If there are more than 4 bytes, it is not going
            // to decode properly into an int.
            throw new RuntimeException("wrong decode attempt");
        }
        return value;
    }

    static short decodeShort(byte[] data, int index) {

        short value = 0;

        if (data[index] == 0x00) {
            throw new RuntimeException("not a number");
        } else if ((data[index] & 0xFF) < OFFSET_SHORT_ITEM) {

            return data[index];

        } else if ((data[index] & 0xFF) <= OFFSET_SHORT_ITEM + Short.BYTES) {

            byte length = (byte) (data[index] - OFFSET_SHORT_ITEM);
            byte pow = (byte) (length - 1);
            for (int i = 1; i <= length; ++i) {
                // << (8 * pow) == bit shift to 0 (*1), 8 (*256) , 16 (*65..)
                value += (data[index + i] & 0xFF) << (8 * pow);
                pow--;
            }
        } else {

            // If there are more than 2 bytes, it is not going
            // to decode properly into a short.
            throw new RuntimeException("wrong decode attempt");
        }
        return value;
    }

    public static long decodeLong(byte[] data, int index) {

        long value = 0;

        if (data[index] == 0x00) {
            throw new RuntimeException("not a number");
        } else if ((data[index] & 0xFF) < OFFSET_SHORT_ITEM) {

            return data[index];

        } else if ((data[index] & 0xFF) <= OFFSET_SHORT_ITEM + Long.BYTES) {

            byte length = (byte) (data[index] - OFFSET_SHORT_ITEM);
            byte pow = (byte) (length - 1);
            for (int i = 1; i <= length; ++i) {
                // << (8 * pow) == bit shift to 0 (*1), 8 (*256) , 16 (*65..)..
                value += (long) (data[index + i] & 0xFF) << (8 * pow);
                pow--;
            }
        } else {

            // If there are more than 8 bytes, it is not going
            // to decode properly into a long.
            throw new RuntimeException("wrong decode attempt");
        }
        return value;
    }

    private static String decodeStringItem(byte[] data, int index) {

        final byte[] valueBytes = decodeItemBytes(data, index);

        if (valueBytes.length == 0) {
            // shortcut
            return "";
        } else {
            return new String(valueBytes);
        }
    }

    public static BigInteger decodeBigInteger(byte[] data, int index) {

        final byte[] valueBytes = decodeItemBytes(data, index);

        if (valueBytes.length == 0) {
            // shortcut
            return BigInteger.ZERO;
        } else {
            BigInteger res = new BigInteger(1, valueBytes);
            return res;
        }
    }

    private static byte[] decodeByteArray(byte[] data, int index) {

        return decodeItemBytes(data, index);
    }

    private static int nextItemLength(byte[] data, int index) {

        if (index >= data.length)
            return -1;
        // [0xf8, 0xff]
        if ((data[index] & 0xFF) > OFFSET_LONG_LIST) {
            byte lengthOfLength = (byte) (data[index] - OFFSET_LONG_LIST);

            return calcLength(lengthOfLength, data, index);
        }
        // [0xc0, 0xf7]
        if ((data[index] & 0xFF) >= OFFSET_SHORT_LIST
                && (data[index] & 0xFF) <= OFFSET_LONG_LIST) {

            return (byte) ((data[index] & 0xFF) - OFFSET_SHORT_LIST);
        }
        // [0xb8, 0xbf]
        if ((data[index] & 0xFF) > OFFSET_LONG_ITEM
                && (data[index] & 0xFF) < OFFSET_SHORT_LIST) {

            byte lengthOfLength = (byte) (data[index] - OFFSET_LONG_ITEM);
            return calcLength(lengthOfLength, data, index);
        }
        // [0x81, 0xb7]
        if ((data[index] & 0xFF) > OFFSET_SHORT_ITEM
                && (data[index] & 0xFF) <= OFFSET_LONG_ITEM) {
            return (byte) ((data[index] & 0xFF) - OFFSET_SHORT_ITEM);
        }
        // [0x00, 0x80]
        if ((data[index] & 0xFF) <= OFFSET_SHORT_ITEM) {
            return 1;
        }
        return -1;
    }

    public static byte[] decodeIP4Bytes(byte[] data, int index) {

        int offset = 1;

        final byte[] result = new byte[4];
        for (int i = 0; i < 4; i++) {
            result[i] = decodeOneByteItem(data, index + offset);
            if ((data[index + offset] & 0xFF) > OFFSET_SHORT_ITEM)
                offset += 2;
            else
                offset += 1;
        }

        // return IP address
        return result;
    }

    public static int getFirstListElement(byte[] payload, int pos) {

        if (pos >= payload.length)
            return -1;

        // [0xf8, 0xff]
        if ((payload[pos] & 0xFF) > OFFSET_LONG_LIST) {
            byte lengthOfLength = (byte) (payload[pos] - OFFSET_LONG_LIST);
            return pos + lengthOfLength + 1;
        }
        // [0xc0, 0xf7]
        if ((payload[pos] & 0xFF) >= OFFSET_SHORT_LIST
                && (payload[pos] & 0xFF) <= OFFSET_LONG_LIST) {
            return pos + 1;
        }
        // [0xb8, 0xbf]
        if ((payload[pos] & 0xFF) > OFFSET_LONG_ITEM
                && (payload[pos] & 0xFF) < OFFSET_SHORT_LIST) {
            byte lengthOfLength = (byte) (payload[pos] - OFFSET_LONG_ITEM);
            return pos + lengthOfLength + 1;
        }
        return -1;
    }

    public static int getNextElementIndex(byte[] payload, int pos) {

        if (pos >= payload.length)
            return -1;

        // [0xf8, 0xff]
        if ((payload[pos] & 0xFF) > OFFSET_LONG_LIST) {
            byte lengthOfLength = (byte) (payload[pos] - OFFSET_LONG_LIST);
            int length = calcLength(lengthOfLength, payload, pos);
            return pos + lengthOfLength + length + 1;
        }
        // [0xc0, 0xf7]
        if ((payload[pos] & 0xFF) >= OFFSET_SHORT_LIST
                && (payload[pos] & 0xFF) <= OFFSET_LONG_LIST) {

            byte length = (byte) ((payload[pos] & 0xFF) - OFFSET_SHORT_LIST);
            return pos + 1 + length;
        }
        // [0xb8, 0xbf]
        if ((payload[pos] & 0xFF) > OFFSET_LONG_ITEM
                && (payload[pos] & 0xFF) < OFFSET_SHORT_LIST) {

            byte lengthOfLength = (byte) (payload[pos] - OFFSET_LONG_ITEM);
            int length = calcLength(lengthOfLength, payload, pos);
            return pos + lengthOfLength + length + 1;
        }
        // [0x81, 0xb7]
        if ((payload[pos] & 0xFF) > OFFSET_SHORT_ITEM
                && (payload[pos] & 0xFF) <= OFFSET_LONG_ITEM) {

            byte length = (byte) ((payload[pos] & 0xFF) - OFFSET_SHORT_ITEM);
            return pos + 1 + length;
        }
        // []0x80]
        if ((payload[pos] & 0xFF) == OFFSET_SHORT_ITEM) {
            return pos + 1;
        }
        // [0x00, 0x7f]
        if ((payload[pos] & 0xFF) < OFFSET_SHORT_ITEM) {
            return pos + 1;
        }
        return -1;
    }

    /**
     * Parse length of long item or list.
     * RLP supports lengths with up to 8 bytes long,
     * but due to java limitation it returns either encoded length
     * or {@link Integer#MAX_VALUE} in case if encoded length is greater
     *
     * @param lengthOfLength length of length in bytes
     * @param msgData        message
     * @param pos            position to parse from
     * @return calculated length
     */
    private static int calcLength(int lengthOfLength, byte[] msgData, int pos) {
        byte pow = (byte) (lengthOfLength - 1);
        int length = 0;
        for (int i = 1; i <= lengthOfLength; ++i) {

            int bt = msgData[pos + i] & 0xFF;
            int shift = 8 * pow;

            // no leading zeros are acceptable
            if (bt == 0 && length == 0) {
                throw new RuntimeException("RLP length contains leading zeros");
            }

            // return MAX_VALUE if index of highest bit is more than 31
            if (32 - Integer.numberOfLeadingZeros(bt) + shift > 31) {
                return Integer.MAX_VALUE;
            }

            length += bt << shift;
            pow--;
        }

        // check that length is in payload bounds
        verifyLength(length, msgData.length - pos - lengthOfLength);

        return length;
    }

    public static byte getCommandCode(byte[] data) {
        int index = getFirstListElement(data, 0);
        final byte command = data[index];
        return ((command & 0xFF) == OFFSET_SHORT_ITEM) ? 0 : command;
    }

    /**
     * Parse wire byte[] message into RLP elements
     *
     * @param msgData - raw RLP data
     * @param depthLimit - limits depth of decoding
     * @return rlpList
     * - outcome of recursive RLP structure
     */


    /**
     * Parse wire byte[] message into RLP elements
     *
     * @param msgData - raw RLP data
     * @return rlpList
     * - outcome of recursive RLP structure
     */


    /**
     * Decodes RLP with list without going deep after 1st level list
     * (actually, 2nd as 1st level is wrap only)
     *
     * So assuming you've packed several byte[] with {@link #encodeList(byte[]...)},
     * you could use this method to unpack them,
     * getting RLPList with RLPItem's holding byte[] inside
     * @param msgData rlp data
     * @return list of RLPItems
     */


    /**
     * Get exactly one message payload
     */


    /**
     * Compares supplied length information with maximum possible
     *
     * @param suppliedLength  Length info from header
     * @param availableLength Length of remaining object
     * @throws RuntimeException if supplied length is bigger than available
     */
    private static void verifyLength(int suppliedLength, int availableLength) {
        if (suppliedLength > availableLength) {
            throw new RuntimeException(String.format("Length parsed from RLP (%s bytes) is greater " +
                    "than possible size of data (%s bytes)", suppliedLength, availableLength));
        }
    }

    /**
     * Reads any RLP encoded byte-array and returns all objects as byte-array or list of byte-arrays
     *
     * @param data RLP encoded byte-array
     * @param pos  position in the array to start reading
     * @return DecodeResult encapsulates the decoded items as a single Object and the final read position
     */
    public static DecodeResult decode(byte[] data, int pos) {
        if (data == null || data.length < 1) {
            return null;
        }
        int prefix = data[pos] & 0xFF;
        if (prefix == OFFSET_SHORT_ITEM) {  // 0x80
            return new DecodeResult(pos + 1, ""); // means no length or 0
        } else if (prefix < OFFSET_SHORT_ITEM) {  // [0x00, 0x7f]
            return new DecodeResult(pos + 1, new byte[]{data[pos]}); // byte is its own RLP encoding
        } else if (prefix <= OFFSET_LONG_ITEM) {  // [0x81, 0xb7]
            int len = prefix - OFFSET_SHORT_ITEM; // length of the encoded bytes
            return new DecodeResult(pos + 1 + len, copyOfRange(data, pos + 1, pos + 1 + len));
        } else if (prefix < OFFSET_SHORT_LIST) {  // [0xb8, 0xbf]
            int lenlen = prefix - OFFSET_LONG_ITEM; // length of length the encoded bytes
            int lenbytes = ByteUtil.byteArrayToInt(copyOfRange(data, pos + 1, pos + 1 + lenlen)); // length of encoded bytes
            // check that length is in payload bounds
            verifyLength(lenbytes, data.length - pos - 1 - lenlen);
            return new DecodeResult(pos + 1 + lenlen + lenbytes, copyOfRange(data, pos + 1 + lenlen, pos + 1 + lenlen
                    + lenbytes));
        } else if (prefix <= OFFSET_LONG_LIST) {  // [0xc0, 0xf7]
            int len = prefix - OFFSET_SHORT_LIST; // length of the encoded list
            int prevPos = pos;
            pos++;
            return decodeList(data, pos, prevPos, len);
        } else if (prefix <= 0xFF) {  // [0xf8, 0xff]
            int lenlen = prefix - OFFSET_LONG_LIST; // length of length the encoded list
            int lenlist = ByteUtil.byteArrayToInt(copyOfRange(data, pos + 1, pos + 1 + lenlen)); // length of encoded bytes
            pos = pos + lenlen + 1; // start at position of first element in list
            int prevPos = lenlist;
            return decodeList(data, pos, prevPos, lenlist);
        } else {
            throw new RuntimeException("Only byte values between 0x00 and 0xFF are supported, but got: " + prefix);
        }
    }

    public static final class LList {
        private final byte[] rlp;
        private final int[] offsets = new int[32];
        private final int[] lens = new int[32];
        private int cnt;

        public LList(byte[] rlp) {
            this.rlp = rlp;
        }

        public byte[] getEncoded() {
            byte encoded[][] = new byte[cnt][];
            for (int i = 0; i < cnt; i++) {
                encoded[i] = encodeElement(getBytes(i));
            }
            return encodeList(encoded);
        }

        public void add(int off, int len, boolean isList) {
            offsets[cnt] = off;
            lens[cnt] = isList ? (-1 - len) : len;
            cnt++;
        }

        public byte[] getBytes(int idx) {
            int len = lens[idx];
            len = len < 0 ? (-len - 1) : len;
            byte[] ret = new byte[len];
            System.arraycopy(rlp, offsets[idx], ret, 0, len);
            return ret;
        }

        public LList getList(int idx) {
            return decodeLazyList(rlp, offsets[idx], -lens[idx] - 1);
        }

        public boolean isList(int idx) {
            return lens[idx] < 0;
        }

        public int size() {
            return cnt;
        }
    }

    public static LList decodeLazyList(byte[] data) {
        return decodeLazyList(data, 0, data.length).getList(0);
    }

    public static LList decodeLazyList(byte[] data, int pos, int length) {
        if (data == null || data.length < 1) {
            return null;
        }
        LList ret = new LList(data);
        int end = pos + length;

        while (pos < end) {
            int prefix = data[pos] & 0xFF;
            if (prefix == OFFSET_SHORT_ITEM) {  // 0x80
                ret.add(pos, 0, false); // means no length or 0
                pos++;
            } else if (prefix < OFFSET_SHORT_ITEM) {  // [0x00, 0x7f]
                ret.add(pos, 1, false); // means no length or 0
                pos++;
            } else if (prefix <= OFFSET_LONG_ITEM) {  // [0x81, 0xb7]
                int len = prefix - OFFSET_SHORT_ITEM; // length of the encoded bytes
                ret.add(pos + 1, len, false);
                pos += len + 1;
            } else if (prefix < OFFSET_SHORT_LIST) {  // [0xb8, 0xbf]
                int lenlen = prefix - OFFSET_LONG_ITEM; // length of length the encoded bytes
                int lenbytes = ByteUtil.byteArrayToInt(copyOfRange(data, pos + 1, pos + 1 + lenlen)); // length of encoded bytes
                // check that length is in payload bounds
                verifyLength(lenbytes, data.length - pos - 1 - lenlen);
                ret.add(pos + 1 + lenlen, lenbytes, false);
                pos += 1 + lenlen + lenbytes;
            } else if (prefix <= OFFSET_LONG_LIST) {  // [0xc0, 0xf7]
                int len = prefix - OFFSET_SHORT_LIST; // length of the encoded list
                ret.add(pos + 1, len, true);
                pos += 1 + len;
            } else if (prefix <= 0xFF) {  // [0xf8, 0xff]
                int lenlen = prefix - OFFSET_LONG_LIST; // length of length the encoded list
                int lenlist = ByteUtil.byteArrayToInt(copyOfRange(data, pos + 1, pos + 1 + lenlen)); // length of encoded bytes
                // check that length is in payload bounds
                verifyLength(lenlist, data.length - pos - 1 - lenlen);
                ret.add(pos + 1 + lenlen, lenlist, true);
                pos += 1 + lenlen + lenlist; // start at position of first element in list
            } else {
                throw new RuntimeException("Only byte values between 0x00 and 0xFF are supported, but got: " + prefix);
            }
        }
        return ret;
    }


    private static DecodeResult decodeList(byte[] data, int pos, int prevPos, int len) {
        // check that length is in payload bounds
        verifyLength(len, data.length - pos);

        List<Object> slice = new ArrayList<>();
        for (int i = 0; i < len; ) {
            // Get the next item in the data list and append it
            DecodeResult result = decode(data, pos);
            slice.add(result.getDecoded());
            // Increment pos by the amount bytes in the previous read
            prevPos = result.getPos();
            i += (prevPos - pos);
            pos = prevPos;
        }
        return new DecodeResult(pos, slice.toArray());
    }

    /* ******************************************************
     *                      ENCODING                        *
     * ******************************************************/

    /**
     * Turn Object into its RLP encoded equivalent of a byte-array
     * Support for String, Integer, BigInteger and Lists of any of these types.
     *
     * @param input as object or List of objects
     * @return byte[] RLP encoded
     */
    public static byte[] encode(Object input) {
        Value val = new Value(input);
        if (val.isList()) {
            List<Object> inputArray = val.asList();
            if (inputArray.isEmpty()) {
                return encodeLength(inputArray.size(), OFFSET_SHORT_LIST);
            }
            byte[] output = ByteUtil.EMPTY_BYTE_ARRAY;
            for (Object object : inputArray) {
                output = concatenate(output, encode(object));
            }
            byte[] prefix = encodeLength(output.length, OFFSET_SHORT_LIST);
            return concatenate(prefix, output);
        } else {
            byte[] inputAsBytes = toBytes(input);
            if (inputAsBytes.length == 1 && (inputAsBytes[0] & 0xff) <= 0x80) {
                return inputAsBytes;
            } else {
                byte[] firstByte = encodeLength(inputAsBytes.length, OFFSET_SHORT_ITEM);
                return concatenate(firstByte, inputAsBytes);
            }
        }
    }

    /**
     * Integer limitation goes up to 2^31-1 so length can never be bigger than MAX_ITEM_LENGTH
     */
    public static byte[] encodeLength(int length, int offset) {
        if (length < SIZE_THRESHOLD) {
            byte firstByte = (byte) (length + offset);
            return new byte[]{firstByte};
        } else if (length < MAX_ITEM_LENGTH) {
            byte[] binaryLength;
            if (length > 0xFF)
                binaryLength = ByteUtil.intToBytesNoLeadZeroes(length);
            else
                binaryLength = new byte[]{(byte) length};
            byte firstByte = (byte) (binaryLength.length + offset + SIZE_THRESHOLD - 1);
            return concatenate(new byte[]{firstByte}, binaryLength);
        } else {
            throw new RuntimeException("Input too long");
        }
    }


    public static byte[] encodeShort(short singleShort) {

        if ((singleShort & 0xFF) == singleShort)
            return encodeByte((byte) singleShort);
        else {
            return new byte[]{(byte) (OFFSET_SHORT_ITEM + 2),
                    (byte) (singleShort >> 8 & 0xFF),
                    (byte) (singleShort >> 0 & 0xFF)};
        }
    }

    public static byte[] encodeInt(int singleInt) {

        if ((singleInt & 0xFF) == singleInt)
            return encodeByte((byte) singleInt);
        else if ((singleInt & 0xFFFF) == singleInt)
            return encodeShort((short) singleInt);
        else if ((singleInt & 0xFFFFFF) == singleInt)
            return new byte[]{(byte) (OFFSET_SHORT_ITEM + 3),
                    (byte) (singleInt >>> 16),
                    (byte) (singleInt >>> 8),
                    (byte) singleInt};
        else {
            return new byte[]{(byte) (OFFSET_SHORT_ITEM + 4),
                    (byte) (singleInt >>> 24),
                    (byte) (singleInt >>> 16),
                    (byte) (singleInt >>> 8),
                    (byte) singleInt};
        }
    }

    public static byte[] encodeString(String srcString) {
        return encodeElement(srcString.getBytes());
    }


    public static int calcElementPrefixSize(byte[] srcData) {

        if (ByteUtil.isNullOrZeroArray(srcData))
            return 0;
        else if (ByteUtil.isSingleZero(srcData))
            return 0;
        else if (srcData.length == 1 && (srcData[0] & 0xFF) < 0x80) {
            return 0;
        } else if (srcData.length < SIZE_THRESHOLD) {
            return 1;
        } else {
            // length of length = BX
            // prefix = [BX, [length]]
            int tmpLength = srcData.length;
            byte byteNum = 0;
            while (tmpLength != 0) {
                ++byteNum;
                tmpLength = tmpLength >> 8;
            }

            return 1 + byteNum;
        }
    }


    public static byte[] encodeListHeader(int size) {

        if (size == 0) {
            return new byte[]{(byte) OFFSET_SHORT_LIST};
        }

        int totalLength = size;

        byte[] header;
        if (totalLength < SIZE_THRESHOLD) {

            header = new byte[1];
            header[0] = (byte) (OFFSET_SHORT_LIST + totalLength);
        } else {
            // length of length = BX
            // prefix = [BX, [length]]
            int tmpLength = totalLength;
            byte byteNum = 0;
            while (tmpLength != 0) {
                ++byteNum;
                tmpLength = tmpLength >> 8;
            }
            tmpLength = totalLength;

            byte[] lenBytes = new byte[byteNum];
            for (int i = 0; i < byteNum; ++i) {
                lenBytes[byteNum - 1 - i] = (byte) ((tmpLength >> (8 * i)) & 0xFF);
            }
            // first byte = F7 + bytes.length
            header = new byte[1 + lenBytes.length];
            header[0] = (byte) (OFFSET_LONG_LIST + byteNum);
            System.arraycopy(lenBytes, 0, header, 1, lenBytes.length);

        }

        return header;
    }


    public static byte[] encodeLongElementHeader(int length) {

        if (length < SIZE_THRESHOLD) {

            if (length == 0)
                return new byte[]{(byte) 0x80};
            else
                return new byte[]{(byte) (0x80 + length)};

        } else {

            // length of length = BX
            // prefix = [BX, [length]]
            int tmpLength = length;
            byte byteNum = 0;
            while (tmpLength != 0) {
                ++byteNum;
                tmpLength = tmpLength >> 8;
            }

            byte[] lenBytes = new byte[byteNum];
            for (int i = 0; i < byteNum; ++i) {
                lenBytes[byteNum - 1 - i] = (byte) ((length >> (8 * i)) & 0xFF);
            }

            // first byte = F7 + bytes.length
            byte[] header = new byte[1 + lenBytes.length];
            header[0] = (byte) (OFFSET_LONG_ITEM + byteNum);
            System.arraycopy(lenBytes, 0, header, 1, lenBytes.length);

            return header;
        }
    }

    public static byte[] encodeSet(Set<ByteArrayWrapper> data) {

        int dataLength = 0;
        Set<byte[]> encodedElements = new HashSet<>();
        for (ByteArrayWrapper element : data) {

            byte[] encodedElement = encodeElement(element.getData());
            dataLength += encodedElement.length;
            encodedElements.add(encodedElement);
        }

        byte[] listHeader = encodeListHeader(dataLength);

        byte[] output = new byte[listHeader.length + dataLength];

        System.arraycopy(listHeader, 0, output, 0, listHeader.length);

        int cummStart = listHeader.length;
        for (byte[] element : encodedElements) {
            System.arraycopy(element, 0, output, cummStart, element.length);
            cummStart += element.length;
        }

        return output;
    }


    public static byte[] wrapList(byte[]... data) {
        byte[][] elements = new byte[data.length][];
        for (int i = 0; i < data.length; i++) {
            elements[i] = encodeElement(data[i]);
        }
        return encodeList(elements);
    }


    /*
     *  Utility function to convert Objects into byte arrays
     */
    private static byte[] toBytes(Object input) {
        if (input instanceof byte[]) {
            return (byte[]) input;
        } else if (input instanceof String) {
            String inputString = (String) input;
            return inputString.getBytes();
        } else if (input instanceof Long) {
            Long inputLong = (Long) input;
            return (inputLong == 0) ? ByteUtil.EMPTY_BYTE_ARRAY : asUnsignedByteArray(BigInteger.valueOf(inputLong));
        } else if (input instanceof Integer) {
            Integer inputInt = (Integer) input;
            return (inputInt == 0) ? ByteUtil.EMPTY_BYTE_ARRAY : asUnsignedByteArray(BigInteger.valueOf(inputInt));
        } else if (input instanceof BigInteger) {
            BigInteger inputBigInt = (BigInteger) input;
            return (inputBigInt.equals(BigInteger.ZERO)) ? ByteUtil.EMPTY_BYTE_ARRAY : asUnsignedByteArray(inputBigInt);
        } else if (input instanceof Value) {
            Value val = (Value) input;
            return toBytes(val.asObj());
        }
        throw new RuntimeException("Unsupported type: Only accepting String, Integer and BigInteger for now");
    }


    private static byte[] decodeItemBytes(byte[] data, int index) {

        final int length = calculateItemLength(data, index);
        // [0x80]
        if (length == 0) {

            return new byte[0];

            // [0x00, 0x7f] - single byte with item
        } else if ((data[index] & 0xFF) < OFFSET_SHORT_ITEM) {

            byte[] valueBytes = new byte[1];
            System.arraycopy(data, index, valueBytes, 0, 1);
            return valueBytes;

            // [0x01, 0xb7] - 1-55 bytes item
        } else if ((data[index] & 0xFF) <= OFFSET_LONG_ITEM) {

            byte[] valueBytes = new byte[length];
            System.arraycopy(data, index + 1, valueBytes, 0, length);
            return valueBytes;

            // [0xb8, 0xbf] - 56+ bytes item
        } else if ((data[index] & 0xFF) > OFFSET_LONG_ITEM
                && (data[index] & 0xFF) < OFFSET_SHORT_LIST) {

            byte lengthOfLength = (byte) (data[index] - OFFSET_LONG_ITEM);
            byte[] valueBytes = new byte[length];
            System.arraycopy(data, index + 1 + lengthOfLength, valueBytes, 0, length);
            return valueBytes;
        } else {
            throw new RuntimeException("wrong decode attempt");
        }
    }


    private static int calculateItemLength(byte[] data, int index) {

        // [0xb8, 0xbf] - 56+ bytes item
        if ((data[index] & 0xFF) > OFFSET_LONG_ITEM
                && (data[index] & 0xFF) < OFFSET_SHORT_LIST) {

            byte lengthOfLength = (byte) (data[index] - OFFSET_LONG_ITEM);
            return calcLength(lengthOfLength, data, index);

            // [0x81, 0xb7] - 0-55 bytes item
        } else if ((data[index] & 0xFF) > OFFSET_SHORT_ITEM
                && (data[index] & 0xFF) <= OFFSET_LONG_ITEM) {

            return (byte) (data[index] - OFFSET_SHORT_ITEM);

            // [0x80] - item = 0 itself
        } else if ((data[index] & 0xFF) == OFFSET_SHORT_ITEM) {

            return (byte) 0;

            // [0x00, 0x7f] - 1 byte item, no separate length representation
        } else if ((data[index] & 0xFF) < OFFSET_SHORT_ITEM) {

            return (byte) 1;

        } else {
            throw new RuntimeException("wrong decode attempt");
        }
    }
}

