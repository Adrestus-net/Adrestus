package io.Adrestus.rpc;

import io.Adrestus.config.ConsensusConfiguration;
import io.Adrestus.util.SerializationUtil;
import io.activej.eventloop.Eventloop;
import io.activej.promise.Promises;
import io.activej.rpc.server.RpcRequestHandler;
import io.activej.rpc.server.RpcServer;
import io.activej.serializer.SerializerBuilder;
import lombok.SneakyThrows;
import org.slf4j.Logger;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class RpcErasureServer<T> implements Runnable {
    private final SerializerBuilder rpcSerialize;

    private final Eventloop eventloop;
    private final T typeParameterClass;

    private final IChunksService<T> service;

    private int serializable_length;
    private SerializationUtil<T> valueMapper;
    private InetSocketAddress inetSocketAddress;
    private String host;
    private int port;
    private RpcServer rpcServer;

    static {
        RPCLogger.setLevelOff();
    }

    public RpcErasureServer(T typeParameterClass, String host, int port, Eventloop eventloop, int serializable_length) {
        this.rpcSerialize = SerializerBuilder.create();
        this.host = host;
        this.port = port;
        this.serializable_length = serializable_length;
        this.eventloop = eventloop;
        this.typeParameterClass = typeParameterClass;
        this.valueMapper = new SerializationUtil<T>(this.typeParameterClass.getClass());
        this.service = new ChunksService<T>();
    }

    public RpcErasureServer(String host, int port, Eventloop eventloop) {
        this.rpcSerialize = SerializerBuilder.create();
        this.host = host;
        this.port = port;
        this.eventloop = eventloop;
        this.service = new ChunksService<T>();
        this.inetSocketAddress = null;
        this.typeParameterClass = null;
    }

    public RpcErasureServer(T typeParameterClass, InetSocketAddress inetSocketAddress, Eventloop eventloop, int serializable_length) {
        this.rpcSerialize = SerializerBuilder.create();
        this.inetSocketAddress = inetSocketAddress;
        this.eventloop = eventloop;
        this.serializable_length = serializable_length;
        this.typeParameterClass = typeParameterClass;
        this.valueMapper = new SerializationUtil<T>(this.typeParameterClass.getClass());
        this.service = new ChunksService<T>();
    }

    @SneakyThrows
    @Override
    public void run() {
        if (inetSocketAddress != null) {
            rpcServer = RpcServer.create(eventloop)
                    .withMessageTypes(ErasureRequest.class, ErasureResponse.class)
                    .withSerializerBuilder(this.rpcSerialize)
                    .withHandler(ErasureRequest.class, downloadErasureChunks(service))
                    .withListenAddress(inetSocketAddress);
        } else {
            rpcServer = RpcServer.create(eventloop)
                    .withMessageTypes(ErasureRequest.class, ErasureResponse.class, ConsensusChunksRequest.class, ConsensusChunksRequest2.class, ConsensusChunksRequest3.class, ConsensusChunksRequest4.class, ConsensusChunksResponse.class)
                    .withSerializerBuilder(this.rpcSerialize)
                    .withHandler(ErasureRequest.class, downloadErasureChunks(service))
                    .withHandler(ConsensusChunksRequest.class, downloadAnnounceConsensusChunks(service))
                    .withHandler(ConsensusChunksRequest2.class, downloadPrepareConsensusChunks(service))
                    .withHandler(ConsensusChunksRequest3.class, downloadCommitConsensusChunks(service))
                    .withHandler(ConsensusChunksRequest4.class, downloadVrfAggregateConsensusChunks(service))
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

    private RpcRequestHandler<ErasureRequest, ErasureResponse> downloadErasureChunks(IChunksService<T> service) throws InterruptedException {
        return request -> Promises.loop(0,
                        rc -> rc < ConsensusConfiguration.CYCLES &&
                                (serializable_length == 0 ||
                                        CachedSerializableErasureObject.getInstance().getSerializableErasureObject() == null),
                        rc -> Promises.delay(ConsensusConfiguration.HEARTBEAT_INTERVAL, rc + 1)
                )
                .map(rc -> {
                    if (rc == ConsensusConfiguration.CYCLES)
                        return new ErasureResponse(null);
                    T result = service.downloadErasureChunks();
                    if (result == null)
                        return new ErasureResponse(null);
                    return new ErasureResponse(this.valueMapper.encode(result, serializable_length));
                });
    }

    private RpcRequestHandler<ConsensusChunksRequest, ConsensusChunksResponse> downloadAnnounceConsensusChunks(IChunksService<T> service) throws InterruptedException {
        return request -> Promises.loop(0,
                rc -> rc < ConsensusConfiguration.CYCLES && CachedConsensusPublisherData.getInstance().getDataAtPosition(0) == null,
                rc -> Promises.delay(ConsensusConfiguration.HEARTBEAT_INTERVAL, rc + 1)).map(rc -> {
            if (rc == ConsensusConfiguration.CYCLES)
                return new ConsensusChunksResponse(new byte[1]);
            byte[] result = service.downloadConsensusChunks(0);
            return new ConsensusChunksResponse(result);
        });
    }

    private RpcRequestHandler<ConsensusChunksRequest2, ConsensusChunksResponse> downloadPrepareConsensusChunks(IChunksService<T> service) throws InterruptedException {
        return request -> Promises.loop(0,
                rc -> rc < ConsensusConfiguration.CYCLES && CachedConsensusPublisherData.getInstance().getDataAtPosition(1) == null,
                rc -> Promises.delay(ConsensusConfiguration.HEARTBEAT_INTERVAL, rc + 1)).map(rc -> {
            if (rc == ConsensusConfiguration.CYCLES)
                return new ConsensusChunksResponse(new byte[1]);
            byte[] result = service.downloadConsensusChunks(1);
            return new ConsensusChunksResponse(result);
        });
    }

    private RpcRequestHandler<ConsensusChunksRequest3, ConsensusChunksResponse> downloadCommitConsensusChunks(IChunksService<T> service) throws InterruptedException {
        return request -> Promises.loop(0,
                rc -> rc < ConsensusConfiguration.CYCLES && CachedConsensusPublisherData.getInstance().getDataAtPosition(2) == null,
                rc -> Promises.delay(ConsensusConfiguration.HEARTBEAT_INTERVAL, rc + 1)).map(rc -> {
            if (rc == ConsensusConfiguration.CYCLES)
                return new ConsensusChunksResponse(new byte[1]);
            byte[] result = service.downloadConsensusChunks(2);
            return new ConsensusChunksResponse(result);
        });
    }

    private RpcRequestHandler<ConsensusChunksRequest4, ConsensusChunksResponse> downloadVrfAggregateConsensusChunks(IChunksService<T> service) throws InterruptedException {
        return request -> Promises.loop(0,
                rc -> rc < ConsensusConfiguration.CYCLES && CachedConsensusPublisherData.getInstance().getDataAtPosition(3) == null,
                rc -> Promises.delay(ConsensusConfiguration.HEARTBEAT_INTERVAL, rc + 1)).map(rc -> {
            if (rc == ConsensusConfiguration.CYCLES)
                return new ConsensusChunksResponse(new byte[1]);
            byte[] result = service.downloadConsensusChunks(3);
            return new ConsensusChunksResponse(result);
        });
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
