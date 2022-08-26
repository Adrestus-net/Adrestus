package io.Adrestus.network;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

public class AdrestusServerClient {

    @Test
    public void test_subscribe() throws InterruptedException {
        AdrestusServer adrestusServer = new AdrestusServer("localhost");

        AdrestusClient adrestusClient1 = new AdrestusClient("localhost");
        AdrestusClient adrestusClient2 = new AdrestusClient("localhost");
        AdrestusClient adrestusClient3 = new AdrestusClient("localhost");
        AdrestusClient adrestusClient4 = new AdrestusClient("localhost");


        (new Thread() {
            public void run() {
                byte[] res = adrestusClient1.receiveData();
                System.out.println(new String(res));
            }
        }).start();
        (new Thread() {
            public void run() {
                byte[] res = adrestusClient2.receiveData();
                System.out.println(new String(res));
            }
        }).start();
        (new Thread() {
            public void run() {
                byte[] res = adrestusClient3.receiveData();
                System.out.println(new String(res));
            }
        }).start();
        (new Thread() {
            public void run() {
                byte[] res = adrestusClient4.receiveData();
                System.out.println(new String(res));
            }
        }).start();


        Thread.sleep(2000);
        adrestusServer.publishMessage("Message".getBytes(StandardCharsets.UTF_8));
        adrestusServer.close();
        adrestusClient1.close();
        adrestusClient2.close();
        adrestusClient3.close();
        adrestusClient4.close();
    }

    @Test
    public void test_subscribe_with_delay() throws InterruptedException {
        AdrestusServer adrestusServer = new AdrestusServer("localhost");

        AdrestusClient adrestusClient1 = new AdrestusClient("localhost");
        AdrestusClient adrestusClient2 = new AdrestusClient("localhost");
        AdrestusClient adrestusClient3 = new AdrestusClient("localhost");
        AdrestusClient adrestusClient4 = new AdrestusClient("localhost");


        (new Thread() {
            public void run() {
                byte[] res = adrestusClient1.receiveData();
                if (res == null)
                    System.out.println("Timeout caught not receiving");
                else
                    System.out.println(new String(res));
            }
        }).start();
        (new Thread() {
            public void run() {
                byte[] res = adrestusClient2.receiveData();
                if (res == null)
                    System.out.println("Timeout caught not receiving");
                else
                    System.out.println(new String(res));
            }
        }).start();
        (new Thread() {
            public void run() {
                byte[] res = adrestusClient3.receiveData();
                if (res == null)
                    System.out.println("Timeout caught not receiving");
                else
                    System.out.println(new String(res));
            }
        }).start();
        (new Thread() {
            public void run() {
                byte[] res = adrestusClient4.receiveData();
                if (res == null)
                    System.out.println("Timeout caught not receiving");
                else
                    System.out.println(new String(res));
            }
        }).start();


        Thread.sleep(5000);
        adrestusServer.publishMessage("Message".getBytes(StandardCharsets.UTF_8));
        adrestusServer.close();
        adrestusClient1.close();
        adrestusClient2.close();
        adrestusClient3.close();
        adrestusClient4.close();
    }

