package io.Adrestus.network;

public class RemoteClientTest {

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
