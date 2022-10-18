package io.Adrestus.network;

import org.apache.tuweni.bytes.Bytes;
import org.junit.jupiter.api.Test;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static io.Adrestus.config.ConsensusConfiguration.PUBLISHER_PORT;
import static io.Adrestus.config.ConsensusConfiguration.SUBSCRIBER_PORT;

public class ServerClientTest {

    @Test
    public void simple_test() throws InterruptedException {
        System.out.println("SimpleTest");
        ZContext ctx = new ZContext();
        String IP = "localhost";
        Socket pub = ctx.createSocket(SocketType.PUB);
        Socket sub = ctx.createSocket(SocketType.SUB);
        sub.subscribe(ZMQ.SUBSCRIPTION_ALL);

        pub.bind("tcp://" + IP + ":" + PUBLISHER_PORT);
        sub.connect("tcp://" + IP + ":" + SUBSCRIBER_PORT);

        // Eliminate slow subscriber problem
        Thread.sleep(100);
        (new Thread() {
            public void run() {
                System.out.println("SUB: " + new String(sub.recv()));
            }
        }).start();
        Thread.sleep(100);
        pub.send("Hello, world!".getBytes(StandardCharsets.UTF_8));
        Thread.sleep(100);
        sub.close();
        pub.close();
        ctx.close();
    }

    @Test
    public void test_with_no_delays0() throws InterruptedException {
        System.out.println("test_with_no_delays0");
        SimpleServer adrestusServer = new SimpleServer("localhost");


        SimpleClient adrestusClient1 = new SimpleClient("localhost");
        (new Thread() {
            public void run() {
                System.out.println(new String(adrestusClient1.receiveData()));
                adrestusClient1.close();
            }
        }).start();
        Thread.sleep(1000);
        adrestusServer.publishMessage("Message".getBytes(StandardCharsets.UTF_8));
        Thread.sleep(10);
        adrestusServer.close();
    }

    @Test
    public void test_with_with_delays() throws InterruptedException {
        System.out.println("test_with_with_delays");
        SimpleServer adrestusServer = new SimpleServer("localhost");


        SimpleClient adrestusClient1 = new SimpleClient("localhost");
        (new Thread() {
            public void run() {
                byte[] data = adrestusClient1.receiveData();
                if (data == null) {
                    System.out.println("not receive");
                } else {
                    System.out.println(new String(data));
                }
                adrestusClient1.close();
            }
        }).start();
        Thread.sleep(6000);
        adrestusServer.publishMessage("Message".getBytes(StandardCharsets.UTF_8));
        Thread.sleep(10);
        adrestusServer.close();
    }

    @Test
    public void test_with_no_delays() throws InterruptedException {
        System.out.println("test_with_no_delays");
        SimpleServer adrestusServer = new SimpleServer("localhost");

        SimpleClient adrestusClient1 = new SimpleClient("localhost");
        SimpleClient adrestusClient2 = new SimpleClient("localhost");
        SimpleClient adrestusClient3 = new SimpleClient("localhost");
        SimpleClient adrestusClient4 = new SimpleClient("localhost");


        (new Thread() {
            public void run() {
                byte[] res = adrestusClient1.receiveData();
                if (res == null) {
                    System.out.println("not receive");
                } else {
                    System.out.println(new String(res));
                }
                adrestusClient1.close();
            }
        }).start();
        (new Thread() {
            public void run() {
                byte[] res = adrestusClient2.receiveData();
                if (res == null) {
                    System.out.println("not receive");
                } else {
                    System.out.println(new String(res));
                }
                adrestusClient2.close();
            }
        }).start();
        (new Thread() {
            public void run() {
                byte[] res = adrestusClient3.receiveData();
                if (res == null) {
                    System.out.println("not receive");
                } else {
                    System.out.println(new String(res));
                }
                adrestusClient3.close();
            }
        }).start();
        (new Thread() {
            public void run() {
                byte[] res = adrestusClient4.receiveData();
                if (res == null) {
                    System.out.println("not receive");
                } else {
                    System.out.println(new String(res));
                }
                adrestusClient4.close();
            }
        }).start();


        Thread.sleep(3000);
        adrestusServer.publishMessage("Message".getBytes(StandardCharsets.UTF_8));
        Thread.sleep(10);
        adrestusServer.close();

    }

