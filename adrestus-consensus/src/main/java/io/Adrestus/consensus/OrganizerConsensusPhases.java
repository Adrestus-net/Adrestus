package io.Adrestus.consensus;

import io.Adrestus.core.AbstractBlock;
import io.Adrestus.core.BlockType;
import io.Adrestus.core.DefaultFactory;
import io.Adrestus.core.TransactionBlock;
import io.Adrestus.crypto.bls.model.BLSPublicKey;
import io.Adrestus.crypto.bls.model.BLSSignature;
import io.Adrestus.crypto.bls.model.Signature;
import io.Adrestus.util.SerializationUtil;
import org.apache.tuweni.bytes.Bytes;

import java.util.List;
import java.util.stream.Collectors;

public class OrganizerConsensusPhases {

    protected static class ProposeTransactionBlock extends OrganizerConsensusPhases implements BFTConsensusPhase<TransactionBlock> {
        private final DefaultFactory factory;
        private final SerializationUtil<AbstractBlock> serialize;
        private boolean DEBUG;
        public ProposeTransactionBlock(boolean DEBUG) {
            this.DEBUG=DEBUG;
            factory = new DefaultFactory();
            serialize = new SerializationUtil<AbstractBlock>(AbstractBlock.class);
        }

        @Override
        public void AnnouncePhase(ConsensusMessage<TransactionBlock> data) throws Exception {
            var regural_block = factory.getBlock(BlockType.REGULAR);
            regural_block.forgeTransactionBlock(data.getData());
            data.setMessageType(ConsensusMessageType.ANNOUNCE);
            if(DEBUG)
                return;
        }

        @Override
        public void PreparePhase(ConsensusMessage<TransactionBlock> data) {
            data.setMessageType(ConsensusMessageType.PREPARE);

            List<BLSPublicKey> publicKeys = data.getSignatures().stream().map(ConsensusMessage.ChecksumData::getBlsPublicKey).collect(Collectors.toList());
            List<Signature> signature = data.getSignatures().stream().map(ConsensusMessage.ChecksumData::getSignature).collect(Collectors.toList());


            Signature aggregatedSignature = BLSSignature.aggregate(signature);
            Bytes message = Bytes.wrap(serialize.encode(data.getData()));
            boolean verify = BLSSignature.fastAggregateVerify(publicKeys, message, aggregatedSignature);
            if (!verify)
                throw new IllegalArgumentException("Abort consensus phase BLS multi_signature is invalid during prepare phase");

            if(DEBUG)
                return;
        }

        @Override
        public void CommitPhase(ConsensusMessage<TransactionBlock> data) {
            data.setMessageType(ConsensusMessageType.COMMIT);

            List<BLSPublicKey> publicKeys = data.getSignatures().stream().map(ConsensusMessage.ChecksumData::getBlsPublicKey).collect(Collectors.toList());
            List<Signature> signature = data.getSignatures().stream().map(ConsensusMessage.ChecksumData::getSignature).collect(Collectors.toList());


            Signature aggregatedSignature = BLSSignature.aggregate(signature);
            Bytes message = Bytes.wrap(serialize.encode(data.getData()));
            boolean verify = BLSSignature.fastAggregateVerify(publicKeys, message, aggregatedSignature);
            if (!verify)
                throw new IllegalArgumentException("Abort consensus phase BLS multi_signature is invalid during commit phase");

            //commit save to db

            if(DEBUG)
                return;
        }
    }

}
