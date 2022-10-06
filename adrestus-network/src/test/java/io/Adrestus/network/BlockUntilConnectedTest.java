package io.Adrestus.network;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

public class BlockUntilConnectedTest {
    @Test
    public void simple_test() throws InterruptedException {


        //client already started and block until server is connected
        (new Thread() {
            int i=0;
            public void run() {
                ConsensusClient Client = new ConsensusClient("localhost");
                while (true) {
                    if(i==3){
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


        Thread.sleep(2000);

        //server started
        ConsensusServer Server = new ConsensusServer("localhost");

        Thread.sleep(60);
        Server.publishMessage("Message".getBytes(StandardCharsets.UTF_8));
        Server.publishMessage("Message".getBytes(StandardCharsets.UTF_8));
        Server.publishMessage("Message".getBytes(StandardCharsets.UTF_8));
        Thread.sleep(3000);

        Server.close();

    }
}
