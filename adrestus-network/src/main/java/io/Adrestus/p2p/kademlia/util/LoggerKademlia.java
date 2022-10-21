package io.Adrestus.p2p.kademlia.util;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import org.slf4j.LoggerFactory;

public class LoggerKademlia {

    public static void setLevelOFF(){
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger rootLogger = loggerContext.getLogger("io.netty");
        rootLogger.setLevel(ch.qos.logback.classic.Level.OFF);
    }

}
