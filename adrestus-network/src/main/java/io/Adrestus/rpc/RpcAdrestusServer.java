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
import io.activej.promise.Promise;
import io.activej.rpc.server.RpcRequestHandler;
import io.activej.rpc.server.RpcServer;
import io.activej.serializer.SerializerBuilder;
import io.distributedLedger.DatabaseInstance;
import io.distributedLedger.PatriciaTreeInstance;
import lombok.SneakyThrows;

import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class RpcAdrestusServer<T> implements Runnable {

    private final SerializerBuilder rpcserialize;
    private final Eventloop eventloop;
    private final T typeParameterClass;
    private final SerializationUtil valueMapper;
    private final SerializationUtil<T> valueMapper2;
    private InetSocketAddress inetSocketAddress;
    private String host;
    private int port;
    private RpcServer rpcServer;
    private DatabaseInstance instance;
    private PatriciaTreeInstance patriciaTreeInstance;

    static {
        RPCLogger.setLevelOff();
    }


    public RpcAdrestusServer(T typeParameterClass, String host, int port, Eventloop eventloop) {
        this.rpcserialize = SerializerBuilder.create();
        this.host = host;
        this.port = port;
        this.eventloop = eventloop;
        this.typeParameterClass = typeParameterClass;
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(ECP.class, ctx -> new ECPmapper()));
        list.add(new SerializationUtil.Mapping(ECP2.class, ctx -> new ECP2mapper()));
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        list.add(new SerializationUtil.Mapping(TreeMap.class, ctx -> new CustomSerializerTreeMap()));
        list.add(new SerializationUtil.Mapping(MemoryTreePool.class, ctx -> new MemoryTreePoolSerializer()));
        this.valueMapper = new SerializationUtil(typeParameterClass.getClass(), list, true);
        this.valueMapper2 = new SerializationUtil<T>(typeParameterClass.getClass(), list, true);
    }

    public RpcAdrestusServer(T typeParameterClass, InetSocketAddress inetSocketAddress, Eventloop eventloop) {
        this.rpcserialize = SerializerBuilder.create();
        this.inetSocketAddress = inetSocketAddress;
        this.eventloop = eventloop;
        this.typeParameterClass = typeParameterClass;
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(ECP.class, ctx -> new ECPmapper()));
        list.add(new SerializationUtil.Mapping(ECP2.class, ctx -> new ECP2mapper()));
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        list.add(new SerializationUtil.Mapping(TreeMap.class, ctx -> new CustomSerializerTreeMap()));
        list.add(new SerializationUtil.Mapping(MemoryTreePool.class, ctx -> new MemoryTreePoolSerializer()));
        this.valueMapper = new SerializationUtil(typeParameterClass.getClass(), list, true);
        this.valueMapper2 = new SerializationUtil<T>(typeParameterClass.getClass(), list, true);
    }

    public RpcAdrestusServer(T typeParameterClass, DatabaseInstance instance, InetSocketAddress inetSocketAddress, Eventloop eventloop) {
        this.rpcserialize = SerializerBuilder.create();
        this.instance = instance;
        this.inetSocketAddress = inetSocketAddress;
        this.eventloop = eventloop;
        this.typeParameterClass = typeParameterClass;
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(ECP.class, ctx -> new ECPmapper()));
        list.add(new SerializationUtil.Mapping(ECP2.class, ctx -> new ECP2mapper()));
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        list.add(new SerializationUtil.Mapping(TreeMap.class, ctx -> new CustomSerializerTreeMap()));
        list.add(new SerializationUtil.Mapping(MemoryTreePool.class, ctx -> new MemoryTreePoolSerializer()));
        this.valueMapper = new SerializationUtil(typeParameterClass.getClass(), list, true);
        this.valueMapper2 = new SerializationUtil<T>(typeParameterClass.getClass(), list, true);
    }

    public RpcAdrestusServer(T typeParameterClass, PatriciaTreeInstance patriciaTreeInstance, InetSocketAddress inetSocketAddress, Eventloop eventloop) {
        this.rpcserialize = SerializerBuilder.create();
        this.patriciaTreeInstance = patriciaTreeInstance;
        this.inetSocketAddress = inetSocketAddress;
        this.eventloop = eventloop;
        this.typeParameterClass = typeParameterClass;
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(ECP.class, ctx -> new ECPmapper()));
        list.add(new SerializationUtil.Mapping(ECP2.class, ctx -> new ECP2mapper()));
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        list.add(new SerializationUtil.Mapping(TreeMap.class, ctx -> new CustomSerializerTreeMap()));
        list.add(new SerializationUtil.Mapping(MemoryTreePool.class, ctx -> new MemoryTreePoolSerializer()));
        this.valueMapper = new SerializationUtil(typeParameterClass.getClass(), list, true);
        this.valueMapper2 = new SerializationUtil<T>(typeParameterClass.getClass(), list, true);
    }

    public RpcAdrestusServer(T typeParameterClass, DatabaseInstance instance, String host, int port, Eventloop eventloop) {
        this.rpcserialize = SerializerBuilder.create();
        this.instance = instance;
        this.host = host;
        this.port = port;
        this.eventloop = eventloop;
        this.typeParameterClass = typeParameterClass;
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(ECP.class, ctx -> new ECPmapper()));
        list.add(new SerializationUtil.Mapping(ECP2.class, ctx -> new ECP2mapper()));
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        list.add(new SerializationUtil.Mapping(TreeMap.class, ctx -> new CustomSerializerTreeMap()));
        list.add(new SerializationUtil.Mapping(MemoryTreePool.class, ctx -> new MemoryTreePoolSerializer()));
        this.valueMapper = new SerializationUtil(typeParameterClass.getClass(), list, true);
        this.valueMapper2 = new SerializationUtil<T>(typeParameterClass.getClass(), list, true);
    }

    public RpcAdrestusServer(T typeParameterClass, PatriciaTreeInstance patriciaTreeInstance, String host, int port, Eventloop eventloop) {
        this.rpcserialize = SerializerBuilder.create();
        this.patriciaTreeInstance = patriciaTreeInstance;
        this.host = host;
        this.port = port;
        this.eventloop = eventloop;
        this.typeParameterClass = typeParameterClass;
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(ECP.class, ctx -> new ECPmapper()));
        list.add(new SerializationUtil.Mapping(ECP2.class, ctx -> new ECP2mapper()));
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        list.add(new SerializationUtil.Mapping(TreeMap.class, ctx -> new CustomSerializerTreeMap()));
        list.add(new SerializationUtil.Mapping(MemoryTreePool.class, ctx -> new MemoryTreePoolSerializer()));
        this.valueMapper = new SerializationUtil(typeParameterClass.getClass(), list, true);
        this.valueMapper2 = new SerializationUtil<T>(typeParameterClass.getClass(), list, true);
    }

    @SneakyThrows
    @Override
    public void run() {
        IService<T> service = null;
        if (instance != null) {
            service = new Service(typeParameterClass.getClass(), instance);
        } else {
            if (patriciaTreeInstance != null)
                service = new Service(typeParameterClass.getClass(), patriciaTreeInstance);
            else
                service = new Service(typeParameterClass.getClass());
        }
        if (inetSocketAddress != null) {
            rpcServer = RpcServer.create(eventloop)
                    .withMessageTypes(BlockRequest.class, ListBlockResponse.class, BlockRequest2.class, BlockResponse.class, PatriciaTreeRequest.class, PatriciaTreeResponse.class)
                    .withSerializerBuilder(this.rpcserialize)
                    .withHandler(BlockRequest.class, download_blocks(service))
                    .withHandler(BlockRequest2.class, migrate_block(service))
                    .withHandler(PatriciaTreeRequest.class, downloadPatriciaTree(service))
                    .withListenAddress(inetSocketAddress);
        } else {
            rpcServer = RpcServer.create(eventloop)
                    .withMessageTypes(BlockRequest.class, ListBlockResponse.class, BlockRequest2.class, BlockResponse.class, PatriciaTreeRequest.class, PatriciaTreeResponse.class)
                    .withSerializerBuilder(this.rpcserialize)
                    .withHandler(BlockRequest.class, download_blocks(service))
                    .withHandler(BlockRequest2.class, migrate_block(service))
                    .withHandler(PatriciaTreeRequest.class, downloadPatriciaTree(service))
                    .withListenAddress(new InetSocketAddress(host, port));
        }
        rpcServer.listen();
    }

    private RpcRequestHandler<BlockRequest, ListBlockResponse> download_blocks(IService<T> service) {
        return request -> {
            List<T> result;
            try {
                result = service.download(request.hash);
            } catch (Exception e) {
                return Promise.ofException(e);
            }
            return Promise.of(new ListBlockResponse(this.valueMapper.encode_list(result)));
        };
    }

    private RpcRequestHandler<PatriciaTreeRequest, PatriciaTreeResponse> downloadPatriciaTree(IService<T> service) {
        return request -> {
            List<T> result;
            PatriciaTreeResponse response;
            try {
                result = service.downloadPatriciaTree(request.hash);
                response = new PatriciaTreeResponse(this.valueMapper.encode_list(result));
            } catch (Exception e) {
                return Promise.ofException(e);
            }
            return Promise.of(response);
        };
    }

    private RpcRequestHandler<BlockRequest2, BlockResponse> migrate_block(IService<T> service) {
        return request -> {
            List<T> result;
            try {
                result = service.migrateBlock(request.getHash());
            } catch (Exception e) {
                return Promise.ofException(e);
            }
            if (result.isEmpty())
                return Promise.of(new BlockResponse(null));
            else
                return Promise.of(new BlockResponse(this.valueMapper2.encode_list(result)));
        };
    }

    public void close() {
        rpcServer.close();
        rpcServer.stopMonitoring();
        rpcServer = null;
    }
}