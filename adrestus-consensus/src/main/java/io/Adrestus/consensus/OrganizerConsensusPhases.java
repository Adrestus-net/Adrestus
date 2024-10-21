package io.Adrestus.consensus;

import com.google.common.reflect.TypeToken;
import io.Adrestus.Trie.MerkleNode;
import io.Adrestus.Trie.MerkleTreeOptimizedImp;
import io.Adrestus.Trie.MerkleTreePlainImp;
import io.Adrestus.core.*;
import io.Adrestus.core.Resourses.CachedLatestBlocks;
import io.Adrestus.core.Resourses.CachedLeaderIndex;
import io.Adrestus.core.Resourses.CachedZoneIndex;
import io.Adrestus.core.Resourses.ErasureServerInstance;
import io.Adrestus.core.Util.BlockSizeCalculator;
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
import io.Adrestus.network.ConsensusServer;
import io.Adrestus.rpc.CachedConsensusPublisherData;
import io.Adrestus.util.GetTime;
import io.Adrestus.util.SerializationUtil;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tuweni.bytes.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

public class OrganizerConsensusPhases {

    protected static class ProposeTransactionBlock extends OrganizerConsensusPhases implements BFTConsensusPhase<TransactionBlock> {
        private static final Type fluentType = new TypeToken<ConsensusMessage<TransactionBlock>>() {
        }.getType();


        private static Logger LOG = LoggerFactory.getLogger(ProposeTransactionBlock.class);
        private static final String delimeter = "||";

        private final DefaultFactory factory;
        private final SerializationUtil<AbstractBlock> block_serialize;
        private final SerializationUtil<ConsensusMessage> consensus_serialize;
        private final SerializationUtil<Signature> signatureMapper;
        private final SerializationUtil<SerializableErasureObject> serenc_erasure;
        private final boolean DEBUG;
        private final IBlockIndex blockIndex;

        private final BlockSizeCalculator sizeCalculator;
        private CountDownLatch latch;
        private int N, N_COPY;
        private int F;

        private BLSPublicKey leader_bls;
        private int current;
        private TransactionBlock original_copy;


        public ProposeTransactionBlock(boolean DEBUG) {
            this.DEBUG = DEBUG;
            this.blockIndex = new BlockIndex();
            this.factory = new DefaultFactory();
            List<SerializationUtil.Mapping> list = new ArrayList<>();
            list.add(new SerializationUtil.Mapping(ECP.class, ctx -> new ECPmapper()));
            list.add(new SerializationUtil.Mapping(ECP2.class, ctx -> new ECP2mapper()));
            list.add(new SerializationUtil.Mapping(BigDecimal.class, ctx -> new BigDecimalSerializer()));
            list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
            list.add(new SerializationUtil.Mapping(TreeMap.class, ctx -> new CustomSerializerTreeMap()));
            this.block_serialize = new SerializationUtil<AbstractBlock>(AbstractBlock.class, list);
            this.consensus_serialize = new SerializationUtil<ConsensusMessage>(fluentType, list);
            this.sizeCalculator = new BlockSizeCalculator();
            this.signatureMapper = new SerializationUtil<Signature>(Signature.class, list);
            this.serenc_erasure = new SerializationUtil<SerializableErasureObject>(SerializableErasureObject.class,list);
            ErasureServerInstance.getInstance();
        }

        @Override
        public void InitialSetup() {
            long start = System.currentTimeMillis();
            try {
                if (!DEBUG) {
                    //this.N = 1;
                    CachedConsensusPublisherData.getInstance().clear();
                    this.N = CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(CachedZoneIndex.getInstance().getZoneIndex()).size();
                    this.F = (this.N - 1) / 3;
                    this.latch = new CountDownLatch(N - 1);
                    this.current = CachedLeaderIndex.getInstance().getTransactionPositionLeader();
                    if (current == CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(CachedZoneIndex.getInstance().getZoneIndex()).size() - 1) {
//                        this.leader_bls = this.blockIndex.getPublicKeyByIndex(CachedZoneIndex.getInstance().getZoneIndex(), this.current);
                        ConsensusServer.getInstance().setLatch(latch);
                        CachedLeaderIndex.getInstance().setTransactionPositionLeader(0);
                    } else {
//                        this.leader_bls = this.blockIndex.getPublicKeyByIndex(CachedZoneIndex.getInstance().getZoneIndex(), this.current);
                        ConsensusServer.getInstance().setLatch(latch);
                        CachedLeaderIndex.getInstance().setTransactionPositionLeader(current + 1);
                    }
                    this.N_COPY = (this.N - 1) - ConsensusServer.getInstance().getPeers_not_connected();
                    ConsensusServer.getInstance().setMAX_MESSAGES(this.N_COPY * 2);
                    ConsensusServer.getInstance().receive_handler();
                    long finish = System.currentTimeMillis();
                    long timeElapsed = finish - start;
                    System.out.println("Organizer construcotr " + timeElapsed);
                }
            } catch (Exception e) {
                LOG.info("InitialSetup: Exception caught " + e.toString());
                throw new IllegalArgumentException("Exception caught " + e.toString());
            }
        }

