package io.Adrestus.rpc;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import org.slf4j.LoggerFactory;


public class RPCLogger {
    private static volatile RPCLogger instance;

    private RPCLogger() {
        if (instance != null) {
            throw new IllegalStateException("Already initialized.");
        }
        setLevelOff();
    }

    public static RPCLogger getInstance() {

        var result = instance;
        if (result == null) {
            synchronized (RPCLogger.class) {
                result = instance;
                if (result == null) {
                    result = new RPCLogger();
                    instance = result;
                }
            }
        }
        return result;
    }
    private static void setLevelOff() {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger rootLogger = loggerContext.getLogger("io.activej");
        Logger rootLogger2 = loggerContext.getLogger("io.Adrestus.network");
        Logger rootLogger3 = loggerContext.getLogger("org.apache.kafka");
        Logger rootLogger4 = loggerContext.getLogger("org.apache.zookeeper");
        Logger rootLogger5 = loggerContext.getLogger("kafka");
        Logger rootLogger6 = loggerContext.getLogger("state.change.logger");
        rootLogger.setLevel(ch.qos.logback.classic.Level.OFF);
        rootLogger2.setLevel(ch.qos.logback.classic.Level.OFF);
        rootLogger3.setLevel(ch.qos.logback.classic.Level.OFF);
        rootLogger4.setLevel(ch.qos.logback.classic.Level.OFF);
        rootLogger5.setLevel(ch.qos.logback.classic.Level.OFF);
        rootLogger6.setLevel(ch.qos.logback.classic.Level.OFF);
    }


}
