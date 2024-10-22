package io.Adrestus.network;

import io.Adrestus.config.ConsensusConfiguration;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQException;

import java.nio.charset.StandardCharsets;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.*;

import static io.Adrestus.config.ConsensusConfiguration.*;

public class ConsensusServer {

    private static Logger LOG = LoggerFactory.getLogger(ConsensusServer.class);
    private static final int PHASES = 2;
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
    private SharedResource sharedResource;

    private int MAX_MESSAGES;
    private final ExecutorService executorService;


    private ConsensusServer(String IP) {
        this.executorService = Executors.newSingleThreadExecutor();
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
        this.connected.setConflate(true);
        this.publisher.setLinger(200);
        this.publisher.setHWM(1000);
        this.connected.setHWM(1000);
        this.collector.setHWM(1000);
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
        this.IP = IP;
        this.receive_latch = new CountDownLatch(1);
        this.ctx = new ZContext();
        this.publisher = ctx.createSocket(SocketType.PUB);
        this.publisher.setHeartbeatIvl(2);
        this.collector = ctx.createSocket(SocketType.PULL);
        this.chunksCollector = ctx.createSocket(SocketType.ROUTER);


        this.publisher.setLinger(200);
        this.publisher.setHWM(1000);
        this.connected.setHWM(1000);
        this.collector.setHWM(1000);
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
        this.connected.setConflate(true);
        this.publisher.setLinger(200);
        this.publisher.setHWM(1000);
        this.connected.setHWM(1000);
        this.collector.setHWM(1000);
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
                result = instance;
                if (result == null) {
                    result = new ConsensusServer();
                    instance = result;
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
        Consumer task = new Consumer(this.sharedResource);
        task.setFinish(-1);
        Thread vThreadExecution = Thread.ofVirtual().start(task);
        vThreadExecution.join();
        byte[] data = null;
        if (!task.resource.message_deque.isEmpty()) {
            data = task.resource.message_deque.pollFirst();
            System.out.println("Data received"+task.resource.message_deque.size());
            if (task.resource.message_deque.isEmpty()) {
                task.setFinish(0);
                Thread vThreadFinishConsumeExecution = Thread.ofVirtual().start(task);
                vThreadFinishConsumeExecution.join();
            }
        }
        return data;
    }

    @SneakyThrows
    public void receive_handler() {
        Producer task = new Producer(this.sharedResource);
        Thread.ofVirtual().start(task);
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
            e.printStackTrace();
        } catch (ZMQException e) {
            e.printStackTrace();
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

    public void resetSharedResource() {
        this.sharedResource = new SharedResource();
    }

    public void close() {
        instance = null;
        this.sharedResource.clear();
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

    private final class Consumer implements Runnable {
        private final SharedResource resource;
        private int finish;

        public Consumer(SharedResource resource) {
            this.resource = resource;
        }


        @SneakyThrows
        @Override
        public void run() {
            this.resource.consume(finish);
        }

        public SharedResource getResource() {
            return resource;
        }

        public int getFinish() {
            return finish;
        }

        public void setFinish(int finish) {
            this.finish = finish;
        }
    }

    private final class Producer implements Runnable {
        private final SharedResource resource;

        public Producer(SharedResource resource) {
            this.resource = resource;
        }

        @SneakyThrows
        @Override
        public void run() {
            int connected_validators = MAX_MESSAGES / 2;
            for (int i = 0; i < PHASES; i++) {
                this.resource.produce(0);
                System.out.println("after_prd_release "+resource.isProduced());
                int unique = 0;
                ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
                while (unique < connected_validators) {
                    Runnable task = () -> {
                        byte[] data = {1};
                        try {
                            System.out.println("before rec");
                            data = collector.recv(0);
                            System.out.println("Rec" + Hex.toHexString(data));
                        } catch (ZMQException e) {
                            if (e.getErrorCode() != 156384765) {
                                LOG.info("ZMQ EXCEPTION caught");
                            }
                        } catch (NullPointerException exception) {
                            LOG.info("NullPointerException caught " + exception.toString());
                        }
                        if (data != null) {
                            this.resource.getMessage_deque().add(data);
                        } else {
                            this.resource.getMessage_deque().add(new byte[0]);
                        }
                    };
                    executor.submit(task);
                    unique++;
                }
                executor.shutdown();
                try {
                    executor.awaitTermination(CONSENSUS_CONNECTED_RECEIVE_TIMEOUT, TimeUnit.SECONDS);
                } catch (InterruptedException e) {

                }
                System.out.println();
                this.resource.produce(1);
                executor.shutdownNow();
                executor.close();
            }
        }
    }

    private final class SharedResource {
        @Getter
        private final LinkedBlockingDeque<byte[]> message_deque;
        @Getter
        @Setter
        private boolean isProduced = true;

        public SharedResource() {
            this.message_deque = new LinkedBlockingDeque<>();
        }

        public synchronized void produce(int finish) throws InterruptedException {
            while (!isProduced) {
                wait();
                System.out.println("Prod release" + finish);
            }
            if (finish == 1) {
                isProduced = false;
                notify();
            }
        }

        public synchronized void consume(int finish) throws InterruptedException {
            while (isProduced) {
                wait();
            }
            if (finish == 0) {
                System.out.println("Consume release" + finish);
                isProduced = true;
                notify();
            }
        }

        public void clear() {
            this.message_deque.clear();
            this.isProduced = true;
        }
    }
}