        @Override
        public void DispersePhase(ConsensusMessage<TransactionBlock> data) throws Exception {
            long Dispersestart = System.currentTimeMillis();
            var regural_block = factory.getBlock(BlockType.REGULAR);
            regural_block.forgeTransactionBlock(data.getData());
            this.original_copy = (TransactionBlock) data.getData().clone();
            data.setMessageType(ConsensusMessageType.DISPERSE);
            if (DEBUG)
                return;

            ArrayList<String> proofs = new ArrayList<>();
            ArrayList<String> existed = new ArrayList<>();

            int unique = 0;
            while (unique < N_COPY) {
                String rec = new String(ConsensusServer.getInstance().receiveErasureData(), StandardCharsets.UTF_8);
                if (!existed.contains(rec)) {
                    existed.add(rec);
                    unique++;
                    proofs.add(rec);
                }
            }

            if (proofs.size() < N_COPY - F) {
                cleanup();
                LOG.info("DispersePhase: Size of validators are not correct Abort");
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            }

            if (N_COPY == 0) {
                cleanup();
                LOG.info("DispersePhase: None of validators connected abort");
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            }

//           ###############################################Get_Chucks###############################################
            BlockSizeCalculator sizeCalculator = new BlockSizeCalculator();
            sizeCalculator.setTransactionBlock(this.original_copy);
            byte[] buffer = block_serialize.encode(this.original_copy, sizeCalculator.TransactionBlockSizeCalculator());

            long dataLen = buffer.length;
            int sizeOfCommittee = N_COPY;
            double loss = .6;
            int numSrcBlks = sizeOfCommittee;
            int symbSize = (int) (dataLen / sizeOfCommittee);
            FECParameterObject object = FECParametersPreConditions.CalculateFECParameters(dataLen, symbSize, numSrcBlks);
            FECParameters fecParams = FECParameters.newParameters(object.getDataLen(), object.getSymbolSize(), object.getNumberOfSymbols());

            byte[] content = new byte[fecParams.dataLengthAsInt()];
            System.arraycopy(buffer, 0, content, 0, content.length);
            final ArrayDataEncoder enc = OpenRQ.newEncoder(content, fecParams);
            ArrayList<SerializableErasureObject> serializableErasureObjects = new ArrayList<SerializableErasureObject>();
            ArrayList<EncodingPacket> n = new ArrayList<EncodingPacket>();
            for (SourceBlockEncoder sbEnc : enc.sourceBlockIterable()) {
                for (EncodingPacket srcPacket : sbEnc.sourcePacketsIterable()) {
                    n.add(srcPacket);
                }
            }
            MerkleTreeOptimizedImp tree = new MerkleTreeOptimizedImp();
            ArrayList<MerkleNode> merkleNodes = new ArrayList<MerkleNode>();
            for (int i = 0; i < n.size(); i++) {
                SerializableErasureObject serializableErasureObject = new SerializableErasureObject(object, n.get(i).asArray(), new ArrayList<byte[]>());
                serializableErasureObjects.add(serializableErasureObject);
                merkleNodes.add(new MerkleNode(HashUtil.XXH3(serializableErasureObject.getOriginalPacketChunks())));
            }
            tree.constructTree(merkleNodes);
            for (int j = 0; j < serializableErasureObjects.size(); j++) {
                tree.build_proofs(new MerkleNode(HashUtil.XXH3(serializableErasureObjects.get(j).getOriginalPacketChunks())));
                serializableErasureObjects.get(j).setProofs(tree.getMerkleeproofs());
                serializableErasureObjects.get(j).setRootMerkleHash(tree.getRootHash());
            }
            ArrayList<byte[]> toSend = new ArrayList<>();
            for (SerializableErasureObject obj : serializableErasureObjects) {
                toSend.add(serenc_erasure.encode(obj));
            }
//           ###############################################Get_Chucks###############################################

            ArrayList<String> identities = new ArrayList<>();
            int valid = 0;
            for (int j = 0; j < proofs.size(); j++) {
                try {
                    StringJoiner joiner2 = new StringJoiner(delimeter);
                    String[] splits = StringUtils.split(proofs.get(j), delimeter);
                    BLSPublicKey blsPublicKey = BLSPublicKey.fromByte(Hex.decodeHex(splits[0]));
                    Timestamp timestamp = GetTime.GetTimestampFromString(splits[1]);
                    boolean val = GetTime.CheckIfTimestampIsUnderOneMinute(timestamp);
                    Signature bls_sig2 = this.signatureMapper.decode(Hex.decodeHex(splits[2]));
                    String strsgn = joiner2.add(Hex.encodeHexString(blsPublicKey.toBytes())).add(splits[1]).toString();
                    Boolean signcheck = BLSSignature.verify(bls_sig2, strsgn.getBytes(StandardCharsets.UTF_8), blsPublicKey);
                    if (signcheck && val) {
                        identities.add(strsgn);
                        valid++;
                    }
                } catch (Exception e) {
                    LOG.info("DispersePhase: Decoding String Erasure Proofs failed");
                }
            }

            if (valid < N_COPY - F) {
                cleanup();
                LOG.info("DispersePhase: Validators dont send correct header messages abort");
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            }

            for (int i = 0; i < valid; i++) {
                ConsensusServer.getInstance().setErasureMessage(toSend.get(i), identities.get(i));
            }
            long Dispersefinish = System.currentTimeMillis();
            long DispersetimeElapsed = Dispersefinish - Dispersestart;
            System.out.println("Organizer Disperse: " + DispersetimeElapsed);
        }

