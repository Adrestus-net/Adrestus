package io.Adrestus.network;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

public class RemoteClientTest {


    @Test
    public void simple_test2() throws InterruptedException {
        (new Thread() {
            int i=0;
            public void run() {
                ConsensusServer server=new ConsensusServer("localhost");
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                server.publishMessage("HELLO".getBytes(StandardCharsets.UTF_8));

            }
        }).start();
        (new Thread() {
            int i=0;
            public void run() {
                ConsensusClient Client = new ConsensusClient("localhost");
                byte[] res2 = Client.receiveData();
                System.out.println(new String(res2));

            }
        }).start();
        Thread.sleep(2000);
    }
    //@Test
    public void simple_test() throws InterruptedException {

        (new Thread() {
            int i=0;
            public void run() {
                ConsensusClient Client = new ConsensusClient("192.168.1.106");
                Client.send_heartbeat("1");
                String res=Client.rec_heartbeat();
                System.out.println(res);
                byte[] res2 = Client.receiveData();
                System.out.println(new String(res2));
                i++;

            }
        }).start();
        Thread.sleep(20000);
    }
}
