package io.Adrestus.rpc;

import io.Adrestus.util.SerializationFuryUtil;
import io.activej.eventloop.Eventloop;
import io.activej.reactor.AbstractNioReactive;
import io.activej.reactor.Reactor;
import io.activej.rpc.client.RpcClient;
import io.activej.rpc.client.sender.strategy.RpcStrategies;
import io.activej.rpc.client.sender.strategy.RpcStrategy;
import io.activej.rpc.protocol.RpcMessage;
import io.activej.serializer.BinarySerializer;
import io.activej.serializer.SerializerFactory;
import io.distributedLedger.LevelDBTransactionWrapper;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.activej.rpc.client.sender.strategy.RpcStrategies.firstAvailable;
import static io.activej.rpc.client.sender.strategy.RpcStrategies.server;

public class RpcAdrestusClient<T> extends AbstractNioReactive implements AutoCloseable {
    private static Logger LOG = LoggerFactory.getLogger(RpcAdrestusClient.class);

    private int TIMEOUT = 8000;

    private final BinarySerializer<RpcMessage> rpc_serialize;
    private final Eventloop eventloop;
    private T typeParameterClass;
    private List<InetSocketAddress> inetSocketAddresses;
    private InetSocketAddress inetSocketAddress;
    private String host;
    private int port;
    private RpcClient client;


    public RpcAdrestusClient(T typeParameterClass, InetSocketAddress inetSocketAddress, Eventloop eventloop) {
        super(eventloop.getReactor());
        this.rpc_serialize = SerializerFactory.defaultInstance().create((RpcMessage.class));
        this.typeParameterClass = typeParameterClass;
        this.inetSocketAddress = inetSocketAddress;
        this.eventloop = eventloop;
    }

    public RpcAdrestusClient(T typeParameterClass, String host, int port, Eventloop eventloop) {
        super(eventloop.getReactor());
        this.rpc_serialize = SerializerFactory.defaultInstance().create((RpcMessage.class));
        this.typeParameterClass = typeParameterClass;
        this.host = host;
        this.port = port;
        this.eventloop = eventloop;
    }

    public RpcAdrestusClient(T typeParameterClass, String host, int port, int timeout, Eventloop eventloop) {
        super(eventloop.getReactor());
        this.rpc_serialize = SerializerFactory.defaultInstance().create((RpcMessage.class));
        this.typeParameterClass = typeParameterClass;
        this.host = host;
        this.port = port;
        this.eventloop = eventloop;
        this.TIMEOUT = timeout;
    }

    public RpcAdrestusClient(T typeParameterClass, List<InetSocketAddress> inetSocketAddresses) {
        super(Reactor.getCurrentReactor());
        this.rpc_serialize = SerializerFactory.defaultInstance().create((RpcMessage.class));
        this.typeParameterClass = typeParameterClass;
        this.inetSocketAddresses = inetSocketAddresses;
        this.eventloop = Reactor.getCurrentReactor();
        new Thread(eventloop).start();
    }

    public RpcAdrestusClient(T typeParameterClass, List<InetSocketAddress> inetSocketAddresses, Eventloop eventloop) {
        super(eventloop.getReactor());
        this.rpc_serialize = SerializerFactory.defaultInstance().create((RpcMessage.class));
        this.typeParameterClass = typeParameterClass;
        this.inetSocketAddresses = inetSocketAddresses;
        this.eventloop = eventloop;
    }

    public RpcAdrestusClient(Type fluentType, List<InetSocketAddress> inetSocketAddresses, Eventloop eventloop) {
        super(eventloop.getReactor());
        this.rpc_serialize = SerializerFactory.defaultInstance().create((RpcMessage.class));
        this.inetSocketAddresses = inetSocketAddresses;
        this.eventloop = eventloop;
    }

