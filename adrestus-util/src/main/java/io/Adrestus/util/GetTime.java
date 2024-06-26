package io.Adrestus.util;

import lombok.SneakyThrows;
import org.apache.commons.net.ntp.TimeStamp;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class GetTime {
    private static final String FORMAT_STRING = "yyyy-MM-dd HH:mm:ss.SSS";
    private static final int ONE_MINUTE = 60 * 1000;
    private static final int TRANSACTION_BLOCK_DELAY = 1000;

    private static long ExtractUTCTimestamp() {
        try {
            return TimeStamp.getCurrentTime().getTime();
        } catch (Exception e) {
            return System.currentTimeMillis();
        }
    }

    public static String GetTimeStampInString() {
        SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_STRING);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        Timestamp timestamp = new Timestamp(ExtractUTCTimestamp());
        return sdf.format(timestamp);
    }

    public static Timestamp GetTimeStamp() {
        Timestamp timestamp = new Timestamp(ExtractUTCTimestamp());
        return timestamp;
    }

    @SneakyThrows
    public static Timestamp GetTimeStampWithDelay() {
        Timestamp timestamp = new Timestamp(ExtractUTCTimestamp() + ONE_MINUTE);
        SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_STRING);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date parsedDate = sdf.parse(timestamp.toString());
        Timestamp ts = new java.sql.Timestamp(parsedDate.getTime());
        return ts;
    }

    public static Timestamp GetTimeStampWithDelay(Timestamp timestamp) {
        Long milliseconds = timestamp.getTime() + TRANSACTION_BLOCK_DELAY;
        Timestamp updatedTimestamp = new Timestamp(milliseconds);
        return updatedTimestamp;
    }

    public static Timestamp GetTimestampFromString(String parseDate) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_STRING);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date parsedDate = sdf.parse(parseDate);
        Timestamp timestamp = new java.sql.Timestamp(parsedDate.getTime());
        return timestamp;
    }

    public static boolean CheckIfTimestampIsUnderOneMinute(Timestamp timestamp) {
        long tenAgo = System.currentTimeMillis() - ONE_MINUTE;

        if (tenAgo < timestamp.getTime())
            return true;

        return false;
    }
}
