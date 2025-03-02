package io.Adrestus.network;

import io.Adrestus.config.ConsensusConfiguration;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQException;

import java.nio.charset.StandardCharsets;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

import static io.Adrestus.config.ConsensusConfiguration.*;


// If you want synchronization code reset here Fixes synchronization and parallelizes the whole process e8698f68
//or in this commit Zermq old configuration you can reset here in this commit 5571828
public class ConsensusServer {

    private static Logger LOG = LoggerFactory.getLogger(ConsensusServer.class);
    private static volatile ConsensusServer instance;

    private final ZContext ctx;
    private final String IP;
    private final ZMQ.Socket publisher;
    private final ZMQ.Socket collector;

    private final ZMQ.Socket chunksCollector;


    private ZMQ.Socket connected;
    private volatile boolean terminate;
    private Timer timer;
    private ConnectedTaskTimeout task;
    private CountDownLatch latch;
    private CountDownLatch receive_latch;
    private int peers_not_connected;

    private int MAX_MESSAGES;
    private final ExecutorService executorService;

    private final LinkedBlockingDeque<byte[]> message_deque;

//    public ConsensusServer(String IP, CountDownLatch latch) {
//        this.executorService = Executors.newSingleThreadExecutor();
//        this.message_deque = new LinkedBlockingDeque<>();
//        this.peers_not_connected = 0;
//        this.terminate = false;
//        this.IP = IP;
//        this.latch = latch;
//        this.ctx = new ZContext();
//        this.publisher = ctx.createSocket(SocketType.PUB);
//        this.collector = ctx.createSocket(SocketType.PULL);
//        this.connected = ctx.createSocket(SocketType.REP);
//        this.chunksCollector = ctx.createSocket(SocketType.ROUTER);
//
//        this.connected.setLinger(200);
//        this.connected.setHWM(1);
//        this.connected.setConflate(true);
//        this.publisher.setLinger(200);
//        this.publisher.setHWM(3);
//        this.publisher.setConflate(true);
////        this.chunksCollector.setHWM(10000);
//
//        this.chunksCollector.bind("tcp://" + IP + ":" + CHUNKS_COLLECTOR_PORT);
//        this.publisher.bind("tcp://" + IP + ":" + PUBLISHER_PORT);
//        this.collector.bind("tcp://" + IP + ":" + COLLECTOR_PORT);
//        this.connected.bind("tcp://" + IP + ":" + CONNECTED_PORT);
//
//        this.collector.setReceiveTimeOut(CONSENSUS_COLLECTED_TIMEOUT);
//        this.publisher.setSendTimeOut(CONSENSUS_PUBLISHER_TIMEOUT);
//
//        this.connected.setSendTimeOut(CONSENSUS_CONNECTED_SEND_TIMEOUT);
//        this.connected.setReceiveTimeOut(CONSENSUS_CONNECTED_RECEIVE_TIMEOUT);
//
//        this.chunksCollector.setSendTimeOut(CONSENSUS_CONNECTED_SEND_TIMEOUT);
//        this.chunksCollector.setReceiveTimeOut(CONSENSUS_CONNECTED_RECEIVE_TIMEOUT);
//        this.chunksCollector.setSndHWM(0);
//
//        this.timer = new Timer(ConsensusConfiguration.CONSENSUS);
//        this.task = new ConnectedTaskTimeout();
//        this.BlockUntilConnected();
//    }

//    public ConsensusServer(String IP, CountDownLatch latch, int random) {
//        this.executorService = Executors.newSingleThreadExecutor();
//        this.message_deque = new LinkedBlockingDeque<>();
//        this.peers_not_connected = 0;
//        this.terminate = false;
//        this.IP = IP;
//        this.latch = latch;
//        this.ctx = new ZContext();
//        this.publisher = ctx.createSocket(SocketType.PUB);
//        this.collector = ctx.createSocket(SocketType.PULL);
//        this.connected = ctx.createSocket(SocketType.REP);
//        this.chunksCollector = ctx.createSocket(SocketType.ROUTER);
//
//        this.connected.setLinger(200);
//        this.connected.setHWM(1);
//        this.connected.setConflate(true);
//        this.publisher.setLinger(200);
//        this.publisher.setHWM(3);
//        this.publisher.setConflate(true);
////        this.chunksCollector.setHWM(10000);
//
//        this.chunksCollector.bind("tcp://" + IP + ":" + CHUNKS_COLLECTOR_PORT);
//        this.publisher.bind("tcp://" + IP + ":" + PUBLISHER_PORT);
//        this.collector.bind("tcp://" + IP + ":" + COLLECTOR_PORT);
//        this.connected.bind("tcp://" + IP + ":" + CONNECTED_PORT);
//
//        this.collector.setReceiveTimeOut(CONSENSUS_COLLECTED_TIMEOUT);
//        this.publisher.setSendTimeOut(CONSENSUS_PUBLISHER_TIMEOUT);
//
//        this.connected.setSendTimeOut(CONSENSUS_CONNECTED_SEND_TIMEOUT);
//        this.connected.setReceiveTimeOut(CONSENSUS_CONNECTED_RECEIVE_TIMEOUT);
//
//        this.chunksCollector.setSendTimeOut(CONSENSUS_CONNECTED_SEND_TIMEOUT);
//        this.chunksCollector.setReceiveTimeOut(CONSENSUS_CONNECTED_RECEIVE_TIMEOUT);
//        this.chunksCollector.setSndHWM(0);
//
//        this.timer = new Timer(ConsensusConfiguration.CONSENSUS);
//        this.task = new ConnectedTaskTimeout();
//    }

//    public ConsensusServer(String IP, CountDownLatch latch, int collector_timeout, int connected_timeout) {
//        this.executorService = Executors.newSingleThreadExecutor();
//        this.message_deque = new LinkedBlockingDeque<>();
//        this.peers_not_connected = 0;
//        this.terminate = false;
//        this.IP = IP;
//        this.latch = latch;
//        this.ctx = new ZContext();
//        this.publisher = ctx.createSocket(SocketType.PUB);
//        this.collector = ctx.createSocket(SocketType.PULL);
//        this.connected = ctx.createSocket(SocketType.REP);
//        this.chunksCollector = ctx.createSocket(SocketType.ROUTER);
//
//        this.connected.setLinger(200);
//        this.connected.setHWM(1);
//        this.connected.setConflate(true);
//        this.publisher.setLinger(200);
//        this.publisher.setHWM(3);
//        this.publisher.setConflate(true);

