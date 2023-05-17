package io.Adrestus.protocol;

import io.Adrestus.consensus.CachedConsensusState;
import io.Adrestus.core.CommitteeBlock;
import io.Adrestus.core.Resourses.*;
import io.Adrestus.core.TransactionBlock;
import io.Adrestus.crypto.SecurityHeader;
import io.Adrestus.crypto.bls.BLS381.ECP;
import io.Adrestus.crypto.bls.BLS381.ECP2;
import io.Adrestus.crypto.bls.mapper.ECP2mapper;
import io.Adrestus.crypto.bls.mapper.ECPmapper;
import io.Adrestus.crypto.elliptic.mapper.BigIntegerSerializer;
import io.Adrestus.crypto.elliptic.mapper.CustomSerializerTreeMap;
import io.Adrestus.network.AsyncServiceNetworkData;
import io.Adrestus.util.SerializationUtil;
import io.activej.bytebuf.ByteBuf;
import io.activej.csp.binary.ByteBufsDecoder;
import io.activej.eventloop.Eventloop;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CachedDataChannelTest {
    private static SerializationUtil<CachedNetworkData> serialize;
    private static final ByteBufsDecoder<ByteBuf> DECODER = ByteBufsDecoder.ofVarIntSizePrefixedBytes();
    @BeforeAll
    public static void setup() {
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(ECP.class, ctx -> new ECPmapper()));
        list.add(new SerializationUtil.Mapping(ECP2.class, ctx -> new ECP2mapper()));
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        list.add(new SerializationUtil.Mapping(TreeMap.class, ctx -> new CustomSerializerTreeMap()));
        serialize = new SerializationUtil<CachedNetworkData>(CachedNetworkData.class, list);
    }

    @Test
    public void test() throws InterruptedException {
        IAdrestusFactory factory = new AdrestusFactory();
        List<AdrestusTask> tasks = new java.util.ArrayList<>(List.of(factory.createBindServerCachedTask()));
        ExecutorService executor = Executors.newFixedThreadPool(tasks.size());
        tasks.stream().map(Worker::new).forEach(executor::execute);

        CachedEpochGeneration.getInstance().setEpoch_counter(3);
        CachedLatestBlocks.getInstance().setCommitteeBlock(new CommitteeBlock());
        CachedLatestBlocks.getInstance().setTransactionBlock(new TransactionBlock());
        CachedLeaderIndex.getInstance().setCommitteePositionLeader(3);
        CachedLeaderIndex.getInstance().setTransactionPositionLeader(2);
        CachedSecurityHeaders.getInstance().setSecurityHeader(new SecurityHeader("1".getBytes(StandardCharsets.UTF_8), "2".getBytes(StandardCharsets.UTF_8)));
        CachedZoneIndex.getInstance().setZoneIndex(1);
        final CachedNetworkData cachedNetworkData = new CachedNetworkData(
                CachedConsensusState.getInstance().isValid(),
                CachedEpochGeneration.getInstance().getEpoch_counter(),
                CachedLatestBlocks.getInstance().getCommitteeBlock(),
                CachedLatestBlocks.getInstance().getTransactionBlock(),
                CachedLeaderIndex.getInstance().getCommitteePositionLeader(),
                CachedLeaderIndex.getInstance().getTransactionPositionLeader(),
                CachedSecurityHeaders.getInstance().getSecurityHeader(),
                CachedZoneIndex.getInstance().getZoneIndex());


        Thread.sleep(4000);

        //check this a bug when connection is open and no progress code is stuck no terminate

        //eventloop.run();*/
        for(int i=0;i<2;i++) {
            ArrayList<String>ips = new ArrayList<>();
            ips.add(new String("192.168.1.116"));
            ips.add(new String("192.168.1.113"));

            var ex = new AsyncServiceNetworkData<Long>(ips);
            var asyncResult = ex.startProcess(1L);
            var result = ex.endProcess(asyncResult);
            try {
                CachedNetworkData networkData = serialize.decode(ex.getResult());
                assertEquals(cachedNetworkData, networkData);
                System.out.println(i);
            } catch (NoSuchElementException e) {
                System.out.println(e.toString());
            }
        }
    }

}
