package io.Adrestus.consensus;

import com.google.common.reflect.TypeToken;
import io.Adrestus.core.AbstractBlock;
import io.Adrestus.core.BlockType;
import io.Adrestus.core.DefaultFactory;
import io.Adrestus.core.Resourses.CachedLatestBlocks;
import io.Adrestus.core.TransactionBlock;
import io.Adrestus.crypto.bls.BLS381.ECP;
import io.Adrestus.crypto.bls.BLS381.ECP2;
import io.Adrestus.crypto.bls.mapper.ECP2mapper;
import io.Adrestus.crypto.bls.mapper.ECPmapper;
import io.Adrestus.crypto.bls.model.BLSPublicKey;
import io.Adrestus.crypto.bls.model.BLSSignature;
import io.Adrestus.crypto.bls.model.CachedBLSKeyPair;
import io.Adrestus.crypto.bls.model.Signature;
import io.Adrestus.network.ConsensusServer;
import io.Adrestus.util.SerializationUtil;
import org.apache.tuweni.bytes.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

public class OrganizerConsensusPhases {

    protected static class ProposeTransactionBlock extends OrganizerConsensusPhases implements BFTConsensusPhase<TransactionBlock> {
        private static final Type fluentType = new TypeToken<ConsensusMessage<TransactionBlock>>() {
        }.getType();


        private static Logger LOG = LoggerFactory.getLogger(ProposeTransactionBlock.class);

        private final DefaultFactory factory;
        private final SerializationUtil<AbstractBlock> block_serialize;
        private final SerializationUtil<ConsensusMessage> consensus_serialize;
        private final boolean DEBUG;
        private final CountDownLatch latch;

        private int N;
        private int F;

        private ConsensusServer consensusServer;
        private BLSPublicKey leader_bls;
        private int current;

