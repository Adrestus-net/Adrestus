package io.Adrestus.network;

import io.Adrestus.config.ConsensusConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

import static io.Adrestus.config.ConsensusConfiguration.*;

public class ConsensusServer {

    private static Logger LOG = LoggerFactory.getLogger(ConsensusServer.class);

    private final ZContext ctx;
    private final String IP;
    private final ZMQ.Socket publisher;
    private final ZMQ.Socket collector;


    private ZMQ.Socket connected;
    private volatile boolean terminate;
    private Timer timer;
    private ConnectedTaskTimeout task;
    private CountDownLatch latch;
    private int peers_not_connected;


    public ConsensusServer(String IP, CountDownLatch latch) {
        this.peers_not_connected = 0;
        this.terminate = false;
        this.IP = IP;
        this.latch = latch;
        this.ctx = new ZContext();
        this.publisher = ctx.createSocket(SocketType.PUB);
        this.collector = ctx.createSocket(SocketType.PULL);
        this.connected = ctx.createSocket(SocketType.REP);

        this.publisher.setHeartbeatIvl(2);


        this.publisher.bind("tcp://" + IP + ":" + PUBLISHER_PORT);
        this.collector.bind("tcp://" + IP + ":" + COLLECTOR_PORT);
        this.connected.bind("tcp://" + IP + ":" + CONNECTED_PORT);

        this.collector.setReceiveTimeOut(CONSENSUS_TIMEOUT);
        this.publisher.setSendTimeOut(CONSENSUS_TIMEOUT);
        this.connected.setSendTimeOut(CONSENSUS_TIMEOUT);

        this.timer = new Timer(ConsensusConfiguration.CONSENSUS);
        this.task = new ConnectedTaskTimeout();
        this.BlockUntilConnected();
    }

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

    public void BlockUntilConnected() {
        this.timer.scheduleAtFixedRate(task, CONSENSUS_TIMEOUT, CONSENSUS_TIMEOUT);
        while (latch.getCount() > 0 && !terminate) {
            String rec = receiveStringData();
            System.out.println(rec);
            connected.send(HEARTBEAT_MESSAGE.getBytes(StandardCharsets.UTF_8));
            latch.countDown();
            setPeers_not_connected((int) latch.getCount());
        }
        task.cancel();
        timer.purge();
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
        publisher.send(data, 0);
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

    public String receiveStringData() {
        String data = "";
        try {
            byte[] recv = connected.recv();
            return new String(recv, StandardCharsets.UTF_8);
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

    public int getPeers_not_connected() {
        return peers_not_connected;
    }

    public void setPeers_not_connected(int peers_not_connected) {
        this.peers_not_connected = peers_not_connected;
    }

    public void close() {
        this.publisher.close();
        this.collector.close();
        this.ctx.close();
    }

    protected final class ConnectedTaskTimeout extends TimerTask {


        @Override
        public void run() {
            LOG.info("ConnectedTaskTimeout Terminated!!!");
            terminate = true;
            setPeers_not_connected((int) latch.getCount());
            while (latch.getCount() > 0) {
                latch.countDown();
            }
            cancel();
        }

        @Override
        public boolean cancel() {
            super.cancel();
            return true;
        }
    }
}
