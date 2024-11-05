package io.Adrestus.network;

import io.activej.eventloop.Eventloop;
import io.activej.promise.Promise;
import io.activej.rpc.server.RpcRequestHandler;
import io.activej.rpc.server.RpcServer;
import lombok.SneakyThrows;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import static io.activej.common.exception.FatalErrorHandlers.rethrow;

public class CachedEventLoop {
    private static volatile CachedEventLoop instance;
    private static Eventloop eventloop;

    @SneakyThrows
    private CachedEventLoop() {
        if (instance != null) {
            throw new IllegalStateException("Already initialized.");
        }
        eventloop = Eventloop.builder()
                .withCurrentThread()
                .withFatalErrorHandler(rethrow())
                .build();
    }

    /**
     * Public accessor.
     *
     * @return an instance of the class.
     */
    public static synchronized CachedEventLoop getInstance() {
        var result = instance;
        if (result == null) {
            synchronized (CachedEventLoop.class) {
                result = instance;
                if (result == null) {
                    instance = result = new CachedEventLoop();
                }
            }
        }
        return result;
    }


    public synchronized Eventloop getEventloop() {
        return eventloop;
    }

    public void setEventloop(Eventloop eventloop) {
        CachedEventLoop.eventloop = eventloop;
    }

    @SneakyThrows
    public void start() {
        RpcServer.builder(eventloop)
                .withMessageTypes(String.class)
                .withHandler(HelloRequest.class, helloServiceRequestHandler(new HelloServiceImplOne()))
                .withListenAddress(new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 6100))
                .build()
                .listen();
        new Thread(eventloop).start();
    }

    private interface HelloService {
        String hello(String name) throws Exception;
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

    private static final class HelloRequest {
        public String name;

    }

    private static final class HelloResponse {
        public String message;

        public HelloResponse(String message) {
            this.message = message;
        }

    }
}
