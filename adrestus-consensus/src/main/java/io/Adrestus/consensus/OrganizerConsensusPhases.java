package io.Adrestus.consensus;

import io.Adrestus.core.CommitteeBlock;
import jdk.swing.interop.SwingInterOpUtils;

public class OrganizerConsensusPhases {

    protected static class ProposeVDF extends OrganizerConsensusPhases implements BFTConsensusPhase<String>{

        @Override
        public void AnnouncePhase(ConsensusMessage<String> data) {
            System.out.println(data.toString());
        }

        @Override
        public void PreparePhase(ConsensusMessage<String> data) {

        }

        @Override
        public void CommitPhase(ConsensusMessage<String> data) {

        }
    }


    protected static class ProposeVRF extends OrganizerConsensusPhases implements VRFConsensusPhase<String>{

        @Override
        public void AnnouncePhase(ConsensusMessage<String> data) {

        }

        @Override
        public void PreparePhase(ConsensusMessage<String> data) {

        }

        @Override
        public void CommitPhase(ConsensusMessage<String> data) {

        }

        @Override
        public void init() {
            System.out.println("edw");
        }
    }

    protected static class ProposeCommitteeBlock extends OrganizerConsensusPhases implements BFTConsensusPhase<CommitteeBlock>{

        @Override
        public void AnnouncePhase(ConsensusMessage<CommitteeBlock> block) {

        }

        @Override
        public void PreparePhase(ConsensusMessage<CommitteeBlock> block) {

        }

        @Override
        public void CommitPhase(ConsensusMessage<CommitteeBlock> block) {

        }
    }
}
