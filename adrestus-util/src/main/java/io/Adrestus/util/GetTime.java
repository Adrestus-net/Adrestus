package io.Adrestus.util;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;

import java.net.InetAddress;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class GetTime {
    private static final String FORMAT_STRING = "yyyy-MM-dd HH:mm:ss.SSS";
    private static final String TIME_SERVER = "pool.ntp.org";

    private static long ExtractUTCTimestamp() {
        try {
            NTPUDPClient timeClient = new NTPUDPClient();
            timeClient.setDefaultTimeout(1_000);
            InetAddress inetAddress = InetAddress.getByName(TIME_SERVER);
            TimeInfo timeInfo = timeClient.getTime(inetAddress);
            long returnTime = timeInfo.getReturnTime();
            return returnTime;
        } catch (Exception e) {
            System.out.println("time problem");
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

    public static Timestamp GetTimestampFromString(String parseDate) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_STRING);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date parsedDate = sdf.parse(parseDate);
        Timestamp timestamp = new java.sql.Timestamp(parsedDate.getTime());
        return timestamp;
    }
}
