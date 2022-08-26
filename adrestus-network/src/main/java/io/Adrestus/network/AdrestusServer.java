package io.Adrestus.network;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import static io.Adrestus.config.ConsensusConfiguration.*;

public class AdrestusServer {

    private static Logger LOG = LoggerFactory.getLogger(AdrestusServer.class);

    private final ZContext ctx;
    private final String IP;
    private final ZMQ.Socket publisher;
    private final ZMQ.Socket collector;


    public AdrestusServer(String IP) {
        this.IP = IP;
        this.ctx = new ZContext();
        this.publisher = ctx.createSocket(SocketType.PUB);
        this.collector = ctx.createSocket(SocketType.PULL);

        this.publisher.bind("tcp://" + IP + ":" + PUBLISHER_PORT);
        this.collector.bind("tcp://" + IP + ":" + COLLECTOR_PORT);
        this.collector.setReceiveTimeOut(CONSENSUS_TIMEOUT);
    }

    public void publishMessage(byte[] data) {
        publisher.send(data);
    }

    public byte[] receiveData() {
        byte[] data = null;
        try {
            data = collector.recv();
        } catch (Exception e) {
            e.printStackTrace();
            LOG.info("Socket Closed");
        }
        return data;
    }

    public static Logger getLOG() {
        return LOG;
    }

    public static void setLOG(Logger LOG) {
        AdrestusServer.LOG = LOG;
    }

    public ZContext getCtx() {
        return ctx;
    }

    public String getIP() {
        return IP;
    }

    public ZMQ.Socket getPublisher() {
        return publisher;
    }

    public ZMQ.Socket getCollector() {
        return collector;
    }

    public void close() {
        this.publisher.close();
        this.collector.close();
        this.ctx.close();
    }
}
