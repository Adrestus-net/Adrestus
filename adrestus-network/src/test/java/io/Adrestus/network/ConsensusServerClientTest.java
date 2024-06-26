package io.Adrestus.network;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

public class ConsensusServerClientTest {

    @Test
    public void test_subscribe() throws InterruptedException {
        System.out.println("test_subscribe");
        ConsensusServer.getInstance("localhost");
        ConsensusServer.getInstance("localhost").receive_handler();

        (new Thread() {
            public void run() {
                ConsensusClient adrestusClient1 = new ConsensusClient("localhost");
                byte[] res = adrestusClient1.receiveData();
                if (res != null)
                    System.out.println(new String(res));
                adrestusClient1.close();
            }
        }).start();
        (new Thread() {
            public void run() {
                ConsensusClient adrestusClient2 = new ConsensusClient("localhost");
                byte[] res = adrestusClient2.receiveData();
                if (res != null)
                    System.out.println(new String(res));
                adrestusClient2.close();
            }
        }).start();
        (new Thread() {
            public void run() {
                ConsensusClient adrestusClient3 = new ConsensusClient("localhost");
                byte[] res = adrestusClient3.receiveData();
                if (res != null)
                    System.out.println(new String(res));
                adrestusClient3.close();
            }
        }).start();
        (new Thread() {
            public void run() {
                ConsensusClient adrestusClient4 = new ConsensusClient("localhost");
                byte[] res = adrestusClient4.receiveData();
                if (res != null)
                    System.out.println(new String(res));
                adrestusClient4.close();
            }
        }).start();


        Thread.sleep(2000);
        ConsensusServer.getInstance("localhost").publishMessage("Message".getBytes(StandardCharsets.UTF_8));
        ConsensusServer.getInstance("localhost").close();
    }

    @Test
    public void test_subscribe_with_delay() throws InterruptedException {
        System.out.println("test_subscribe_with_delay");
        ConsensusServer.getInstance("localhost");
        ConsensusServer.getInstance("localhost").receive_handler();
        (new Thread() {
            public void run() {
                ConsensusClient adrestusClient1 = new ConsensusClient("localhost");
                byte[] res = adrestusClient1.receiveData();
                if (res == null)
                    System.out.println("Timeout caught not receiving");
                else
                    System.out.println(new String(res));
                adrestusClient1.close();
            }
        }).start();
        (new Thread() {
            public void run() {
                ConsensusClient adrestusClient2 = new ConsensusClient("localhost");
                byte[] res = adrestusClient2.receiveData();
                if (res == null)
                    System.out.println("Timeout caught not receiving");
                else
                    System.out.println(new String(res));
                adrestusClient2.close();
            }
        }).start();
        (new Thread() {
            public void run() {
                ConsensusClient adrestusClient3 = new ConsensusClient("localhost");
                byte[] res = adrestusClient3.receiveData();
                if (res == null)
                    System.out.println("Timeout caught not receiving");
                else
                    System.out.println(new String(res));
                adrestusClient3.close();
            }
        }).start();
        (new Thread() {
            public void run() {
                ConsensusClient adrestusClient4 = new ConsensusClient("localhost");
                byte[] res = adrestusClient4.receiveData();
                if (res == null)
                    System.out.println("Timeout caught not receiving");
                else
                    System.out.println(new String(res));
                adrestusClient4.close();
            }
        }).start();


        Thread.sleep(5000);
        ConsensusServer.getInstance("localhost").publishMessage("Message".getBytes(StandardCharsets.UTF_8));
        ConsensusServer.getInstance("localhost").close();

    }

    @Test
    public void test_client_push_Server() throws InterruptedException {
        System.out.println("test_client_push_Server");
        ConsensusServer.getInstance("localhost", 4);
        ConsensusServer.getInstance("localhost", 4).receive_handler();

        ConsensusClient adrestusClient1 = new ConsensusClient("localhost");
        ConsensusClient adrestusClient2 = new ConsensusClient("localhost");
        ConsensusClient adrestusClient3 = new ConsensusClient("localhost");
        ConsensusClient adrestusClient4 = new ConsensusClient("localhost");

        (new Thread() {
            public void run() {
                byte[] res = ConsensusServer.getInstance("localhost", 4).receiveData();
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
                byte[] res = ConsensusServer.getInstance("localhost", 4).receiveData();
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
                byte[] res = ConsensusServer.getInstance("localhost", 4).receiveData();
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
                byte[] res = ConsensusServer.getInstance("localhost", 4).receiveData();
                if (res == null)
                    System.out.println("Timeout caught not receiving");
                else
                    System.out.println(new String(res));
            }
        }).start();


        Thread.sleep(100);
        adrestusClient4.pushMessage("4".getBytes(StandardCharsets.UTF_8));
        Thread.sleep(5);
        ConsensusServer.getInstance("localhost", 4).close();
        adrestusClient1.close();
        adrestusClient2.close();
        adrestusClient3.close();
        adrestusClient4.close();
    }