    /// /        this.chunksCollector.setHWM(10000);
//
//        this.chunksCollector.bind("tcp://" + IP + ":" + CHUNKS_COLLECTOR_PORT);
//        this.publisher.bind("tcp://" + IP + ":" + PUBLISHER_PORT);
//        this.collector.bind("tcp://" + IP + ":" + COLLECTOR_PORT);
//        this.connected.bind("tcp://" + IP + ":" + CONNECTED_PORT);
//
//        this.collector.setReceiveTimeOut(collector_timeout);
//        this.publisher.setSendTimeOut(CONSENSUS_TIMEOUT);
//
//        this.connected.setSendTimeOut(connected_timeout);
//        this.connected.setReceiveTimeOut(connected_timeout);
//
//        this.chunksCollector.setSendTimeOut(connected_timeout);
//        this.chunksCollector.setReceiveTimeOut(connected_timeout);
//        this.chunksCollector.setSndHWM(0);
//
//        this.timer = new Timer(ConsensusConfiguration.CONSENSUS);
//        this.task = new ConnectedTaskTimeout();
//        this.BlockUntilConnected();
//    }
    private ConsensusServer(String IP) {
        this.executorService = Executors.newSingleThreadExecutor();
        this.message_deque = new LinkedBlockingDeque<>();
        this.IP = IP;
        this.receive_latch = new CountDownLatch(1);
        this.peers_not_connected = 0;
        this.terminate = false;
        this.ctx = new ZContext();
        this.publisher = ctx.createSocket(SocketType.PUB);
        this.collector = ctx.createSocket(SocketType.PULL);
        this.connected = ctx.createSocket(SocketType.REP);
        this.chunksCollector = ctx.createSocket(SocketType.ROUTER);


        this.connected.setLinger(200);
        this.connected.setHWM(1);
        this.connected.setConflate(true);
        this.publisher.setLinger(200);
        this.publisher.setHWM(3);
        this.publisher.setConflate(true);

        this.chunksCollector.bind("tcp://" + IP + ":" + CHUNKS_COLLECTOR_PORT);
        this.publisher.bind("tcp://" + IP + ":" + PUBLISHER_PORT);
        this.collector.bind("tcp://" + IP + ":" + COLLECTOR_PORT);
        this.connected.bind("tcp://" + IP + ":" + CONNECTED_PORT);

        this.collector.setReceiveTimeOut(CONSENSUS_COLLECTED_TIMEOUT);
        this.publisher.setSendTimeOut(CONSENSUS_PUBLISHER_TIMEOUT);

        this.connected.setSendTimeOut(CONSENSUS_CONNECTED_SEND_TIMEOUT);
        this.connected.setReceiveTimeOut(CONSENSUS_CONNECTED_RECEIVE_TIMEOUT);


        this.chunksCollector.setSendTimeOut(CONSENSUS_CONNECTED_SEND_TIMEOUT);
        this.chunksCollector.setReceiveTimeOut(CONSENSUS_CONNECTED_RECEIVE_TIMEOUT);
        this.chunksCollector.setSndHWM(0);
    }

