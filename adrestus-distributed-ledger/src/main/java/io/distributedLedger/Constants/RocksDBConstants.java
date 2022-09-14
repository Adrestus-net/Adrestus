package io.distributedLedger.Constants;

public final class RocksDBConstants {
    public static int MAX_OPEN_FILES = 1024;
    public static int BLOCK_SIZE = 4 * 1024;
    public static int WRITE_BUFFER_SIZE = 8 * 1024 * 1024;
    public static int READ_BUFFER_SIZE = 8 * 1024 * 1024;
    public static int CACHE_SIZE = 16 * 1024 * 1024;

    public static int BYTES_PER_SYNC = 1024 * 1024;
    public static int OPTIMIZE_LEVEL_STYLE_COMPACTION = 128 * 1024 * 1024;
    public static int MAX_BACKGROUND_COMPACTIONS = Math.min(Runtime.getRuntime().availableProcessors(), 8);
    public static int MAX_BACKGROUND_FLUSHES = 2;
    public static int BLOOMFILTER_BITS_PER_KEY = 10;
    public static int MIN_WRITE_BUFFER_NUMBER_TOMERGE = 4;
    public static int LEVEL0_STOP_WRITES_TRIGGER = 16;
    public static int LEVEL0_SLOWDOWN_WRITES_TRIGGER = 8;
}
