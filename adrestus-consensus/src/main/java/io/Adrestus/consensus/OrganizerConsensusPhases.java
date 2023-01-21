package io.Adrestus.consensus;

import com.google.common.reflect.TypeToken;
import io.Adrestus.core.*;
import io.Adrestus.core.Resourses.CachedLatestBlocks;
import io.Adrestus.core.Resourses.CachedLeaderIndex;
import io.Adrestus.core.Resourses.CachedZoneIndex;
import io.Adrestus.crypto.bls.BLS381.ECP;
import io.Adrestus.crypto.bls.BLS381.ECP2;
import io.Adrestus.crypto.bls.mapper.ECP2mapper;
import io.Adrestus.crypto.bls.mapper.ECPmapper;
import io.Adrestus.crypto.bls.model.BLSPublicKey;
import io.Adrestus.crypto.bls.model.BLSSignature;
import io.Adrestus.crypto.bls.model.CachedBLSKeyPair;
import io.Adrestus.crypto.bls.model.Signature;
import io.Adrestus.crypto.elliptic.mapper.BigIntegerSerializer;
import io.Adrestus.crypto.elliptic.mapper.CustomSerializerTreeMap;
import io.Adrestus.network.ConsensusServer;
import io.Adrestus.util.SerializationUtil;
import org.apache.tuweni.bytes.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.*;
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
        private final IBlockIndex blockIndex;
        private final Map<BLSPublicKey, SignatureData> signatureDataMap;
        private CountDownLatch latch;
        private int N;
        private int F;

        private ConsensusServer consensusServer;
        private BLSPublicKey leader_bls;
        private int current;

        public ProposeTransactionBlock(boolean DEBUG) {
            this.DEBUG = DEBUG;
            this.blockIndex = new BlockIndex();
            this.factory = new DefaultFactory();
            List<SerializationUtil.Mapping> list = new ArrayList<>();
            list.add(new SerializationUtil.Mapping(ECP.class, ctx -> new ECPmapper()));
            list.add(new SerializationUtil.Mapping(ECP2.class, ctx -> new ECP2mapper()));
            list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
            list.add(new SerializationUtil.Mapping(TreeMap.class, ctx -> new CustomSerializerTreeMap()));
            this.block_serialize = new SerializationUtil<AbstractBlock>(AbstractBlock.class, list);
            this.consensus_serialize = new SerializationUtil<ConsensusMessage>(fluentType, list);
            this.signatureDataMap = new HashMap<BLSPublicKey, SignatureData>();
        }

        @Override
        public void InitialSetup() {
            try {
                if (!DEBUG) {
                    //this.N = 1;
                    this.N = CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(CachedZoneIndex.getInstance().getZoneIndex()).size() - 1;
                    this.F = (this.N - 1) / 3;
                    this.latch = new CountDownLatch(N);
                    this.current = CachedLeaderIndex.getInstance().getTransactionPositionLeader();
                    if (current == CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(CachedZoneIndex.getInstance().getZoneIndex()).size() - 1) {
                        CachedLeaderIndex.getInstance().setTransactionPositionLeader(0);
                        this.leader_bls = this.blockIndex.getPublicKeyByIndex(CachedZoneIndex.getInstance().getZoneIndex(), 0);
                        this.consensusServer = new ConsensusServer(this.blockIndex.getIpValue(CachedZoneIndex.getInstance().getZoneIndex(), this.leader_bls), latch);
                    } else {
                        CachedLeaderIndex.getInstance().setTransactionPositionLeader(current + 1);
                        this.leader_bls = this.blockIndex.getPublicKeyByIndex(CachedZoneIndex.getInstance().getZoneIndex(), current + 1);
                        this.consensusServer = new ConsensusServer(this.blockIndex.getIpValue(CachedZoneIndex.getInstance().getZoneIndex(), this.leader_bls), latch);
                    }
                }
            } catch (Exception e) {
                cleanup();
                LOG.info("InitialSetup: Exception caught " + e.toString());
                throw new IllegalArgumentException("Exception caught " + e.toString());
            }
        }

        @Override
        public void AnnouncePhase(ConsensusMessage<TransactionBlock> data) throws Exception {
            if (this.consensusServer.getPeers_not_connected() >F) {
                LOG.info("AnnouncePhase: Byzantine network not meet requirements abort " + String.valueOf(this.consensusServer.getPeers_not_connected()));
                data.setStatusType(ConsensusStatusType.ABORT);
                cleanup();
                return;
            }
            var regural_block = factory.getBlock(BlockType.REGULAR);
            regural_block.forgeTransactionBlock(data.getData());
            data.setMessageType(ConsensusMessageType.ANNOUNCE);
            if (DEBUG)
                return;

            byte[] message = block_serialize.encode(data.getData());
            Signature sig = BLSSignature.sign(message, CachedBLSKeyPair.getInstance().getPrivateKey());
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
                        } else {
                            ConsensusMessage<TransactionBlock> received = consensus_serialize.decode(receive);
                            data.setData(received.getData());
                            if (!CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(CachedZoneIndex.getInstance().getZoneIndex()).containsKey(received.getChecksumData().getBlsPublicKey())) {
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
                        //data.setStatusType(ConsensusStatusType.ABORT);
                        // cleanup();
                        return;
                    }
                }


                if (N > F) {
                    LOG.info("PreparePhase: Byzantine network not meet requirements abort " + String.valueOf(N));
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
            if (!verify) {
                LOG.info("Abort consensus phase BLS multi_signature is invalid during prepare phase");
                data.setStatusType(ConsensusStatusType.ABORT);
                cleanup();
                return;
            }


            if (DEBUG)
                return;

            //##############################################################
            int pos = 0;
            for (BLSPublicKey blsPublicKey : publicKeys) {
                SignatureData signatureData = new SignatureData(blsPublicKey);
                signatureData.getSignature()[0] = signature.get(pos);
                signatureDataMap.put(blsPublicKey, signatureData);
                pos++;
            }
            //##############################################################

            Signature sig = BLSSignature.sign(block_serialize.encode(data.getData()), CachedBLSKeyPair.getInstance().getPrivateKey());
            data.setChecksumData(new ConsensusMessage.ChecksumData(sig, CachedBLSKeyPair.getInstance().getPublicKey()));

            this.N = CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(CachedZoneIndex.getInstance().getZoneIndex()).size() - 1;
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
                            data.setData(received.getData());
                            if (!CachedLatestBlocks.
                                    getInstance()
                                    .getCommitteeBlock()
                                    .getStructureMap()
                                    .get(CachedZoneIndex
                                            .getInstance()
                                            .getZoneIndex())
                                    .containsKey(received.getChecksumData().getBlsPublicKey())) {
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
                        //data.setStatusType(ConsensusStatusType.ABORT);
                        //  cleanup();
                        //  return;
                    }
                }


                if (N > F) {
                    LOG.info("CommitPhase: Byzantine network not meet requirements abort " + String.valueOf(N));
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
            if (!verify) {
                LOG.info("CommitPhase: Abort consensus phase BLS multi_signature is invalid during commit phase");
                data.setStatusType(ConsensusStatusType.ABORT);
                cleanup();
                return;
            }


            //commit save to db
            if (DEBUG)
                return;

            //##############################################################
            int pos = 0;
            for (BLSPublicKey blsPublicKey : publicKeys) {
                SignatureData signatureData = signatureDataMap.get(blsPublicKey);
                signatureData.getSignature()[1] = signature.get(pos);
                signatureDataMap.put(blsPublicKey, signatureData);
                pos++;
            }
            //##############################################################

            Signature sig = BLSSignature.sign(block_serialize.encode(data.getData()), CachedBLSKeyPair.getInstance().getPrivateKey());
            data.setChecksumData(new ConsensusMessage.ChecksumData(sig, CachedBLSKeyPair.getInstance().getPublicKey()));

            this.N = CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(CachedZoneIndex.getInstance().getZoneIndex()).size() - 1;
            this.F = (this.N - 1) / 3;
            int i = N;

            byte[] toSend = consensus_serialize.encode(data);
            consensusServer.publishMessage(toSend);

          /*  if (current == CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(1).size() - 1)
                data.getData().setLeaderPublicKey(this.blockIndex.getPublicKeyByIndex(CachedZoneIndex.getInstance().getZoneIndex(), 0));
            else {
                data.getData().setLeaderPublicKey(this.blockIndex.getPublicKeyByIndex(CachedZoneIndex.getInstance().getZoneIndex(), current + 1));
            }*/
            // CachedLatestBlocks.getInstance().setTransactionBlock(data.getData());
            data.getData().setSignatureData(signatureDataMap);
            BlockInvent regural_block = (BlockInvent) factory.getBlock(BlockType.REGULAR);
            regural_block.InventTransactionBlock(data.getData());
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
            consensusServer = null;
        }
    }


}
