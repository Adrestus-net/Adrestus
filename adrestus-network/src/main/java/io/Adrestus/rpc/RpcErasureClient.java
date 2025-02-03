package io.Adrestus.rpc;

import io.activej.eventloop.Eventloop;
import io.activej.reactor.AbstractNioReactive;
import io.activej.rpc.client.RpcClient;
import io.activej.rpc.protocol.RpcMessage;
import io.activej.serializer.BinarySerializer;
import io.activej.serializer.SerializerFactory;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static io.activej.rpc.client.sender.strategy.RpcStrategies.server;

public class RpcErasureClient extends AbstractNioReactive implements AutoCloseable {
    private static Logger LOG = LoggerFactory.getLogger(RpcErasureClient.class);
    private int TIMEOUT = 4000;

    private final BinarySerializer<RpcMessage> rpc_serialize;

    private final Eventloop eventloop;


    private InetSocketAddress inetSocketAddress;
    private String host;
    private int port;
    private RpcClient client;


    public RpcErasureClient(InetSocketAddress inetSocketAddress, Eventloop eventloop) {
        super(eventloop.getReactor());
        this.rpc_serialize = SerializerFactory.defaultInstance().create((RpcMessage.class));
        this.inetSocketAddress = inetSocketAddress;
        this.eventloop = eventloop;
    }

    public RpcErasureClient(String host, int port, Eventloop eventloop) {
        super(eventloop.getReactor());
        this.rpc_serialize = SerializerFactory.defaultInstance().create((RpcMessage.class));
        this.host = host;
        this.port = port;
        this.eventloop = eventloop;
    }

    public RpcErasureClient(String host, int port, int timeout, Eventloop eventloop) {
        super(eventloop.getReactor());
        this.rpc_serialize = SerializerFactory.defaultInstance().create((RpcMessage.class));
        this.host = host;
        this.port = port;
        this.eventloop = eventloop;
        this.TIMEOUT = timeout;
    }


    public void connect() {
        if (inetSocketAddress != null) {
            client = RpcClient.builder(eventloop)
                    .withSerializer(this.rpc_serialize)
                    .withMessageTypes(ErasureRequest.class, ErasureResponse.class)
                    .withKeepAlive(Duration.ofMillis(TIMEOUT))
                    .withStrategy(server(inetSocketAddress))
                    .withConnectTimeout(Duration.ofMillis(TIMEOUT))
                    .build();
        } else {
            client = RpcClient.builder(eventloop)
                    .withSerializer(this.rpc_serialize)
                    .withMessageTypes(ErasureRequest.class, ErasureResponse.class)
                    .withKeepAlive(Duration.ofMillis(TIMEOUT))
                    .withStrategy(server(new InetSocketAddress(host, port)))
                    .withConnectTimeout(Duration.ofMillis(TIMEOUT))
                    .build();
        }
        try {
            this.client.startFuture().get(TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            e.printStackTrace();
            //throw new IllegalArgumentException("Connection could not be established " + e.toString());
        }
    }


    public HashMap<String, byte[]> getErasureChunks(ArrayList<String> keys) {
        Optional<ErasureResponse> blockResponse = download_erasure_chunks(this.client, keys);
        return (HashMap<String, byte[]>) blockResponse.map(ErasureResponse::getErasureData).orElseGet(HashMap::new);
    }


    @SneakyThrows
    private Optional<ErasureResponse> download_erasure_chunks(RpcClient rpcClient, ArrayList<String> keys) {
        try {
            ErasureResponse response = rpcClient.getReactor().submit(
                            () -> rpcClient
                                    .<ErasureRequest, ErasureResponse>sendRequest(new ErasureRequest(keys)))
                    .get(TIMEOUT, TimeUnit.MILLISECONDS);
            if (response.getErasureData().isEmpty())
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
                client.stopFuture().get(10000, TimeUnit.MILLISECONDS);
                client = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
