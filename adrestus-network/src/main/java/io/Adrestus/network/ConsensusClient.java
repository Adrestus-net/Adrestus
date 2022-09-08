package io.Adrestus.network;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;


import static io.Adrestus.config.ConsensusConfiguration.*;

public class ConsensusClient {

    private static Logger LOG = LoggerFactory.getLogger(ConsensusClient.class);
    private volatile boolean terminate;
    private final String IP;
    private ZContext ctx;
    private final ZMQ.Socket subscriber;
    private final ZMQ.Socket push;


    public ConsensusClient(String IP) {
        this.ctx = new ZContext();
        this.IP = IP;
        this.terminate = false;
        this.subscriber = ctx.createSocket(SocketType.SUB);
        this.push = ctx.createSocket(SocketType.PUSH);

        this.subscriber.connect("tcp://" + IP + ":" + SUBSCRIBER_PORT);
        this.subscriber.subscribe(ZMQ.SUBSCRIPTION_ALL);
        this.subscriber.setReceiveTimeOut(CONSENSUS_TIMEOUT);
        this.push.connect("tcp://" + IP + ":" + COLLECTOR_PORT);
    }


    public void pushMessage(byte[] data) {
        push.send(data);
    }

    public byte[] receiveData() {
        byte[] data = subscriber.recv(0);
        return data;
    }

    public void close() {
        if (!terminate) {
            this.subscriber.close();
            this.push.close();
            this.ctx.close();
        }
    }
}