        @Override
        public void AnnouncePhase(ConsensusMessage<TransactionBlock> data) throws Exception {
            long Announceestart = System.currentTimeMillis();
            long Announceestarta = System.currentTimeMillis();
            if (!DEBUG) {
                //this.consensusServer.BlockUntilConnected();
                if (ConsensusServer.getInstance().getPeers_not_connected() > F) {
                    cleanup();
                    LOG.info("AnnouncePhase: Byzantine network not meet requirements abort " + String.valueOf(ConsensusServer.getInstance().getPeers_not_connected()));
                    data.setStatusType(ConsensusStatusType.ABORT);
                    return;
                }
            }
            long Announceefinisha = System.currentTimeMillis();
            long AnnounceetimeElapseda = Announceefinisha - Announceestarta;
            System.out.println("Organizer Announce a " + AnnounceetimeElapseda);
            long Announceestartb = System.currentTimeMillis();
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

            if (DEBUG)
                return;

            //data.setChecksumData(new ConsensusMessage.ChecksumData(sig, CachedBLSKeyPair.getInstance().getPublicKey()));
            byte[] toSend = consensus_serialize.encode(data);
            CachedConsensusPublisherData.getInstance().storeAtPosition(0, toSend);
            ConsensusServer.getInstance().publishMessage(toSend);

            long Announceefinishb = System.currentTimeMillis();
            long AnnounceetimeElapsedb = Announceefinishb - Announceestartb;
            System.out.println("Organizer Announce b " + AnnounceetimeElapsedb);

            long Announceefinish = System.currentTimeMillis();
            long AnnounceetimeElapsed = Announceefinish - Announceestart;
            System.out.println("Organizer Announce " + AnnounceetimeElapsed);
        }

