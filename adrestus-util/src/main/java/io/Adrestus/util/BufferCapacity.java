package io.Adrestus.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BufferCapacity {
    private static Logger LOG = LoggerFactory.getLogger(BufferCapacity.class);

    public static int nextPowerOf2(int maxQueueCapacity) {
        int adjustedCapacity = maxQueueCapacity == 1 ? 1 : Integer.highestOneBit(maxQueueCapacity - 1) * 2;
        if (adjustedCapacity != maxQueueCapacity) {
            LOG.warn(String.format("Adjusting %d to nearest power of 2 ->  %d", maxQueueCapacity, adjustedCapacity));
        }
        return adjustedCapacity;
    }
}
