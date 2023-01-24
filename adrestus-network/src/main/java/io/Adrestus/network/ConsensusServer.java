package io.Adrestus.network;

import io.Adrestus.config.ConsensusConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

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


        this.publisher.setHWM(10000);


        this.publisher.bind("tcp://" + IP + ":" + PUBLISHER_PORT);
        this.collector.bind("tcp://" + IP + ":" + COLLECTOR_PORT);
        this.connected.bind("tcp://" + IP + ":" + CONNECTED_PORT);

        this.collector.setReceiveTimeOut(CONSENSUS_COLLECTED_TIMEOUT);
        this.publisher.setSendTimeOut(CONSENSUS_PUBLISHER_TIMEOUT);

        this.connected.setSendTimeOut(CONSENSUS_CONNECTED_TIMEOUT);
        this.connected.setReceiveTimeOut(CONSENSUS_CONNECTED_TIMEOUT);

        this.timer = new Timer(ConsensusConfiguration.CONSENSUS);
        this.task = new ConnectedTaskTimeout();
        this.BlockUntilConnected();
    }

    public ConsensusServer(String IP, CountDownLatch latch,int collector_timeout,int connected_timeout) {
        this.peers_not_connected = 0;
        this.terminate = false;
        this.IP = IP;
        this.latch = latch;
        this.ctx = new ZContext();
        this.publisher = ctx.createSocket(SocketType.PUB);
        this.collector = ctx.createSocket(SocketType.PULL);
        this.connected = ctx.createSocket(SocketType.REP);


        this.publisher.setHWM(10000);


        this.publisher.bind("tcp://" + IP + ":" + PUBLISHER_PORT);
        this.collector.bind("tcp://" + IP + ":" + COLLECTOR_PORT);
        this.connected.bind("tcp://" + IP + ":" + CONNECTED_PORT);

        this.collector.setReceiveTimeOut(collector_timeout);
        this.publisher.setSendTimeOut(CONSENSUS_TIMEOUT);
        this.connected.setSendTimeOut(connected_timeout);
        this.connected.setReceiveTimeOut(connected_timeout);

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
        //this.IP = IPFinder.getExternalIP();
        this.IP = IPFinder.getLocalIP();
        this.ctx = new ZContext();
        this.publisher = ctx.createSocket(SocketType.PUB);
        this.collector = ctx.createSocket(SocketType.PULL);

        this.publisher.bind("tcp://" + IP + ":" + PUBLISHER_PORT);
        this.collector.bind("tcp://" + IP + ":" + COLLECTOR_PORT);
        this.publisher.setSendTimeOut(CONSENSUS_TIMEOUT);
        this.collector.setReceiveTimeOut(CONSENSUS_TIMEOUT);
    }

    public void BlockUntilConnected() {
        this.timer = new Timer(ConsensusConfiguration.CONSENSUS);
        this.task = new ConnectedTaskTimeout();
        this.timer.scheduleAtFixedRate(task, CONSENSUS_TIMEOUT, CONSENSUS_TIMEOUT);
        int counter= (int) latch.getCount();
        while (latch.getCount() > 0 && !terminate) {
            String rec = receiveStringData();
            System.out.println(rec);
            if(!rec.equals("")) {
                connected.send(HEARTBEAT_MESSAGE.getBytes(StandardCharsets.UTF_8));
                counter--;
                setPeers_not_connected(counter);
            }
            if(!terminate) {
                latch.countDown();
            }
        }
        task.cancel();
        timer.purge();
    }

    public void PollOut() {
        ZMQ.Poller poller = ctx.createPoller(1);
        poller.register(this.publisher, ZMQ.Poller.POLLOUT);
        int rc = -1;
        while (rc == -1) {
            rc = poller.poll(15000);
        }
        poller.pollout(0);
    }


    public void publishMessage(byte[] data) {
        publisher.send(data, 0);
    }

    public byte[] receiveData() {
        byte[] data = null;
        try {
            data = collector.recv(0);
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
            LOG.info("receiveStringData: Socket Closed");
            connected.close();
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
        this.connected.close();
        this.ctx.close();
    }

    protected final class ConnectedTaskTimeout extends TimerTask {


        @Override
        public void run() {
            LOG.info("ConnectedTaskTimeout Terminated!!!");
            terminate = true;
            int peers= (int) latch.getCount();
            while (latch.getCount() > 0) {
                latch.countDown();
            }
            setPeers_not_connected(peers);
            cancel();
        }

        @Override
        public boolean cancel() {
            super.cancel();
            return true;
        }
    }
}
