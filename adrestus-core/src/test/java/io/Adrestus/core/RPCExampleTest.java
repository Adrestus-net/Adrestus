package io.Adrestus.core;

import com.google.common.reflect.TypeToken;
import io.Adrestus.MemoryTreePool;
import io.Adrestus.TreeFactory;
import io.Adrestus.Trie.PatriciaTreeNode;
import io.Adrestus.crypto.HashUtil;
import io.Adrestus.crypto.bls.BLS381.ECP;
import io.Adrestus.crypto.bls.BLS381.ECP2;
import io.Adrestus.crypto.bls.mapper.ECP2mapper;
import io.Adrestus.crypto.bls.mapper.ECPmapper;
import io.Adrestus.crypto.elliptic.mapper.BigIntegerSerializer;
import io.Adrestus.crypto.elliptic.mapper.CustomSerializerTreeMap;
import io.Adrestus.mapper.MemoryTreePoolSerializer;
import io.Adrestus.network.CachedEventLoop;
import io.Adrestus.rpc.RpcAdrestusClient;
import io.Adrestus.rpc.RpcAdrestusServer;
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
import io.distributedLedger.*;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import static io.activej.rpc.client.sender.RpcStrategies.server;
import static org.junit.jupiter.api.Assertions.assertEquals;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RPCExampleTest {
    private static final int TIMEOUT = 1500;
    private static RpcServer serverOne, serverTwo, serverThree;
    private static Thread thread;
    private static Eventloop eventloop = CachedEventLoop.getInstance().getEventloop();
    private static InetSocketAddress address1, address2, address3;
    private static SerializationUtil<AbstractBlock> encode;

    @BeforeAll
    public static void setup() throws IOException {
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(ECP.class, ctx -> new ECPmapper()));
        list.add(new SerializationUtil.Mapping(ECP2.class, ctx -> new ECP2mapper()));
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        list.add(new SerializationUtil.Mapping(TreeMap.class, ctx -> new CustomSerializerTreeMap()));
        encode = new SerializationUtil<AbstractBlock>(AbstractBlock.class, list);

    }

    @Test
    @Order(1)
    public void myAtest2() throws Exception {

        address1 = new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 8080);
        address2 = new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 8081);
        address3 = new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 8084);
        Eventloop eventloop = Eventloop.getCurrentEventloop();
        serverOne = RpcServer.create(eventloop)
                .withMessageTypes(HelloRequest.class, HelloResponse.class)
                .withHandler(HelloRequest.class,
                        helloServiceRequestHandler(new HelloServiceImplOne()))
                .withListenAddress(address1);
        serverOne.listen();


        serverTwo = RpcServer.create(eventloop)
                .withMessageTypes(HelloRequest.class, HelloResponse.class)
                .withHandler(HelloRequest.class,
                        helloServiceRequestHandler(new HelloServiceImplTwo()))
                .withListenAddress(address2);

        serverTwo.listen();

        serverThree = RpcServer.create(eventloop)
                .withMessageTypes(HelloRequest.class, HelloResponse.class)
                .withHandler(HelloRequest.class,
                        helloServiceRequestHandler(new HelloServiceImplThree()))
                .withListenAddress(address3);

        serverThree.listen();
        thread = new Thread(eventloop);
        thread.start();


        ArrayList<RpcStrategy> list = new ArrayList<>();
        list.add(server(address1));
        list.add(server(address2));
        list.add(server(address3));
        RpcStrategyList rpcStrategyList = RpcStrategyList.ofStrategies(list);

        RpcClient client = RpcClient.create(eventloop)
                .withMessageTypes(HelloRequest.class, HelloResponse.class)
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

    @AfterAll
    public static void after() {
        serverOne.close();
        serverTwo.close();
        serverThree.close();

        serverOne = null;
        serverTwo = null;
        serverThree = null;
    }

    @Test
    public void myEdownload() throws Exception {

        try {
            //this is important if ports its the same it needs time to close
            //Thread.sleep(3000);
            IDatabase<String, CommitteeBlock> database = new DatabaseFactory(String.class, CommitteeBlock.class).getDatabase(DatabaseType.ROCKS_DB, DatabaseInstance.COMMITTEE_BLOCK);
            CommitteeBlock firstblock = new CommitteeBlock();
            firstblock.setDifficulty(112);
            firstblock.getHeaderData().setTimestamp(GetTime.GetTimeStampInString());
            database.save("1", firstblock);
            Thread.sleep(200);
            CommitteeBlock secondblock = new CommitteeBlock();
            secondblock.setDifficulty(117);
            secondblock.getHeaderData().setTimestamp(GetTime.GetTimeStampInString());
            Thread.sleep(200);
            database.save("2", secondblock);
            CommitteeBlock thirdblock = new CommitteeBlock();
            thirdblock.setDifficulty(119);
            thirdblock.getHeaderData().setTimestamp(GetTime.GetTimeStampInString());
            database.save("3", thirdblock);

            Thread.sleep(200);


            RpcAdrestusServer<AbstractBlock> example = new RpcAdrestusServer<AbstractBlock>(new CommitteeBlock(), DatabaseInstance.COMMITTEE_BLOCK, "localhost", 8082, eventloop);
            new Thread(example).start();
            RpcAdrestusClient<AbstractBlock> client = new RpcAdrestusClient<AbstractBlock>(new CommitteeBlock(), "localhost", 8082, eventloop);
            client.connect();
            List<AbstractBlock> blocks = client.getBlocksList("1");

            ArrayList<String> list = new ArrayList<>();
            list.add("2");
            list.add("3");
            List<AbstractBlock> list_block = client.getBlock(list);
            assertEquals(secondblock, list_block.get(0));
            assertEquals(thirdblock, list_block.get(1));
            assertEquals(firstblock, blocks.get(0));
            assertEquals(secondblock, blocks.get(1));
            assertEquals(thirdblock, blocks.get(2));

            client.close();
            example.close();
            example = null;

            database.delete_db();
        } catch (Exception e) {
            System.out.println("Exception caught: " + e.toString());
        }
    }

    @Test
    public void myCdownload2() throws Exception {
        try {
            IDatabase<String, TransactionBlock> database = new DatabaseFactory(String.class, TransactionBlock.class).getDatabase(DatabaseType.ROCKS_DB, DatabaseInstance.ZONE_1_TRANSACTION_BLOCK);
            TransactionBlock transactionBlock = new TransactionBlock();
            String hash = HashUtil.sha256_bytetoString(encode.encode(transactionBlock));
            transactionBlock.setHash(hash);

            database.save(transactionBlock.getHash(), transactionBlock);


            RpcAdrestusServer<AbstractBlock> example = new RpcAdrestusServer<AbstractBlock>(new TransactionBlock(), DatabaseInstance.ZONE_1_TRANSACTION_BLOCK, "localhost", 8085, eventloop);
            new Thread(example).start();
            RpcAdrestusClient<AbstractBlock> client = new RpcAdrestusClient<AbstractBlock>(new TransactionBlock(), "localhost", 8085, eventloop);
            client.connect();
            ArrayList<String> list = new ArrayList<>();
            list.add(transactionBlock.getHash());
            List<AbstractBlock> blocks = client.getBlock(list);
            if (blocks.isEmpty()) {
                System.out.println("error");
            }
            assertEquals(transactionBlock, blocks.get(0));


            client.close();
            example.close();
            example = null;
            database.delete_db();
        } catch (Exception e) {
            System.out.println("Exception caught: " + e.toString());
        }
    }
    @Test
    public void myCadownload2() throws Exception {
        try {
            IDatabase<String, TransactionBlock> database = new DatabaseFactory(String.class, TransactionBlock.class).getDatabase(DatabaseType.ROCKS_DB, DatabaseInstance.ZONE_1_TRANSACTION_BLOCK);
            IDatabase<String, TransactionBlock> database2 = new DatabaseFactory(String.class, TransactionBlock.class).getDatabase(DatabaseType.ROCKS_DB, DatabaseInstance.ZONE_2_TRANSACTION_BLOCK);
            TransactionBlock transactionBlock = new TransactionBlock();
            TransactionBlock transactionBlock2 = new TransactionBlock();
            String hash = HashUtil.sha256_bytetoString(encode.encode(transactionBlock));
            transactionBlock.setHash(hash);
            transactionBlock2.setHash("1");
            database.save(transactionBlock.getHash(), transactionBlock);
            database2.save(transactionBlock2.getHash(), transactionBlock2);

            RpcAdrestusServer<AbstractBlock> example = new RpcAdrestusServer<AbstractBlock>(new TransactionBlock(), DatabaseInstance.ZONE_2_TRANSACTION_BLOCK, "localhost", 8095, eventloop);
            new Thread(example).start();
            RpcAdrestusClient<AbstractBlock> client = new RpcAdrestusClient<AbstractBlock>(new TransactionBlock(), "localhost", 8095, eventloop);
            client.connect();
            List<AbstractBlock> blocks = client.getBlocksList("1");
            if (blocks.isEmpty()) {
                System.out.println("error");
            }
            assertEquals(transactionBlock2, blocks.get(0));


            client.close();
            example.close();
            example = null;
            database.delete_db();
        } catch (Exception e) {
            System.out.println("Exception caught: " + e.toString());
        }
    }

    @Test
    @Order(3)
    public void myDmultiple_download() throws Exception {
        try {
            IDatabase<String, CommitteeBlock> database = new DatabaseFactory(String.class, CommitteeBlock.class).getDatabase(DatabaseType.ROCKS_DB, DatabaseInstance.COMMITTEE_BLOCK);
            CommitteeBlock firstblock = new CommitteeBlock();
            firstblock.setDifficulty(112);
            firstblock.getHeaderData().setTimestamp(GetTime.GetTimeStampInString());
            database.save("1", firstblock);
            Thread.sleep(200);
            CommitteeBlock secondblock = new CommitteeBlock();
            secondblock.setDifficulty(117);
            secondblock.getHeaderData().setTimestamp(GetTime.GetTimeStampInString());
            Thread.sleep(200);
            database.save("2", secondblock);
            CommitteeBlock thirdblock = new CommitteeBlock();
            thirdblock.setDifficulty(119);
            thirdblock.getHeaderData().setTimestamp(GetTime.GetTimeStampInString());
            database.save("3", thirdblock);


            ArrayList<InetSocketAddress> list = new ArrayList<>();
            InetSocketAddress address1 = new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 3070);
            InetSocketAddress address2 = new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 6071);
            InetSocketAddress address3 = new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 6072);
            list.add(address1);
            list.add(address2);
            list.add(address3);
            RpcAdrestusServer server1 = new RpcAdrestusServer(new CommitteeBlock(), address1, eventloop);
            RpcAdrestusServer server2 = new RpcAdrestusServer(new CommitteeBlock(), address2, eventloop);
            RpcAdrestusServer server3 = new RpcAdrestusServer(new CommitteeBlock(), address3, eventloop);
            new Thread(server1).start();
            new Thread(server2).start();
            new Thread(server3).start();

            RpcAdrestusClient client = new RpcAdrestusClient(new CommitteeBlock(), list, eventloop);
            client.connect();
            List<AbstractBlock> blocks = client.getBlocksList("1");

            assertEquals(firstblock, blocks.get(0));
            assertEquals(secondblock, blocks.get(1));
            assertEquals(thirdblock, blocks.get(2));

            client.close();
            server1.close();
            server2.close();
            server3.close();
            server1 = null;
            server2 = null;
            server3 = null;
            database.delete_db();
        } catch (Exception e) {
            System.out.println("Exception caught: " + e.toString());
        }

    }

    @Test
    @Order(2)
    public void myBmultiple_download_patricia_tree() throws Exception {
        try {
            IDatabase<String, byte[]> tree_datasbase = new DatabaseFactory(String.class, byte[].class).getDatabase(DatabaseType.ROCKS_DB, ZoneDatabaseFactory.getPatriciaTreeZoneInstance(0));

            Type fluentType = new TypeToken<MemoryTreePool>() {
            }.getType();
            List<SerializationUtil.Mapping> seri = new ArrayList<>();
            seri.add(new SerializationUtil.Mapping(MemoryTreePool.class, ctx -> new MemoryTreePoolSerializer()));
            SerializationUtil valueMapper = new SerializationUtil<>(fluentType, seri);

            String address = "ADR-ADL3-VDZK-ZU7H-2BX5-M2H4-S7LF-5SR4-ECQA-EIUJ-CBFK";
            PatriciaTreeNode treeNode = new PatriciaTreeNode(2, 1);
            TreeFactory.getMemoryTree(0).store(address, treeNode);
            MemoryTreePool m = (MemoryTreePool) TreeFactory.getMemoryTree(1);

            //m.getByaddress(address);
            //use only special
            byte[] bt = valueMapper.encode_special(m, SerializationUtils.serialize(m).length);
            tree_datasbase.save("patricia_tree_root", bt);

            ArrayList<InetSocketAddress> list = new ArrayList<>();
            InetSocketAddress address1 = new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 3070);
            InetSocketAddress address2 = new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 3071);
            InetSocketAddress address3 = new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 3072);
            list.add(address1);
            list.add(address2);
            list.add(address3);
            RpcAdrestusServer server1 = new RpcAdrestusServer(new byte[]{}, ZoneDatabaseFactory.getPatriciaTreeZoneInstance(0), address1, eventloop);
            RpcAdrestusServer server2 = new RpcAdrestusServer(new byte[]{}, ZoneDatabaseFactory.getPatriciaTreeZoneInstance(0), address2, eventloop);
            RpcAdrestusServer server3 = new RpcAdrestusServer(new byte[]{}, ZoneDatabaseFactory.getPatriciaTreeZoneInstance(0), address3, eventloop);
            new Thread(server1).start();
            new Thread(server2).start();
            new Thread(server3).start();
            RpcAdrestusClient client = new RpcAdrestusClient(new byte[]{}, list, eventloop);
            client.connect();
            List<byte[]> trees = client.getPatriciaTreeList("patricia_tree_root");

            assertEquals(m, valueMapper.decode(trees.get(0)));

            client.close();
            server1.close();
            server2.close();
            server3.close();
            server1 = null;
            server2 = null;
            server3 = null;
            tree_datasbase.delete_db();
        } catch (Exception e) {
            System.out.println("Exception caught: " + e.toString());
        }
    }

    @Test
    public void myFmultiple_download_noserver() throws Exception {
        try {
            ArrayList<InetSocketAddress> list = new ArrayList<>();
            InetSocketAddress address1 = new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 6070);
            InetSocketAddress address2 = new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 6071);
            InetSocketAddress address3 = new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 6072);
            list.add(address1);
            list.add(address2);
            list.add(address3);
            RpcAdrestusClient client = null;
            try {
                client = new RpcAdrestusClient(new CommitteeBlock(), list, eventloop);
                client.connect();

                if (client != null)
                    client.close();

                List<AbstractBlock> blocks = client.getBlocksList("1");
            } catch (IllegalArgumentException e) {
            }
        } catch (Exception e) {
            System.out.println("Exception caught: " + e.toString());
        }

    }

    @Test
    public void myGmultiple_download_one_server() throws Exception {
        try {
            IDatabase<String, CommitteeBlock> database = new DatabaseFactory(String.class, CommitteeBlock.class).getDatabase(DatabaseType.ROCKS_DB, DatabaseInstance.COMMITTEE_BLOCK);
            CommitteeBlock firstblock = new CommitteeBlock();
            firstblock.setDifficulty(112);
            firstblock.getHeaderData().setTimestamp(GetTime.GetTimeStampInString());
            database.save("1", firstblock);
            Thread.sleep(200);


            ArrayList<InetSocketAddress> list = new ArrayList<>();
            InetSocketAddress address1 = new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 4070);
            InetSocketAddress address2 = new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 4071);
            InetSocketAddress address3 = new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 4072);
            list.add(address1);
            list.add(address2);
            list.add(address3);
            try {
                RpcAdrestusServer server1 = new RpcAdrestusServer(new CommitteeBlock(), address1, eventloop);
                new Thread(server1).start();
                RpcAdrestusClient client = new RpcAdrestusClient(new CommitteeBlock(), list, eventloop);
                client.connect();
                List<AbstractBlock> blocks = client.getBlocksList("1");

                client.close();
            } catch (Exception e) {
            }

        } catch (Exception e) {
            System.out.println("Exception caught: " + e.toString());
        }
    }

    @Test
    public void myHmultiple_download_one_server_one_response() throws Exception {
        try {
            IDatabase<String, CommitteeBlock> database = new DatabaseFactory(String.class, CommitteeBlock.class).getDatabase(DatabaseType.ROCKS_DB, DatabaseInstance.COMMITTEE_BLOCK);
            CommitteeBlock firstblock = new CommitteeBlock();
            firstblock.setDifficulty(112);
            firstblock.getHeaderData().setTimestamp(GetTime.GetTimeStampInString());
            database.save("1", firstblock);
            Thread.sleep(200);
            ArrayList<InetSocketAddress> list = new ArrayList<>();
            InetSocketAddress address1 = new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 5070);
            InetSocketAddress address2 = new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 5071);
            InetSocketAddress address3 = new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 5072);
            list.add(address1);
            list.add(address2);
            list.add(address3);
            RpcAdrestusServer server1 = new RpcAdrestusServer(new CommitteeBlock(), address1, eventloop);
            new Thread(server1).start();
            RpcAdrestusClient client = new RpcAdrestusClient(new CommitteeBlock(), list, eventloop);
            client.connect();
            List<AbstractBlock> blocks = client.getBlocksList("1");
            assertEquals(firstblock, blocks.get(0));


            client.close();
            server1.close();
            server1 = null;
            database.delete_db();
        } catch (Exception e) {
            System.out.println("Exception caught: " + e.toString());
        }
    }

    @Test
    public void myImultiple_download_one_server_one_response() throws Exception {
        try {
            IDatabase<String, CommitteeBlock> database = new DatabaseFactory(String.class, CommitteeBlock.class).getDatabase(DatabaseType.ROCKS_DB, DatabaseInstance.COMMITTEE_BLOCK);
            CommitteeBlock firstblock = new CommitteeBlock();
            firstblock.setDifficulty(112);
            firstblock.getHeaderData().setTimestamp(GetTime.GetTimeStampInString());
            database.save("1", firstblock);
            Thread.sleep(200);
            CommitteeBlock secondblock = new CommitteeBlock();
            secondblock.setDifficulty(117);
            secondblock.getHeaderData().setTimestamp(GetTime.GetTimeStampInString());
            Thread.sleep(200);
            database.save("2", secondblock);
            CommitteeBlock thirdblock = new CommitteeBlock();
            thirdblock.setDifficulty(119);
            thirdblock.getHeaderData().setTimestamp(GetTime.GetTimeStampInString());
            database.save("3", thirdblock);


            InetSocketAddress address1 = new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 6070);
            RpcAdrestusServer server1 = new RpcAdrestusServer(new CommitteeBlock(), address1, eventloop);
            new Thread(server1).start();


            RpcAdrestusClient client = new RpcAdrestusClient(new CommitteeBlock(), address1, eventloop);
            client.connect();
            List<AbstractBlock> blocks = client.getBlocksList("1");

            assertEquals(firstblock, blocks.get(0));
            assertEquals(secondblock, blocks.get(1));
            assertEquals(thirdblock, blocks.get(2));

            client.close();
            server1.close();
            server1 = null;
            database.delete_db();
        } catch (Exception e) {
            System.out.println("Exception caught: " + e.toString());
        }
    }

    @Test
    public void download_transaction_database() throws Exception {
        IDatabase<String, LevelDBTransactionWrapper<Transaction>> database = new DatabaseFactory(String.class, Transaction.class, new TypeToken<LevelDBTransactionWrapper<Transaction>>() {
        }.getType()).getDatabase(DatabaseType.LEVEL_DB);
        Transaction transaction = new RegularTransaction();
        transaction.setAmount(100);
        transaction.setHash("Hash123");
        transaction.setFrom("1");
        transaction.setTo("2");
        transaction.setTimestamp(GetTime.GetTimeStampInString());
        Thread.sleep(200);


        Transaction transaction2 = new RegularTransaction();
        transaction2.setAmount(200);
        transaction2.setHash("Hash124");
        transaction2.setFrom("3");
        transaction2.setTo("1");
        transaction2.setTimestamp(GetTime.GetTimeStampInString());
        Thread.sleep(200);

        Transaction transaction3 = new RegularTransaction();
        transaction3.setAmount(200);
        transaction3.setHash("Hash345");
        transaction3.setFrom("4");
        transaction3.setTo("1");
        transaction3.setTimestamp(GetTime.GetTimeStampInString());
        Thread.sleep(200);

        database.save("1", transaction);
        database.save("1", transaction2);
        database.save("1", transaction2);
        database.save("1", transaction3);
        //   database.save("1",transaction2);
        Map<String, LevelDBTransactionWrapper<Transaction>> map = database.seekFromStart();


        Type fluentType = new TypeToken<LevelDBTransactionWrapper<Transaction>>() {
        }.getType();
        RpcAdrestusClient client = null;
        RpcAdrestusServer<Transaction> server1 = null, server2 = null, server3 = null;
        try {
            InetSocketAddress address1 = new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 3077);
            server1 = new RpcAdrestusServer<Transaction>(new RegularTransaction(), fluentType, address1, eventloop);
            new Thread(server1).start();


            client = new RpcAdrestusClient(fluentType, address1, eventloop);
            client.connect();
            Map<String, LevelDBTransactionWrapper<Transaction>> copymaps = (Map<String, LevelDBTransactionWrapper<Transaction>>) client.getTransactionDatabase("1");

            assertEquals(map, copymaps);

            if (client != null)
                client.close();
            if (server1 != null)
                server1.close();
            server1 = null;
        } catch (Exception e) {
            System.out.println("Database 1 Exception caught: " + e.toString());
        } finally {
            database.delete_db();
        }
    }

    @Test
    public void download_transaction_database_from_multiple_servers() throws Exception {
        IDatabase<String, LevelDBTransactionWrapper<Transaction>> database = new DatabaseFactory(String.class, Transaction.class, new TypeToken<LevelDBTransactionWrapper<Transaction>>() {
        }.getType()).getDatabase(DatabaseType.LEVEL_DB);
        Transaction transaction = new RegularTransaction();
        transaction.setAmount(100);
        transaction.setHash("Hash123");
        transaction.setFrom("1");
        transaction.setTo("2");
        transaction.setTimestamp(GetTime.GetTimeStampInString());
        Thread.sleep(200);


        Transaction transaction2 = new RegularTransaction();
        transaction2.setAmount(200);
        transaction2.setHash("Hash124");
        transaction2.setFrom("3");
        transaction2.setTo("1");
        transaction2.setTimestamp(GetTime.GetTimeStampInString());
        Thread.sleep(200);

        Transaction transaction3 = new RegularTransaction();
        transaction3.setAmount(200);
        transaction3.setHash("Hash345");
        transaction3.setFrom("4");
        transaction3.setTo("1");
        transaction3.setTimestamp(GetTime.GetTimeStampInString());
        Thread.sleep(200);

        database.save("1", transaction);
        database.save("1", transaction2);
        database.save("1", transaction2);
        database.save("1", transaction3);
        //   database.save("1",transaction2);
        Map<String, LevelDBTransactionWrapper<Transaction>> map = database.seekFromStart();


        Type fluentType = new TypeToken<LevelDBTransactionWrapper<Transaction>>() {
        }.getType();
        RpcAdrestusClient client = null;
        RpcAdrestusServer<Transaction> server1 = null, server2 = null, server3 = null;
        try {
            ArrayList<InetSocketAddress> list = new ArrayList<>();
            InetSocketAddress address1 = new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 3074);
            InetSocketAddress address2 = new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 3075);
            InetSocketAddress address3 = new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 3076);
            list.add(address1);
            list.add(address2);
            list.add(address3);
            server1 = new RpcAdrestusServer<Transaction>(new RegularTransaction(), fluentType, address1, eventloop);
            server2 = new RpcAdrestusServer<Transaction>(new RegularTransaction(), fluentType, address2, eventloop);
            server3 = new RpcAdrestusServer<Transaction>(new RegularTransaction(), fluentType, address3, eventloop);
            new Thread(server1).start();
            new Thread(server2).start();
            new Thread(server3).start();


            client = new RpcAdrestusClient(fluentType, list, eventloop);
            client.connect();
            Map<String, LevelDBTransactionWrapper<Transaction>> copymaps = (Map<String, LevelDBTransactionWrapper<Transaction>>) client.getTransactionDatabase("1");

            assertEquals(map, copymaps);

            if (client != null)
                client.close();
            if (server1 != null)
                server1.close();
            if (server2 != null)
                server2.close();
            if (server3 != null)
                server3.close();
            server1 = null;
        } catch (Exception e) {
            System.out.println("Database2 Exception caught: " + e.toString());
        } finally {
            database.delete_db();
        }
    }

    //@Test
    public void test3() throws Exception {
        Eventloop eventloop = Eventloop.create();
        new Thread(eventloop).start();
        RpcClient client = RpcClient.create(eventloop)
                .withMessageTypes(HelloRequest.class, HelloResponse.class)
                .withStrategy(server(new InetSocketAddress("127.0.0.1", 8085)));

        try {
            client.startFuture().get();

            String currentName;
            String currentResponse;

            currentName = "John";
            currentResponse = blockingRequest(client, currentName);
            System.out.println("Request with name \"" + currentName + "\": " + currentResponse);
            assertEquals("Hello, " + currentName + "!", currentResponse);
        } catch (Exception e) {
            System.out.println("Connection not established");
        }
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

    private static RpcRequestHandler<HelloRequest, HelloResponse> helloServiceRequestHandler(HelloService helloService) {
        return request -> {
            String result;
            try {
                result = helloService.hello(request.name);
            } catch (Exception e) {
                return Promise.ofException(e);
            }
            return Promise.of(new HelloResponse(result));
        };
    }

    private static String blockingRequest(RpcClient rpcClient, String name) throws Exception {
        try {
            return rpcClient.getEventloop().submit(
                            () -> rpcClient
                                    .<HelloRequest, HelloResponse>sendRequest(new HelloRequest(name), TIMEOUT))
                    .get(5, TimeUnit.SECONDS)
                    .message;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return name;
    }
}