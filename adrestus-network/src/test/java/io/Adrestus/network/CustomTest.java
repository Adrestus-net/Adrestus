package io.Adrestus.network;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;

public class CustomTest {

    @Test
    public void Server_push_to_client_with_delay() throws InterruptedException {
       /* try {
            ConsensusServer consensusServer = new ConsensusServer("192.168.1.103");

            ConsensusClient consensusClient1 = new ConsensusClient("192.168.1.103");
            ConsensusClient consensusClient2 = new ConsensusClient("192.168.1.103");
            CountDownLatch latch=new CountDownLatch(1);


            (new Thread() {
                public void run() {
                    byte[] data = consensusClient1.receiveData();
                    latch.countDown();
                    if (data == null) {
                        System.out.println("not receive");
                    } else {
                        System.out.println(new String(data));
                    }
                }
            }).start();

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            consensusServer.publishMessage("1".getBytes(StandardCharsets.UTF_8));
            latch.await();
            //consensusServer.publishMessage("1".getBytes(StandardCharsets.UTF_8));
        } catch (AssertionError e) {
            System.out.println("Server_push_to_client_with_delay");
        }*/
    }
}
