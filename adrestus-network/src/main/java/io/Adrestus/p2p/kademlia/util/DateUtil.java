package io.Adrestus.p2p.kademlia.util;

import java.util.Date;

import java.util.Date;

public class DateUtil {
    private DateUtil(){}

    public static Date getDateOfSecondsAgo(int seconds){
        return new Date(new Date().getTime() - (seconds * 1000L));
    }
}