    @Test
    public void client_push_to_server() throws InterruptedException {
        System.out.println("client_push_to_server");
        SimpleServer adrestusServer = new SimpleServer("localhost");

        SimpleClient adrestusClient1 = new SimpleClient("localhost");
        SimpleClient adrestusClient2 = new SimpleClient("localhost");
        SimpleClient adrestusClient3 = new SimpleClient("localhost");
        SimpleClient adrestusClient4 = new SimpleClient("localhost");
        (new Thread() {
            public void run() {
                List<Bytes> lis = adrestusServer.receiveData(4);
                lis.forEach(x -> System.out.println(new String(x.toArray())));
                adrestusServer.close();
            }
        }).start();
        Thread.sleep(500);
        adrestusClient1.publishMessage("1".getBytes(StandardCharsets.UTF_8));
        adrestusClient2.publishMessage("2".getBytes(StandardCharsets.UTF_8));
        adrestusClient3.publishMessage("3".getBytes(StandardCharsets.UTF_8));
        adrestusClient4.publishMessage("4".getBytes(StandardCharsets.UTF_8));


        adrestusClient1.close();
        adrestusClient2.close();
        adrestusClient3.close();
        adrestusClient4.close();

    }

    @Test
    public void client_push_to_byzantine_server() throws InterruptedException {
        System.out.println("client_push_to_byzantine_server");
        SimpleServer adrestusServer = new SimpleServer("localhost");

        SimpleClient adrestusClient1 = new SimpleClient("localhost");
        SimpleClient adrestusClient2 = new SimpleClient("localhost");
        SimpleClient adrestusClient3 = new SimpleClient("localhost");
        //AdrestusClient adrestusClient4 = new AdrestusClient("localhost");
        CountDownLatch latch=new CountDownLatch(1);
        (new Thread() {
            public void run() {
                List<Bytes> lis = adrestusServer.receiveData(4);
                lis.forEach(x -> System.out.println(new String(x.toArray())));
                adrestusServer.close();
                latch.countDown();
            }
        }).start();
        Thread.sleep(500);
        adrestusClient1.publishMessage("1".getBytes(StandardCharsets.UTF_8));
        adrestusClient2.publishMessage("2".getBytes(StandardCharsets.UTF_8));
        adrestusClient3.publishMessage("3".getBytes(StandardCharsets.UTF_8));
        // adrestusClient4.publishMessage("4".getBytes(StandardCharsets.UTF_8));


        adrestusClient1.close();
        adrestusClient2.close();
        adrestusClient3.close();
        //adrestusClient4.close();
        Thread.sleep(4000);
        latch.await();
    }

    @Test
    public void client_push_to_byzantine_server2() throws InterruptedException {
        System.out.println("client_push_to_byzantine_server2");
        SimpleServer adrestusServer = new SimpleServer("localhost");

        SimpleClient adrestusClient1 = new SimpleClient("localhost");
        SimpleClient adrestusClient2 = new SimpleClient("localhost");
        CountDownLatch latch=new CountDownLatch(1);
        (new Thread() {
            public void run() {
                List<Bytes> lis = adrestusServer.receiveData(4);
                if (!lis.isEmpty())
                    lis.forEach(x -> System.out.println(new String(x.toArray())));
                adrestusServer.close();
                latch.countDown();
            }
        }).start();
        Thread.sleep(500);
        adrestusClient1.publishMessage("1".getBytes(StandardCharsets.UTF_8));
        adrestusClient2.publishMessage("2".getBytes(StandardCharsets.UTF_8));


        adrestusClient1.close();
        adrestusClient2.close();
        Thread.sleep(4000);
        latch.await();
    }
}