        @Override
        public void PreparePhase(ConsensusMessage<TransactionBlock> data) {
            long Preparestart = System.currentTimeMillis();
            if (data.getStatusType().equals(ConsensusStatusType.ABORT))
                return;

            if (!DEBUG) {
                int i = N_COPY;
                while (i > 0) {
                    byte[] receive = ConsensusServer.getInstance().receiveData();
                    try {
                        if (receive == null || receive.length <= 0) {
                            LOG.info("PreparePhase: Null message from validators");
                            i--;
                        } else {
                            ConsensusMessage<TransactionBlock> received = consensus_serialize.decode(receive);
                            //data.setData(received.getData());
                            if (!CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(CachedZoneIndex.getInstance().getZoneIndex()).containsKey(received.getChecksumData().getBlsPublicKey())) {
                                LOG.info("PreparePhase: Validator does not exist on consensus... Ignore");
                                // i--;
                            } else {
                                BLSSignatureData blsSignatureData = new BLSSignatureData();
                                blsSignatureData.getSignature()[0] = received.getChecksumData().getSignature();
                                data.getSignatures().put(received.getChecksumData().getBlsPublicKey(), blsSignatureData);
                                N_COPY--;
                                i--;
                            }
                        }
                    } catch (IllegalArgumentException | StringIndexOutOfBoundsException e) {
                        LOG.info("PreparePhase: Problem at message deserialization");
                    } catch (ArrayIndexOutOfBoundsException e) {
                        LOG.info("PreparePhase: Receiving out of bounds response from organizer");
                    } catch (NullPointerException e) {
                        LOG.info("PreparePhase: Receiving null response from organizer");
                    }
                }


                if (N_COPY > F) {
                    cleanup();
                    LOG.info("PreparePhase: Byzantine network not meet requirements abort " + String.valueOf(N_COPY));
                    data.setStatusType(ConsensusStatusType.ABORT);
                    return;
                }
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

            if (DEBUG)
                return;

            this.N_COPY = (this.N - 1) - ConsensusServer.getInstance().getPeers_not_connected();


            byte[] toSend = consensus_serialize.encode(data);
            CachedConsensusPublisherData.getInstance().storeAtPosition(1, toSend);
            ConsensusServer.getInstance().publishMessage(toSend);
            long Preparefinish = System.currentTimeMillis();
            long PreparetimeElapsed = Preparefinish - Preparestart;
            System.out.println("Organizer Prepare " + PreparetimeElapsed);
        }

        @Override
        public void CommitPhase(ConsensusMessage<TransactionBlock> data) {
            long Commiteerestart = System.currentTimeMillis();
            if (data.getStatusType().equals(ConsensusStatusType.ABORT))
                return;

            if (!DEBUG) {
                int i = N_COPY;
                while (i > 0) {
                    byte[] receive = ConsensusServer.getInstance().receiveData();
                    try {
                        if (receive == null || receive.length <= 0) {
                            LOG.info("CommitPhase: Not Receiving from Validators");
                            i--;
                        } else {
                            ConsensusMessage<TransactionBlock> received = consensus_serialize.decode(receive);
                            if (!CachedLatestBlocks.
                                    getInstance()
                                    .getCommitteeBlock()
                                    .getStructureMap()
                                    .get(CachedZoneIndex
                                            .getInstance()
                                            .getZoneIndex())
                                    .containsKey(received.getChecksumData().getBlsPublicKey())) {
                                LOG.info("CommitPhase: Validator does not exist on consensus... Ignore");
                                //i--;
                            } else {
                                BLSSignatureData blsSignatureData = data.getSignatures().get(received.getChecksumData().getBlsPublicKey());
                                blsSignatureData.getSignature()[1] = received.getChecksumData().getSignature();
                                data.getSignatures().put(received.getChecksumData().getBlsPublicKey(), blsSignatureData);
                                N_COPY--;
                                i--;
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

            //commit save to db
            if (DEBUG)
                return;

            this.N_COPY = (this.N - 1) - ConsensusServer.getInstance().getPeers_not_connected();
            int i = this.N_COPY;

            byte[] toSend = consensus_serialize.encode(data);
            CachedConsensusPublisherData.getInstance().storeAtPosition(2, toSend);
            ConsensusServer.getInstance().publishMessage(toSend);

            BlockInvent regural_block = (BlockInvent) factory.getBlock(BlockType.REGULAR);
            regural_block.InventTransactionBlock(this.original_copy);

            BLSPublicKey next_key = blockIndex.getPublicKeyByIndex(CachedZoneIndex.getInstance().getZoneIndex(), CachedLeaderIndex.getInstance().getTransactionPositionLeader());
            CachedLatestBlocks.getInstance().getTransactionBlock().setBlockProposer(next_key.toRaw());
            CachedLatestBlocks.getInstance().getTransactionBlock().setLeaderPublicKey(next_key);
            long Commiteefinish = System.currentTimeMillis();
            long timeElapsed = Commiteefinish - Commiteerestart;
            System.out.println("Organizer committee " + timeElapsed);
            LOG.info("Block is finalized with Success");
        }
    }

    private static void cleanup() {
    }

}
