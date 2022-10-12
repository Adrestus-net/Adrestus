package io.Adrestus.network;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import static io.Adrestus.config.ConsensusConfiguration.*;

public class ConsensusClient {

    private static Logger LOG = LoggerFactory.getLogger(ConsensusClient.class);
    private final String IP;
    private ZContext ctx;
    private final ZMQ.Socket subscriber;
    private final ZMQ.Socket push;
    private final ZMQ.Socket connected;

    public ConsensusClient(String IP) {
        this.ctx = new ZContext();
        this.IP = IP;
        this.subscriber = ctx.createSocket(SocketType.PULL);
        this.push = ctx.createSocket(SocketType.PUSH);
        this.connected=ctx.createSocket(SocketType.REQ);


        this.subscriber.connect("tcp://" + IP + ":" + SUBSCRIBER_PORT);
        this.connected.connect("tcp://" + IP + ":" + CONNECTED_PORT);
       // this.subscriber.subscribe(ZMQ.SUBSCRIPTION_ALL);
        this.subscriber.setReceiveTimeOut(CONSENSUS_TIMEOUT);
        PollIn();
        this.push.connect("tcp://" + IP + ":" + COLLECTOR_PORT);
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
    public void PollIn() {
        ZMQ.Poller poller = ctx.createPoller(1);
        poller.register(this.subscriber, ZMQ.Poller.POLLIN);
        int rc = -1;
        while (rc == -1) {
            rc = poller.poll(1);
        }
        poller.pollin(0);
    }

    public void pushMessage(byte[] data) {
        push.send(data);
    }

    public byte[] receiveData() {
        byte[] data = subscriber.recv();
        return data;
    }

    public String rec_heartbeat(){
        return this.connected.recvStr(0);
    }

    public void send_heartbeat(String data){
        this.connected.send(data);
    }

    public void close() {
        this.subscriber.close();
        this.push.close();
        this.ctx.close();
    }
}
