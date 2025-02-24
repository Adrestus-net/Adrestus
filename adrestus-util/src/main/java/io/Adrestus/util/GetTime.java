package io.Adrestus.util;

import lombok.SneakyThrows;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class GetTime {
    private static final String FORMAT_STRING = "yyyy-MM-dd HH:mm:ss.SSS";
    private static final int ONE_MINUTE = 60 * 1000;
    private static final int TRANSACTION_BLOCK_DELAY = 1000;


    // Thread-safe formatter (create once, reuse forever)
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_INSTANT.withZone(ZoneOffset.UTC);
    private static final DateTimeFormatter FORMATTER2 = DateTimeFormatter.ofPattern(FORMAT_STRING).withZone(ZoneOffset.UTC);


    public static String GetTimeStampInString() {
        return FORMATTER.format(Instant.now());
    }

    public static Instant GetTimeStamp() {
        return Instant.now();
    }

    @SneakyThrows
    public static Instant GetTimeStampWithDelay() {
        return Instant.now().plusMillis(ONE_MINUTE);
    }

    public static Instant GetTimeStampWithDelay(Instant timestamp) {
        return timestamp.plusMillis(TRANSACTION_BLOCK_DELAY);
    }

    public static Instant GetTimestampFromString(String parseDate) {
        try {
            return FORMATTER.parse(parseDate, Instant::from);
        } catch (Exception e) {
            return FORMATTER2.parse(parseDate, Instant::from);
        }
    }

    public static boolean CheckIfTimestampIsUnderOneMinute(Instant timestamp) {
        long tenAgo = Instant.now().toEpochMilli() - ONE_MINUTE;
        return tenAgo < timestamp.toEpochMilli();
    }
}
