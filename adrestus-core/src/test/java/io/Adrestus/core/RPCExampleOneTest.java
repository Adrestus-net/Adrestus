package io.Adrestus.core;

import io.Adrestus.network.CachedEventLoop;
import io.Adrestus.rpc.RpcAdrestusClient;
import io.Adrestus.rpc.RpcAdrestusServer;
import io.Adrestus.util.GetTime;
import io.distributedLedger.DatabaseFactory;
import io.distributedLedger.DatabaseInstance;
import io.distributedLedger.DatabaseType;
import io.distributedLedger.IDatabase;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RPCExampleOneTest {


    @Test
    public void test() {
        IDatabase<String, CommitteeBlock> database = new DatabaseFactory(String.class, CommitteeBlock.class).getDatabase(DatabaseType.ROCKS_DB, DatabaseInstance.COMMITTEE_BLOCK);
        try {
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
            RpcAdrestusServer server1 = new RpcAdrestusServer(new CommitteeBlock(), address1, CachedEventLoop.getInstance().getEventloop());

            new Thread(server1).start();
            CachedEventLoop.getInstance().start();
            RpcAdrestusClient client = new RpcAdrestusClient(new CommitteeBlock(), address1, CachedEventLoop.getInstance().getEventloop());
            client.connect();
            List<AbstractBlock> blocks = client.getBlocksList("1");

            assertEquals(firstblock, blocks.get(0));
            assertEquals(secondblock, blocks.get(1));
            assertEquals(thirdblock, blocks.get(2));


        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception caught: " + e.toString());
        } finally {
            database.delete_db();
        }
    }
}
