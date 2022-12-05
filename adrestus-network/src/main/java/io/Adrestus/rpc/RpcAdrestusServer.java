package io.Adrestus.rpc;


import com.google.common.reflect.TypeParameter;
import com.google.common.reflect.TypeToken;
import io.Adrestus.core.AbstractBlock;
import io.Adrestus.crypto.bls.BLS381.ECP;
import io.Adrestus.crypto.bls.BLS381.ECP2;
import io.Adrestus.crypto.bls.mapper.ECP2mapper;
import io.Adrestus.crypto.bls.mapper.ECPmapper;
import io.Adrestus.crypto.elliptic.mapper.BigIntegerSerializer;
import io.Adrestus.crypto.elliptic.mapper.CustomSerializerTreeMap;
import io.Adrestus.util.SerializationUtil;
import io.activej.eventloop.Eventloop;
import io.activej.promise.Promise;
import io.activej.rpc.server.RpcRequestHandler;
import io.activej.rpc.server.RpcServer;
import io.activej.serializer.SerializerBuilder;
import lombok.SneakyThrows;

import java.lang.reflect.Type;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class RpcAdrestusServer<T extends Object> implements Runnable {

    private final SerializerBuilder rpcserialize;
    private final Eventloop eventloop;
    private final T typeParameterClass;
    private final SerializationUtil valueMapper;
    private InetSocketAddress inetSocketAddress;
    private String host;
    private int port;
    private RpcServer rpcServer;

    static {
        RPCLogger.setLevelOff();
    }


    public RpcAdrestusServer(T typeParameterClass, String host, int port, Eventloop eventloop) {
        rpcserialize = SerializerBuilder.create();
        this.host = host;
        this.port = port;
        this.eventloop = eventloop;
        this.typeParameterClass = typeParameterClass;
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(ECP.class, ctx -> new ECPmapper()));
        list.add(new SerializationUtil.Mapping(ECP2.class, ctx -> new ECP2mapper()));
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        list.add(new SerializationUtil.Mapping(TreeMap.class, ctx -> new CustomSerializerTreeMap()));
        this.valueMapper=new SerializationUtil(typeParameterClass.getClass(),list,true);
    }

    public RpcAdrestusServer(T typeParameterClass, InetSocketAddress inetSocketAddress, Eventloop eventloop) {
        rpcserialize = SerializerBuilder.create();
        this.inetSocketAddress = inetSocketAddress;
        this.eventloop = eventloop;
        this.typeParameterClass = typeParameterClass;
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(ECP.class, ctx -> new ECPmapper()));
        list.add(new SerializationUtil.Mapping(ECP2.class, ctx -> new ECP2mapper()));
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        list.add(new SerializationUtil.Mapping(TreeMap.class, ctx -> new CustomSerializerTreeMap()));
        this.valueMapper=new SerializationUtil(typeParameterClass.getClass(),list,true);
    }


    @SneakyThrows
    @Override
    public void run() {
        if (inetSocketAddress != null) {
            rpcServer = RpcServer.create(eventloop)
                    .withMessageTypes(Request.class, Response.class)
                    .withSerializerBuilder(this.rpcserialize)
                    .withHandler(Request.class, helloServiceRequestHandler(new Service(AbstractBlock.class)))
                    .withListenAddress(inetSocketAddress);
        } else {
            rpcServer = RpcServer.create(eventloop)
                    .withMessageTypes(Request.class, Response.class)
                    .withSerializerBuilder(this.rpcserialize)
                    .withHandler(Request.class, helloServiceRequestHandler(new Service(AbstractBlock.class)))
                    .withListenAddress(new InetSocketAddress(host, port));
        }
        rpcServer.listen();
    }

    private RpcRequestHandler<Request, Response> helloServiceRequestHandler(IService helloService) {
        return request -> {
            List<AbstractBlock> result;
            try {
                result = helloService.download(request.hash);
            } catch (Exception e) {
                return Promise.ofException(e);
            }
            return Promise.of(new Response(this.valueMapper.encode_list(result)));
        };
    }

    public void close() {
        rpcServer.close();
    }
}