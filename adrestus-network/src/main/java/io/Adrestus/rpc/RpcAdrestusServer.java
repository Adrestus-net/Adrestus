package io.Adrestus.rpc;


import io.Adrestus.core.AbstractBlock;
import io.Adrestus.crypto.bls.BLS381.ECP;
import io.Adrestus.crypto.bls.BLS381.ECP2;
import io.Adrestus.crypto.bls.mapper.ECP2mapper;
import io.Adrestus.crypto.bls.mapper.ECPmapper;
import io.Adrestus.crypto.elliptic.mapper.BigIntegerSerializer;
import io.Adrestus.crypto.elliptic.mapper.CustomSerializerTreeMap;
import io.activej.eventloop.Eventloop;
import io.activej.promise.Promise;
import io.activej.rpc.server.RpcRequestHandler;
import io.activej.rpc.server.RpcServer;
import io.activej.serializer.SerializerBuilder;
import lombok.SneakyThrows;

import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.TreeMap;

public class RpcAdrestusServer implements Runnable {

    private final SerializerBuilder serialize;
    private final Eventloop eventloop;

    private InetSocketAddress inetSocketAddress;
    private String host;
    private int port;
    private RpcServer rpcServer;

    static {
        RPCLogger.setLevelOff();
    }

    public RpcAdrestusServer(String host, int port) {
        serialize = SerializerBuilder
                .create()
                .with(ECP.class, ctx -> new ECPmapper())
                .with(ECP2.class, ctx -> new ECP2mapper())
                .with(BigInteger.class, ctx -> new BigIntegerSerializer())
                .with(TreeMap.class, ctx -> new CustomSerializerTreeMap());
        this.host = host;
        this.port = port;
        this.eventloop = Eventloop.create();
    }

    public RpcAdrestusServer(String host, int port, Eventloop eventloop) {
        serialize = SerializerBuilder
                .create()
                .with(ECP.class, ctx -> new ECPmapper())
                .with(ECP2.class, ctx -> new ECP2mapper())
                .with(BigInteger.class, ctx -> new BigIntegerSerializer())
                .with(TreeMap.class, ctx -> new CustomSerializerTreeMap());
        this.host = host;
        this.port = port;
        this.eventloop = eventloop;
    }

    public RpcAdrestusServer(InetSocketAddress inetSocketAddress, Eventloop eventloop) {
        serialize = SerializerBuilder
                .create()
                .with(ECP.class, ctx -> new ECPmapper())
                .with(ECP2.class, ctx -> new ECP2mapper())
                .with(BigInteger.class, ctx -> new BigIntegerSerializer())
                .with(TreeMap.class, ctx -> new CustomSerializerTreeMap());
        this.inetSocketAddress = inetSocketAddress;
        this.eventloop = eventloop;
    }

    @SneakyThrows
    @Override
    public void run() {
        if (inetSocketAddress != null) {
            rpcServer = RpcServer.create(eventloop)
                    .withMessageTypes(Request.class, Response.class)
                    .withSerializerBuilder(this.serialize)
                    .withHandler(Request.class, helloServiceRequestHandler(new Service()))
                    .withListenAddress(inetSocketAddress);
        } else {
            rpcServer = RpcServer.create(eventloop)
                    .withMessageTypes(Request.class, Response.class)
                    .withSerializerBuilder(this.serialize)
                    .withHandler(Request.class, helloServiceRequestHandler(new Service()))
                    .withListenAddress(new InetSocketAddress(host, port));
        }
        rpcServer.listen();
    }

    private static RpcRequestHandler<Request, Response> helloServiceRequestHandler(IService helloService) {
        return request -> {
            List<AbstractBlock> result;
            try {
                result = helloService.download(request.hash);
            } catch (Exception e) {
                return Promise.ofException(e);
            }
            return Promise.of(new Response(result));
        };
    }

    public void close() {
        rpcServer.close();
    }
}