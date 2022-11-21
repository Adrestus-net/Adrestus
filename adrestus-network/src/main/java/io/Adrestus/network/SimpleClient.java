package io.Adrestus.network;

import io.Adrestus.config.ConsensusConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;

import java.nio.charset.StandardCharsets;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

import static io.Adrestus.config.ConsensusConfiguration.COLLECTOR_PORT;
import static io.Adrestus.config.ConsensusConfiguration.SUBSCRIBER_PORT;

public class SimpleClient {
    private static Logger LOG = LoggerFactory.getLogger(SimpleClient.class);
    private volatile boolean terminate;
    private final String IP;
    private ZContext ctx;
    private final Socket subscriber;
    private final Socket push;
    private final CountDownLatch latch;
    private final Timer timer;
    private final ReceiveTaskTimeout task;

    public SimpleClient(String IP) {
        this.ctx = new ZContext();
        this.latch = new CountDownLatch(1);
        this.IP = IP;
        this.terminate = false;
        this.timer = new Timer(ConsensusConfiguration.CONSENSUS);
        this.task = new ReceiveTaskTimeout();
        this.subscriber = ctx.createSocket(SocketType.SUB);
        this.push = ctx.createSocket(SocketType.PUSH);

        this.subscriber.connect("tcp://" + IP + ":" + SUBSCRIBER_PORT);
        this.subscriber.subscribe(ZMQ.SUBSCRIPTION_ALL);
        //subscriber.setReceiveTimeOut(2000);
        this.push.connect("tcp://" + IP + ":" + COLLECTOR_PORT);

       /* (new Thread() {
            public void run() {
                System.out.println("SUB: " + new String(subscriber.recv()));
            }
        }).start();*/
    }


    public void publishMessage(byte[] data) {
        push.send(data);
    }

    public byte[] receiveData() {
        this.timer.scheduleAtFixedRate(task, ConsensusConfiguration.CONSENSUS_TEST_TIMEOUT, ConsensusConfiguration.CONSENSUS_TEST_TIMEOUT);
        this.subscriber.subscribe("".getBytes(StandardCharsets.UTF_8));
        byte[] data = null;
        try {
            data = subscriber.recv(0);
        } catch (Exception e) {
            LOG.info("Socket Closed");
        } catch (AssertionError e) {
            LOG.info("Socket Closed");
        } finally {
            timer.cancel();
            task.cancel();
        }
        return data;
    }

    public void close() {
        if (!ctx.isEmpty()) {
            this.subscriber.close();
            this.push.close();
            this.ctx.close();
        }
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
