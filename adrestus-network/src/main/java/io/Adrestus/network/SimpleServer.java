package io.Adrestus.network;

import io.Adrestus.config.ConsensusConfiguration;
import org.apache.tuweni.bytes.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ.Socket;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

import static io.Adrestus.config.ConsensusConfiguration.COLLECTOR_PORT;
import static io.Adrestus.config.ConsensusConfiguration.PUBLISHER_PORT;

public class SimpleServer {
    private static Logger LOG = LoggerFactory.getLogger(SimpleServer.class);

    private volatile boolean terminate;

    private final ZContext ctx;
    private final String IP;
    private final Socket publisher;
    private final Socket collector;
    private final CountDownLatch latch;
    private final Timer timer;
    private final ReceiveTaskTimeout task;

    public SimpleServer(String IP) {
        this.IP = IP;
        this.terminate = false;
        this.latch = new CountDownLatch(1);
        this.timer = new Timer(ConsensusConfiguration.CONSENSUS);
        this.task = new ReceiveTaskTimeout();
        this.ctx = new ZContext();
        this.publisher = ctx.createSocket(SocketType.PUB);
        this.collector = ctx.createSocket(SocketType.PULL);

        this.publisher.bind("tcp://" + IP + ":" + PUBLISHER_PORT);
        this.collector.bind("tcp://" + IP + ":" + COLLECTOR_PORT);

    }

    public void publishMessage(byte[] data) {
        publisher.send(data);
    }

    public List<Bytes> receiveData(int byzantine_counter) {
        int N = byzantine_counter;
        int F = (byzantine_counter - 1) / 3;


        ArrayList<Bytes> list = new ArrayList<>();

        this.timer.scheduleAtFixedRate(task, ConsensusConfiguration.CONSENSUS_TIMEOUT, ConsensusConfiguration.CONSENSUS_TIMEOUT);
        try {
            while (byzantine_counter > 0 && latch.getCount() == 1) {
                byte[] buff = collector.recv();
                list.add(Bytes.wrap(buff));
                byzantine_counter--;
            }
        } catch (Exception e) {
            LOG.info("Socket Closed");
        }

        timer.cancel();
        task.cancel();
        if (byzantine_counter > F) {
            LOG.info("Byzantine network not meet requirements abort");
            list.clear();
        }

        return list;
    }


    public static Logger getLOG() {
        return LOG;
    }

    public static void setLOG(Logger LOG) {
        SimpleServer.LOG = LOG;
    }

    public ZContext getCtx() {
        return ctx;
    }

    public String getIP() {
        return IP;
    }

    public Socket getPublisher() {
        return publisher;
    }

    public Socket getCollector() {
        return collector;
    }

    public CountDownLatch getLatch() {
        return latch;
    }

    public Timer getTimer() {
        return timer;
    }

    public ReceiveTaskTimeout getTask() {
        return task;
    }

    public void close() {
        this.publisher.close();
        this.collector.close();
        this.ctx.close();
    }

    protected final class ReceiveTaskTimeout extends TimerTask {


        @Override
        public void run() {
            LOG.info("Consensus Timer Reach end Abort");
            latch.countDown();
            close();
            terminate = true;
        }

        @Override
        public boolean cancel() {
            super.cancel();
            close();
            return true;
        }
    }
}
