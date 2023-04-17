package io.Adrestus.protocol;

import io.Adrestus.core.AbstractBlock;
import io.Adrestus.core.CommitteeBlock;
import io.Adrestus.core.TransactionBlock;
import io.Adrestus.crypto.bls.model.BLSPublicKey;
import io.Adrestus.network.CachedEventLoop;
import io.Adrestus.network.IPFinder;
import io.Adrestus.rpc.RpcAdrestusClient;
import io.distributedLedger.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RPCWorkerTest {


    @BeforeAll
    public static void setup() {
        delete_test();
    }

    @Test
    public void test() throws IOException, InterruptedException {
        DatabaseInstance instance = DatabaseInstance.ZONE_0_TRANSACTION_BLOCK;
        IDatabase<String, TransactionBlock> database = new DatabaseFactory(String.class, TransactionBlock.class).getDatabase(DatabaseType.ROCKS_DB, instance);
        IAdrestusFactory factory = new AdrestusFactory();
        List<AdrestusTask> tasks = new java.util.ArrayList<>(List.of(
                //factory.createBindServerKademliaTask(),
                factory.createRepositoryTransactionTask(DatabaseInstance.ZONE_0_TRANSACTION_BLOCK)));
        ExecutorService executor = Executors.newFixedThreadPool(tasks.size());
        tasks.stream().map(Worker::new).forEach(executor::execute);

        CachedEventLoop.getInstance().start();
       /* RpcServer.create(eventloop)
                .withMessageTypes(String.class)
                .withListenAddress(new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 8083)).listen();
        new Thread(eventloop).start();*/

        //RpcAdrestusServer<AbstractBlock> example = new RpcAdrestusServer<AbstractBlock>(new TransactionBlock(), instance, IPFinder.getLocalIP(), ZoneDatabaseFactory.getDatabaseRPCPort(instance), eventloop);
        //new Thread(example).start();
        TransactionBlock transactionBlock = new TransactionBlock();
        transactionBlock.setHeight(1);
        transactionBlock.setHash("1");
        transactionBlock.setLeaderPublicKey(new BLSPublicKey());

        database.save(transactionBlock.getHash(), transactionBlock);


        Optional<TransactionBlock> empty = database.findByKey("1");
        ArrayList<InetSocketAddress> list = new ArrayList<>();
        InetSocketAddress address1 = new InetSocketAddress(InetAddress.getByName(IPFinder.getLocalIP()), ZoneDatabaseFactory.getDatabaseRPCPort(instance));
        list.add(address1);


        try {

            RpcAdrestusClient client = new RpcAdrestusClient(new TransactionBlock(), list, CachedEventLoop.getInstance().getEventloop());
            client.connect();

            List<AbstractBlock> blocks = client.getBlocksList("1");
            assertEquals(1, blocks.size());

            client.close();
            client = null;

            RpcAdrestusClient client2 = new RpcAdrestusClient(new TransactionBlock(), list, CachedEventLoop.getInstance().getEventloop());
            client2.connect();

            List<AbstractBlock> blocks2 = client2.getBlocksList("1");
            assertEquals(1, blocks2.size());
        } catch (Exception e) {

        }
        database.delete_db();
    }

    public static void delete_test() {
        IDatabase<String, TransactionBlock> transaction_block1 = new DatabaseFactory(String.class, TransactionBlock.class).getDatabase(DatabaseType.ROCKS_DB, DatabaseInstance.ZONE_0_TRANSACTION_BLOCK);
        IDatabase<String, TransactionBlock> transaction_block2 = new DatabaseFactory(String.class, TransactionBlock.class).getDatabase(DatabaseType.ROCKS_DB, DatabaseInstance.ZONE_1_TRANSACTION_BLOCK);
        IDatabase<String, TransactionBlock> transaction_block3 = new DatabaseFactory(String.class, TransactionBlock.class).getDatabase(DatabaseType.ROCKS_DB, DatabaseInstance.ZONE_2_TRANSACTION_BLOCK);
        IDatabase<String, TransactionBlock> transaction_block4 = new DatabaseFactory(String.class, TransactionBlock.class).getDatabase(DatabaseType.ROCKS_DB, DatabaseInstance.ZONE_3_TRANSACTION_BLOCK);

        IDatabase<String, byte[]> patricia_tree0 = new DatabaseFactory(String.class, byte[].class).getDatabase(DatabaseType.ROCKS_DB, PatriciaTreeInstance.PATRICIA_TREE_INSTANCE_0);
        IDatabase<String, byte[]> patricia_tree1 = new DatabaseFactory(String.class, byte[].class).getDatabase(DatabaseType.ROCKS_DB, PatriciaTreeInstance.PATRICIA_TREE_INSTANCE_1);
        IDatabase<String, byte[]> patricia_tree2 = new DatabaseFactory(String.class, byte[].class).getDatabase(DatabaseType.ROCKS_DB, PatriciaTreeInstance.PATRICIA_TREE_INSTANCE_2);
        IDatabase<String, byte[]> patricia_tree3 = new DatabaseFactory(String.class, byte[].class).getDatabase(DatabaseType.ROCKS_DB, PatriciaTreeInstance.PATRICIA_TREE_INSTANCE_3);

        IDatabase<String, CommitteeBlock> commit = new DatabaseFactory(String.class, CommitteeBlock.class).getDatabase(DatabaseType.ROCKS_DB, DatabaseInstance.COMMITTEE_BLOCK);


        //ITS IMPORTANT FIRST DELETE PATRICIA TREE AND AFTER TRASNACTION BLOCKS
        patricia_tree0.delete_db();
        patricia_tree1.delete_db();
        patricia_tree2.delete_db();
        patricia_tree3.delete_db();

        transaction_block1.delete_db();
        transaction_block2.delete_db();
        transaction_block3.delete_db();
        transaction_block4.delete_db();


        commit.delete_db();
    }
}
