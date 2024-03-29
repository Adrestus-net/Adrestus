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
        RpcServer.create(eventloop)
                .withMessageTypes(String.class)
                .withListenAddress(new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 6100)).listen();
        new Thread(eventloop).start();
    }

}
