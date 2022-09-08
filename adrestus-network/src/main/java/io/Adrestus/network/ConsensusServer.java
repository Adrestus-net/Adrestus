package io.Adrestus.network;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;


import java.net.InetSocketAddress;
import java.net.Socket;


import static io.Adrestus.config.ConsensusConfiguration.*;

public class ConsensusServer {

    private static Logger LOG = LoggerFactory.getLogger(ConsensusServer.class);

    private final ZContext ctx;
    private final String IP;
    private final ZMQ.Socket publisher;
    private final ZMQ.Socket collector;


    public ConsensusServer(String IP) {
        this.IP = IP;
        this.ctx = new ZContext();
        this.publisher = ctx.createSocket(SocketType.PUB);
        this.publisher.setHeartbeatIvl(2);
        this.collector = ctx.createSocket(SocketType.PULL);

        this.publisher.bind("tcp://" + IP + ":" + PUBLISHER_PORT);
        this.collector.bind("tcp://" + IP + ":" + COLLECTOR_PORT);
        this.collector.setReceiveTimeOut(CONSENSUS_TIMEOUT);
        this.publisher.setSendTimeOut(CONSENSUS_TIMEOUT);
    }

    public ConsensusServer() {
        this.IP = findIP();
        this.ctx = new ZContext();
        this.publisher = ctx.createSocket(SocketType.PUB);
        this.collector = ctx.createSocket(SocketType.PULL);

        this.publisher.bind("tcp://" + IP + ":" + PUBLISHER_PORT);
        this.collector.bind("tcp://" + IP + ":" + COLLECTOR_PORT);
        this.publisher.setSendTimeOut(CONSENSUS_TIMEOUT);
        this.collector.setReceiveTimeOut(CONSENSUS_TIMEOUT);
    }


    private String findIP() {
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress("google.com", 80));
            return socket.getLocalAddress().getHostAddress();
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new IllegalArgumentException("Make sure you intern connection is working");
    }

    public void publishMessage(byte[] data) {
        publisher.send(data,1);
    }

    public byte[] receiveData() {
        byte[] data = null;
        try {
            data = collector.recv();
        } catch (Exception e) {
            LOG.info("Socket Closed");
        }
        return data;
    }

    public static Logger getLOG() {
        return LOG;
    }

    public static void setLOG(Logger LOG) {
        ConsensusServer.LOG = LOG;
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
