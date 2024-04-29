package io.Adrestus.consensus;

import io.Adrestus.network.CachedEventLoop;
import io.Adrestus.network.IPFinder;
import io.Adrestus.rpc.CachedConsensusPublisherData;
import io.Adrestus.rpc.RpcErasureClient;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static io.Adrestus.config.ConsensusConfiguration.ERASURE_CLIENT_TIMEOUT;
import static io.Adrestus.config.ConsensusConfiguration.ERASURE_SERVER_PORT;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class RPCErasureConsensusTest {

    @Test
    public void asdasd() {
        ConsensusManager consensusManager = new ConsensusManager(true);
        consensusManager.changeStateTo(ConsensusRoleType.ORGANIZER);
        var organizerphase = consensusManager.getRole().manufacturePhases(ConsensusType.TRANSACTION_BLOCK);
        consensusManager.changeStateTo(ConsensusRoleType.VALIDATOR);
        consensusManager.changeStateTo(ConsensusRoleType.ORGANIZER);
        consensusManager.changeStateTo(ConsensusRoleType.VALIDATOR);
        consensusManager.changeStateTo(ConsensusRoleType.ORGANIZER);
        consensusManager.changeStateTo(ConsensusRoleType.VALIDATOR);
        consensusManager.changeStateTo(ConsensusRoleType.ORGANIZER);
        consensusManager.changeStateTo(ConsensusRoleType.SUPERVISOR);
        BFTConsensusPhase validatorphase = (BFTConsensusPhase) consensusManager.getRole().manufacturePhases(ConsensusType.TRANSACTION_BLOCK);
    }


    @Test
    public void Clienttest() throws InterruptedException {
        ErasureServerInstance.getInstance();
        CachedEventLoop.getInstance().start();
        CachedConsensusPublisherData.getInstance().storeAtPosition(0, new String("test").getBytes(StandardCharsets.UTF_8));
        (new Thread() {
            @SneakyThrows
            public void run() {
                System.out.println("before");
                Thread.sleep(5000);
                CachedConsensusPublisherData.getInstance().storeAtPosition(1, new String("test").getBytes(StandardCharsets.UTF_8));
                System.out.println("done");
            }
        }).start();
        (new Thread() {
            @SneakyThrows
            public void run() {
                System.out.println("before1");
                Thread.sleep(5000);
                CachedConsensusPublisherData.getInstance().storeAtPosition(2, new String("test").getBytes(StandardCharsets.UTF_8));
                System.out.println("done1");
            }
        }).start();
        RpcErasureClient<byte[]> collector_client = new RpcErasureClient<byte[]>(IPFinder.getLocalIP(), ERASURE_SERVER_PORT, ERASURE_CLIENT_TIMEOUT, CachedEventLoop.getInstance().getEventloop());
        collector_client.connect();
        //RpcErasureClient<byte[]> collector_client1 = new RpcErasureClient<byte[]>(IPFinder.getLocalIP(), ERASURE_SERVER_PORT, ERASURE_CLIENT_TIMEOUT, CachedEventLoop.getInstance().getEventloop());
        //collector_client1.connect();

        byte[] res = collector_client.getPrepareConsensusChunks("1").get();
        byte[] res1 = collector_client.getCommitConsensusChunks("2").get();
        assertEquals("test", new String(res, StandardCharsets.UTF_8));
        assertEquals("test", new String(res1, StandardCharsets.UTF_8));
        //assertEquals("test", new String(res1, StandardCharsets.UTF_8));
        collector_client.close();
        //collector_client1.close();
    }


    //Run this code on one machine as server and run the below test code as client in antoher machine
    //@Test
    //make sure you give enough erasurec_client_timeout
    public void ServerTest() throws InterruptedException {
        CachedEventLoop.getInstance().start();
        CachedConsensusPublisherData.getInstance().clear();
        CachedConsensusPublisherData.getInstance().storeAtPosition(0, new String("test").getBytes(StandardCharsets.UTF_8));
        (new Thread() {
            @SneakyThrows
            public void run() {
                System.out.println("before");
                Thread.sleep(10000);
                CachedConsensusPublisherData.getInstance().storeAtPosition(1, new String("test").getBytes(StandardCharsets.UTF_8));
                System.out.println("done");
            }
        }).start();
        CachedConsensusPublisherData.getInstance().storeAtPosition(2, new String("test").getBytes(StandardCharsets.UTF_8));
        ErasureServerInstance.getInstance();

        while (true) {
            Thread.sleep(400);
        }
    }

    //Run this as client machine
    //make sure you give enough erasurec_client_timeout
    //@Test
    public void test() throws InterruptedException {
        CachedEventLoop.getInstance().start();
        RpcErasureClient<byte[]> collector_client = new RpcErasureClient<byte[]>("192.168.1.116", ERASURE_SERVER_PORT, ERASURE_CLIENT_TIMEOUT, CachedEventLoop.getInstance().getEventloop());
        collector_client.connect();
        while (true) {
            byte[] res = collector_client.getPrepareConsensusChunks("1").get();
            byte[] res1 = collector_client.getCommitConsensusChunks("2").get();

            System.out.println(new String(res, StandardCharsets.UTF_8));
            System.out.println(new String(res1, StandardCharsets.UTF_8));
            collector_client.close();
            collector_client.connect();
        }
    }
}
