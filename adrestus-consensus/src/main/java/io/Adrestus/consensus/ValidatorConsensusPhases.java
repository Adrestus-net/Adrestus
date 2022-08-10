package io.Adrestus.consensus;

import io.Adrestus.core.CommitteeBlock;
import io.Adrestus.core.TransactionBlock;

public class ValidatorConsensusPhases {

    public ValidatorConsensusPhases() {}

    protected static class VerifyVDF extends ValidatorConsensusPhases implements BFTConsensusPhase<String>{

        @Override
        public void AnnouncePhase(ConsensusMessage<String> data) {
            //data.setData("another data");
            System.out.println(data.toString());
        }

        @Override
        public void PreparePhase(ConsensusMessage<String> data) {

        }

        @Override
        public void CommitPhase(ConsensusMessage<String> data) {

        }
    }


    protected static class VerifyVRF extends ValidatorConsensusPhases implements BFTConsensusPhase<String>{

        @Override
        public void AnnouncePhase(ConsensusMessage<String> data) {

        }

        @Override
        public void PreparePhase(ConsensusMessage<String> data) {

        }

        @Override
        public void CommitPhase(ConsensusMessage<String> data) {

        }
    }


    protected static class VerifyTransactionBlock extends ValidatorConsensusPhases implements BFTConsensusPhase<TransactionBlock>{


        @Override
        public void AnnouncePhase(ConsensusMessage<TransactionBlock> block) {
            System.out.println(block.toString());
        }

        @Override
        public void PreparePhase(ConsensusMessage<TransactionBlock> block) {

        }

        @Override
        public void CommitPhase(ConsensusMessage<TransactionBlock> block) {

        }
    }

    protected static class VerifyCommitteeBlock extends ValidatorConsensusPhases implements BFTConsensusPhase<CommitteeBlock>{

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
