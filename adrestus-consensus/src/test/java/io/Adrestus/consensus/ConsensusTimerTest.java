package io.Adrestus.consensus;

import io.Adrestus.crypto.elliptic.ECDSASignature;
import org.junit.jupiter.api.Test;

public class ConsensusTimerTest {


    @Test
    public void consensus_timer_test() throws InterruptedException {
        ConsensusTimer c=new ConsensusTimer();
        new Thread(c).start();
        Thread.sleep(10000);
        c.stopThread();
    }
}
