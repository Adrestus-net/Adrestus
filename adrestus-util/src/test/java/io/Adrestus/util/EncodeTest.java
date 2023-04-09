package io.Adrestus.util;

import org.junit.jupiter.api.Test;

public class EncodeTest {
    private final SerializationUtil<String> wrapper = new SerializationUtil<String>(String.class);

    @Test
    public void test(){
        String a="test";
        wrapper.encodeNotOptimalPrevious(a,9626);
    }
}
