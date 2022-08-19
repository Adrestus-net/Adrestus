package io.Adrestus.util;

import static com.google.common.base.Preconditions.checkState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.MutableBytes;

abstract class AbstractRLPOutput implements RLPOutput {
    private static final Bytes LIST_MARKER = Bytes.wrap(new byte[0]);

    private final List<Bytes> values = new ArrayList<>();
    // For every value i in values, rlpEncoded.get(i) will be true only if the value stored is an
    // already encoded item.
    private final BitSet rlpEncoded = new BitSet();

    // First element is the total size of everything (the encoding may be a single non-list item, so
    // this handles that case more easily; we need that value to size out final output). Following
    // elements holds the size of the payload of the ith list in 'values'.
    private int[] payloadSizes = new int[8];
    private int listsCount = 1; // number of lists current in 'values' + 1.

    private int[] parentListStack = new int[4];
    private int stackSize = 1;

    private int currentList() {
        return parentListStack[stackSize - 1];
    }

    @Override
    public void writeBytes(final Bytes v) {
        checkState(
                stackSize > 1 || values.isEmpty(), "Terminated RLP output, cannot add more elements");
        values.add(v);
        payloadSizes[currentList()] += RLPEncodingHelpers.elementSize(v);
    }

    @Override
    public void writeRaw(final Bytes v) {
        checkState(
                stackSize > 1 || values.isEmpty(), "Terminated RLP output, cannot add more elements");
        values.add(v);
        // Mark that last value added as already encoded.
        rlpEncoded.set(values.size() - 1);
        payloadSizes[currentList()] += v.size();
    }

    @Override
    public void startList() {
        values.add(LIST_MARKER);
        ++listsCount; // we'll add a new element to payloadSizes
        ++stackSize; // and to the list stack.

        // Resize our lists if necessary.
        if (listsCount > payloadSizes.length) {
            payloadSizes = Arrays.copyOf(payloadSizes, (payloadSizes.length * 3) / 2);
        }
        if (stackSize > parentListStack.length) {
            parentListStack = Arrays.copyOf(parentListStack, (parentListStack.length * 3) / 2);
        }

        // The new current list size is store in the slot we just made room for by incrementing
        // listsCount
        parentListStack[stackSize - 1] = listsCount - 1;
    }

    @Override
    public void endList() {
        checkState(stackSize > 1, "LeaveList() called with no prior matching startList()");

        final int current = currentList();
        final int finishedListSize = RLPEncodingHelpers.listSize(payloadSizes[current]);
        --stackSize;

        // We just finished an item of our parent list, add it to that parent list size now.
        final int newCurrent = currentList();
        payloadSizes[newCurrent] += finishedListSize;
    }

    /**
     * Computes the final encoded data size.
     *
     * @return The size of the RLP-encoded data written to this output.
     * @throws IllegalStateException if some opened list haven't been closed (the output is not valid
     *     as is).
     */
    public int encodedSize() {
        checkState(stackSize == 1, "A list has been entered (startList()) but not left (endList())");
        return payloadSizes[0];
    }

    /**
     * Write the rlp encoded value to the provided {@link MutableBytes}
     *
     * @param mutableBytes the value to which the rlp-data will be written
     */
    public void writeEncoded(final MutableBytes mutableBytes) {
        // Special case where we encode only a single non-list item (note that listsCount is initially
        // set to 1, so listsCount == 1 really mean no list explicitly added to the output).
        if (listsCount == 1) {
            // writeBytes make sure we cannot have more than 1 value without a list
            assert values.size() == 1;
            final Bytes value = values.get(0);

            final int finalOffset;
            // Single non-list value.
            if (rlpEncoded.get(0)) {
                value.copyTo(mutableBytes, 0);
                finalOffset = value.size();
            } else {
                finalOffset = RLPEncodingHelpers.writeElement(value, mutableBytes, 0);
            }
            checkState(
                    finalOffset == mutableBytes.size(),
                    "Expected single element RLP encode to be of size %s but was of size %s.",
                    mutableBytes.size(),
                    finalOffset);
            return;
        }

        int offset = 0;
        int listIdx = 0;
        for (int i = 0; i < values.size(); i++) {
            final Bytes value = values.get(i);
            if (value == LIST_MARKER) {
                final int payloadSize = payloadSizes[++listIdx];
                offset = RLPEncodingHelpers.writeListHeader(payloadSize, mutableBytes, offset);
            } else if (rlpEncoded.get(i)) {
                value.copyTo(mutableBytes, offset);
                offset += value.size();
            } else {
                offset = RLPEncodingHelpers.writeElement(value, mutableBytes, offset);
            }
        }

        checkState(
                offset == mutableBytes.size(),
                "Expected RLP encoding to be of size %s but was of size %s.",
                mutableBytes.size(),
                offset);
    }
}
