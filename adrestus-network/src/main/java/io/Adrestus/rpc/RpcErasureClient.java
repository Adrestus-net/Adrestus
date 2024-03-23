package io.Adrestus.rpc;

import io.Adrestus.util.SerializationUtil;
import io.activej.eventloop.Eventloop;
import io.activej.rpc.client.RpcClient;
import io.activej.rpc.client.sender.RpcStrategy;
import io.activej.rpc.client.sender.RpcStrategyList;
import io.activej.rpc.client.sender.RpcStrategyRoundRobin;
import io.activej.serializer.SerializerBuilder;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static io.activej.rpc.client.sender.RpcStrategies.server;

public class RpcErasureClient<T> {
    private static Logger LOG = LoggerFactory.getLogger(RpcErasureClient.class);
    private int TIMEOUT = 4000;

    private final SerializerBuilder rpcSerialize;

    private final Eventloop eventloop;

    private T typeParameterClass;
    private SerializationUtil valueMapper;
    private SerializationUtil<ErasureResponse> serializationUtil;

    private List<InetSocketAddress> inetSocketAddresses;
    private InetSocketAddress inetSocketAddress;
    private String host;
    private int port;
    private RpcClient client;

    static {
        RPCLogger.setLevelOff();
    }

    public RpcErasureClient(T typeParameterClass, InetSocketAddress inetSocketAddress, Eventloop eventloop) {
        this.rpcSerialize = SerializerBuilder.create();
        this.typeParameterClass = typeParameterClass;
        this.inetSocketAddress = inetSocketAddress;
        this.eventloop = eventloop;
        this.serializationUtil = new SerializationUtil<ErasureResponse>(ErasureResponse.class);
        this.valueMapper = new SerializationUtil(this.typeParameterClass.getClass());
    }

    public RpcErasureClient(T typeParameterClass, String host, int port, Eventloop eventloop) {
        this.rpcSerialize = SerializerBuilder.create();
        this.typeParameterClass = typeParameterClass;
        this.host = host;
        this.port = port;
        this.eventloop = eventloop;
        this.serializationUtil = new SerializationUtil<ErasureResponse>(ErasureResponse.class);
        this.valueMapper = new SerializationUtil(this.typeParameterClass.getClass());
    }

    public RpcErasureClient(T typeParameterClass, String host, int port, int timeout, Eventloop eventloop) {
        this.rpcSerialize = SerializerBuilder.create();
        this.typeParameterClass = typeParameterClass;
        this.host = host;
        this.port = port;
        this.eventloop = eventloop;
        this.TIMEOUT = timeout;
        this.serializationUtil = new SerializationUtil<ErasureResponse>(ErasureResponse.class);
        this.valueMapper = new SerializationUtil(this.typeParameterClass.getClass());
    }

    public RpcErasureClient(T typeParameterClass, List<InetSocketAddress> inetSocketAddresses, Eventloop eventloop) {
        this.rpcSerialize = SerializerBuilder.create();
        this.typeParameterClass = typeParameterClass;
        this.inetSocketAddresses = inetSocketAddresses;
        this.eventloop = eventloop;
        this.serializationUtil = new SerializationUtil<ErasureResponse>(ErasureResponse.class);
        this.valueMapper = new SerializationUtil(this.typeParameterClass.getClass());
    }

    public RpcErasureClient(T typeParameterClass, List<InetSocketAddress> inetSocketAddresses, int port, Eventloop eventloop) {
        this.rpcSerialize = SerializerBuilder.create();
        this.typeParameterClass = typeParameterClass;
        this.port = port;
        this.inetSocketAddresses = inetSocketAddresses;
        this.eventloop = eventloop;
        this.serializationUtil = new SerializationUtil<ErasureResponse>(ErasureResponse.class);
        this.valueMapper = new SerializationUtil(this.typeParameterClass.getClass());
    }

    public void connect() {
        if (inetSocketAddresses != null) {
            ArrayList<RpcStrategy> strategies = new ArrayList<>();
            inetSocketAddresses.forEach(x -> strategies.add(server(x)));
            RpcStrategyList rpcStrategyList = RpcStrategyList.ofStrategies(strategies);
            client = RpcClient.create(eventloop)
                    .withSerializerBuilder(this.rpcSerialize)
                    .withMessageTypes(ErasureRequest.class, ErasureResponse.class)
                    .withStrategy(RpcStrategyRoundRobin.create(rpcStrategyList))
                    .withKeepAlive(Duration.ofMillis(TIMEOUT))
                    .withConnectTimeout(Duration.ofMillis(TIMEOUT));
        } else {
            if (inetSocketAddress != null) {
                client = RpcClient.create(eventloop)
                        .withSerializerBuilder(this.rpcSerialize)
                        .withMessageTypes(ErasureRequest.class, ErasureResponse.class)
                        .withKeepAlive(Duration.ofMillis(TIMEOUT))
                        .withStrategy(server(inetSocketAddress))
                        .withConnectTimeout(Duration.ofMillis(TIMEOUT));
            } else {
                client = RpcClient.create(eventloop)
                        .withSerializerBuilder(this.rpcSerialize)
                        .withMessageTypes(ErasureRequest.class, ErasureResponse.class)
                        .withStrategy(server(new InetSocketAddress(host, port)))
                        .withKeepAlive(Duration.ofMillis(TIMEOUT))
                        .withConnectTimeout(Duration.ofMillis(TIMEOUT));

            }
        }
        try {
            this.client.startFuture().get(TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Connection could not be established " + e.toString());
        }
    }


    public List<T> getErasureChunks(byte[] toSend) {
        ArrayList<T> lst = new ArrayList<>();
        if (inetSocketAddresses != null) {
            inetSocketAddresses.forEach(val -> {
                Optional<ErasureResponse> blockResponse = download_erasure_chunks(this.client, toSend);
                if (blockResponse.isPresent())
                    lst.add((T) this.valueMapper.decode(blockResponse.get().getErasure_data()));
            });
        } else {
            Optional<ErasureResponse> blockResponse = download_erasure_chunks(this.client, toSend);
            if (blockResponse.isPresent())
                lst.add((T) this.valueMapper.decode(blockResponse.get().getErasure_data()));
        }
        return lst;
    }

    @SneakyThrows
    private Optional<ErasureResponse> download_erasure_chunks(RpcClient rpcClient, byte[] toSend) {
        try {
            ErasureResponse response = rpcClient.getEventloop().submit(
                            () -> rpcClient
                                    .<ErasureRequest, ErasureResponse>sendRequest(new ErasureRequest(toSend)))
                    .get(TIMEOUT, TimeUnit.MILLISECONDS);
            if (response.getErasure_data() == null)
                return Optional.empty();

            return Optional.of(response);
        } catch (Exception e) {
            e.printStackTrace();
            LOG.info(e.toString());
        }
        return Optional.empty();
    }

    public void close() {
        try {
            if (client != null) {
                client.stopFuture().get(TIMEOUT, TimeUnit.MILLISECONDS);
                client = null;
            }
        } catch (InterruptedException e) {
        } catch (ExecutionException e) {
        } catch (TimeoutException e) {
        }
    }
}