    @Test
    public void test_client_push_Server() throws InterruptedException {
        AdrestusServer adrestusServer = new AdrestusServer("localhost");

        AdrestusClient adrestusClient1 = new AdrestusClient("localhost");
        AdrestusClient adrestusClient2 = new AdrestusClient("localhost");
        AdrestusClient adrestusClient3 = new AdrestusClient("localhost");
        AdrestusClient adrestusClient4 = new AdrestusClient("localhost");

        (new Thread() {
            public void run() {
                byte[] res = adrestusServer.receiveData();
                if (res == null)
                    System.out.println("Timeout caught not receiving");
                else
                    System.out.println(new String(res));
            }
        }).start();


        Thread.sleep(100);
        adrestusClient1.pushMessage("1".getBytes(StandardCharsets.UTF_8));

        (new Thread() {
            public void run() {
                byte[] res = adrestusServer.receiveData();
                if (res == null)
                    System.out.println("Timeout caught not receiving");
                else
                    System.out.println(new String(res));
            }
        }).start();


        Thread.sleep(100);
        adrestusClient2.pushMessage("2".getBytes(StandardCharsets.UTF_8));

        (new Thread() {
            public void run() {
                byte[] res = adrestusServer.receiveData();
                if (res == null)
                    System.out.println("Timeout caught not receiving");
                else
                    System.out.println(new String(res));
            }
        }).start();


        Thread.sleep(100);
        adrestusClient3.pushMessage("3".getBytes(StandardCharsets.UTF_8));
        (new Thread() {
            public void run() {
                byte[] res = adrestusServer.receiveData();
                if (res == null)
                    System.out.println("Timeout caught not receiving");
                else
                    System.out.println(new String(res));
            }
        }).start();


        Thread.sleep(100);
        adrestusClient4.pushMessage("4".getBytes(StandardCharsets.UTF_8));
        Thread.sleep(5);
        adrestusServer.close();
        adrestusClient1.close();
        adrestusClient2.close();
        adrestusClient3.close();
        adrestusClient4.close();
    }

    @Test
    public void test_client_push_Server2() throws InterruptedException {
        AdrestusServer adrestusServer = new AdrestusServer("localhost");

        AdrestusClient adrestusClient1 = new AdrestusClient("localhost");
        AdrestusClient adrestusClient2 = new AdrestusClient("localhost");
        AdrestusClient adrestusClient3 = new AdrestusClient("localhost");
        AdrestusClient adrestusClient4 = new AdrestusClient("localhost");


        (new Thread() {
            public void run() {
                int i = 4;
                while (i >= 1) {
                    byte[] res = adrestusServer.receiveData();
                    if (res == null)
                        System.out.println("Timeout caught not receiving");
                    else
                        System.out.println(new String(res));
                    i--;
                }
            }
        }).start();


        Thread.sleep(500);
        adrestusClient1.pushMessage("1".getBytes(StandardCharsets.UTF_8));
        adrestusClient2.pushMessage("2".getBytes(StandardCharsets.UTF_8));
        adrestusClient3.pushMessage("3".getBytes(StandardCharsets.UTF_8));
        adrestusClient4.pushMessage("4".getBytes(StandardCharsets.UTF_8));
        Thread.sleep(500);
        adrestusServer.close();
        adrestusClient1.close();
        adrestusClient2.close();
        adrestusClient3.close();
        adrestusClient4.close();
    }

    @Test
    public void test_client_push_Server2_with_dealy() throws InterruptedException {
        AdrestusServer adrestusServer = new AdrestusServer("localhost");

        AdrestusClient adrestusClient1 = new AdrestusClient("localhost");
        AdrestusClient adrestusClient2 = new AdrestusClient("localhost");
        AdrestusClient adrestusClient3 = new AdrestusClient("localhost");
        AdrestusClient adrestusClient4 = new AdrestusClient("localhost");


        (new Thread() {
            public void run() {
                int i = 4;
                while (i >= 1) {
                    byte[] res = adrestusServer.receiveData();
                    if (res == null)
                        System.out.println("Timeout caught not receiving");
                    else
                        System.out.println(new String(res));
                    i--;
                }
            }
        }).start();


        Thread.sleep(500);
        adrestusClient1.pushMessage("1".getBytes(StandardCharsets.UTF_8));
        adrestusClient2.pushMessage("2".getBytes(StandardCharsets.UTF_8));
        adrestusClient3.pushMessage("3".getBytes(StandardCharsets.UTF_8));
        Thread.sleep(4000);
        adrestusClient4.pushMessage("4".getBytes(StandardCharsets.UTF_8));

        adrestusServer.close();
        adrestusClient1.close();
        adrestusClient2.close();
        adrestusClient3.close();
        adrestusClient4.close();
    }
}
