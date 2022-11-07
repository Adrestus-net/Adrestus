package io.Adrestus.crypto;

import com.google.common.net.InetAddresses;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HashingTest {

    @Test
    public void test1() {
        Random random = new Random();
        int count = 10000;

        while (count > 0) {
            String ipString = InetAddresses.fromInteger(random.nextInt()).getHostAddress();
            String res = HashUtil.convertIPtoHex(ipString, 4);

            BigInteger a = new BigInteger(res);
            count--;
            assertEquals(4, res.length());
        }
    }
}
