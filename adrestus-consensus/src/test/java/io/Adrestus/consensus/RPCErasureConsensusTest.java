package io.Adrestus.consensus;

import io.Adrestus.core.Resourses.ErasureServerInstance;
import io.Adrestus.network.CachedEventLoop;
import io.Adrestus.network.IPFinder;
import io.Adrestus.rpc.CachedConsensusPublisherData;
import io.Adrestus.rpc.RpcErasureClient;
import io.activej.eventloop.Eventloop;
import io.activej.promise.Promise;
import io.activej.rpc.client.RpcClient;
import io.activej.rpc.client.sender.strategy.RpcStrategies;
import io.activej.rpc.client.sender.strategy.RpcStrategy;
import io.activej.rpc.server.RpcRequestHandler;
import io.activej.rpc.server.RpcServer;
import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static io.Adrestus.config.ConsensusConfiguration.ERASURE_CLIENT_TIMEOUT;
import static io.Adrestus.config.ConsensusConfiguration.ERASURE_SERVER_PORT;
import static io.activej.rpc.client.sender.strategy.RpcStrategies.server;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class RPCErasureConsensusTest {
    private static org.slf4j.Logger LOG = LoggerFactory.getLogger(RPCErasureConsensusTest.class);
    private static RpcServer serverOne, serverTwo, serverThree;
    private static InetSocketAddress address1, address2, address3;
    @BeforeAll
    public static void setup() throws IOException {
        LOG.info("Starting up");
        address1 = new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 8080);
        address2 = new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 8081);
        address3 = new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 8084);
        Eventloop eventloop = CachedEventLoop.getInstance().getEventloop();
        serverOne = RpcServer.builder(eventloop)
                .withMessageTypes(HelloRequest.class, HelloResponse.class)
                .withHandler(HelloRequest.class, helloServiceRequestHandler(new HelloServiceImplOne()))
                .withListenAddress(address1).build();
        serverOne.listen();


        serverTwo = RpcServer.builder(eventloop)
                .withMessageTypes(HelloRequest.class, HelloResponse.class)
                .withHandler(HelloRequest.class, helloServiceRequestHandler(new HelloServiceImplTwo()))
                .withListenAddress(address2)
                .build();

        serverTwo.listen();

        serverThree = RpcServer.builder(eventloop)
                .withMessageTypes(HelloRequest.class, HelloResponse.class)
                .withHandler(HelloRequest.class,
                        helloServiceRequestHandler(new HelloServiceImplThree()))
                .withListenAddress(address3)
                .build();

        serverThree.listen();
        CachedEventLoop.getInstance().start();


        ArrayList<RpcStrategy> list = new ArrayList<>();
        list.add(server(address1));
        list.add(server(address2));
        list.add(server(address3));

        RpcClient client = RpcClient.builder(eventloop)
                .withMessageTypes(HelloRequest.class, HelloResponse.class)
                .withStrategy(RpcStrategies.roundRobin(list))
                .build();

        try {
            client.startFuture().get(5, TimeUnit.SECONDS);

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

        } catch (Exception e) {
            // e.printStackTrace();
        }
    }
    @Test
    public void test1() {
        ConsensusManager consensusManager = new ConsensusManager(true);
        consensusManager.changeStateTo(ConsensusRoleType.ORGANIZER);
        var organizerphase = consensusManager.getRole().manufacturePhases(ConsensusType.TRANSACTION_BLOCK);
        consensusManager.changeStateTo(ConsensusRoleType.VALIDATOR);
        consensusManager.changeStateTo(ConsensusRoleType.ORGANIZER);
        consensusManager.changeStateTo(ConsensusRoleType.VALIDATOR);
        consensusManager.changeStateTo(ConsensusRoleType.ORGANIZER);
        consensusManager.changeStateTo(ConsensusRoleType.VALIDATOR);
        consensusManager.changeStateTo(ConsensusRoleType.ORGANIZER);
        consensusManager.changeStateTo(ConsensusRoleType.SUPERVISOR);
        BFTConsensusPhase validatorphase = (BFTConsensusPhase) consensusManager.getRole().manufacturePhases(ConsensusType.TRANSACTION_BLOCK);
    }


    @Test
    public void Clienttest() throws InterruptedException {
        ErasureServerInstance.getInstance();
        CachedConsensusPublisherData.getInstance().clear();
        CachedConsensusPublisherData.getInstance().storeAtPosition(1, "test".getBytes());
        CachedConsensusPublisherData.getInstance().storeAtPosition(2, "test".getBytes());
        CachedConsensusPublisherData.getInstance().storeAtPosition(0, "test".getBytes());
        RpcErasureClient<byte[]> collector_client = new RpcErasureClient<byte[]>(IPFinder.getLocalIP(), ERASURE_SERVER_PORT, ERASURE_CLIENT_TIMEOUT, CachedEventLoop.getInstance().getEventloop());
        collector_client.connect();
        //RpcErasureClient<byte[]> collector_client1 = new RpcErasureClient<byte[]>(IPFinder.getLocalIP(), ERASURE_SERVER_PORT, ERASURE_CLIENT_TIMEOUT, CachedEventLoop.getInstance().getEventloop());
        //collector_client1.connect();

        byte[] res = collector_client.getPrepareConsensusChunks("0").get();
        byte[] res1 = collector_client.getPrepareConsensusChunks("1").get();
        byte[] res2 = collector_client.getCommitConsensusChunks("2").get();
        assertEquals("test", new String(res, StandardCharsets.UTF_8));
        assertEquals("test", new String(res1, StandardCharsets.UTF_8));
        assertEquals("test", new String(res2, StandardCharsets.UTF_8));
        //assertEquals("test", new String(res1, StandardCharsets.UTF_8));
        collector_client.close();
        ErasureServerInstance.getInstance().getServer().close();
        //collector_client1.close();
    }


    //Run this code on one machine as server and run the below test code as client in antoher machine
    //@Test
    //make sure you give enough erasurec_client_timeout
    public void ServerTest() throws InterruptedException {
        CachedEventLoop.getInstance().start();
        CachedConsensusPublisherData.getInstance().clear();
        CachedConsensusPublisherData.getInstance().storeAtPosition(0, new String("test").getBytes(StandardCharsets.UTF_8));
        (new Thread() {
            @SneakyThrows
            public void run() {
                System.out.println("before");
                Thread.sleep(10000);
                CachedConsensusPublisherData.getInstance().storeAtPosition(1, new String("test").getBytes(StandardCharsets.UTF_8));
                System.out.println("done");
            }
        }).start();
        CachedConsensusPublisherData.getInstance().storeAtPosition(2, new String("test").getBytes(StandardCharsets.UTF_8));
        ErasureServerInstance.getInstance();

        while (true) {
            Thread.sleep(400);
        }
    }

    //Run this as client machine
    //make sure you give enough erasurec_client_timeout
    //@Test
    public void test() throws InterruptedException {
        CachedEventLoop.getInstance().start();
        RpcErasureClient<byte[]> collector_client = new RpcErasureClient<byte[]>("192.168.1.116", ERASURE_SERVER_PORT, ERASURE_CLIENT_TIMEOUT, CachedEventLoop.getInstance().getEventloop());
        collector_client.connect();
        while (true) {
            byte[] res = collector_client.getPrepareConsensusChunks("1").get();
            byte[] res1 = collector_client.getCommitConsensusChunks("2").get();

            System.out.println(new String(res, StandardCharsets.UTF_8));
            System.out.println(new String(res1, StandardCharsets.UTF_8));
            collector_client.close();
            collector_client.connect();
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
            return rpcClient.getReactor().submit(
                            () -> rpcClient
                                    .<HelloRequest, HelloResponse>sendRequest(new HelloRequest(name), 10000))
                    .get(5, TimeUnit.SECONDS)
                    .message;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return name;
    }
}
