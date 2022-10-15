package io.Adrestus.util;

import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.text.ParseException;

public class TimestampTest {
    @Test
    public void test_timestamp() throws ParseException, InterruptedException {

        int count=1000000;
        while (count>0){
            String val=GetTime.GetTimeStampInString();
            Timestamp t=GetTime.GetTimestampFromString(val);
            count--;
        }
    }
}
