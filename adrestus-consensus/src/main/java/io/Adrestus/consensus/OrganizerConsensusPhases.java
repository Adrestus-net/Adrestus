package io.Adrestus.consensus;

import com.google.common.reflect.TypeToken;
import io.Adrestus.Trie.MerkleNode;
import io.Adrestus.Trie.MerkleTreeOptimizedImp;
import io.Adrestus.config.KafkaConfiguration;
import io.Adrestus.core.*;
import io.Adrestus.core.Resourses.CachedLatestBlocks;
import io.Adrestus.core.Resourses.CachedLeaderIndex;
import io.Adrestus.core.Resourses.CachedZoneIndex;
import io.Adrestus.core.Resourses.ErasureServerInstance;
import io.Adrestus.core.Util.BlockSizeCalculator;
import io.Adrestus.core.comparators.SortSignatureMapByBlsPublicKey;
import io.Adrestus.crypto.HashUtil;
import io.Adrestus.crypto.bls.BLS381.ECP;
import io.Adrestus.crypto.bls.BLS381.ECP2;
import io.Adrestus.crypto.bls.BLSSignatureData;
import io.Adrestus.crypto.bls.mapper.ECP2mapper;
import io.Adrestus.crypto.bls.mapper.ECPmapper;
import io.Adrestus.crypto.bls.model.BLSPublicKey;
import io.Adrestus.crypto.bls.model.BLSSignature;
import io.Adrestus.crypto.bls.model.CachedBLSKeyPair;
import io.Adrestus.crypto.bls.model.Signature;
import io.Adrestus.crypto.elliptic.mapper.BigDecimalSerializer;
import io.Adrestus.crypto.elliptic.mapper.BigIntegerSerializer;
import io.Adrestus.crypto.elliptic.mapper.CustomSerializerTreeMap;
import io.Adrestus.erasure.code.ArrayDataEncoder;
import io.Adrestus.erasure.code.EncodingPacket;
import io.Adrestus.erasure.code.OpenRQ;
import io.Adrestus.erasure.code.encoder.SourceBlockEncoder;
import io.Adrestus.erasure.code.parameters.FECParameterObject;
import io.Adrestus.erasure.code.parameters.FECParameters;
import io.Adrestus.erasure.code.parameters.FECParametersPreConditions;
import io.Adrestus.network.ConsensusBrokerInstance;
import io.Adrestus.network.IPFinder;
import io.Adrestus.network.TopicType;
import io.Adrestus.util.SerializationFuryUtil;
import io.Adrestus.util.SerializationUtil;
import lombok.SneakyThrows;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.tuweni.bytes.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class OrganizerConsensusPhases {

    protected static class ProposeTransactionBlock extends OrganizerConsensusPhases implements BFTConsensusPhase<TransactionBlock> {
        private static final Type fluentType = new TypeToken<ConsensusMessage<TransactionBlock>>() {
        }.getType();


        private static Logger LOG = LoggerFactory.getLogger(ProposeTransactionBlock.class);
        private static final String delimeter = "||";

        private final DefaultFactory factory;
        private final SerializationUtil<AbstractBlock> block_serialize;
        private final SerializationUtil<Signature> signatureMapper;
        private final SerializationUtil<SerializableErasureObject> serenc_erasure;
        private final IBlockIndex blockIndex;
        private final OptionalInt position;
        private final ArrayList<String> ips;

        private final BlockSizeCalculator sizeCalculator;
        private CountDownLatch latch;
        private int N;
        private int F;

        private MerkleTreeOptimizedImp tree;
        private BLSPublicKey leader_bls;
        private int current;
        private TransactionBlock original_copy;


        @SneakyThrows
        public ProposeTransactionBlock() {
            this.blockIndex = new BlockIndex();
            this.factory = new DefaultFactory();
            this.factory.getBlock(BlockType.GENESIS);
            this.original_copy = new TransactionBlock().clone();
            List<SerializationUtil.Mapping> list = new ArrayList<>();
            list.add(new SerializationUtil.Mapping(ECP.class, ctx -> new ECPmapper()));
            list.add(new SerializationUtil.Mapping(ECP2.class, ctx -> new ECP2mapper()));
            list.add(new SerializationUtil.Mapping(BigDecimal.class, ctx -> new BigDecimalSerializer()));
            list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
            list.add(new SerializationUtil.Mapping(TreeMap.class, ctx -> new CustomSerializerTreeMap<BLSPublicKey, BLSSignatureData>()));
            this.block_serialize = new SerializationUtil<AbstractBlock>(AbstractBlock.class, list);
            this.sizeCalculator = new BlockSizeCalculator();
            this.signatureMapper = new SerializationUtil<Signature>(Signature.class, list);
            this.serenc_erasure = new SerializationUtil<SerializableErasureObject>(SerializableErasureObject.class, list);
            this.ips = this.blockIndex.getIpList(CachedZoneIndex.getInstance().getZoneIndex());
            KafkaConfiguration.KAFKA_HOST = IPFinder.getLocalIP();
            this.position = IntStream.range(0, this.ips.size()).filter(i -> KafkaConfiguration.KAFKA_HOST.equals(ips.get(i))).findFirst();
            ConsensusBrokerInstance.getInstance(ips, ips.getFirst(), position.getAsInt());
            ErasureServerInstance.getInstance();
        }

        @Override
        public void InitialSetup() {
            try {
                this.N = CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(CachedZoneIndex.getInstance().getZoneIndex()).size();
                this.F = (this.N - 1) / 3;
                this.current = CachedLeaderIndex.getInstance().getTransactionPositionLeader();
                ConsensusBrokerInstance.getInstance().getConsensusBroker().setLeader_host(this.blockIndex.getIpValue(CachedZoneIndex.getInstance().getZoneIndex(), CachedBLSKeyPair.getInstance().getPublicKey()));
                ConsensusBrokerInstance.getInstance().getConsensusBroker().setLeader_position(CachedLeaderIndex.getInstance().getTransactionPositionLeader());
                if (current == CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(CachedZoneIndex.getInstance().getZoneIndex()).size() - 1) {
                    CachedLeaderIndex.getInstance().setTransactionPositionLeader(0);
                } else {
                    CachedLeaderIndex.getInstance().setTransactionPositionLeader(current + 1);
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new IllegalArgumentException("Exception caught " + e.toString());
            }
        }

        @Override
        public void DispersePhase(ConsensusMessage<TransactionBlock> data) throws Exception {
            var regural_block = factory.getBlock(BlockType.REGULAR);
            regural_block.forgeTransactionBlock(data.getData());
            this.original_copy = data.getData().clone();
            data.setMessageType(ConsensusMessageType.DISPERSE);

            try {
                BlockSizeCalculator sizeCalculator = new BlockSizeCalculator();
                sizeCalculator.setTransactionBlock(this.original_copy);
                byte[] buffer = block_serialize.encode(this.original_copy, sizeCalculator.TransactionBlockSizeCalculator());

                long dataLen = buffer.length;
                int sizeOfCommittee = this.N - 1;
                double loss = .6;
                int numSrcBlks = sizeOfCommittee;
                int symbSize = (int) (dataLen / sizeOfCommittee);
                FECParameterObject object = FECParametersPreConditions.CalculateFECParameters(dataLen, symbSize, numSrcBlks);
                FECParameters fecParams = FECParameters.newParameters(object.getDataLen(), object.getSymbolSize(), object.getNumberOfSymbols());

                byte[] chunks = new byte[fecParams.dataLengthAsInt()];
                System.arraycopy(buffer, 0, chunks, 0, chunks.length);
                final ArrayDataEncoder enc = OpenRQ.newEncoder(chunks, fecParams);
                ArrayList<SerializableErasureObject> serializableErasureObjects = new ArrayList<SerializableErasureObject>();
                ArrayList<EncodingPacket> n = new ArrayList<EncodingPacket>();
                for (SourceBlockEncoder sbEnc : enc.sourceBlockIterable()) {
                    for (EncodingPacket srcPacket : sbEnc.sourcePacketsIterable()) {
                        n.add(srcPacket);
                    }
                }
                tree = new MerkleTreeOptimizedImp();
                ArrayList<MerkleNode> merkleNodes = new ArrayList<MerkleNode>();
                for (int i = 0; i < n.size(); i++) {
                    SerializableErasureObject serializableErasureObject = new SerializableErasureObject(object, n.get(i).asArray(), new ArrayList<byte[]>());
                    serializableErasureObjects.add(serializableErasureObject);
                    merkleNodes.add(new MerkleNode(HashUtil.XXH3(serializableErasureObject.getOriginalPacketChunks())));
                }
                tree.constructTree(merkleNodes);
                for (int j = 0; j < serializableErasureObjects.size(); j++) {
                    SerializableErasureObject serializableErasureObject = serializableErasureObjects.get(j);
                    tree.build_proofs(new MerkleNode(HashUtil.XXH3(serializableErasureObject.getOriginalPacketChunks())));
                    serializableErasureObject.setProofs(tree.getMerkleeproofs());
                    serializableErasureObject.setRootMerkleHash(tree.getRootHash());
                }
                int sendSize = 0;
                int onlyFirstSize = 0;
                if (serializableErasureObjects.size() >= sizeOfCommittee) {
                    sendSize = serializableErasureObjects.size() / sizeOfCommittee;
                    onlyFirstSize = (n.size() - sendSize * sizeOfCommittee);
                } else {
                    sendSize = sizeOfCommittee;
                    onlyFirstSize = sizeOfCommittee - sendSize * n.size();
                }

                int startPosition = 0;
                ArrayList<ArrayList<byte[]>> finalList = new ArrayList<>();
                while (startPosition < serializableErasureObjects.size()) {
                    int endPosition = Math.min(startPosition + sendSize + onlyFirstSize, serializableErasureObjects.size());
                    ArrayList<byte[]> toSend = new ArrayList<>(endPosition - startPosition);
                    for (int i = startPosition; i < endPosition; i++) {
                        SerializableErasureObject serializableErasureObject = serializableErasureObjects.get(i);
                        toSend.add(serenc_erasure.encode(serializableErasureObject, serializableErasureObject.getSize()));
                    }
                    if (toSend.isEmpty()) {
                        throw new IllegalArgumentException("Size of toSend is 0");
                    }
                    finalList.add(toSend);
                    startPosition = endPosition;
                    onlyFirstSize = 0;
                }
                ConsensusBrokerInstance.getInstance().getConsensusBroker().seekDisperseOffsetToEnd();
                ConsensusBrokerInstance.getInstance().getConsensusBroker().distributeDisperseMessageFromLeader(finalList, String.valueOf(data.getData().getViewID()));
            } catch (Exception e) {
                cleanup();
                LOG.info("DispersePhase: Organizer Failed to create the message " + e.getMessage());
                data.setStatusType(ConsensusStatusType.ABORT);
            }
        }

        @Override
        public void AnnouncePhase(ConsensusMessage<TransactionBlock> data) throws Exception {
            data.setData(null);
            data.setMessageType(ConsensusMessageType.ANNOUNCE);

            this.sizeCalculator.setTransactionBlock(original_copy);
            byte[] message = block_serialize.encode(original_copy, this.sizeCalculator.TransactionBlockSizeCalculator());
            data.setHash(HashUtil.sha256_bytetoString(message));
            Signature sig = BLSSignature.sign(message, CachedBLSKeyPair.getInstance().getPrivateKey());
            data.getChecksumData().setBlsPublicKey(CachedBLSKeyPair.getInstance().getPublicKey());
            data.getChecksumData().setSignature(sig);

            BLSSignatureData BLSLeaderSignatureData = new BLSSignatureData(2);
            BLSLeaderSignatureData.getSignature()[0] = Signature.fromByte(sig.toBytes());
            BLSLeaderSignatureData.getMessageHash()[0] = BLSSignature.GetMessageHashAsBase64String(message);
            this.original_copy.getSignatureData().put(BLSPublicKey.fromByte(CachedBLSKeyPair.getInstance().getPublicKey().toBytes()), BLSLeaderSignatureData);

            //data.setChecksumData(new ConsensusMessage.ChecksumData(sig, CachedBLSKeyPair.getInstance().getPublicKey()));
            byte[] toSend = SerializationFuryUtil.getInstance().getFury().serialize(data);
            ConsensusBrokerInstance.getInstance().getConsensusBroker().produceMessage(TopicType.ANNOUNCE_PHASE, String.valueOf(this.original_copy.getViewID()), toSend);
        }

        @Override
        public void PreparePhase(ConsensusMessage<TransactionBlock> data) {
            if (data.getStatusType().equals(ConsensusStatusType.ABORT))
                return;

            List<byte[]> result = ConsensusBrokerInstance.getInstance().getConsensusBroker().receiveMessageFromValidators(TopicType.ANNOUNCE_PHASE, String.valueOf(this.original_copy.getViewID()));
            if (result == null || result.isEmpty()) {
                cleanup();
                LOG.info("PreparePhase: Not Receiving from Validators");
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            }
            int N_COPY = N - 1;
            for (int START = 0; START < result.size(); START++) {
                byte[] receive = result.get(START);
                try {
                    if (receive == null || receive.length <= 0) {
                        LOG.info("PreparePhase: Null message from validators");
                    } else {
                        ConsensusMessage<TransactionBlock> received = (ConsensusMessage<TransactionBlock>) SerializationFuryUtil.getInstance().getFury().deserialize(receive);
                        //data.setData(received.getData());
                        if (!CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(CachedZoneIndex.getInstance().getZoneIndex()).containsKey(received.getChecksumData().getBlsPublicKey())) {
                            LOG.info("PreparePhase: Validator does not exist on consensus... Ignore");
                            // i--;
                        } else {
                            BLSSignatureData blsSignatureData = new BLSSignatureData();
                            blsSignatureData.getSignature()[0] = received.getChecksumData().getSignature();
                            data.getSignatures().put(received.getChecksumData().getBlsPublicKey(), blsSignatureData);
                            N_COPY--;
                        }
                    }
                } catch (IllegalArgumentException | StringIndexOutOfBoundsException e) {
                    LOG.info("PreparePhase: Problem at message deserialization");
                } catch (ArrayIndexOutOfBoundsException e) {
                    LOG.info("PreparePhase: Receiving out of bounds response from organizer");
                } catch (NullPointerException e) {
                    LOG.info("PreparePhase: Receiving null response from organizer");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


            if (N_COPY > F) {
                cleanup();
                LOG.info("PreparePhase: Byzantine network not meet requirements abort " + String.valueOf(N_COPY));
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            }

            data.setData(null);
            data.setMessageType(ConsensusMessageType.PREPARE);

            List<BLSPublicKey> publicKeys = new ArrayList<>(data.getSignatures().keySet());
            List<Signature> signature = data.getSignatures().values().stream().map(BLSSignatureData::getSignature).map(r -> r[0]).collect(Collectors.toList());

            Signature aggregatedSignature = BLSSignature.aggregate(signature);

            this.sizeCalculator.setTransactionBlock(original_copy);
            byte[] toVerify = block_serialize.encode(original_copy, this.sizeCalculator.TransactionBlockSizeCalculator());
            Bytes message = Bytes.wrap(toVerify);
            boolean verify = BLSSignature.fastAggregateVerify(publicKeys, message, aggregatedSignature);
            if (!verify) {
                cleanup();
                LOG.info("PreparePhase: Abort consensus phase BLS multi_signature is invalid during prepare phase");
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            }


            //##############################################################
            String messageHashAsBase64String = BLSSignature.GetMessageHashAsBase64String(message.toArray());
            for (Map.Entry<BLSPublicKey, BLSSignatureData> entry : SerializationUtils.clone(data.getSignatures()).entrySet()) {
                BLSSignatureData BLSSignatureData = entry.getValue();
                BLSSignatureData.getSignature()[0] = entry.getValue().getSignature()[0];
                BLSSignatureData.getMessageHash()[0] = messageHashAsBase64String;
                this.original_copy.getSignatureData().put(entry.getKey(), BLSSignatureData);
            }
            //##############################################################

            this.sizeCalculator.setTransactionBlock(this.original_copy);
            byte[] toSign = block_serialize.encode(this.original_copy, this.sizeCalculator.TransactionBlockSizeCalculator());
            data.setHash(HashUtil.sha256_bytetoString(toSign));
            Signature sig = BLSSignature.sign(toSign, CachedBLSKeyPair.getInstance().getPrivateKey());
            data.setChecksumData(new ChecksumData(sig, CachedBLSKeyPair.getInstance().getPublicKey()));


            BLSSignatureData BLSLeaderSignatureData = this.original_copy.getSignatureData().get(CachedBLSKeyPair.getInstance().getPublicKey());
            BLSLeaderSignatureData.getSignature()[1] = Signature.fromByte(sig.toBytes());
            BLSLeaderSignatureData.getMessageHash()[1] = BLSSignature.GetMessageHashAsBase64String(toSign);
            this.original_copy.getSignatureData().put(BLSPublicKey.fromByte(CachedBLSKeyPair.getInstance().getPublicKey().toBytes()), BLSLeaderSignatureData);


            byte[] toSend = SerializationFuryUtil.getInstance().getFury().serialize(data);
            ConsensusBrokerInstance.getInstance().getConsensusBroker().produceMessage(TopicType.PREPARE_PHASE, String.valueOf(this.original_copy.getViewID()), toSend);
        }

        @Override
        public void CommitPhase(ConsensusMessage<TransactionBlock> data) throws InterruptedException {
            if (data.getStatusType().equals(ConsensusStatusType.ABORT))
                return;

            List<byte[]> res = ConsensusBrokerInstance.getInstance().getConsensusBroker().receiveMessageFromValidators(TopicType.PREPARE_PHASE, String.valueOf(this.original_copy.getViewID()));
            if (res == null || res.isEmpty()) {
                cleanup();
                LOG.info("CommitPhase: Not Receiving from Validators");
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            }
            int N_COPY = N - 1;
            for (int START = 0; START < res.size(); START++) {
                byte[] receive = res.get(START);
                try {
                    if (receive == null || receive.length <= 0) {
                        LOG.info("CommitPhase: Not Receiving from Validators");
                    } else {
                        ConsensusMessage<TransactionBlock> received = (ConsensusMessage<TransactionBlock>) SerializationFuryUtil.getInstance().getFury().deserialize(receive);
                        if (!CachedLatestBlocks.
                                getInstance()
                                .getCommitteeBlock()
                                .getStructureMap()
                                .get(CachedZoneIndex.getInstance().getZoneIndex())
                                .containsKey(received.getChecksumData().getBlsPublicKey())) {
                            LOG.info("CommitPhase: Validator does not exist on consensus... Ignore");
                            //i--;
                        } else {
                            BLSSignatureData blsSignatureData = data.getSignatures().get(received.getChecksumData().getBlsPublicKey());
                            blsSignatureData.getSignature()[1] = received.getChecksumData().getSignature();
                            data.getSignatures().put(received.getChecksumData().getBlsPublicKey(), blsSignatureData);
                            N_COPY--;
                        }
                    }
                } catch (IllegalArgumentException | StringIndexOutOfBoundsException e) {
                    LOG.info("CommitPhase: Problem at message deserialization");
                } catch (ArrayIndexOutOfBoundsException e) {
                    LOG.info("CommitPhase: Receiving out of bounds response from organizer");
                } catch (NullPointerException e) {
                    LOG.info("CommitPhase: Receiving null response from organizer");
                }
            }


            if (N_COPY > F) {
                cleanup();
                LOG.info("CommitPhase: Byzantine network not meet requirements abort " + String.valueOf(N_COPY));
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            }
            data.setMessageType(ConsensusMessageType.COMMIT);

            List<BLSPublicKey> publicKeys = new ArrayList<>(data.getSignatures().keySet());
            List<Signature> signature = data.getSignatures().values().stream().map(BLSSignatureData::getSignature).map(r -> r[1]).collect(Collectors.toList());


            Signature aggregatedSignature = BLSSignature.aggregate(signature);
            this.sizeCalculator.setTransactionBlock(original_copy);
            byte[] toVerify = block_serialize.encode(original_copy, this.sizeCalculator.TransactionBlockSizeCalculator());
            Bytes message = Bytes.wrap(toVerify);
            boolean verify = BLSSignature.fastAggregateVerify(publicKeys, message, aggregatedSignature);
            if (!verify) {
                cleanup();
                LOG.info("CommitPhase: Abort consensus phase BLS multi_signature is invalid during commit phase");
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            }


            //##############################################################
            String messageHashAsBase64String = BLSSignature.GetMessageHashAsBase64String(message.toArray());
            for (Map.Entry<BLSPublicKey, BLSSignatureData> entry : data.getSignatures().entrySet()) {
                BLSSignatureData BLSSignatureData = this.original_copy.getSignatureData().get(entry.getKey());
                BLSSignatureData.getSignature()[1] = entry.getValue().getSignature()[1];
                BLSSignatureData.getMessageHash()[1] = messageHashAsBase64String;
                this.original_copy.getSignatureData().put(entry.getKey(), BLSSignatureData);
            }
            //##############################################################


            this.sizeCalculator.setTransactionBlock(this.original_copy);
            byte[] toSign = block_serialize.encode(this.original_copy, this.sizeCalculator.TransactionBlockSizeCalculator());
            data.setHash(HashUtil.sha256_bytetoString(toSign));
            Signature sig = BLSSignature.sign(toSign, CachedBLSKeyPair.getInstance().getPrivateKey());
            data.setChecksumData(new ChecksumData(sig, CachedBLSKeyPair.getInstance().getPublicKey()));


            byte[] toSend = SerializationFuryUtil.getInstance().getFury().serialize(data);
            ConsensusBrokerInstance.getInstance().getConsensusBroker().produceMessage(TopicType.COMMITTEE_PHASE, String.valueOf(this.original_copy.getViewID()), toSend);


            List<byte[]> result = ConsensusBrokerInstance.getInstance().getConsensusBroker().receiveMessageFromValidators(TopicType.COMMITTEE_PHASE, String.valueOf(this.original_copy.getViewID()));
            if (result == null || result.isEmpty()) {
                cleanup();
                LOG.info("CommitPhase: Not Receiving commit message from Validators");
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            }
            TreeMap<BLSPublicKey, BLSSignatureData> commitSignatures = new TreeMap<BLSPublicKey, BLSSignatureData>(new SortSignatureMapByBlsPublicKey());
            for (int START = 0; START < result.size(); START++) {
                byte[] receive = result.get(START);
                try {
                    if (receive == null || receive.length <= 0) {
                        LOG.info("CommitPhase: Null commit message from validators");
                    } else {
                        ConsensusMessage<String> received = SerializationUtils.deserialize(receive);
                        BLSSignatureData blsSignatureData = new BLSSignatureData();
                        blsSignatureData.getSignature()[0] = received.getChecksumData().getSignature();
                        commitSignatures.put(received.getChecksumData().getBlsPublicKey(), blsSignatureData);
                    }
                } catch (IllegalArgumentException | StringIndexOutOfBoundsException e) {
                    LOG.info("CommitPhase: Problem at message deserialization");
                } catch (ArrayIndexOutOfBoundsException e) {
                    LOG.info("CommitPhase: Receiving out of bounds response from organizer");
                } catch (NullPointerException e) {
                    LOG.info("CommitPhase: Receiving null response from organizer");
                }
            }

            List<BLSPublicKey> pubKeys = new ArrayList<>(commitSignatures.keySet());
            List<Signature> sigs = commitSignatures.values().stream().map(BLSSignatureData::getSignature).map(r -> r[0]).collect(Collectors.toList());

            Signature aggregate = BLSSignature.aggregate(sigs);

            boolean isCommit = BLSSignature.fastAggregateVerify(pubKeys, Bytes.wrap("COMMIT".getBytes(StandardCharsets.UTF_8)), aggregate);

            if (!isCommit) {
                cleanup();
                LOG.info("CommitPhase: Abort validators dont commit the block with success revert");
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            }

            BlockInvent regural_block = (BlockInvent) factory.getBlock(BlockType.REGULAR);
            regural_block.InventTransactionBlock(this.original_copy);

            BLSPublicKey next_key = blockIndex.getPublicKeyByIndex(CachedZoneIndex.getInstance().getZoneIndex(), CachedLeaderIndex.getInstance().getTransactionPositionLeader());
            CachedLatestBlocks.getInstance().getTransactionBlock().setBlockProposer(next_key.toRaw());
            CachedLatestBlocks.getInstance().getTransactionBlock().setLeaderPublicKey(next_key);
            if (tree != null)
                tree.clear();
            LOG.info("Block is finalized with Success");
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static void cleanup() {
        if (ConsensusBrokerInstance.getInstance().getConsensusBroker() != null) {
            ConsensusBrokerInstance.getInstance().getConsensusBroker().clear();
        }
    }

}
