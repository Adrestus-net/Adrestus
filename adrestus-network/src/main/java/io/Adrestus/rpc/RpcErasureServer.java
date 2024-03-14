package io.Adrestus.rpc;

import io.Adrestus.config.ConsensusConfiguration;
import io.Adrestus.util.SerializationUtil;
import io.activej.eventloop.Eventloop;
import io.activej.promise.Promise;
import io.activej.rpc.server.RpcRequestHandler;
import io.activej.rpc.server.RpcServer;
import io.activej.serializer.SerializerBuilder;
import lombok.SneakyThrows;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class RpcErasureServer<T> implements Runnable {
    private final SerializerBuilder rpcSerialize;

    private final Eventloop eventloop;
    private final T typeParameterClass;

    private final IErasureService<T> service;

    private int serializable_length;
    private SerializationUtil<T> valueMapper;
    private InetSocketAddress inetSocketAddress;
    private String host;
    private int port;
    private RpcServer rpcServer;


    public RpcErasureServer(T typeParameterClass, String host, int port, Eventloop eventloop, int serializable_length) {
        this.rpcSerialize = SerializerBuilder.create();
        this.host = host;
        this.port = port;
        this.serializable_length = serializable_length;
        this.eventloop = eventloop;
        this.typeParameterClass = typeParameterClass;
        this.valueMapper = new SerializationUtil<T>(this.typeParameterClass.getClass());
        this.service = new ErasureService<T>();
    }

    public RpcErasureServer(T typeParameterClass, InetSocketAddress inetSocketAddress, Eventloop eventloop, int serializable_length) {
        this.rpcSerialize = SerializerBuilder.create();
        this.inetSocketAddress = inetSocketAddress;
        this.eventloop = eventloop;
        this.serializable_length = serializable_length;
        this.typeParameterClass = typeParameterClass;
        this.valueMapper = new SerializationUtil<T>(this.typeParameterClass.getClass());
        this.service = new ErasureService<T>();
    }

    @SneakyThrows
    @Override
    public void run() {
        if (inetSocketAddress != null) {
            rpcServer = RpcServer.create(eventloop)
                    .withMessageTypes(ErasureRequest.class, ErasureResponse.class)
                    .withSerializerBuilder(this.rpcSerialize)
                    .withHandler(ErasureRequest.class, downloadChunks(service))
                    .withListenAddress(inetSocketAddress);
        } else {
            rpcServer = RpcServer.create(eventloop)
                    .withMessageTypes(ErasureRequest.class, ErasureResponse.class)
                    .withSerializerBuilder(this.rpcSerialize)
                    .withHandler(ErasureRequest.class, downloadChunks(service))
                    .withListenAddress(new InetSocketAddress(host, port));
        }
        rpcServer.listen();
    }

    public int getSerializable_length() {
        return serializable_length;
    }

    public void setSerializable_length(int serializable_length) {
        this.serializable_length = serializable_length;
    }

    private RpcRequestHandler<ErasureRequest, ErasureResponse> downloadChunks(IErasureService<T> service) throws InterruptedException {
        return request -> {
            T result;
            try {
                int rc = 0;
                while (rc < ConsensusConfiguration.CYCLES && serializable_length == 0) {
                    rc++;
                    Thread.sleep(ConsensusConfiguration.HEARTBEAT_INTERVAL);
                }
                if (rc == ConsensusConfiguration.CYCLES)
                    return Promise.of(new ErasureResponse(null));
                result = service.downloadChunks();
                if (result == null)
                    return Promise.of(new ErasureResponse(null));
                return Promise.of(new ErasureResponse(this.valueMapper.encode(result, serializable_length)));
            } catch (Exception e) {
                return Promise.ofException(e);
            }

        };
    }

    @SneakyThrows
    public void close() {
        rpcServer.closeFuture().cancel(true);
        try {
            rpcServer.closeFuture().get(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
        rpcServer.close();
        rpcServer.stopMonitoring();
        rpcServer = null;
    }
}
