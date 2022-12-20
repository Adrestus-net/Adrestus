package io.Adrestus.network;

import io.activej.eventloop.Eventloop;
import io.activej.rpc.server.RpcServer;
import lombok.SneakyThrows;

import java.net.InetAddress;
import java.net.InetSocketAddress;

public class CachedEventLoop {
    private static volatile CachedEventLoop instance;
    private static Eventloop eventloop;

    @SneakyThrows
    private CachedEventLoop() {
        if (instance != null) {
            throw new IllegalStateException("Already initialized.");
        }
        eventloop = Eventloop.create().withCurrentThread();
        RpcServer.create(eventloop)
                .withMessageTypes(String.class)
                .withListenAddress(new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 8084)).listen();
        new Thread(eventloop).start();
    }

    /**
     * Public accessor.
     *
     * @return an instance of the class.
     */
    public static CachedEventLoop getInstance() {
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


    public Eventloop getEventloop() {
        return eventloop;
    }

    public void setEventloop(Eventloop eventloop) {
        CachedEventLoop.eventloop = eventloop;
    }
}
