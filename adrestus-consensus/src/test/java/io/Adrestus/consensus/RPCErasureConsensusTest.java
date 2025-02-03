package io.Adrestus.consensus;

import io.Adrestus.network.CachedEventLoop;
import io.Adrestus.rpc.CachedSerializableErasureObject;
import io.Adrestus.rpc.RpcErasureClient;
import io.Adrestus.rpc.RpcErasureServer;
import io.activej.eventloop.Eventloop;
import io.activej.promise.Promise;
import io.activej.rpc.client.RpcClient;
import io.activej.rpc.client.sender.strategy.RpcStrategies;
import io.activej.rpc.client.sender.strategy.RpcStrategy;
import io.activej.rpc.server.RpcRequestHandler;
import io.activej.rpc.server.RpcServer;
import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static io.activej.rpc.client.sender.strategy.RpcStrategies.server;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    public void test() throws InterruptedException {
        CachedSerializableErasureObject.getInstance().setSerializableErasureObject("1", "1".getBytes(StandardCharsets.UTF_8));
        CachedSerializableErasureObject.getInstance().setSerializableErasureObject("2", "2".getBytes(StandardCharsets.UTF_8));
        CachedSerializableErasureObject.getInstance().setSerializableErasureObject("3", "3".getBytes(StandardCharsets.UTF_8));
        List<String> immutableList = List.of("1", "2", "3");
        ArrayList<String> keys = new ArrayList<>(immutableList);
        RpcErasureServer example = new RpcErasureServer("localhost", 6082, CachedEventLoop.getInstance().getEventloop());
        new Thread(example).start();
        Thread.sleep(500);
        RpcErasureClient client = new RpcErasureClient("localhost", 6082, CachedEventLoop.getInstance().getEventloop());
        client.connect();
        Map<String, byte[]> serializableErasureObject = client.getErasureChunks(keys);

        assertTrue(areMapsEqual(serializableErasureObject, CachedSerializableErasureObject.getSerializableErasureObject()));
        //#########################################################################################################################
        CachedSerializableErasureObject.getInstance().clear();
        client.close();
        example.close();
        example = null;
    }


    public static boolean areMapsEqual(Map<String, byte[]> map1, Map<String, byte[]> map2) {
        if (map1.size() != map2.size()) {
            return false;
        }
        for (Map.Entry<String, byte[]> entry : map1.entrySet()) {
            String key = entry.getKey();
            byte[] value = entry.getValue();
            if (!map2.containsKey(key) || !Arrays.equals(value, map2.get(key))) {
                return false;
            }
        }
        return true;
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
