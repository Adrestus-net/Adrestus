package io.Adrestus.rpc;

import io.Adrestus.crypto.bls.BLS381.ECP;
import io.Adrestus.crypto.bls.BLS381.ECP2;
import io.Adrestus.crypto.bls.mapper.ECP2mapper;
import io.Adrestus.crypto.bls.mapper.ECPmapper;
import io.Adrestus.crypto.elliptic.mapper.BigIntegerSerializer;
import io.Adrestus.crypto.elliptic.mapper.CustomSerializerTreeMap;
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
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.activej.rpc.client.sender.RpcStrategies.server;

public class RpcAdrestusClient<T> {
    private static Logger LOG = LoggerFactory.getLogger(RpcAdrestusClient.class);

    private static final int TIMEOUT = 4000;

    private final SerializationUtil<ListBlockResponse> serializationUtil;
    private final SerializerBuilder rpc_serialize;
    private final Eventloop eventloop;
    private final T typeParameterClass;
    private final SerializationUtil valueMapper;
    private final SerializationUtil valueMapper2;

    private List<InetSocketAddress> inetSocketAddresses;
    private String host;
    private int port;
    private RpcClient client;

    static {
        RPCLogger.setLevelOff();
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
        this.serializationUtil = new SerializationUtil<ListBlockResponse>(ListBlockResponse.class, list);
        this.valueMapper = new SerializationUtil(typeParameterClass.getClass(), list, true);
        this.valueMapper2 = new SerializationUtil(typeParameterClass.getClass(), list);
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
        this.serializationUtil = new SerializationUtil<ListBlockResponse>(ListBlockResponse.class, list);
        this.valueMapper2 = new SerializationUtil(typeParameterClass.getClass(), list);
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
        this.serializationUtil = new SerializationUtil<ListBlockResponse>(ListBlockResponse.class, list);
        this.valueMapper2 = new SerializationUtil(typeParameterClass.getClass(), list);
        this.valueMapper = new SerializationUtil(typeParameterClass.getClass(), list, true);
    }


    public void connect() {
        if (inetSocketAddresses != null) {
            ArrayList<RpcStrategy> strategies = new ArrayList<>();
            inetSocketAddresses.forEach(x -> strategies.add(server(x)));
            RpcStrategyList rpcStrategyList = RpcStrategyList.ofStrategies(strategies);
            client = RpcClient.create(eventloop)
                    .withSerializerBuilder(this.rpc_serialize)
                    .withMessageTypes(BlockRequest.class, ListBlockResponse.class, BlockRequest2.class, BlockResponse.class)
                    .withStrategy(RpcStrategyRoundRobin.create(rpcStrategyList));
        } else {
            client = RpcClient.create(eventloop)
                    .withSerializerBuilder(this.rpc_serialize)
                    .withMessageTypes(BlockRequest.class, ListBlockResponse.class, BlockRequest2.class, BlockResponse.class)
                    .withStrategy(server(new InetSocketAddress(host, port)));
        }
        try {
            this.client.startFuture().get();
        } catch (Exception e) {
            LOG.info("Connection could not be established");
        }
    }

    @SneakyThrows
    public T getBlock(String hash) {
        Optional<BlockResponse> val = (Optional<BlockResponse>) getBlockResponse(this.client, hash);
        if (val.isEmpty())
            return null;
        return (T) this.valueMapper2.decode(val.get().getByte_data());
    }

    @SneakyThrows
    public List<T> getBlocksList(String hash) {
        if (inetSocketAddresses != null) {
            ArrayList<ListBlockResponse> responses = new ArrayList<ListBlockResponse>();
            ArrayList<String> toCompare = new ArrayList<String>();
            inetSocketAddresses.forEach(val -> responses.add(getBlockListResponse(this.client, hash)));
            responses.forEach(val -> toCompare.add(Hex.toHexString(this.serializationUtil.encode(val))));
            toCompare.removeIf(Objects::isNull);
            if (toCompare.isEmpty()) {
                LOG.info("Download blocks failed empty response");
            }

            Map<String, Long> collect = toCompare.stream()
                    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
            byte[] list_data = this.serializationUtil.decode(Hex.decode(collect.keySet().stream().findFirst().get())).getByte_data();
            List<T> toSend = this.valueMapper.decode_list(list_data);
            collect.clear();
            return toSend;
        } else {
            return this.valueMapper.decode_list(getBlockListResponse(this.client, hash).getByte_data());
        }
    }

    public void close() {
        try {
            client.stopFuture().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    @SneakyThrows
    private Optional<BlockResponse> getBlockResponse(RpcClient rpcClient, String name) {
        try {
            BlockResponse response = rpcClient.getEventloop().submit(
                            () -> rpcClient
                                    .<BlockRequest2, BlockResponse>sendRequest(new BlockRequest2(name)))
                    .get();
            if (response.getByte_data() == null)
                return Optional.empty();

            return Optional.of(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    private ListBlockResponse getBlockListResponse(RpcClient rpcClient, String name) {
        try {
            ListBlockResponse response = rpcClient.getEventloop().submit(
                            () -> rpcClient
                                    .<BlockRequest, ListBlockResponse>sendRequest(new BlockRequest(name), TIMEOUT))
                    .get();
            ///LOG.info("Download: ..... " + response.getAbstractBlock().toString());
            return response;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
