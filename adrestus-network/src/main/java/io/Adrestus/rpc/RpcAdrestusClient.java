package io.Adrestus.rpc;

import io.Adrestus.core.AbstractBlock;
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

public class RpcAdrestusClient {
    private static Logger LOG = LoggerFactory.getLogger(RpcAdrestusClient.class);

    private static final int TIMEOUT = 4000;

    private final SerializationUtil<Response> serializationUtil;
    private final SerializerBuilder serialize;
    private final Eventloop eventloop;

    private List<InetSocketAddress> inetSocketAddresses;
    private String host;
    private int port;
    private RpcClient client;

    static {
        RPCLogger.setLevelOff();
    }

    public RpcAdrestusClient(String host, int port) {
        serialize = SerializerBuilder
                .create()
                .with(ECP.class, ctx -> new ECPmapper())
                .with(ECP2.class, ctx -> new ECP2mapper())
                .with(BigInteger.class, ctx -> new BigIntegerSerializer())
                .with(TreeMap.class, ctx -> new CustomSerializerTreeMap());
        this.host = host;
        this.port = port;
        this.eventloop = Eventloop.create();
        new Thread(eventloop).start();
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(ECP.class, ctx -> new ECPmapper()));
        list.add(new SerializationUtil.Mapping(ECP2.class, ctx -> new ECP2mapper()));
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        list.add(new SerializationUtil.Mapping(TreeMap.class, ctx -> new CustomSerializerTreeMap()));
        this.serializationUtil = new SerializationUtil<Response>(Response.class, list);
    }

    public RpcAdrestusClient(String host, int port, Eventloop eventloop) {
        serialize = SerializerBuilder
                .create()
                .with(ECP.class, ctx -> new ECPmapper())
                .with(ECP2.class, ctx -> new ECP2mapper())
                .with(BigInteger.class, ctx -> new BigIntegerSerializer())
                .with(TreeMap.class, ctx -> new CustomSerializerTreeMap());
        this.host = host;
        this.port = port;
        this.eventloop = eventloop;
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(ECP.class, ctx -> new ECPmapper()));
        list.add(new SerializationUtil.Mapping(ECP2.class, ctx -> new ECP2mapper()));
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        list.add(new SerializationUtil.Mapping(TreeMap.class, ctx -> new CustomSerializerTreeMap()));
        this.serializationUtil = new SerializationUtil<Response>(Response.class, list);
    }

    public RpcAdrestusClient(List<InetSocketAddress> inetSocketAddresses) {
        serialize = SerializerBuilder
                .create()
                .with(ECP.class, ctx -> new ECPmapper())
                .with(ECP2.class, ctx -> new ECP2mapper())
                .with(BigInteger.class, ctx -> new BigIntegerSerializer())
                .with(TreeMap.class, ctx -> new CustomSerializerTreeMap());
        this.inetSocketAddresses = inetSocketAddresses;
        this.eventloop = Eventloop.create();
        new Thread(eventloop).start();
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(ECP.class, ctx -> new ECPmapper()));
        list.add(new SerializationUtil.Mapping(ECP2.class, ctx -> new ECP2mapper()));
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        list.add(new SerializationUtil.Mapping(TreeMap.class, ctx -> new CustomSerializerTreeMap()));
        this.serializationUtil = new SerializationUtil<Response>(Response.class, list);
    }

    public RpcAdrestusClient(List<InetSocketAddress> inetSocketAddresses, Eventloop eventloop) {
        serialize = SerializerBuilder
                .create()
                .with(ECP.class, ctx -> new ECPmapper())
                .with(ECP2.class, ctx -> new ECP2mapper())
                .with(BigInteger.class, ctx -> new BigIntegerSerializer())
                .with(TreeMap.class, ctx -> new CustomSerializerTreeMap());
        this.inetSocketAddresses = inetSocketAddresses;
        this.eventloop = eventloop;
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(ECP.class, ctx -> new ECPmapper()));
        list.add(new SerializationUtil.Mapping(ECP2.class, ctx -> new ECP2mapper()));
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        list.add(new SerializationUtil.Mapping(TreeMap.class, ctx -> new CustomSerializerTreeMap()));
        this.serializationUtil = new SerializationUtil<Response>(Response.class, list);
    }


    public void connect() {
        if (inetSocketAddresses != null) {
            ArrayList<RpcStrategy> strategies = new ArrayList<>();
            inetSocketAddresses.forEach(x -> strategies.add(server(x)));
            RpcStrategyList rpcStrategyList = RpcStrategyList.ofStrategies(strategies);
            client = RpcClient.create(eventloop)
                    .withSerializerBuilder(this.serialize)
                    .withMessageTypes(Request.class, Response.class)
                    .withStrategy(RpcStrategyRoundRobin.create(rpcStrategyList));
        } else {
            client = RpcClient.create(eventloop)
                    .withSerializerBuilder(this.serialize)
                    .withMessageTypes(Request.class, Response.class)
                    .withStrategy(server(new InetSocketAddress(host, port)));
        }
        try {
            this.client.startFuture().get();
        } catch (Exception e) {
            LOG.info("Connection could not be established");
        }
    }

    @SneakyThrows
    public List<AbstractBlock> getSyncResult(String hash) {
        if (inetSocketAddresses != null) {
            ArrayList<Response> responses = new ArrayList<Response>();
            ArrayList<String> toCompare = new ArrayList<String>();
            inetSocketAddresses.forEach(val -> responses.add(blockingRequest(this.client, hash)));
            responses.forEach(val -> toCompare.add(Hex.toHexString(this.serializationUtil.encode(val))));
            toCompare.removeIf(Objects::isNull);
            if (toCompare.isEmpty()) {
                LOG.info("Download blocks failed empty response");
            }

            Map<String, Long> collect = toCompare.stream()
                    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
            List<AbstractBlock> toSend = this.serializationUtil.decode(Hex.decode(collect.keySet().stream().findFirst().get())).getAbstractBlock();
            collect.clear();
            return toSend;
        } else {
            return blockingRequest(this.client, hash).getAbstractBlock();
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

    private static Response blockingRequest(RpcClient rpcClient, String name) {
        try {
            Response response = rpcClient.getEventloop().submit(
                            () -> rpcClient
                                    .<Request, Response>sendRequest(new Request(name), TIMEOUT))
                    .get();
            LOG.info("Download: ..... " + response.getAbstractBlock().toString());
            return response;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
