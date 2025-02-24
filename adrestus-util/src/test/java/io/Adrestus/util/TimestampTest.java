package io.Adrestus.util;

import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.time.Instant;

public class TimestampTest {
    @Test
    public void test_timestamp() throws ParseException, InterruptedException {

        int count = 1000000;
        while (count > 0) {
            String val = GetTime.GetTimeStampInString();
            Instant t = GetTime.GetTimestampFromString(val);
            count--;
        }
    }
}