    private ConsensusServer(String IP, int MAX_MESSAGES) {
        this.MAX_MESSAGES = MAX_MESSAGES;
        this.executorService = Executors.newSingleThreadExecutor();
        this.message_deque = new LinkedBlockingDeque<>();
        this.IP = IP;
        this.receive_latch = new CountDownLatch(1);
        this.ctx = new ZContext();
        this.publisher = ctx.createSocket(SocketType.PUB);
        this.publisher.setHeartbeatIvl(2);
        this.collector = ctx.createSocket(SocketType.PULL);
        this.chunksCollector = ctx.createSocket(SocketType.ROUTER);


        this.publisher.setLinger(200);
        this.publisher.setHWM(3);
        this.publisher.setConflate(true);

        this.chunksCollector.bind("tcp://" + IP + ":" + CHUNKS_COLLECTOR_PORT);
        this.publisher.bind("tcp://" + IP + ":" + PUBLISHER_PORT);
        this.collector.bind("tcp://" + IP + ":" + COLLECTOR_PORT);
        this.collector.setReceiveTimeOut(CONSENSUS_TIMEOUT);
        this.publisher.setSendTimeOut(CONSENSUS_TIMEOUT);

        this.chunksCollector.setSendTimeOut(CONSENSUS_TIMEOUT);
        this.chunksCollector.setReceiveTimeOut(CONSENSUS_TIMEOUT);
        this.chunksCollector.setSndHWM(0);
    }

    private ConsensusServer() {
        this.executorService = Executors.newSingleThreadExecutor();
        this.message_deque = new LinkedBlockingDeque<>();
        this.IP = IPFinder.getLocalIP();
        this.receive_latch = new CountDownLatch(1);
        this.peers_not_connected = 0;
        this.terminate = false;
        this.ctx = new ZContext();
        this.publisher = ctx.createSocket(SocketType.PUB);
        this.collector = ctx.createSocket(SocketType.PULL);
        this.connected = ctx.createSocket(SocketType.REP);
        this.chunksCollector = ctx.createSocket(SocketType.ROUTER);

        this.connected.setLinger(200);
        this.connected.setHWM(1);
        this.connected.setConflate(true);
        this.publisher.setLinger(200);
        this.publisher.setHWM(3);
        this.publisher.setConflate(true);

        this.chunksCollector.bind("tcp://" + IP + ":" + CHUNKS_COLLECTOR_PORT);
        this.publisher.bind("tcp://" + IP + ":" + PUBLISHER_PORT);
        this.collector.bind("tcp://" + IP + ":" + COLLECTOR_PORT);
        this.connected.bind("tcp://" + IP + ":" + CONNECTED_PORT);

        this.collector.setReceiveTimeOut(CONSENSUS_COLLECTED_TIMEOUT);
        this.publisher.setSendTimeOut(CONSENSUS_PUBLISHER_TIMEOUT);

        this.connected.setSendTimeOut(CONSENSUS_CONNECTED_SEND_TIMEOUT);
        this.connected.setReceiveTimeOut(CONSENSUS_CONNECTED_RECEIVE_TIMEOUT);

        this.chunksCollector.setSendTimeOut(CONSENSUS_CONNECTED_SEND_TIMEOUT);
        this.chunksCollector.setReceiveTimeOut(CONSENSUS_CONNECTED_RECEIVE_TIMEOUT);
        this.chunksCollector.setSndHWM(0);
    }

    public static ConsensusServer getInstance() {
        var result = instance;
        if (result == null) {
            synchronized (ConsensusServer.class) {
                result = instance;
                if (result == null) {
                    result = new ConsensusServer();
                    instance = result;
                }
            }
        }
        return result;
    }

    public static ConsensusServer getInstance(String ip) {
        var result = instance;
        if (result == null) {
            synchronized (ConsensusServer.class) {
                result = instance;
                if (result == null) {
                    result = new ConsensusServer(ip);
                    instance = result;
                }
            }
        }
        return result;
    }

    public static ConsensusServer getInstance(String ip, int max_messages) {
        var result = instance;
        if (result == null) {
            synchronized (ConsensusServer.class) {
                result = instance;
                if (result == null) {
                    result = new ConsensusServer(ip, max_messages);
                    instance = result;
                }
            }
        }
        return result;
    }