        public ProposeTransactionBlock(boolean DEBUG) {
            this.DEBUG = DEBUG;
            this.factory = new DefaultFactory();
            //this.N = CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(1).size()-1;
            this.N = 3;
            this.F = (this.N - 1) / 3;
            this.latch = new CountDownLatch(N);
            if (!DEBUG) {
                this.current = CachedLatestBlocks.getInstance().getCommitteeBlock().getPublicKeyIndex(1, CachedLatestBlocks.getInstance().getTransactionBlock().getLeaderPublicKey());
                if (current == CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(1).size()-1) {
                    this.leader_bls = CachedLatestBlocks.getInstance().getCommitteeBlock().getPublicKeyByIndex(1, 0);
                    this.consensusServer = new ConsensusServer(CachedLatestBlocks.getInstance().getCommitteeBlock().getValue(1, this.leader_bls), latch);
                } else {
                    this.leader_bls = CachedLatestBlocks.getInstance().getCommitteeBlock().getPublicKeyByIndex(1, current + 1);
                    this.consensusServer = new ConsensusServer(CachedLatestBlocks.getInstance().getCommitteeBlock().getValue(1, this.leader_bls), latch);
                }
            }


            List<SerializationUtil.Mapping> list = new ArrayList<>();
            list.add(new SerializationUtil.Mapping(ECP.class, ctx -> new ECPmapper()));
            list.add(new SerializationUtil.Mapping(ECP2.class, ctx -> new ECP2mapper()));
            this.block_serialize = new SerializationUtil<AbstractBlock>(AbstractBlock.class, list);
            this.consensus_serialize = new SerializationUtil<ConsensusMessage>(fluentType, list);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void AnnouncePhase(ConsensusMessage<TransactionBlock> data) throws Exception {
            var regural_block = factory.getBlock(BlockType.REGULAR);
            regural_block.forgeTransactionBlock(data.getData());
            data.setMessageType(ConsensusMessageType.ANNOUNCE);
            if (DEBUG)
                return;

            byte[] message = block_serialize.encode(data.getData());

            Signature sig = BLSSignature.sign(block_serialize.encode(data.getData()), CachedBLSKeyPair.getInstance().getPrivateKey());
            data.getChecksumData().setBlsPublicKey(CachedBLSKeyPair.getInstance().getPublicKey());
            data.getChecksumData().setSignature(sig);

            //data.setChecksumData(new ConsensusMessage.ChecksumData(sig, CachedBLSKeyPair.getInstance().getPublicKey()));
            byte[] toSend = consensus_serialize.encode(data);
            consensusServer.publishMessage(toSend);
        }

        @Override
        public void PreparePhase(ConsensusMessage<TransactionBlock> data) {
            if (!DEBUG) {
                int i = N;
                while (i > 0) {
                    byte[] receive = consensusServer.receiveData();
                    try {
                        if (receive == null) {
                            LOG.info("PreparePhase: Null message from validators");
                            i--;
                        }
                        else {
                            ConsensusMessage<TransactionBlock> received = consensus_serialize.decode(receive);
                            if (!CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(1).containsKey(received.getChecksumData().getBlsPublicKey())) {
                                LOG.info("PreparePhase: Validator does not exist on consensus... Ignore");
                                i--;
                            } else {
                                data.getSignatures().add(received.getChecksumData());
                                N--;
                                i--;
                            }
                        }
                    } catch (IllegalArgumentException e) {
                        LOG.info("PreparePhase: Problem at message deserialization");
                        data.setStatusType(ConsensusStatusType.ABORT);
                        cleanup();
                        return;
                    }
                }


                if (N > F) {
                    LOG.info("PreparePhase: Byzantine network not meet requirements abort");
                    data.setStatusType(ConsensusStatusType.ABORT);
                    cleanup();
                    return;
                }
            }
            data.setMessageType(ConsensusMessageType.PREPARE);

            List<BLSPublicKey> publicKeys = data.getSignatures().stream().map(ConsensusMessage.ChecksumData::getBlsPublicKey).collect(Collectors.toList());
            List<Signature> signature = data.getSignatures().stream().map(ConsensusMessage.ChecksumData::getSignature).collect(Collectors.toList());

            Signature aggregatedSignature = BLSSignature.aggregate(signature);

            Bytes message = Bytes.wrap(block_serialize.encode(data.getData()));
            boolean verify = BLSSignature.fastAggregateVerify(publicKeys, message, aggregatedSignature);
            if (!verify)
                throw new IllegalArgumentException("Abort consensus phase BLS multi_signature is invalid during prepare phase");

            if (DEBUG)
                return;

            Signature sig = BLSSignature.sign(block_serialize.encode(data.getData()), CachedBLSKeyPair.getInstance().getPrivateKey());
            data.setChecksumData(new ConsensusMessage.ChecksumData(sig, CachedBLSKeyPair.getInstance().getPublicKey()));

            this.N = 3;
            this.F = (this.N - 1) / 3;


            byte[] toSend = consensus_serialize.encode(data);
            consensusServer.publishMessage(toSend);
        }

        @Override
        public void CommitPhase(ConsensusMessage<TransactionBlock> data) {
            if (!DEBUG) {
                int i = N;
                data.getSignatures().clear();
                while (i > 0) {
                    byte[] receive = consensusServer.receiveData();
                    try {
                        if (receive == null) {
                            LOG.info("CommitPhase: Not Receiving from Validators");
                            i--;
                        } else {
                            ConsensusMessage<TransactionBlock> received = consensus_serialize.decode(receive);
                            if (!CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(1).containsKey(received.getChecksumData().getBlsPublicKey())) {
                                LOG.info("CommitPhase: Validator does not exist on consensus... Ignore");
                                i--;
                            } else {
                                data.getSignatures().add(received.getChecksumData());
                                N--;
                                i--;
                            }
                        }
                    } catch (IllegalArgumentException e) {
                        LOG.info("CommitPhase: Problem at message deserialization");
                        data.setStatusType(ConsensusStatusType.ABORT);
                        cleanup();
                        return;
                    }
                }


                if (N > F) {
                    LOG.info("CommitPhase: Byzantine network not meet requirements abort");
                    data.setStatusType(ConsensusStatusType.ABORT);
                    cleanup();
                    return;
                }
            }
            data.setMessageType(ConsensusMessageType.COMMIT);

            List<BLSPublicKey> publicKeys = data.getSignatures().stream().map(ConsensusMessage.ChecksumData::getBlsPublicKey).collect(Collectors.toList());
            List<Signature> signature = data.getSignatures().stream().map(ConsensusMessage.ChecksumData::getSignature).collect(Collectors.toList());


            Signature aggregatedSignature = BLSSignature.aggregate(signature);
            Bytes message = Bytes.wrap(block_serialize.encode(data.getData()));
            boolean verify = BLSSignature.fastAggregateVerify(publicKeys, message, aggregatedSignature);
            if (!verify)
                throw new IllegalArgumentException("CommitPhase: Abort consensus phase BLS multi_signature is invalid during commit phase");

            //commit save to db

            if (DEBUG)
                return;

            Signature sig = BLSSignature.sign(block_serialize.encode(data.getData()), CachedBLSKeyPair.getInstance().getPrivateKey());
            data.setChecksumData(new ConsensusMessage.ChecksumData(sig, CachedBLSKeyPair.getInstance().getPublicKey()));

            this.N = 3;
            this.F = (this.N - 1) / 3;
            int i = N;

            byte[] toSend = consensus_serialize.encode(data);
            consensusServer.publishMessage(toSend);

            if (current == CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(1).size()-1)
                data.getData().setLeaderPublicKey(CachedLatestBlocks.getInstance().getCommitteeBlock().getPublicKeyByIndex(1, 0));
            else {
                data.getData().setLeaderPublicKey(CachedLatestBlocks.getInstance().getCommitteeBlock().getPublicKeyByIndex(1, current + 1));
            }
            CachedLatestBlocks.getInstance().setTransactionBlock(data.getData());

            while (i > 0) {
                try {
                    consensusServer.receiveStringData();
                } catch (NullPointerException ex) {
                } finally {
                    i--;
                }
            }
            cleanup();
            LOG.info("Block is finalized with Success");
        }

        private void cleanup() {
            consensusServer.close();
        }
    }


}
