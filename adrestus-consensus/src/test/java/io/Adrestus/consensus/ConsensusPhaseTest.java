package io.Adrestus.consensus;

import io.Adrestus.core.TransactionBlock;
import org.junit.jupiter.api.Test;

public class ConsensusPhaseTest {

    @Test
    public void consensus_phase_test() throws InterruptedException {
      ConsensusRole role=new Validator();
      BFTConsensusPhase phase=role.manufacturePhases(ConsensusMessageType.VDF);
      ConsensusMessage<String> message=new ConsensusMessage<>("test");
      phase.AnnouncePhase(message);


        BFTConsensusPhase phase1=role.manufacturePhases(ConsensusMessageType.TRANSACTION_BLOCK);
        TransactionBlock b=new TransactionBlock();
        b.setHash("Hash");
        ConsensusMessage<TransactionBlock> message2=new ConsensusMessage<>(b);
        phase1.AnnouncePhase(message2);

    }
}
