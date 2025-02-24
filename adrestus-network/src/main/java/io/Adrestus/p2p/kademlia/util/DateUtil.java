package io.Adrestus.p2p.kademlia.util;


import java.time.Instant;

public class DateUtil {
    private DateUtil() {
    }

    public static Instant getDateOfSecondsAgo(int seconds) {
        return Instant.now()
                .minusSeconds(seconds * 1000L);
    }
}
