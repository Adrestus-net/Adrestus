package io.Adrestus.util;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.MutableBytes;

/**
 * An {@link RLPOutput} that writes RLP encoded data to a {@link Bytes}.
 */
public class BytesValueRLPOutput extends AbstractRLPOutput {
    /**
     * Computes the final encoded data.
     *
     * @return A value containing the data written to this output RLP-encoded.
     */
    public Bytes encoded() {
        final int size = encodedSize();
        if (size == 0) {
            return Bytes.EMPTY;
        }

        final MutableBytes output = MutableBytes.create(size);
        writeEncoded(output);
        return output;
    }
}
