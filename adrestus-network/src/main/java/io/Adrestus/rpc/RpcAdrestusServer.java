package io.Adrestus.rpc;


import io.Adrestus.MemoryTreePool;
import io.Adrestus.crypto.bls.BLS381.ECP;
import io.Adrestus.crypto.bls.BLS381.ECP2;
import io.Adrestus.crypto.bls.mapper.ECP2mapper;
import io.Adrestus.crypto.bls.mapper.ECPmapper;
import io.Adrestus.crypto.elliptic.mapper.BigDecimalSerializer;
import io.Adrestus.crypto.elliptic.mapper.BigIntegerSerializer;
import io.Adrestus.crypto.elliptic.mapper.CustomFurySerializer;
import io.Adrestus.crypto.elliptic.mapper.CustomSerializerTreeMap;
import io.Adrestus.mapper.MemoryTreePoolSerializer;
import io.Adrestus.util.SerializationUtil;
import io.activej.eventloop.Eventloop;
import io.activej.promise.Promise;
import io.activej.reactor.AbstractNioReactive;
import io.activej.reactor.Reactor;
import io.activej.rpc.protocol.RpcMessage;
import io.activej.rpc.server.RpcRequestHandler;
import io.activej.rpc.server.RpcServer;
import io.activej.serializer.BinarySerializer;
import io.activej.serializer.SerializerFactory;
import io.distributedLedger.DatabaseInstance;
import io.distributedLedger.LevelDBTransactionWrapper;
import io.distributedLedger.PatriciaTreeInstance;
import lombok.SneakyThrows;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class RpcAdrestusServer<T> extends AbstractNioReactive implements AutoCloseable,Runnable {

    private final BinarySerializer<RpcMessage> rpcserialize;
    private final Eventloop eventloop;
    private final T typeParameterClass;

    private final SerializationUtil<T> transactionvalueMapper;

    private SerializationUtil<T> valueMapper;
    private SerializationUtil<T> valueMapper2;
    private InetSocketAddress inetSocketAddress;
    private String host;
    private int port;
    private RpcServer rpcServer;
    private DatabaseInstance instance;
    private PatriciaTreeInstance patriciaTreeInstance;

    private Type fluentType;

    static {
        RPCLogger.setLevelOff();
    }


    public RpcAdrestusServer(T typeParameterClass, String host, int port, Eventloop eventloop) {
        super(eventloop.getReactor());
        this.rpcserialize = SerializerFactory.defaultInstance().create((RpcMessage.class));
        this.host = host;
        this.port = port;
        this.eventloop = eventloop;
        this.typeParameterClass = typeParameterClass;
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(ECP.class, ctx -> new ECPmapper()));
        list.add(new SerializationUtil.Mapping(ECP2.class, ctx -> new ECP2mapper()));
        list.add(new SerializationUtil.Mapping(BigDecimal.class, ctx -> new BigDecimalSerializer()));
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        list.add(new SerializationUtil.Mapping(TreeMap.class, ctx -> new CustomSerializerTreeMap()));
        list.add(new SerializationUtil.Mapping(MemoryTreePool.class, ctx -> new MemoryTreePoolSerializer()));
        this.valueMapper = new SerializationUtil<T>(typeParameterClass.getClass(), list, true);
        this.valueMapper2 = new SerializationUtil<T>(typeParameterClass.getClass(), list, true);
        this.transactionvalueMapper = new SerializationUtil<T>(typeParameterClass.getClass(), list);
    }

    public RpcAdrestusServer(T typeParameterClass, InetSocketAddress inetSocketAddress, Eventloop eventloop) {
        super(eventloop.getReactor());
        this.rpcserialize = SerializerFactory.defaultInstance().create((RpcMessage.class));
        this.inetSocketAddress = inetSocketAddress;
        this.eventloop = eventloop;
        this.typeParameterClass = typeParameterClass;
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(ECP.class, ctx -> new ECPmapper()));
        list.add(new SerializationUtil.Mapping(ECP2.class, ctx -> new ECP2mapper()));
        list.add(new SerializationUtil.Mapping(BigDecimal.class, ctx -> new BigDecimalSerializer()));
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        list.add(new SerializationUtil.Mapping(TreeMap.class, ctx -> new CustomSerializerTreeMap()));
        list.add(new SerializationUtil.Mapping(MemoryTreePool.class, ctx -> new MemoryTreePoolSerializer()));
        this.valueMapper = new SerializationUtil<T>(typeParameterClass.getClass(), list, true);
        this.valueMapper2 = new SerializationUtil<T>(typeParameterClass.getClass(), list, true);
        this.transactionvalueMapper = new SerializationUtil<T>(typeParameterClass.getClass(), list);
    }

    public RpcAdrestusServer(T typeParameterClass, Type fluentType, InetSocketAddress inetSocketAddress, Eventloop eventloop) {
        super(eventloop.getReactor());
        this.fluentType = fluentType;
        this.rpcserialize = SerializerFactory.defaultInstance().create((RpcMessage.class));
        this.inetSocketAddress = inetSocketAddress;
        this.eventloop = eventloop;
        this.typeParameterClass = typeParameterClass;
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(ECP.class, ctx -> new ECPmapper()));
        list.add(new SerializationUtil.Mapping(ECP2.class, ctx -> new ECP2mapper()));
        list.add(new SerializationUtil.Mapping(BigDecimal.class, ctx -> new BigDecimalSerializer()));
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        list.add(new SerializationUtil.Mapping(TreeMap.class, ctx -> new CustomSerializerTreeMap()));
        list.add(new SerializationUtil.Mapping(MemoryTreePool.class, ctx -> new MemoryTreePoolSerializer()));
        this.transactionvalueMapper = new SerializationUtil<T>(fluentType, list);
    }

    public RpcAdrestusServer(T typeParameterClass, DatabaseInstance instance, InetSocketAddress inetSocketAddress, Eventloop eventloop) {
        super(eventloop.getReactor());
        this.rpcserialize = SerializerFactory.defaultInstance().create((RpcMessage.class));
        this.instance = instance;
        this.inetSocketAddress = inetSocketAddress;
        this.eventloop = eventloop;
        this.typeParameterClass = typeParameterClass;
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(ECP.class, ctx -> new ECPmapper()));
        list.add(new SerializationUtil.Mapping(ECP2.class, ctx -> new ECP2mapper()));
        list.add(new SerializationUtil.Mapping(BigDecimal.class, ctx -> new BigDecimalSerializer()));
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        list.add(new SerializationUtil.Mapping(TreeMap.class, ctx -> new CustomSerializerTreeMap()));
        list.add(new SerializationUtil.Mapping(MemoryTreePool.class, ctx -> new MemoryTreePoolSerializer()));
        this.valueMapper = new SerializationUtil<T>(typeParameterClass.getClass(), list, true);
        this.valueMapper2 = new SerializationUtil<T>(typeParameterClass.getClass(), list, true);
        this.transactionvalueMapper = new SerializationUtil<T>(typeParameterClass.getClass(), list);
    }

    public RpcAdrestusServer(T typeParameterClass, PatriciaTreeInstance patriciaTreeInstance, InetSocketAddress inetSocketAddress, Eventloop eventloop) {
        super(eventloop.getReactor());
        this.rpcserialize = SerializerFactory.defaultInstance().create((RpcMessage.class));
        this.patriciaTreeInstance = patriciaTreeInstance;
        this.inetSocketAddress = inetSocketAddress;
        this.eventloop = eventloop;
        this.typeParameterClass = typeParameterClass;
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(ECP.class, ctx -> new ECPmapper()));
        list.add(new SerializationUtil.Mapping(ECP2.class, ctx -> new ECP2mapper()));
        list.add(new SerializationUtil.Mapping(BigDecimal.class, ctx -> new BigDecimalSerializer()));
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        list.add(new SerializationUtil.Mapping(TreeMap.class, ctx -> new CustomSerializerTreeMap()));
        list.add(new SerializationUtil.Mapping(MemoryTreePool.class, ctx -> new MemoryTreePoolSerializer()));
        this.valueMapper = new SerializationUtil<T>(typeParameterClass.getClass(), list, true);
        this.valueMapper2 = new SerializationUtil<T>(typeParameterClass.getClass(), list, true);
        this.transactionvalueMapper = new SerializationUtil<T>(typeParameterClass.getClass(), list);
    }

    public RpcAdrestusServer(T typeParameterClass, DatabaseInstance instance, String host, int port, Eventloop eventloop) {
        super(eventloop.getReactor());
        this.rpcserialize = SerializerFactory.defaultInstance().create((RpcMessage.class));
        this.instance = instance;
        this.host = host;
        this.port = port;
        this.eventloop = eventloop;
        this.typeParameterClass = typeParameterClass;
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(ECP.class, ctx -> new ECPmapper()));
        list.add(new SerializationUtil.Mapping(ECP2.class, ctx -> new ECP2mapper()));
        list.add(new SerializationUtil.Mapping(BigDecimal.class, ctx -> new BigDecimalSerializer()));
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        list.add(new SerializationUtil.Mapping(TreeMap.class, ctx -> new CustomSerializerTreeMap()));
        list.add(new SerializationUtil.Mapping(MemoryTreePool.class, ctx -> new MemoryTreePoolSerializer()));
        this.valueMapper = new SerializationUtil<T>(typeParameterClass.getClass(), list, true);
        this.valueMapper2 = new SerializationUtil<T>(typeParameterClass.getClass(), list, true);
        this.transactionvalueMapper = new SerializationUtil<T>(typeParameterClass.getClass(), list);
    }


    public RpcAdrestusServer(T typeParameterClass, PatriciaTreeInstance patriciaTreeInstance, String host, int port, Eventloop eventloop) {
        super(eventloop.getReactor());
        this.rpcserialize = SerializerFactory.defaultInstance().create((RpcMessage.class));
        this.patriciaTreeInstance = patriciaTreeInstance;
        this.host = host;
        this.port = port;
        this.eventloop = eventloop;
        this.typeParameterClass = typeParameterClass;
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(ECP.class, ctx -> new ECPmapper()));
        list.add(new SerializationUtil.Mapping(ECP2.class, ctx -> new ECP2mapper()));
        list.add(new SerializationUtil.Mapping(BigDecimal.class, ctx -> new BigDecimalSerializer()));
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        list.add(new SerializationUtil.Mapping(TreeMap.class, ctx -> new CustomSerializerTreeMap()));
        list.add(new SerializationUtil.Mapping(MemoryTreePool.class, ctx -> new MemoryTreePoolSerializer()));
        this.valueMapper = new SerializationUtil<T>(typeParameterClass.getClass(), list, true);
        this.valueMapper2 = new SerializationUtil<T>(typeParameterClass.getClass(), list, true);
        this.transactionvalueMapper = new SerializationUtil<T>(typeParameterClass.getClass(), list);
    }

    @SneakyThrows
    @Override
    public void run() {
        IService<T> service = null;
        if (instance != null) {
            service = new Service(typeParameterClass.getClass(), instance);
        } else if (fluentType != null) {
            service = new Service(typeParameterClass.getClass(), fluentType);
        } else if (patriciaTreeInstance != null) {
            service = new Service(typeParameterClass.getClass(), patriciaTreeInstance);
        } else {
            service = new Service(typeParameterClass.getClass());
        }
        if (inetSocketAddress != null) {
            rpcServer = RpcServer.builder(eventloop)
                    .withMessageTypes(TransactionRequest.class, TransactionResponse.class, BlockRequest.class, ListBlockResponse.class, BlockRequest2.class, BlockResponse.class, PatriciaTreeRequest.class, PatriciaTreeResponse.class)
                    .withHandler(BlockRequest.class, download_blocks(service))
                    .withHandler(BlockRequest2.class, migrate_block(service))
                    .withHandler(PatriciaTreeRequest.class, downloadPatriciaTree(service))
                    .withHandler(TransactionRequest.class, downloadTransactionDatabase(service))
                    .withListenAddress(inetSocketAddress)
                    .build();
        } else {
            rpcServer = RpcServer.builder(eventloop)
                    .withMessageTypes(TransactionRequest.class, TransactionResponse.class, BlockRequest.class, ListBlockResponse.class, BlockRequest2.class, BlockResponse.class, PatriciaTreeRequest.class, PatriciaTreeResponse.class)
                    .withHandler(BlockRequest.class, download_blocks(service))
                    .withHandler(BlockRequest2.class, migrate_block(service))
                    .withHandler(PatriciaTreeRequest.class, downloadPatriciaTree(service))
                    .withHandler(TransactionRequest.class, downloadTransactionDatabase(service))
                    .withListenAddress(new InetSocketAddress(host, port))
                    .build();
        }
        eventloop.submit(() -> {
            try {
                rpcServer.listen();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private RpcRequestHandler<BlockRequest, ListBlockResponse> download_blocks(IService<T> service) {
        return request -> {
            List<T> result;
            ListBlockResponse response;
            try {
                result = service.download(request.hash);
                response = new ListBlockResponse(this.valueMapper.encode_list(result));
            } catch (Exception e) {
                return Promise.ofException(e);
            }
            return Promise.of(response);
        };
    }


    private RpcRequestHandler<TransactionRequest, TransactionResponse> downloadTransactionDatabase(IService<T> service) {
        return request -> {
            Map<String, LevelDBTransactionWrapper<T>> result;
            TransactionResponse response;
            try {
                result = service.downloadTransactionDatabase(request.hash);
                response = new TransactionResponse(CustomFurySerializer.getInstance().getFury().serialize((Serializable) result));
            } catch (Exception e) {
                return Promise.ofException(e);
            }
            return Promise.of(response);
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
                if (result.isEmpty())
                    return Promise.of(new BlockResponse(null));
                else
                    return Promise.of(new BlockResponse(this.valueMapper2.encode_list(result)));
            } catch (Exception e) {
                return Promise.ofException(e);
            }

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