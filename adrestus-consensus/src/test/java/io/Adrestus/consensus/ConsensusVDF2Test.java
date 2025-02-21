package io.Adrestus.consensus;

import io.Adrestus.config.RunningConfig;
import io.Adrestus.consensus.helper.ConsensusVDFTimer;
import io.Adrestus.core.CommitteeBlock;
import io.Adrestus.core.Resourses.CachedLatestBlocks;
import io.Adrestus.core.Resourses.CachedSecurityHeaders;
import io.Adrestus.crypto.bls.model.BLSPrivateKey;
import io.Adrestus.crypto.bls.model.BLSPublicKey;
import io.Adrestus.crypto.bls.model.CachedBLSKeyPair;
import io.Adrestus.network.CachedEventLoop;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class ConsensusVDF2Test {

    private static BLSPrivateKey sk1;
    private static BLSPublicKey vk1;

    private static BLSPrivateKey sk2;
    private static BLSPublicKey vk2;

    private static BLSPrivateKey sk3;
    private static BLSPublicKey vk3;


    @BeforeAll
    public static void setup() throws Exception {
        if (RunningConfig.isRunningInMaven() && !RunningConfig.isRunningInDocker() && RunningConfig.isRunningInAppveyor()) {
            return;
        } else if (RunningConfig.isRunningInMaven() && !RunningConfig.isRunningInDocker() && !RunningConfig.isRunningInAppveyor()) {
            return;
        } else if ((RunningConfig.isRunningInDocker() && !RunningConfig.isRunningInAppveyor()) || !RunningConfig.isRunningInMaven()) {
            sk1 = new BLSPrivateKey(1);
            vk1 = new BLSPublicKey(sk1);

            sk2 = new BLSPrivateKey(2);
            vk2 = new BLSPublicKey(sk2);

            sk3 = new BLSPrivateKey(3);
            vk3 = new BLSPublicKey(sk3);

            CommitteeBlock committeeBlock = new CommitteeBlock();
            committeeBlock.setDifficulty(113);
            committeeBlock.setViewID(1);
            committeeBlock.setHash("hash");
            committeeBlock.getHeaderData().setTimestamp("2022-11-18 15:01:29.304");
            committeeBlock.getStructureMap().get(0).put(vk1, System.getProperty("test.arg0") == null ? "192.168.1.106" : System.getProperty("test.arg0"));
            committeeBlock.getStructureMap().get(0).put(vk2, System.getProperty("test.arg1") == null ? "192.168.1.116" : System.getProperty("test.arg1"));
            committeeBlock.getStructureMap().get(0).put(vk3, System.getProperty("test.arg2") == null ? "192.168.1.115" : System.getProperty("test.arg2"));

//        CachedSecurityHeaders.getInstance().getSecurityHeader().setPRnd(Hex.decode("c1f72aa5bd1e1d53c723b149259b63f759f40d5ab003b547d5c13d45db9a5da8"));
            CachedLatestBlocks.getInstance().setCommitteeBlock(committeeBlock);
            CachedSecurityHeaders.getInstance().getRndSecurityHeaderViewID();
        }

    }

    @Test
    public void vdf_test() throws Exception {
        if (RunningConfig.isRunningInMaven() && !RunningConfig.isRunningInDocker() && RunningConfig.isRunningInAppveyor()) {
            return;
        } else if (RunningConfig.isRunningInMaven() && !RunningConfig.isRunningInDocker() && !RunningConfig.isRunningInAppveyor()) {
            return;
        } else if ((RunningConfig.isRunningInDocker() && !RunningConfig.isRunningInAppveyor()) || !RunningConfig.isRunningInMaven()) {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress("google.com", 80));
            String IP = socket.getLocalAddress().getHostAddress();
            int hit = 0;
            for (Map.Entry<BLSPublicKey, String> entry : CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(0).entrySet()) {
                if (IP.equals(entry.getValue())) {
                    if (vk1.equals(entry.getKey())) {
                        CachedBLSKeyPair.getInstance().setPrivateKey(sk1);
                        CachedBLSKeyPair.getInstance().setPublicKey(vk1);
                    } else if (vk2.equals(entry.getKey())) {
                        CachedBLSKeyPair.getInstance().setPrivateKey(sk2);
                        CachedBLSKeyPair.getInstance().setPublicKey(vk2);
                    } else if (vk3.equals(entry.getKey())) {
                        CachedBLSKeyPair.getInstance().setPrivateKey(sk3);
                        CachedBLSKeyPair.getInstance().setPublicKey(vk3);
                    }
                    hit = 1;
                    break;
                }
            }
            if (hit == 0)
                return;

            CachedEventLoop.getInstance().start();
            CountDownLatch latch = new CountDownLatch(5);
            ConsensusVDFTimer c = new ConsensusVDFTimer(latch);
            latch.await();
        }
    }
}
