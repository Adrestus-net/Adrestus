package io.Adrestus.network;

import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQException;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.Semaphore;

import static io.Adrestus.config.ConsensusConfiguration.*;

public class ConsensusClient {

    private static Logger LOG = LoggerFactory.getLogger(ConsensusClient.class);
    private int MESSAGES = 6;
    private int MAX_MESSAGES = 6;
    private static final int MAX_AVAILABLE = 6;
    private ZContext ctx;
    private final ZMQ.Socket subscriber;
    private final ZMQ.Socket push;
    private final ZMQ.Socket connected;

    private final ZMQ.Poller poller;

    private final String IP;
    private String Identity;
    private ZMQ.Socket erasure;
    private final LinkedBlockingDeque<byte[]> message_deque;
    private final ExecutorService executorService;

    public ConsensusClient(String IP) {
        this.IP=IP;
        this.executorService = Executors.newSingleThreadExecutor();
        this.ctx = new ZContext();
        this.message_deque = new LinkedBlockingDeque<>();
        this.subscriber = ctx.createSocket(SocketType.SUB);
        this.push = ctx.createSocket(SocketType.PUSH);

        this.connected = ctx.createSocket(SocketType.REQ);
        this.connected.setReceiveTimeOut(CONSENSUS_CONNECTED_RECEIVE_TIMEOUT);
        this.connected.setSendTimeOut(CONSENSUS_CONNECTED_SEND_TIMEOUT);

        this.erasure = ctx.createSocket(SocketType.DEALER);
        this.erasure.setReceiveTimeOut(CONSENSUS_CONNECTED_RECEIVE_TIMEOUT);
        this.erasure.setSendTimeOut(CONSENSUS_CONNECTED_SEND_TIMEOUT);

        this.subscriber.setReceiveBufferSize(10000);
        this.subscriber.setHWM(10000);
        this.subscriber.setRcvHWM(1);
        this.subscriber.setConflate(true);

        this.subscriber.connect("tcp://" + IP + ":" + SUBSCRIBER_PORT);
        this.connected.connect("tcp://" + IP + ":" + CONNECTED_PORT);
        this.erasure.connect("tcp://" + IP + ":" + CHUNKS_COLLECTOR_PORT);

        this.subscriber.subscribe(ZMQ.SUBSCRIPTION_ALL);
        this.subscriber.setReceiveTimeOut(CONSENSUS_TIMEOUT);
        this.push.connect("tcp://" + IP + ":" + COLLECTOR_PORT);

        this.poller = ctx.createPoller(1);
        this.poller.register(this.erasure, ZMQ.Poller.POLLIN);

    }

    public ConsensusClient(String IP, String identity) {
        this.IP=IP;
        this.Identity=identity;
        this.executorService = Executors.newSingleThreadExecutor();
        this.ctx = new ZContext();
        this.message_deque = new LinkedBlockingDeque<>();
        this.subscriber = ctx.createSocket(SocketType.SUB);
        this.push = ctx.createSocket(SocketType.PUSH);

        this.connected = ctx.createSocket(SocketType.REQ);
        this.connected.setReceiveTimeOut(CONSENSUS_CONNECTED_RECEIVE_TIMEOUT);
        this.connected.setSendTimeOut(CONSENSUS_CONNECTED_SEND_TIMEOUT);

        this.erasure = ctx.createSocket(SocketType.DEALER);
        this.erasure.setIdentity(identity.getBytes(ZMQ.CHARSET));
        this.erasure.setReceiveTimeOut(CONSENSUS_CONNECTED_RECEIVE_TIMEOUT);
        this.erasure.setSendTimeOut(CONSENSUS_CONNECTED_SEND_TIMEOUT);

        this.subscriber.setReceiveBufferSize(10000);
        this.subscriber.setHWM(10000);
        this.subscriber.setRcvHWM(1);
        this.subscriber.setConflate(true);

        this.subscriber.connect("tcp://" + IP + ":" + SUBSCRIBER_PORT);
        this.connected.connect("tcp://" + IP + ":" + CONNECTED_PORT);
        this.erasure.connect("tcp://" + IP + ":" + CHUNKS_COLLECTOR_PORT);

        this.subscriber.subscribe(ZMQ.SUBSCRIPTION_ALL);
        this.subscriber.setReceiveTimeOut(CONSENSUS_TIMEOUT);
        this.push.connect("tcp://" + IP + ":" + COLLECTOR_PORT);

        this.poller = ctx.createPoller(1);
        this.poller.register(this.erasure, ZMQ.Poller.POLLIN);

    }

