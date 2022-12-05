package io.Adrestus.core;

import io.Adrestus.rpc.RpcAdrestusClient;
import io.Adrestus.rpc.RpcAdrestusServer;
import io.Adrestus.util.GetTime;
import io.activej.eventloop.Eventloop;
import io.activej.promise.Promise;
import io.activej.rpc.client.RpcClient;
import io.activej.rpc.client.sender.RpcStrategy;
import io.activej.rpc.client.sender.RpcStrategyList;
import io.activej.rpc.client.sender.RpcStrategyRoundRobin;
import io.activej.rpc.server.RpcRequestHandler;
import io.activej.rpc.server.RpcServer;
import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;
import io.distributedLedger.DatabaseFactory;
import io.distributedLedger.DatabaseType;
import io.distributedLedger.IDatabase;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import static io.activej.rpc.client.sender.RpcStrategies.server;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class RPCExampleTest {
    private static final int TIMEOUT = 1500;
    private static RpcServer serverOne, serverTwo, serverThree;
    private static Thread thread;
    private static Eventloop eventloop = Eventloop.create().withCurrentThread();
    private static InetSocketAddress address1, address2, address3;

    @BeforeAll
    public static void setup() throws IOException {
        address1 = new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 8080);
        address2 = new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 8081);
        address3 = new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 8084);
        Eventloop eventloop = Eventloop.getCurrentEventloop();
        serverOne = RpcServer.create(eventloop)
                .withMessageTypes(HelloRequest.class, HelloResponse.class)
                .withHandler(HelloRequest.class,
                        helloServiceRequestHandler(new HelloServiceImplOne()))
                .withListenAddress(address1);
        serverOne.listen();


        serverTwo = RpcServer.create(eventloop)
                .withMessageTypes(HelloRequest.class, HelloResponse.class)
                .withHandler(HelloRequest.class,
                        helloServiceRequestHandler(new HelloServiceImplTwo()))
                .withListenAddress(address2);

        serverTwo.listen();

        serverThree = RpcServer.create(eventloop)
                .withMessageTypes(HelloRequest.class, HelloResponse.class)
                .withHandler(HelloRequest.class,
                        helloServiceRequestHandler(new HelloServiceImplThree()))
                .withListenAddress(address3);

        serverThree.listen();
        thread = new Thread(eventloop);
        thread.start();
    }

    @Test
    public void test2() throws Exception {

        ArrayList<RpcStrategy> list = new ArrayList<>();
        list.add(server(address1));
        list.add(server(address2));
        list.add(server(address3));
        RpcStrategyList rpcStrategyList = RpcStrategyList.ofStrategies(list);

        Eventloop eventloop = Eventloop.getCurrentEventloop();
        RpcClient client = RpcClient.create(eventloop)
                .withMessageTypes(HelloRequest.class, HelloResponse.class)
                .withStrategy(RpcStrategyRoundRobin.create(rpcStrategyList));

        client.startFuture().get();

        String currentName;
        String currentResponse;

        currentName = "John";
        currentResponse = blockingRequest(client, currentName);
        System.out.println("Request with name \"" + currentName + "\": " + currentResponse);
        assertEquals("Hello, " + currentName + "!", currentResponse);

        currentName = "Winston";
        currentResponse = blockingRequest(client, currentName);
        System.out.println("Request with name \"" + currentName + "\": " + currentResponse);
        assertEquals("Hello Hello, " + currentName + "!", currentResponse);

        currentName = "Sophia"; // name starts with "s", so hash code is different from previous examples
        currentResponse = blockingRequest(client, currentName);
        System.out.println("Request with name \"" + currentName + "\": " + currentResponse);
        assertEquals("Hello Hello Hello, " + currentName + "!", currentResponse);

    }

    @Test
    public void download() throws Exception {
        IDatabase<String, CommitteeBlock> database = new DatabaseFactory(String.class, CommitteeBlock.class).getDatabase(DatabaseType.ROCKS_DB);
        CommitteeBlock firstblock = new CommitteeBlock();
        firstblock.setDifficulty(112);
        firstblock.getHeaderData().setTimestamp(GetTime.GetTimeStampInString());
        database.save("1", firstblock);
        Thread.sleep(200);
        CommitteeBlock secondblock = new CommitteeBlock();
        secondblock.setDifficulty(117);
        secondblock.getHeaderData().setTimestamp(GetTime.GetTimeStampInString());
        Thread.sleep(200);
        database.save("2", secondblock);
        CommitteeBlock thirdblock = new CommitteeBlock();
        thirdblock.setDifficulty(119);
        thirdblock.getHeaderData().setTimestamp(GetTime.GetTimeStampInString());
        database.save("3", thirdblock);
        Thread.sleep(200);


        Eventloop eventloop = Eventloop.getCurrentEventloop();
        RpcAdrestusServer<AbstractBlock> example = new RpcAdrestusServer<AbstractBlock>(new CommitteeBlock(), "localhost", 8082, eventloop);
        new Thread(example).start();
        RpcAdrestusClient<AbstractBlock> client = new RpcAdrestusClient<AbstractBlock>(new CommitteeBlock(), "localhost", 8082, eventloop);
        client.connect();
        List<AbstractBlock> blocks = client.getSyncResult("1");

        assertEquals(database.findByKey("1").get(), blocks.get(0));
        assertEquals(database.findByKey("2").get(), blocks.get(1));
        assertEquals(database.findByKey("3").get(), blocks.get(2));

        client.close();
        example.close();
        database.delete_db();
    }


    @Test
    public void multiple_download() throws Exception {
        IDatabase<String, CommitteeBlock> database = new DatabaseFactory(String.class, CommitteeBlock.class).getDatabase(DatabaseType.ROCKS_DB);
        CommitteeBlock firstblock = new CommitteeBlock();
        firstblock.setDifficulty(112);
        firstblock.getHeaderData().setTimestamp(GetTime.GetTimeStampInString());
        database.save("1", firstblock);
        Thread.sleep(200);
        CommitteeBlock secondblock = new CommitteeBlock();
        secondblock.setDifficulty(117);
        secondblock.getHeaderData().setTimestamp(GetTime.GetTimeStampInString());
        Thread.sleep(200);
        database.save("2", secondblock);
        CommitteeBlock thirdblock = new CommitteeBlock();
        thirdblock.setDifficulty(119);
        thirdblock.getHeaderData().setTimestamp(GetTime.GetTimeStampInString());
        database.save("3", thirdblock);
        Thread.sleep(200);

        ArrayList<InetSocketAddress> list = new ArrayList<>();
        InetSocketAddress address1 = new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 7070);
        InetSocketAddress address2 = new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 7071);
        InetSocketAddress address3 = new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 7072);
        list.add(address1);
        list.add(address2);
        list.add(address3);
        RpcAdrestusServer server1 = new RpcAdrestusServer(new CommitteeBlock(), address1, eventloop);
        RpcAdrestusServer server2 = new RpcAdrestusServer(new CommitteeBlock(), address2, eventloop);
        RpcAdrestusServer server3 = new RpcAdrestusServer(new CommitteeBlock(), address3, eventloop);
        new Thread(server1).start();
        new Thread(server2).start();
        new Thread(server3).start();

        RpcAdrestusClient client = new RpcAdrestusClient(new CommitteeBlock(), list, eventloop);
        client.connect();
        List<AbstractBlock> blocks = client.getSyncResult("1");

        assertEquals(database.findByKey("1").get(), blocks.get(0));
        assertEquals(database.findByKey("2").get(), blocks.get(1));
        assertEquals(database.findByKey("3").get(), blocks.get(2));

        client.close();
        server1.close();
        server2.close();
        server3.close();
        database.delete_db();

    }

    @AfterAll
    public static void after() {
        serverOne.close();
        serverTwo.close();
        serverThree.close();
    }

    //@Test
    public void test3() throws Exception {
        Eventloop eventloop = Eventloop.create();
        new Thread(eventloop).start();
        RpcClient client = RpcClient.create(eventloop)
                .withMessageTypes(HelloRequest.class, HelloResponse.class)
                .withStrategy(server(new InetSocketAddress("127.0.0.1", 8085)));

        try {
            client.startFuture().get();

            String currentName;
            String currentResponse;

            currentName = "John";
            currentResponse = blockingRequest(client, currentName);
            System.out.println("Request with name \"" + currentName + "\": " + currentResponse);
            assertEquals("Hello, " + currentName + "!", currentResponse);
        } catch (Exception e) {
            System.out.println("Connection not established");
        }
    }

    private interface HelloService {
        String hello(String name) throws Exception;
    }

    private static class HelloServiceImplTwo implements HelloService {
        @Override
        public String hello(String name) throws Exception {
            if (name.equals("--")) {
                throw new Exception("Illegal name");
            }
            return "Hello Hello, " + name + "!";
        }
    }

    private static class HelloServiceImplOne implements HelloService {
        @Override
        public String hello(String name) throws Exception {
            if (name.equals("--")) {
                throw new Exception("Illegal name");
            }
            return "Hello, " + name + "!";
        }
    }


    private static class HelloServiceImplThree implements HelloService {
        @Override
        public String hello(String name) throws Exception {
            if (name.equals("--")) {
                throw new Exception("Illegal name");
            }
            return "Hello Hello Hello, " + name + "!";
        }
    }

    protected static class HelloRequest {
        @Serialize
        public final String name;

        public HelloRequest(@Deserialize("name") String name) {
            this.name = name;
        }
    }

    protected static class HelloResponse {
        @Serialize
        public final String message;

        public HelloResponse(@Deserialize("message") String message) {
            this.message = message;
        }
    }

    private static RpcRequestHandler<HelloRequest, HelloResponse> helloServiceRequestHandler(HelloService helloService) {
        return request -> {
            String result;
            try {
                result = helloService.hello(request.name);
            } catch (Exception e) {
                return Promise.ofException(e);
            }
            return Promise.of(new HelloResponse(result));
        };
    }

    private static String blockingRequest(RpcClient rpcClient, String name) throws Exception {
        try {
            return rpcClient.getEventloop().submit(
                            () -> rpcClient
                                    .<HelloRequest, HelloResponse>sendRequest(new HelloRequest(name), TIMEOUT))
                    .get()
                    .message;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return name;
    }
}
