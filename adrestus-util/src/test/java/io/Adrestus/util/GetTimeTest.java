package io.Adrestus.util;

import org.junit.jupiter.api.Test;

import java.text.ParseException;

public class GetTimeTest {

    @Test
    public void test() throws ParseException {
        GetTime.GetTimestampFromString("2022-11-18 15:01:29.304");
    }
}
