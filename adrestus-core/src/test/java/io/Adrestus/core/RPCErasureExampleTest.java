package io.Adrestus.core;

import io.Adrestus.config.AdrestusConfiguration;
import io.Adrestus.core.Resourses.CachedZoneIndex;
import io.Adrestus.core.Util.BlockSizeCalculator;
import io.Adrestus.crypto.HashUtil;
import io.Adrestus.crypto.WalletAddress;
import io.Adrestus.crypto.bls.BLS381.ECP;
import io.Adrestus.crypto.bls.BLS381.ECP2;
import io.Adrestus.crypto.bls.mapper.ECP2mapper;
import io.Adrestus.crypto.bls.mapper.ECPmapper;
import io.Adrestus.crypto.bls.model.BLSPrivateKey;
import io.Adrestus.crypto.bls.model.BLSPublicKey;
import io.Adrestus.crypto.bls.model.CachedBLSKeyPair;
import io.Adrestus.crypto.elliptic.ECDSASign;
import io.Adrestus.crypto.elliptic.ECDSASignatureData;
import io.Adrestus.crypto.elliptic.ECKeyPair;
import io.Adrestus.crypto.elliptic.Keys;
import io.Adrestus.crypto.elliptic.mapper.BigIntegerSerializer;
import io.Adrestus.crypto.elliptic.mapper.CustomSerializerTreeMap;
import io.Adrestus.crypto.mnemonic.Mnemonic;
import io.Adrestus.crypto.mnemonic.MnemonicException;
import io.Adrestus.crypto.mnemonic.Security;
import io.Adrestus.crypto.mnemonic.WordList;
import io.Adrestus.erasure.code.ArrayDataEncoder;
import io.Adrestus.erasure.code.EncodingPacket;
import io.Adrestus.erasure.code.OpenRQ;
import io.Adrestus.erasure.code.encoder.SourceBlockEncoder;
import io.Adrestus.erasure.code.parameters.FECParameterObject;
import io.Adrestus.erasure.code.parameters.FECParameters;
import io.Adrestus.erasure.code.parameters.FECParametersPreConditions;
import io.Adrestus.network.CachedEventLoop;
import io.Adrestus.rpc.CachedSerializableErasureObject;
import io.Adrestus.rpc.RpcErasureClient;
import io.Adrestus.rpc.RpcErasureServer;
import io.Adrestus.util.GetTime;
import io.Adrestus.util.SerializationUtil;
import io.activej.eventloop.Eventloop;
import io.activej.promise.Promise;
import io.activej.rpc.client.RpcClient;
import io.activej.rpc.client.sender.RpcStrategy;
import io.activej.rpc.client.sender.RpcStrategyList;
import io.activej.rpc.client.sender.RpcStrategyRoundRobin;
import io.activej.rpc.server.RpcRequestHandler;
import io.activej.rpc.server.RpcServer;
import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static io.activej.rpc.client.sender.RpcStrategies.server;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class RPCErasureExampleTest {

    private static final int TRANSACTION_SIZE = 10;
    private static final int TIMEOUT = 1500;

    private static BLSPrivateKey sk1;
    private static BLSPublicKey vk1;
    private static TransactionBlock transactionBlock;
    private static SerializationUtil<TransactionBlock> encode;
    private static SerializationUtil<Transaction> serenc;
    private static SerializationUtil<SerializableErasureObject> serenc_erasure;

    private static Eventloop eventloop = CachedEventLoop.getInstance().getEventloop();

    private static RpcServer serverOne, serverTwo, serverThree;

    private static Thread thread;
    private static InetSocketAddress address1, address2, address3;
    private static ArrayList<SerializableErasureObject> serializableErasureObjects = new ArrayList<SerializableErasureObject>();
    private static int blocksize;

    @BeforeAll
    public static void setup() throws MnemonicException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException, CloneNotSupportedException, IOException {
        sk1 = new BLSPrivateKey(1);
        vk1 = new BLSPublicKey(sk1);
        CachedBLSKeyPair.getInstance().setPublicKey(vk1);
        CachedBLSKeyPair.getInstance().setPrivateKey(sk1);

        CachedZoneIndex.getInstance().setZoneIndex(0);
        ECDSASign ecdsaSign = new ECDSASign();

        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(ECP.class, ctx -> new ECPmapper()));
        list.add(new SerializationUtil.Mapping(ECP2.class, ctx -> new ECP2mapper()));
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        list.add(new SerializationUtil.Mapping(TreeMap.class, ctx -> new CustomSerializerTreeMap()));
        encode = new SerializationUtil<TransactionBlock>(TransactionBlock.class, list);
        serenc = new SerializationUtil<Transaction>(Transaction.class, list);
        serenc_erasure = new SerializationUtil<SerializableErasureObject>(SerializableErasureObject.class);

        ArrayList<String> addreses = new ArrayList<>();
        ArrayList<Transaction> transactions = new ArrayList<>();
        ArrayList<ECKeyPair> keypair = new ArrayList<>();
        int version = 0x00;
        for (int i = 0; i < TRANSACTION_SIZE; i++) {
            Mnemonic mnem = new Mnemonic(Security.NORMAL, WordList.ENGLISH);
            char[] mnemonic_sequence = mnem.create();
            char[] passphrase = "p4ssphr4se".toCharArray();
            byte[] key = mnem.createSeed(mnemonic_sequence, passphrase);
            ECKeyPair ecKeyPair = Keys.createEcKeyPair(new SecureRandom(key));
            String adddress = WalletAddress.generate_address((byte) version, ecKeyPair.getPublicKey());
            addreses.add(adddress);
            keypair.add(ecKeyPair);
        }


        for (int i = 0; i < TRANSACTION_SIZE - 1; i++) {
            Transaction transaction = new RegularTransaction();
            transaction.setFrom(addreses.get(i));
            transaction.setTo(addreses.get(i + 1));
            transaction.setStatus(StatusType.PENDING);
            transaction.setTimestamp(GetTime.GetTimeStampInString());
            transaction.setZoneFrom(1);
            transaction.setZoneTo(2);
            transaction.setAmount(100);
            transaction.setAmountWithTransactionFee(transaction.getAmount() * (10.0 / 100.0));
            transaction.setNonce(1);
            byte byf[] = serenc.encode(transaction);
            transaction.setHash(HashUtil.sha256_bytetoString(byf));
            await().atMost(500, TimeUnit.MILLISECONDS);

            ECDSASignatureData signatureData = ecdsaSign.secp256SignMessage(Hex.decode(transaction.getHash()), keypair.get(i));
            transaction.setSignature(signatureData);
            //MemoryPool.getInstance().add(transaction);
            transactions.add(transaction);
            await().atMost(1000, TimeUnit.MILLISECONDS);
        }
        transactionBlock = new TransactionBlock();
        transactionBlock.getHeaderData().setPreviousHash("4c89512018237bd0cd458b50ffbf47190fc7aa7d4430093418fa7927a9a46ac3");
        transactionBlock.getHeaderData().setVersion(AdrestusConfiguration.version);
        transactionBlock.getHeaderData().setTimestamp(GetTime.GetTimeStampInString());
        transactionBlock.setStatustype(StatusType.PENDING);
        transactionBlock.setHeight(1);
        transactionBlock.setGeneration(1);
        transactionBlock.setViewID(1);
        transactionBlock.setZone(CachedZoneIndex.getInstance().getZoneIndex());
        transactionBlock.setLeaderPublicKey(CachedBLSKeyPair.getInstance().getPublicKey());
        transactionBlock.setTransactionList(transactions);
        transactionBlock.setHash("hash10");
        transactionBlock.setSize(1);
        transactionBlock.setPatriciaMerkleRoot("1d51602355c8255d11baf4915c500a92e9d027f478dfa2286ee509a7469c08ab");
        transactionBlock.setHash("1d51602355c8255d11baf4915c500a92e9d027f478dfa2286ee509a7469c08ab");

        Receipt.ReceiptBlock receiptBlock = new Receipt.ReceiptBlock(transactionBlock.getHash(), transactionBlock.getHeight(), transactionBlock.getGeneration(), transactionBlock.getMerkleRoot());
        ArrayList<Receipt> receiptList = new ArrayList<>();
        for (int i = 0; i < transactionBlock.getTransactionList().size(); i++) {
            Transaction transaction = transactionBlock.getTransactionList().get(i);
            receiptList.add(new Receipt(transaction.getZoneFrom(), transaction.getZoneTo(), transaction.getTo(), transaction.getAmount(), receiptBlock, (Transaction) transaction.clone(), null, i));
        }

        Map<Integer, Map<Receipt.ReceiptBlock, List<Receipt>>> outbound = receiptList
                .stream()
                .collect(Collectors.groupingBy(Receipt::getZoneTo, Collectors.groupingBy(Receipt::getReceiptBlock)));

        OutBoundRelay outBoundRelay = new OutBoundRelay(outbound);
        transactionBlock.setOutbound(outBoundRelay);


        BlockSizeCalculator sizeCalculator = new BlockSizeCalculator();
        sizeCalculator.setTransactionBlock(transactionBlock);
        blocksize = sizeCalculator.TransactionBlockSizeCalculator();
        byte[] buffer = encode.encode(transactionBlock, blocksize);

        long dataLen = buffer.length;
        int sizeOfCommittee = 4;

        int numSrcBlks = sizeOfCommittee;
        int symbSize = (int) (dataLen / sizeOfCommittee);
        FECParameterObject object = FECParametersPreConditions.CalculateFECParameters(dataLen, symbSize, numSrcBlks);
        FECParameters fecParams = FECParameters.newParameters(object.getDataLen(), object.getSymbolSize(), object.getNumberOfSymbols());

        byte[] data = new byte[fecParams.dataLengthAsInt()];
        System.arraycopy(buffer, 0, data, 0, data.length);
        final ArrayDataEncoder enc = OpenRQ.newEncoder(data, fecParams);
        for (SourceBlockEncoder sbEnc : enc.sourceBlockIterable()) {
            for (EncodingPacket srcPacket : sbEnc.sourcePacketsIterable()) {
                serializableErasureObjects.add(new SerializableErasureObject(object, srcPacket.asArray()));
            }
        }
        ArrayList<byte[]> toSend = new ArrayList<>();
        for (SerializableErasureObject obj : serializableErasureObjects) {
            toSend.add(serenc_erasure.encode(obj));
        }

        address1 = new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 8080);
        address2 = new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 8081);
        address3 = new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 8084);
        Eventloop eventloop = Eventloop.getCurrentEventloop();
        serverOne = RpcServer.create(eventloop)
                .withMessageTypes(RPCExampleTest.HelloRequest.class, RPCExampleTest.HelloResponse.class)
                .withHandler(RPCExampleTest.HelloRequest.class,
                        helloServiceRequestHandler(new HelloServiceImplOne()))
                .withListenAddress(address1);
        serverOne.listen();


        serverTwo = RpcServer.create(eventloop)
                .withMessageTypes(RPCExampleTest.HelloRequest.class, RPCExampleTest.HelloResponse.class)
                .withHandler(RPCExampleTest.HelloRequest.class,
                        helloServiceRequestHandler(new HelloServiceImplTwo()))
                .withListenAddress(address2);

        serverTwo.listen();

        serverThree = RpcServer.create(eventloop)
                .withMessageTypes(RPCExampleTest.HelloRequest.class, RPCExampleTest.HelloResponse.class)
                .withHandler(RPCExampleTest.HelloRequest.class,
                        helloServiceRequestHandler(new HelloServiceImplThree()))
                .withListenAddress(address3);

        serverThree.listen();
        thread = new Thread(eventloop);
        thread.start();


        ArrayList<RpcStrategy> lists = new ArrayList<>();
        lists.add(server(address1));
        lists.add(server(address2));
        lists.add(server(address3));
        RpcStrategyList rpcStrategyList = RpcStrategyList.ofStrategies(lists);

        RpcClient client = RpcClient.create(eventloop)
                .withMessageTypes(RPCExampleTest.HelloRequest.class, RPCExampleTest.HelloResponse.class)
                .withStrategy(RpcStrategyRoundRobin.create(rpcStrategyList));

        try {
            client.startFuture().get(5, TimeUnit.SECONDS);

            String currentName;
            String currentResponse;

            currentName = "John";
            currentResponse = blockingRequest(client, currentName);
            System.out.println("Request with name \"" + currentName + "\": " + currentResponse);
            assertEquals("Hello, " + currentName + "!", currentResponse);

            currentName = "Winston";
            currentResponse = blockingRequest(client, currentName);
            System.out.println("Request with name \"" + currentName + "\": " + currentResponse);
            assertEquals("Hello Hello, " + currentName + "!", currentResponse);

            currentName = "Sophia"; // name starts with "s", so hash code is different from previous examples
            currentResponse = blockingRequest(client, currentName);
            System.out.println("Request with name \"" + currentName + "\": " + currentResponse);
            assertEquals("Hello Hello Hello, " + currentName + "!", currentResponse);

        } catch (Exception e) {
            // e.printStackTrace();
        }
    }

    @Test
    public void test1() {

        CachedSerializableErasureObject.getInstance().setSerializableErasureObject(serializableErasureObjects.get(0));
        RpcErasureServer<SerializableErasureObject> example = new RpcErasureServer<SerializableErasureObject>(new SerializableErasureObject(), "localhost", 7082, eventloop, blocksize);
        new Thread(example).start();
        RpcErasureClient<SerializableErasureObject> client = new RpcErasureClient<SerializableErasureObject>(new SerializableErasureObject(), "localhost", 7082, eventloop);
        client.connect();
        ArrayList<SerializableErasureObject> serializableErasureObject = (ArrayList<SerializableErasureObject>) client.getErasureChunks(new byte[0]);

        //#########################################################################################################################
        CachedSerializableErasureObject.getInstance().setSerializableErasureObject(null);
        client.close();
        example.close();
        example = null;

    }

    @Test
    public void test2() {
        RpcErasureServer<SerializableErasureObject> example = new RpcErasureServer<SerializableErasureObject>(new SerializableErasureObject(), "localhost", 7083, eventloop, blocksize);
        new Thread(example).start();
        RpcErasureClient<SerializableErasureObject> client = new RpcErasureClient<SerializableErasureObject>(new SerializableErasureObject(), "localhost", 7083, eventloop);
        client.connect();
        (new Thread() {
            public void run() {
                try {
                    Thread.sleep(400);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                CachedSerializableErasureObject.getInstance().setSerializableErasureObject(serializableErasureObjects.get(0));
            }
        }).start();
        //#########################################################################################################################

        ArrayList<SerializableErasureObject> serializableErasureObject = (ArrayList<SerializableErasureObject>) client.getErasureChunks(new byte[0]);
        int g = 3;

        client.close();
        example.close();
        example = null;

    }

    private interface HelloService {
        String hello(String name) throws Exception;
    }

    private static class HelloServiceImplTwo implements HelloService {
        @Override
        public String hello(String name) throws Exception {
            if (name.equals("--")) {
                throw new Exception("Illegal name");
            }
            return "Hello Hello, " + name + "!";
        }
    }

    private static class HelloServiceImplOne implements HelloService {
        @Override
        public String hello(String name) throws Exception {
            if (name.equals("--")) {
                throw new Exception("Illegal name");
            }
            return "Hello, " + name + "!";
        }
    }


    private static class HelloServiceImplThree implements HelloService {
        @Override
        public String hello(String name) throws Exception {
            if (name.equals("--")) {
                throw new Exception("Illegal name");
            }
            return "Hello Hello Hello, " + name + "!";
        }
    }

    private static RpcRequestHandler<RPCExampleTest.HelloRequest, RPCExampleTest.HelloResponse> helloServiceRequestHandler(HelloService helloService) {
        return request -> {
            String result;
            try {
                result = helloService.hello(request.name);
            } catch (Exception e) {
                return Promise.ofException(e);
            }
            return Promise.of(new RPCExampleTest.HelloResponse(result));
        };
    }

    protected static class HelloRequest {
        @Serialize
        public final String name;

        public HelloRequest(@Deserialize("name") String name) {
            this.name = name;
        }
    }

    protected static class HelloResponse {
        @Serialize
        public final String message;

        public HelloResponse(@Deserialize("message") String message) {
            this.message = message;
        }
    }

    private static String blockingRequest(RpcClient rpcClient, String name) throws Exception {
        try {
            return rpcClient.getEventloop().submit(
                            () -> rpcClient
                                    .<RPCExampleTest.HelloRequest, RPCExampleTest.HelloResponse>sendRequest(new RPCExampleTest.HelloRequest(name), TIMEOUT))
                    .get(5, TimeUnit.SECONDS)
                    .message;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return name;
    }
}