    public RpcAdrestusClient(Type fluentType, InetSocketAddress inetSocketAddress, Eventloop eventloop) {
        super(eventloop.getReactor());
        this.rpc_serialize = SerializerFactory.defaultInstance().create((RpcMessage.class));
        this.inetSocketAddress = inetSocketAddress;
        this.eventloop = eventloop;
    }

    public void connect() {
        if (inetSocketAddresses != null) {
            ArrayList<RpcStrategy> rpcStrategy = new ArrayList<>();
            inetSocketAddresses.forEach(val -> rpcStrategy.add(firstAvailable(server(val))));
            client = RpcClient.builder(eventloop)
                    .withSerializer(this.rpc_serialize)
                    .withMessageTypes(TransactionRequest.class, TransactionResponse.class, BlockRequest.class, ListBlockResponse.class, BlockRequest2.class, BlockResponse.class, PatriciaTreeRequest.class, PatriciaTreeResponse.class)
                    .withStrategy(RpcStrategies.roundRobin(rpcStrategy))
                    .withKeepAlive(Duration.ofMillis(TIMEOUT))
                    .withConnectTimeout(Duration.ofMillis(TIMEOUT))
                    .build();
        } else {
            if (inetSocketAddress != null) {
                client = RpcClient.builder(eventloop)
                        .withSerializer(this.rpc_serialize)
                        .withMessageTypes(TransactionRequest.class, TransactionResponse.class, BlockRequest.class, ListBlockResponse.class, BlockRequest2.class, BlockResponse.class, PatriciaTreeRequest.class, PatriciaTreeResponse.class)
                        .withKeepAlive(Duration.ofMillis(TIMEOUT))
                        .withStrategy(server(inetSocketAddress))
                        .withConnectTimeout(Duration.ofMillis(TIMEOUT))
                        .build();
                ;
            } else {
                client = RpcClient.builder(eventloop)
                        .withSerializer(this.rpc_serialize)
                        .withMessageTypes(TransactionRequest.class, TransactionResponse.class, BlockRequest.class, ListBlockResponse.class, BlockRequest2.class, BlockResponse.class, PatriciaTreeRequest.class, PatriciaTreeResponse.class)
                        .withStrategy(server(new InetSocketAddress(host, port)))
                        .withKeepAlive(Duration.ofMillis(TIMEOUT))
                        .withConnectTimeout(Duration.ofMillis(TIMEOUT))
                        .build();

            }
        }
        try {
            this.client.startFuture().get(TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            //e.printStackTrace();
            throw new IllegalArgumentException("Connection could not be established " + e.toString());
        }
    }

    public Map<String, LevelDBTransactionWrapper<T>> getTransactionDatabase(String hash) {
        if (inetSocketAddresses != null) {
            ArrayList<TransactionResponse> responses = new ArrayList<TransactionResponse>();
            ArrayList<String> toCompare = new ArrayList<String>();
            inetSocketAddresses.forEach(val -> {
                TransactionResponse response = getTransactionDatabase(this.client, hash).get();
                if (response.getByte_data() != null)
                    responses.add(response);
            });
            responses.removeIf(Objects::isNull);
            responses.forEach(val -> toCompare.add(Hex.toHexString(val.getByte_data())));
            toCompare.removeIf(Objects::isNull);
            if (toCompare.isEmpty()) {
                LOG.info("Download blocks failed empty response");
                return new HashMap<String, LevelDBTransactionWrapper<T>>();
            }

            Map<String, Long> collect = toCompare.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
            byte[] map_data = Hex.decode(collect.keySet().stream().findFirst().get());
            Map<String, LevelDBTransactionWrapper<T>> toSend = (Map<String, LevelDBTransactionWrapper<T>>) SerializationFuryUtil.getInstance().getFury().deserialize(map_data);
            collect.clear();
            responses.clear();
            toCompare.clear();
            return toSend;
        } else {
            TransactionResponse response = getTransactionDatabase(this.client, hash).get();
            return (Map<String, LevelDBTransactionWrapper<T>>) (SerializationFuryUtil.getInstance().getFury().deserialize(response.getByte_data()));
        }
    }

    @SneakyThrows
    private Optional<TransactionResponse> getTransactionDatabase(RpcClient rpcClient, String hash) {
        try {
            TransactionResponse response = rpcClient.getReactor().submit(
                            () -> rpcClient
                                    .<TransactionRequest, TransactionResponse>sendRequest(new TransactionRequest(hash)))
                    .get(TIMEOUT, TimeUnit.MILLISECONDS);
            if (response.getByte_data() == null)
                return Optional.empty();

            return Optional.of(response);
        } catch (Exception e) {
            e.printStackTrace();
            LOG.info(e.toString());
        }
        return Optional.empty();
    }

    @SneakyThrows
    public List<T> getBlock(List<String> hash) {
        Optional<BlockResponse> val = (Optional<BlockResponse>) getBlockResponse(this.client, hash);
        if (val.isEmpty())
            return null;
        return (List<T>) SerializationFuryUtil.getInstance().getFury().deserialize(val.get().getByte_data());
    }

    @SneakyThrows
    public List<T> getBlocksList(String hash) {
        if (inetSocketAddresses != null) {
            ArrayList<ListBlockResponse> responses = new ArrayList<ListBlockResponse>();
            ArrayList<String> toCompare = new ArrayList<String>();
            inetSocketAddresses.forEach(val -> {
                ListBlockResponse blockResponse = getBlockListResponse(this.client, hash);
                if (blockResponse.getByte_data() != null)
                    responses.add(blockResponse);
            });
            responses.removeIf(Objects::isNull);
            responses.forEach(val -> toCompare.add(Hex.toHexString(SerializationFuryUtil.getInstance().getFury().serialize(val))));
            toCompare.removeIf(Objects::isNull);
            if (toCompare.isEmpty()) {
                LOG.info("Download blocks failed empty response");
                throw new IllegalArgumentException("Download blocks failed empty response");
            }

            Map<String, Long> collect = toCompare.stream()
                    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
            byte[] list_data = ((ListBlockResponse) SerializationFuryUtil.getInstance().getFury().deserialize(Hex.decode(collect.keySet().stream().findFirst().get()))).getByte_data();
            List<T> toSend = (List<T>) SerializationFuryUtil.getInstance().getFury().deserialize(list_data);
            collect.clear();
            responses.clear();
            toCompare.clear();
            return toSend;
        } else {
            byte[] data = getBlockListResponse(this.client, hash).getByte_data();
            return data == null ? new ArrayList<T>() : (List<T>) SerializationFuryUtil.getInstance().getFury().deserialize(data);
        }
    }

    @SneakyThrows
    public List<T> getPatriciaTreeList(String hash) {
        if (inetSocketAddresses != null) {
            ArrayList<PatriciaTreeResponse> responses = new ArrayList<PatriciaTreeResponse>();
            ArrayList<String> toCompare = new ArrayList<String>();
            inetSocketAddresses.forEach(val -> {
                PatriciaTreeResponse blockResponse = getPatriciaTreeListResponse(this.client, hash);
                if (blockResponse.getByte_data() != null)
                    responses.add(blockResponse);
            });
            responses.removeIf(Objects::isNull);
            responses.forEach(val -> toCompare.add(Hex.toHexString(SerializationFuryUtil.getInstance().getFury().serialize(val))));
            toCompare.removeIf(Objects::isNull);
            if (toCompare.isEmpty()) {
                LOG.info("Download PatriciaTree failed empty response");
                throw new IllegalArgumentException("Download PatriciaTree failed empty response");
            }

            Map<String, Long> collect = toCompare.stream()
                    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
            byte[] list_data = ((PatriciaTreeResponse) SerializationFuryUtil.getInstance().getFury().deserialize(Hex.decode(collect.keySet().stream().findFirst().get()))).getByte_data();
            List<T> toSend = (List<T>) SerializationFuryUtil.getInstance().getFury().deserialize(list_data);
            collect.clear();
            responses.clear();
            toCompare.clear();
            return toSend;
        } else {
            byte[] data = getPatriciaTreeListResponse(this.client, hash).getByte_data();
            return data == null ? new ArrayList<T>() : (List<T>) SerializationFuryUtil.getInstance().getFury().deserialize(data);
        }
    }

    public void close() {
        try {
            if (client != null) {
                client.stopFuture().get(10000, TimeUnit.MILLISECONDS);
                client = null;
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    @SneakyThrows
    private Optional<BlockResponse> getBlockResponse(RpcClient rpcClient, List<String> hashes) {
        try {
            BlockResponse response = rpcClient.getReactor().submit(
                            () -> rpcClient
                                    .<BlockRequest2, BlockResponse>sendRequest(new BlockRequest2(hashes)))
                    .get(TIMEOUT, TimeUnit.MILLISECONDS);
            if (response.getByte_data() == null)
                return Optional.empty();

            return Optional.of(response);
        } catch (Exception e) {
            e.printStackTrace();
            LOG.info(e.toString());
        }
        return Optional.empty();
    }

    //IF DOWNLOAD NOT PRINT PROBABLY EXCEPTION CAUGHT ADD LOG.INFO
    private ListBlockResponse getBlockListResponse(RpcClient rpcClient, String hash) {
        try {
            ListBlockResponse response = rpcClient.getReactor().submit(
                            () -> rpcClient
                                    .<BlockRequest, ListBlockResponse>sendRequest(new BlockRequest(hash), TIMEOUT))
                    .get(TIMEOUT, TimeUnit.MILLISECONDS);
            LOG.info("Download: ..... " + response.hashCode());
            return response;
        } catch (ExecutionException e) {
            e.printStackTrace();
            LOG.error(e.toString());
        } catch (InterruptedException e) {
            e.printStackTrace();
            LOG.error(e.toString());
        } catch (TimeoutException e) {
            e.printStackTrace();
            LOG.error(e.toString());
        }
        return new ListBlockResponse(null);
    }

    private PatriciaTreeResponse getPatriciaTreeListResponse(RpcClient rpcClient, String hash) {
        try {
            PatriciaTreeResponse response = rpcClient.getReactor().submit(
                            () -> rpcClient
                                    .<PatriciaTreeRequest, PatriciaTreeResponse>sendRequest(new PatriciaTreeRequest(hash), TIMEOUT))
                    .get(TIMEOUT, TimeUnit.MILLISECONDS);
            LOG.info("Download: ..... " + response.hashCode());
            return response;
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        return new PatriciaTreeResponse(null);
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public static Logger getLOG() {
        return LOG;
    }

    public static void setLOG(Logger LOG) {
        RpcAdrestusClient.LOG = LOG;
    }

    public RpcClient getClient() {
        return client;
    }

    public void setClient(RpcClient client) {
        this.client = client;
    }

    public InetSocketAddress getInetSocketAddress() {
        return inetSocketAddress;
    }

    public void setInetSocketAddress(InetSocketAddress inetSocketAddress) {
        this.inetSocketAddress = inetSocketAddress;
    }

    public List<InetSocketAddress> getInetSocketAddresses() {
        return inetSocketAddresses;
    }

    public void setInetSocketAddresses(List<InetSocketAddress> inetSocketAddresses) {
        this.inetSocketAddresses = inetSocketAddresses;
    }

    public T getTypeParameterClass() {
        return typeParameterClass;
    }

    public void setTypeParameterClass(T typeParameterClass) {
        this.typeParameterClass = typeParameterClass;
    }

    public Eventloop getEventloop() {
        return eventloop;
    }

    public int getTIMEOUT() {
        return TIMEOUT;
    }

    public void setTIMEOUT(int TIMEOUT) {
        this.TIMEOUT = TIMEOUT;
    }
}
