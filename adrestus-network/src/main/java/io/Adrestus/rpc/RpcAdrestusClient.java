package io.Adrestus.rpc;

import io.Adrestus.MemoryTreePool;
import io.Adrestus.crypto.bls.BLS381.ECP;
import io.Adrestus.crypto.bls.BLS381.ECP2;
import io.Adrestus.crypto.bls.mapper.ECP2mapper;
import io.Adrestus.crypto.bls.mapper.ECPmapper;
import io.Adrestus.crypto.elliptic.mapper.BigIntegerSerializer;
import io.Adrestus.crypto.elliptic.mapper.CustomSerializerTreeMap;
import io.Adrestus.mapper.MemoryTreePoolSerializer;
import io.Adrestus.util.SerializationUtil;
import io.activej.eventloop.Eventloop;
import io.activej.rpc.client.RpcClient;
import io.activej.rpc.client.sender.RpcStrategy;
import io.activej.rpc.client.sender.RpcStrategyList;
import io.activej.rpc.client.sender.RpcStrategyRoundRobin;
import io.activej.serializer.SerializerBuilder;
import io.distributedLedger.LevelDBTransactionWrapper;
import lombok.SneakyThrows;
import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.lang.reflect.Type;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.activej.rpc.client.sender.RpcStrategies.server;

public class RpcAdrestusClient<T> {
    private static Logger LOG = LoggerFactory.getLogger(RpcAdrestusClient.class);

    private int TIMEOUT = 4000;

    private SerializationUtil<ListBlockResponse> serializationUtil;
    private SerializationUtil<PatriciaTreeResponse> serializationUtil2;
    private final SerializerBuilder rpc_serialize;
    private final Eventloop eventloop;
    private T typeParameterClass;
    private SerializationUtil valueMapper;
    private SerializationUtil valueMapper2;

    private SerializationUtil transactionvalueMapper;
    private List<InetSocketAddress> inetSocketAddresses;
    private InetSocketAddress inetSocketAddress;
    private String host;
    private int port;
    private RpcClient client;

    static {
        RPCLogger.setLevelOff();
    }


