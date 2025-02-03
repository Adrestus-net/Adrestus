package io.Adrestus.rpc;

import io.activej.eventloop.Eventloop;
import io.activej.promise.Promise;
import io.activej.reactor.AbstractNioReactive;
import io.activej.rpc.protocol.RpcMessage;
import io.activej.rpc.server.RpcRequestHandler;
import io.activej.rpc.server.RpcServer;
import io.activej.serializer.BinarySerializer;
import io.activej.serializer.SerializerFactory;
import lombok.SneakyThrows;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class RpcErasureServer extends AbstractNioReactive implements AutoCloseable, Runnable {

    private final BinarySerializer<RpcMessage> rpcserialize;
    private final Eventloop eventloop;
    private final IChunksService service;

    private int serializable_length;
    private InetSocketAddress inetSocketAddress;
    private String host;
    private int port;
    private RpcServer rpcServer;


    public RpcErasureServer(String host, int port, Eventloop eventloop) {
        super(eventloop.getReactor());
        this.host = host;
        this.port = port;
        this.eventloop = eventloop;
        this.service = new ChunksService();
        this.rpcserialize = SerializerFactory.defaultInstance().create((RpcMessage.class));
    }

    public RpcErasureServer(InetSocketAddress inetSocketAddress, Eventloop eventloop) {
        super(eventloop.getReactor());
        this.inetSocketAddress = inetSocketAddress;
        this.eventloop = eventloop;
        this.service = new ChunksService();
        this.rpcserialize = SerializerFactory.defaultInstance().create((RpcMessage.class));
    }

    @SneakyThrows
    @Override
    public void run() {
        if (inetSocketAddress != null) {
            rpcServer = RpcServer.builder(eventloop)
                    .withSerializer(this.rpcserialize)
                    .withMessageTypes(ErasureRequest.class, ErasureResponse.class)
                    .withHandler(ErasureRequest.class, downloadErasureChunks(service))
                    .withListenAddress(inetSocketAddress)
                    .build();
        } else {
            rpcServer = RpcServer.builder(eventloop)
                    .withSerializer(this.rpcserialize)
                    .withMessageTypes(ErasureRequest.class, ErasureResponse.class, ConsensusChunksRequest.class, ConsensusChunksRequest2.class, ConsensusChunksRequest3.class, ConsensusChunksRequest4.class, ConsensusChunksResponse.class)
                    .withHandler(ErasureRequest.class, downloadErasureChunks(service))
                    .withListenAddress(new InetSocketAddress(host, port))
                    .build();
        }
        eventloop.submit(() -> {
            try {
                rpcServer.listen();
            } catch (IOException ignore) {
            }
        });
    }

    private RpcRequestHandler<ErasureRequest, ErasureResponse> downloadErasureChunks(IChunksService service) throws InterruptedException {
        return request -> {
            Map<String, byte[]> resultMap = new HashMap<>();
            Map<String, byte[]> originalMap = service.downloadErasureChunks();
            for (String key : request.getValidators()) {
                if (originalMap.containsKey(key)) {
                    resultMap.put(key, originalMap.get(key));
                }
            }
            return Promise.of(new ErasureResponse(resultMap));
        };
    }

    @SneakyThrows
    public void close() {
        rpcServer.closeFuture().cancel(true);
        try {
            rpcServer.closeFuture().get(10000, TimeUnit.MILLISECONDS);
            rpcServer.stopMonitoring();
            rpcServer = null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
