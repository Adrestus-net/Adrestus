package io.Adrestus.network;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;

public class BlockUntilConnectedTest {

    //@Test
    public void simple_test2() throws InterruptedException {
        int N = 2;
        int F = 0;

        CountDownLatch latch = new CountDownLatch(N);
        //server started
        ConsensusServer Server = new ConsensusServer("192.168.1.106", latch);

        latch.await();
        if (Server.getPeers_not_connected() >= N - F) {
            System.out.println("Close with no send");
            Server.close();
            return;
        } else if (Server.getPeers_not_connected() < N - F) {
            Thread.sleep(1000);
            Server.publishMessage("Message".getBytes(StandardCharsets.UTF_8));
            Server.close();
        }
    }

    //@Test
    public void simple_test() throws InterruptedException {


        //client already started and block until server is connected
        (new Thread() {
            int i = 0;

            public void run() {
                ConsensusClient Client = new ConsensusClient("localhost");
                while (true) {
                    if (i == 3) {
                        System.out.println("break");
                        Client.close();
                        break;
                    }
                    byte[] res = Client.receiveData();
                    System.out.println(new String(res));
                    i++;
                }
            }
        }).start();


        Thread.sleep(8000);

        //server started
        ConsensusServer Server = new ConsensusServer("localhost");

        Thread.sleep(100);
        Server.publishMessage("Message".getBytes(StandardCharsets.UTF_8));
        Server.publishMessage("Message".getBytes(StandardCharsets.UTF_8));
        Server.publishMessage("Message".getBytes(StandardCharsets.UTF_8));
        Thread.sleep(3000);

        Server.close();

    }
}
