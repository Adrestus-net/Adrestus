package io.Adrestus.consensus;

import io.Adrestus.core.TransactionBlock;
import jdk.swing.interop.SwingInterOpUtils;
import org.junit.jupiter.api.Test;

public class ConsensusPhaseTest {

    @Test
    public void consensus_phase_test() throws InterruptedException {
        ConsensusRole role = new Validator();
        BFTConsensusPhase phase = role.manufacturePhases(ConsensusType.VDF);
        ConsensusMessage<String> message = new ConsensusMessage<>("test");
        phase.AnnouncePhase(message);


        BFTConsensusPhase phase1 = role.manufacturePhases(ConsensusType.TRANSACTION_BLOCK);
        TransactionBlock b = new TransactionBlock();
        b.setHash("Hash");
        ConsensusMessage<TransactionBlock> message2 = new ConsensusMessage<>(b);
        phase1.AnnouncePhase(message2);

    }

    @Test
    public void consensus_phase_test2() throws InterruptedException {

        ConsensusManager consensusManager = new ConsensusManager();
        consensusManager.changeStateTo(ConsensusRoleType.VALIDATOR);

        BFTConsensusPhase phase1 = consensusManager.getRole().manufacturePhases(ConsensusType.TRANSACTION_BLOCK);
        TransactionBlock b = new TransactionBlock();
        b.setHash("Hash");
        ConsensusMessage<TransactionBlock> message2 = new ConsensusMessage<>(b);
        phase1.AnnouncePhase(message2);
    }

    @Test
    public void consensus_phase_test3() throws InterruptedException {

        ConsensusManager consensusManager = new ConsensusManager();
        consensusManager.changeStateTo(ConsensusRoleType.ORGANIZER);

        BFTConsensusPhase phase1 = consensusManager.getRole().manufacturePhases(ConsensusType.VRF);
        if(phase1 instanceof OrganizerConsensusPhases.ProposeVDF)
            System.out.println("true");
        VRFConsensusPhase ds=(VRFConsensusPhase)phase1;
        TransactionBlock b = new TransactionBlock();
        b.setHash("Hash");
        ConsensusMessage<String> message2 = new ConsensusMessage<>("assa");
        ds.AnnouncePhase(message2);
        ds.init();
    }
}