    @Test
    public void test_client_push_Server2() throws InterruptedException {
        System.out.println("test_client_push_Server2");
        ConsensusServer.getInstance("localhost", 4);
        ConsensusServer.getInstance("localhost", 4).receive_handler();
        Thread.sleep(500);
        ConsensusClient adrestusClient1 = new ConsensusClient("localhost");
        ConsensusClient adrestusClient2 = new ConsensusClient("localhost");
        ConsensusClient adrestusClient3 = new ConsensusClient("localhost");
        ConsensusClient adrestusClient4 = new ConsensusClient("localhost");


        (new Thread() {
            public void run() {
                int i = 4;
                while (i >= 1) {
                    byte[] res = ConsensusServer.getInstance("localhost", 4).receiveData();
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
        ConsensusServer.getInstance("localhost", 4).close();
        adrestusClient1.close();
        adrestusClient2.close();
        adrestusClient3.close();
        adrestusClient4.close();
    }

    @Test
    public void endless_server() throws InterruptedException {
        System.out.println("endless_server");
        try {
            ConsensusServer.getInstance("localhost", 4);
            ConsensusServer.getInstance("localhost", 4).receive_handler();

            int i = 4;
            while (i >= 1) {
                byte[] res = ConsensusServer.getInstance("localhost", 4).receiveData();
                if (res.length == 0)
                    System.out.println("Timeout caught not receiving");
                else
                    System.out.println(new String(res));
                i--;
            }
            ConsensusServer.getInstance("localhost", 4).close();
        } catch (AssertionError e) {
            System.out.println("endless_server()");
        }
    }


    @Test
    public void test_client_push_Server2_with_dealy() throws InterruptedException {
        System.out.println("test_client_push_Server2_with_dealy");
        try {
            ConsensusServer.getInstance("localhost", 4);
            ConsensusServer.getInstance("localhost", 4).receive_handler();
            ConsensusClient consensusClient1 = new ConsensusClient("localhost");
            ConsensusClient adrestusClient2 = new ConsensusClient("localhost");
            ConsensusClient adrestusClient3 = new ConsensusClient("localhost");
            ConsensusClient adrestusClient4 = new ConsensusClient("localhost");


            (new Thread() {
                public void run() {
                    int i = 4;
                    while (i >= 1) {
                        byte[] res = ConsensusServer.getInstance("localhost", 4).receiveData();
                        if (res == null)
                            System.out.println("Timeout caught not receiving");
                        else
                            System.out.println(new String(res));
                        i--;
                    }
                }
            }).start();


            Thread.sleep(500);
            consensusClient1.pushMessage("1".getBytes(StandardCharsets.UTF_8));
            adrestusClient2.pushMessage("2".getBytes(StandardCharsets.UTF_8));
            adrestusClient3.pushMessage("3".getBytes(StandardCharsets.UTF_8));
            Thread.sleep(4000);
            adrestusClient4.pushMessage("4".getBytes(StandardCharsets.UTF_8));

            ConsensusServer.getInstance("localhost", 4).close();
            consensusClient1.close();
            adrestusClient2.close();
            adrestusClient3.close();
            adrestusClient4.close();
        } catch (AssertionError e) {
            System.out.println("test_client_push_Server2_with_dealy");
        }
    }

    @Test
    public void Server_push_to_client_with_delay() throws InterruptedException {
        System.out.println("Server_push_to_client_with_delay");
        try {
            ConsensusServer.getInstance("localhost", 1);

            (new Thread() {
                public void run() {
                    ConsensusClient consensusClient1 = new ConsensusClient("localhost");
                    byte[] res = consensusClient1.receiveData();
                    if (res == null)
                        System.out.println("Timeout caught not receiving");
                    else
                        System.out.println(new String(res));
                    consensusClient1.close();
                }
            }).start();

            Thread.sleep(7000);
            ConsensusServer.getInstance("localhost", 1).publishMessage("1".getBytes(StandardCharsets.UTF_8));
            ConsensusServer.getInstance("localhost", 1).close();
        } catch (AssertionError e) {
            System.out.println("Server_push_to_client_with_delay");
        }
    }
}
