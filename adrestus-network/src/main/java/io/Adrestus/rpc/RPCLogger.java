package io.Adrestus.rpc;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import org.slf4j.LoggerFactory;

public class RPCLogger {

    public static void setLevelOff() {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger rootLogger = loggerContext.getLogger("io.activej");
        Logger rootLogger2 = loggerContext.getLogger("io.Adrestus.network");
        rootLogger.setLevel(ch.qos.logback.classic.Level.OFF);
        rootLogger2.setLevel(ch.qos.logback.classic.Level.OFF);
    }

}
