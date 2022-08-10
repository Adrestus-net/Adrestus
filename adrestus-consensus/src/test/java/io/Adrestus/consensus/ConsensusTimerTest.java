package io.Adrestus.consensus;

import io.Adrestus.crypto.elliptic.ECDSASignature;
import org.junit.jupiter.api.Test;

public class ConsensusTimerTest {


    @Test
    public void consensus_timer_test() throws InterruptedException {
        ConsensusTimer c=new ConsensusTimer();
        Thread thread=new Thread(c);
        thread.setDaemon(false);
        thread.start();
       // thread.join();
        System.out.println("w");
       // Thread.sleep(10000);
       // c.stopThread();
    }
}