    public RpcAdrestusClient(T typeParameterClass, InetSocketAddress inetSocketAddress, Eventloop eventloop) {
        this.rpc_serialize = SerializerBuilder.create();
        this.typeParameterClass = typeParameterClass;
        this.inetSocketAddress = inetSocketAddress;
        this.eventloop = eventloop;
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(ECP.class, ctx -> new ECPmapper()));
        list.add(new SerializationUtil.Mapping(ECP2.class, ctx -> new ECP2mapper()));
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        list.add(new SerializationUtil.Mapping(TreeMap.class, ctx -> new CustomSerializerTreeMap()));
        list.add(new SerializationUtil.Mapping(MemoryTreePool.class, ctx -> new MemoryTreePoolSerializer()));
        this.serializationUtil2 = new SerializationUtil<PatriciaTreeResponse>(PatriciaTreeResponse.class, list);
        this.serializationUtil = new SerializationUtil<ListBlockResponse>(ListBlockResponse.class, list);
        this.valueMapper = new SerializationUtil(typeParameterClass.getClass(), list, true);
        this.valueMapper2 = new SerializationUtil(typeParameterClass.getClass(), list, true);
    }

    public RpcAdrestusClient(T typeParameterClass, String host, int port, Eventloop eventloop) {
        this.rpc_serialize = SerializerBuilder.create();
        this.typeParameterClass = typeParameterClass;
        this.host = host;
        this.port = port;
        this.eventloop = eventloop;
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(ECP.class, ctx -> new ECPmapper()));
        list.add(new SerializationUtil.Mapping(ECP2.class, ctx -> new ECP2mapper()));
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        list.add(new SerializationUtil.Mapping(TreeMap.class, ctx -> new CustomSerializerTreeMap()));
        list.add(new SerializationUtil.Mapping(MemoryTreePool.class, ctx -> new MemoryTreePoolSerializer()));
        this.serializationUtil2 = new SerializationUtil<PatriciaTreeResponse>(PatriciaTreeResponse.class, list);
        this.serializationUtil = new SerializationUtil<ListBlockResponse>(ListBlockResponse.class, list);
        this.valueMapper = new SerializationUtil(typeParameterClass.getClass(), list, true);
        this.valueMapper2 = new SerializationUtil(typeParameterClass.getClass(), list, true);
    }
    public RpcAdrestusClient(T typeParameterClass, String host, int port,int timeout, Eventloop eventloop) {
        this.rpc_serialize = SerializerBuilder.create();
        this.typeParameterClass = typeParameterClass;
        this.host = host;
        this.port = port;
        this.eventloop = eventloop;
        this.TIMEOUT=timeout;
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(ECP.class, ctx -> new ECPmapper()));
        list.add(new SerializationUtil.Mapping(ECP2.class, ctx -> new ECP2mapper()));
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        list.add(new SerializationUtil.Mapping(TreeMap.class, ctx -> new CustomSerializerTreeMap()));
        list.add(new SerializationUtil.Mapping(MemoryTreePool.class, ctx -> new MemoryTreePoolSerializer()));
        this.serializationUtil2 = new SerializationUtil<PatriciaTreeResponse>(PatriciaTreeResponse.class, list);
        this.serializationUtil = new SerializationUtil<ListBlockResponse>(ListBlockResponse.class, list);
        this.valueMapper = new SerializationUtil(typeParameterClass.getClass(), list, true);
        this.valueMapper2 = new SerializationUtil(typeParameterClass.getClass(), list, true);
    }

    public RpcAdrestusClient(T typeParameterClass, List<InetSocketAddress> inetSocketAddresses) {
        this.rpc_serialize = SerializerBuilder.create();
        this.typeParameterClass = typeParameterClass;
        this.inetSocketAddresses = inetSocketAddresses;
        this.eventloop = Eventloop.create();
        new Thread(eventloop).start();
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(ECP.class, ctx -> new ECPmapper()));
        list.add(new SerializationUtil.Mapping(ECP2.class, ctx -> new ECP2mapper()));
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        list.add(new SerializationUtil.Mapping(TreeMap.class, ctx -> new CustomSerializerTreeMap()));
        list.add(new SerializationUtil.Mapping(MemoryTreePool.class, ctx -> new MemoryTreePoolSerializer()));
        this.serializationUtil2 = new SerializationUtil<PatriciaTreeResponse>(PatriciaTreeResponse.class, list);
        this.serializationUtil = new SerializationUtil<ListBlockResponse>(ListBlockResponse.class, list);
        this.valueMapper2 = new SerializationUtil(typeParameterClass.getClass(), list, true);
        this.valueMapper = new SerializationUtil(typeParameterClass.getClass(), list, true);
    }

    public RpcAdrestusClient(T typeParameterClass, List<InetSocketAddress> inetSocketAddresses, Eventloop eventloop) {
        this.rpc_serialize = SerializerBuilder.create();
        this.typeParameterClass = typeParameterClass;
        this.inetSocketAddresses = inetSocketAddresses;
        this.eventloop = eventloop;
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(ECP.class, ctx -> new ECPmapper()));
        list.add(new SerializationUtil.Mapping(ECP2.class, ctx -> new ECP2mapper()));
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        list.add(new SerializationUtil.Mapping(TreeMap.class, ctx -> new CustomSerializerTreeMap()));
        list.add(new SerializationUtil.Mapping(MemoryTreePool.class, ctx -> new MemoryTreePoolSerializer()));
        this.serializationUtil2 = new SerializationUtil<PatriciaTreeResponse>(PatriciaTreeResponse.class, list);
        this.serializationUtil = new SerializationUtil<ListBlockResponse>(ListBlockResponse.class, list);
        this.valueMapper2 = new SerializationUtil(typeParameterClass.getClass(), list, true);
        this.valueMapper = new SerializationUtil(typeParameterClass.getClass(), list, true);
    }

    public RpcAdrestusClient(Type fluentType, List<InetSocketAddress> inetSocketAddresses, Eventloop eventloop) {
        this.rpc_serialize = SerializerBuilder.create();
        this.inetSocketAddresses = inetSocketAddresses;
        this.eventloop = eventloop;
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(ECP.class, ctx -> new ECPmapper()));
        list.add(new SerializationUtil.Mapping(ECP2.class, ctx -> new ECP2mapper()));
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        list.add(new SerializationUtil.Mapping(TreeMap.class, ctx -> new CustomSerializerTreeMap()));
        list.add(new SerializationUtil.Mapping(MemoryTreePool.class, ctx -> new MemoryTreePoolSerializer()));
        this.transactionvalueMapper = new SerializationUtil(fluentType, list);
    }

    public RpcAdrestusClient(Type fluentType, InetSocketAddress inetSocketAddress, Eventloop eventloop) {
        this.rpc_serialize = SerializerBuilder.create();
        this.inetSocketAddress = inetSocketAddress;
        this.eventloop = eventloop;
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(ECP.class, ctx -> new ECPmapper()));
        list.add(new SerializationUtil.Mapping(ECP2.class, ctx -> new ECP2mapper()));
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        list.add(new SerializationUtil.Mapping(TreeMap.class, ctx -> new CustomSerializerTreeMap()));
        list.add(new SerializationUtil.Mapping(MemoryTreePool.class, ctx -> new MemoryTreePoolSerializer()));
        this.transactionvalueMapper = new SerializationUtil(fluentType, list);
    }

    public void connect() {
        if (inetSocketAddresses != null) {
            ArrayList<RpcStrategy> strategies = new ArrayList<>();
            inetSocketAddresses.forEach(x -> strategies.add(server(x)));
            RpcStrategyList rpcStrategyList = RpcStrategyList.ofStrategies(strategies);
            client = RpcClient.create(eventloop)
                    .withSerializerBuilder(this.rpc_serialize)
                    .withMessageTypes(TransactionRequest.class, TransactionResponse.class, BlockRequest.class, ListBlockResponse.class, BlockRequest2.class, BlockResponse.class, PatriciaTreeRequest.class, PatriciaTreeResponse.class)
                    .withStrategy(RpcStrategyRoundRobin.create(rpcStrategyList))
                    .withKeepAlive(Duration.ofMillis(TIMEOUT))
                    .withConnectTimeout(Duration.ofMillis(TIMEOUT));
        } else {
            if (inetSocketAddress != null) {
                client = RpcClient.create(eventloop)
                        .withSerializerBuilder(this.rpc_serialize)
                        .withMessageTypes(TransactionRequest.class, TransactionResponse.class, BlockRequest.class, ListBlockResponse.class, BlockRequest2.class, BlockResponse.class, PatriciaTreeRequest.class, PatriciaTreeResponse.class)
                        .withKeepAlive(Duration.ofMillis(TIMEOUT))
                        .withStrategy(server(inetSocketAddress))
                        .withConnectTimeout(Duration.ofMillis(TIMEOUT));
            } else {
                client = RpcClient.create(eventloop)
                        .withSerializerBuilder(this.rpc_serialize)
                        .withMessageTypes(TransactionRequest.class, TransactionResponse.class, BlockRequest.class, ListBlockResponse.class, BlockRequest2.class, BlockResponse.class, PatriciaTreeRequest.class, PatriciaTreeResponse.class)
                        .withStrategy(server(new InetSocketAddress(host, port)))
                        .withKeepAlive(Duration.ofMillis(TIMEOUT))
                        .withConnectTimeout(Duration.ofMillis(TIMEOUT));

            }
        }
        try {
            this.client.startFuture().get(TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
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
            Map<String, LevelDBTransactionWrapper<T>> toSend = SerializationUtils.deserialize(map_data);
            collect.clear();
            responses.clear();
            toCompare.clear();
            return toSend;
        } else {
            TransactionResponse response = getTransactionDatabase(this.client, hash).get();
            return (Map<String, LevelDBTransactionWrapper<T>>) (SerializationUtils.deserialize(response.getByte_data()));
        }
    }

    @SneakyThrows
    private Optional<TransactionResponse> getTransactionDatabase(RpcClient rpcClient, String hash) {
        try {
            TransactionResponse response = rpcClient.getEventloop().submit(
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
        return this.valueMapper2.decode_list(val.get().getByte_data());
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
            responses.forEach(val -> toCompare.add(Hex.toHexString(this.serializationUtil.encode(val))));
            toCompare.removeIf(Objects::isNull);
            if (toCompare.isEmpty()) {
                LOG.info("Download blocks failed empty response");
                throw new IllegalArgumentException("Download blocks failed empty response");
            }

            Map<String, Long> collect = toCompare.stream()
                    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
            byte[] list_data = this.serializationUtil.decode(Hex.decode(collect.keySet().stream().findFirst().get())).getByte_data();
            List<T> toSend = this.valueMapper.decode_list(list_data);
            collect.clear();
            responses.clear();
            toCompare.clear();
            return toSend;
        } else {
            byte[] data = getBlockListResponse(this.client, hash).getByte_data();
            return data == null ? new ArrayList<T>() : this.valueMapper.decode_list(data);
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
            responses.forEach(val -> toCompare.add(Hex.toHexString(this.serializationUtil2.encode(val))));
            toCompare.removeIf(Objects::isNull);
            if (toCompare.isEmpty()) {
                LOG.info("Download PatriciaTree failed empty response");
                throw new IllegalArgumentException("Download PatriciaTree failed empty response");
            }

            Map<String, Long> collect = toCompare.stream()
                    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
            byte[] list_data = this.serializationUtil2.decode(Hex.decode(collect.keySet().stream().findFirst().get())).getByte_data();
            List<T> toSend = this.valueMapper.decode_list(list_data);
            collect.clear();
            responses.clear();
            toCompare.clear();
            return toSend;
        } else {
            byte[] data = getPatriciaTreeListResponse(this.client, hash).getByte_data();
            return data == null ? new ArrayList<T>() : this.valueMapper.decode_list(data);
        }
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

    @SneakyThrows
    private Optional<BlockResponse> getBlockResponse(RpcClient rpcClient, List<String> hashes) {
        try {
            BlockResponse response = rpcClient.getEventloop().submit(
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
            ListBlockResponse response = rpcClient.getEventloop().submit(
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
            PatriciaTreeResponse response = rpcClient.getEventloop().submit(
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
}
