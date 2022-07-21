package io.Adrestus.util;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class GetTime {
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static String GetTimeStamp(){
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        return sdf.format(timestamp);
    }
}
