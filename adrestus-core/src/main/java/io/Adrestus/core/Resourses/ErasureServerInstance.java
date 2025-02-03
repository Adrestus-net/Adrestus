package io.Adrestus.core.Resourses;

import io.Adrestus.network.CachedEventLoop;
import io.Adrestus.network.IPFinder;
import io.Adrestus.rpc.RpcErasureServer;
import lombok.SneakyThrows;

import java.io.IOException;

import static io.Adrestus.config.ConsensusConfiguration.ERASURE_SERVER_PORT;

public final class ErasureServerInstance {

    private static volatile ErasureServerInstance instance;

    private static RpcErasureServer server;

    private ErasureServerInstance() throws IOException {
        if (instance != null) {
            throw new IllegalStateException("Already initialized.");
        }
        Init();
    }

    @SneakyThrows
    public static ErasureServerInstance getInstance() {
        var result = instance;
        if (result == null) {
            synchronized (ErasureServerInstance.class) {
                result = instance;
                if (result == null) {
                    result = new ErasureServerInstance();
                    instance = result;
                }
            }
        }
        return result;
    }

    private static void Init() throws IOException {
        server = new RpcErasureServer(IPFinder.getLocal_address(), ERASURE_SERVER_PORT, CachedEventLoop.getInstance().getEventloop());
        new Thread(server).start();
    }

    public RpcErasureServer getServer() {
        return server;
    }

    public static void setServer(RpcErasureServer server) {
        ErasureServerInstance.server = server;
    }
}