    public void PollOut() {
        ZMQ.Poller poller = ctx.createPoller(1);
        poller.register(this.subscriber, ZMQ.Poller.POLLOUT);
        int rc = -1;
        while (rc == -1) {
            rc = poller.poll(15000);
        }
        poller.pollout(0);
    }

    public boolean PollIn() {
        int rc = poller.poll(HEARTBEAT_INTERVAL);
        if (poller.pollin(0))
            return true;
        return false;
    }

    public void pushMessage(byte[] data) {
        push.send(data);
    }

    public byte[] receiveData() {
        byte[] data = subscriber.recv();
        return data;
    }

    public byte[] receiveErasureData() {
        byte[] data = erasure.recv();
        return data;
    }

    public boolean SendErasureData(byte[] data) {
        erasure.send(data);//its correct first send id and after data
        //erasure.send(data);
        boolean rec = PollIn();
        if (rec) return true;
        int rc = 0;
        while (rc < CYCLES) {
            poller.unregister(erasure);
            ctx.destroySocket(erasure);
            this.erasure = ctx.createSocket(SocketType.DEALER);
            this.erasure.setIdentity(Identity.getBytes(ZMQ.CHARSET));
            this.erasure.setReceiveTimeOut(CONSENSUS_CONNECTED_RECEIVE_TIMEOUT);
            this.erasure.setSendTimeOut(CONSENSUS_CONNECTED_SEND_TIMEOUT);
            this.erasure.connect("tcp://" + IP + ":" + CHUNKS_COLLECTOR_PORT);
            this.poller.register(erasure, ZMQ.Poller.POLLIN);
            this.erasure.send(data);
            boolean IsRec = PollIn();
            if (IsRec) return true;
            rc++;
        }
        return false;
    }


    @SneakyThrows
    public byte[] deque_message() {
        while (message_deque.isEmpty()) {
            Thread.sleep(50);
        }
        // System.out.println("take");
        return message_deque.pollFirst();
    }

    public void receive_handler() {
        Runnable runnableTask = () -> {
            byte[] data = {1};
            while (MESSAGES > 0 && MAX_MESSAGES > 0) {
                //available.acquire();
                //         System.out.println("acquire");
                try {
                    data = subscriber.recv();
                } catch (ZMQException e) {
                    if (e.getErrorCode() != 156384765) {
                        LOG.info("ZMQ EXCEPTION caught");
                    }
                } catch (NullPointerException exception) {
                    LOG.info("NullPointerException caught " + exception.toString());
                }
                if (data != null) {
                    message_deque.add(data);
                    MESSAGES--;
                } else {
                    message_deque.add(new byte[0]);
                    break;
                }
                // System.out.println("receive" + MESSAGES);
                MAX_MESSAGES--;
                // System.out.println("receive" + MESSAGES);
                // available.release();
            }
        };
        this.executorService.execute(runnableTask);
    }

    public String rec_heartbeat() {
        return this.connected.recvStr(0);
    }

    public void send_heartbeat(String data) {
        try {
            this.connected.send(data);
        } catch (ZMQException e) {
            LOG.info("Operation cannot be accomplished in current state");
        }
    }

    public void close() {
        try {
            this.executorService.shutdownNow().clear();
            this.subscriber.setLinger(0);
            this.push.setLinger(0);
            this.connected.setLinger(0);
            this.subscriber.close();
            this.push.close();
            this.connected.close();
            this.erasure.close();
            this.ctx.destroySocket(this.subscriber);
            this.ctx.destroySocket(this.push);
            this.ctx.destroySocket(this.connected);
            this.ctx.destroySocket(this.erasure);
            this.ctx.destroy();
            Thread.sleep(400);
        } catch (AssertionError e) {
            int g = 3;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