    public void BlockUntilConnected() {
        this.timer = new Timer(ConsensusConfiguration.CONSENSUS);
        this.task = new ConnectedTaskTimeout();
        this.timer.scheduleAtFixedRate(task, CONSENSUS_TIMEOUT / 2, CONSENSUS_TIMEOUT / 2);
        int counter = (int) latch.getCount();
        while (latch.getCount() > 0 && !terminate) {
            String rec = receiveStringData();
            System.out.println(rec);
            if (rec != null) {
                if (rec.equals("1")) {
                    connected.send(HEARTBEAT_MESSAGE.getBytes(StandardCharsets.UTF_8));
                    counter--;
                    setPeers_not_connected(counter);
                    latch.countDown();
                }
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
        try {
            publisher.send(data, 0);
        } catch (ZMQException e) {
            if (e.getErrorCode() == 156384765) {
            } else {
                LOG.info("ZMQException" + e.toString());
            }
        }
    }

    public void setErasureMessage(byte[] data, String identity) {
        try {
            this.chunksCollector.sendMore(identity.getBytes(ZMQ.CHARSET));
            this.chunksCollector.send(data);
        } catch (ZMQException e) {
            if (e.getErrorCode() == 156384765) {
            } else {
                LOG.info("ZMQException" + e.toString());
            }
        }
    }

    @SneakyThrows
    public byte[] receiveData() {
        while (message_deque.isEmpty()) {
            this.receive_latch.await();
            this.receive_latch = new CountDownLatch(1);
        }
        // System.out.println("take");
        return message_deque.pollFirst();
    }

    public void receive_handler() {
        Runnable runnableTask = () -> {
            byte[] data = {1};
            while (MAX_MESSAGES > 0) {
                //available.acquire();
                //         System.out.println("acquire");
                try {
                    data = collector.recv(0);
                } catch (ZMQException e) {
                    if (e.getErrorCode() != 156384765) {
                        LOG.info("ZMQ EXCEPTION caught");
                    }
                } catch (NullPointerException exception) {
                    LOG.info("NullPointerException caught " + exception.toString());
                }
                if (data != null) {
                    message_deque.add(data);
                    MAX_MESSAGES--;
                } else {
                    message_deque.add(new byte[0]);
                    MAX_MESSAGES--;
                }
                this.receive_latch.countDown();
                // System.out.println("receive" + MESSAGES);
                // System.out.println("receive" + MESSAGES);
                // available.release();
            }
        };
        this.executorService.execute(runnableTask);
    }

    public String receiveStringData() {
        String data = "";
        try {
            byte[] recv = connected.recv();
            return new String(recv, StandardCharsets.UTF_8);
        } catch (NullPointerException e) {
            //e.printStackTrace();
        } catch (ZMQException e) {
            if (e.getErrorCode() == 156384765) {
            } else {
                LOG.info("ZMQException" + e.toString());
            }
        }
        return data;
    }

    public byte[] receiveErasureData() {
        try {
            byte[] recv1 = chunksCollector.recv();
            byte[] recv2 = chunksCollector.recv();
            return recv2;
        } catch (NullPointerException e) {
            //e.printStackTrace();
        } catch (ZMQException e) {
            if (e.getErrorCode() == 156384765) {
            } else {
                LOG.info("ZMQException" + e.toString());
            }
        }
        return null;
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

    public int getMAX_MESSAGES() {
        return MAX_MESSAGES;
    }

    public void setMAX_MESSAGES(int MAX_MESSAGES) {
        this.MAX_MESSAGES = MAX_MESSAGES;
    }

    public void setPeers_not_connected(int peers_not_connected) {
        this.peers_not_connected = peers_not_connected;
    }

    public CountDownLatch getLatch() {
        return latch;
    }

    public void setLatch(CountDownLatch latch) {
        this.latch = latch;
    }

    public void close() {
        instance = null;
        if (this.executorService != null) {
            this.executorService.shutdownNow().clear();
        }
        if (timer != null) {
            this.timer.cancel();
            this.timer.purge();
            this.timer = null;
        }
        if (this.publisher != null) {
            this.publisher.setLinger(0);
            this.publisher.close();
        }
        if (this.collector != null) {
            this.collector.setLinger(0);
            this.collector.close();
        }
        if (this.connected != null) {
            this.connected.setLinger(0);
            this.connected.close();
        }
        if (this.chunksCollector != null) {
            this.chunksCollector.setLinger(0);
            this.chunksCollector.close();
        }
        if (this.ctx != null) {
            try {
                this.ctx.destroySocket(this.publisher);
                this.ctx.destroySocket(this.collector);
                this.ctx.destroySocket(this.connected);
                this.ctx.destroySocket(this.chunksCollector);
                this.ctx.close();
                this.ctx.destroy();
            } catch (AssertionError e) {
                e.printStackTrace();
            }
        }
    }

    protected final class ConnectedTaskTimeout extends TimerTask {


        @Override
        public void run() {
            LOG.info("ConnectedTaskTimeout Terminated!!!");
            terminate = true;
            int peers = (int) latch.getCount();
            setPeers_not_connected(peers);
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
