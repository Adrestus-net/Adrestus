package io.Adrestus.protocol;

import ch.qos.logback.core.joran.conditional.ThenAction;
import io.Adrestus.config.NetworkConfiguration;
import io.Adrestus.core.AbstractBlock;
import io.Adrestus.core.CommitteeBlock;
import io.Adrestus.core.Resourses.CachedZoneIndex;
import io.Adrestus.core.TransactionBlock;
import io.Adrestus.crypto.HashUtil;
import io.Adrestus.crypto.bls.model.BLSPublicKey;
import io.Adrestus.network.CachedEventLoop;
import io.Adrestus.network.IPFinder;
import io.Adrestus.rpc.RpcAdrestusClient;
import io.Adrestus.rpc.RpcAdrestusServer;
import io.Adrestus.util.EpochTransitionFinder;
import io.activej.eventloop.Eventloop;
import io.activej.rpc.server.RpcServer;
import io.distributedLedger.*;
import org.junit.jupiter.api.Test;

import javax.swing.plaf.TableHeaderUI;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RPCWorkerTest {

    private static Eventloop eventloop = Eventloop.create().withCurrentThread();
    @Test
    public void test() throws IOException, InterruptedException {
        DatabaseInstance instance=DatabaseInstance.ZONE_2_TRANSACTION_BLOCK;
        IDatabase<String, TransactionBlock> database = new DatabaseFactory(String.class, TransactionBlock.class).getDatabase(DatabaseType.ROCKS_DB, instance);
        IAdrestusFactory factory = new AdrestusFactory();
        List<AdrestusTask> tasks = new java.util.ArrayList<>(List.of(
                //factory.createBindServerKademliaTask(),
                factory.createRepositoryTransactionTask(DatabaseInstance.ZONE_2_TRANSACTION_BLOCK)));
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

       // database.save(transactionBlock.getHash(), transactionBlock);


        Thread.sleep(500);
        Optional<TransactionBlock> empty = database.findByKey("1");
        ArrayList<InetSocketAddress> list = new ArrayList<>();
        InetSocketAddress address1 = new InetSocketAddress(InetAddress.getByName(IPFinder.getLocalIP()), ZoneDatabaseFactory.getDatabaseRPCPort(instance));
        list.add(address1);



        RpcAdrestusClient client = new RpcAdrestusClient(new TransactionBlock(), list, CachedEventLoop.getInstance().getEventloop());
        client.connect();

        List<AbstractBlock>blocks = client.getBlocksList("1");
        assertEquals(1,blocks.size());

        client.close();
        client=null;

        RpcAdrestusClient client2 = new RpcAdrestusClient(new TransactionBlock(), list, CachedEventLoop.getInstance().getEventloop());
        client2.connect();

        List<AbstractBlock>blocks2 = client2.getBlocksList("1");
        assertEquals(1,blocks2.size());
       // database.delete_db();
    }
}
