package io.Adrestus.consensus;

import com.google.common.reflect.TypeToken;
import io.Adrestus.Trie.MerkleNode;
import io.Adrestus.Trie.MerkleTreeImp;
import io.Adrestus.core.*;
import io.Adrestus.core.Resourses.CachedLatestBlocks;
import io.Adrestus.core.Resourses.CachedLeaderIndex;
import io.Adrestus.core.Resourses.CachedZoneIndex;
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
import io.Adrestus.util.GetTime;
import io.Adrestus.util.SerializationUtil;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.apache.tuweni.bytes.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
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
        private final Map<BLSPublicKey, BLSSignatureData> signatureDataMap;

        private final BlockSizeCalculator sizeCalculator;
        private CountDownLatch latch;
        private int N, N_COPY;
        private int F;

        private ConsensusServer consensusServer;
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
            list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
            list.add(new SerializationUtil.Mapping(TreeMap.class, ctx -> new CustomSerializerTreeMap()));
            this.block_serialize = new SerializationUtil<AbstractBlock>(AbstractBlock.class, list);
            this.consensus_serialize = new SerializationUtil<ConsensusMessage>(fluentType, list);
            this.signatureDataMap = new HashMap<BLSPublicKey, BLSSignatureData>();
            this.sizeCalculator = new BlockSizeCalculator();
            this.signatureMapper = new SerializationUtil<Signature>(Signature.class, list);
            this.serenc_erasure = new SerializationUtil<SerializableErasureObject>(SerializableErasureObject.class);
        }

        @Override
        public void InitialSetup() {
            try {
                if (!DEBUG) {
                    //this.N = 1;
                    this.N = CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(CachedZoneIndex.getInstance().getZoneIndex()).size();
                    this.F = (this.N - 1) / 3;
                    this.latch = new CountDownLatch(N - 1);
                    this.current = CachedLeaderIndex.getInstance().getTransactionPositionLeader();
                    if (current == CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(CachedZoneIndex.getInstance().getZoneIndex()).size() - 1) {
                        this.leader_bls = this.blockIndex.getPublicKeyByIndex(CachedZoneIndex.getInstance().getZoneIndex(), this.current);
                        this.consensusServer = new ConsensusServer(this.blockIndex.getIpValue(CachedZoneIndex.getInstance().getZoneIndex(), this.leader_bls), latch);
                        CachedLeaderIndex.getInstance().setTransactionPositionLeader(0);
                    } else {
                        this.leader_bls = this.blockIndex.getPublicKeyByIndex(CachedZoneIndex.getInstance().getZoneIndex(), this.current);
                        this.consensusServer = new ConsensusServer(this.blockIndex.getIpValue(CachedZoneIndex.getInstance().getZoneIndex(), this.leader_bls), latch);
                        CachedLeaderIndex.getInstance().setTransactionPositionLeader(current + 1);
                    }
                    this.N_COPY = (this.N - 1) - consensusServer.getPeers_not_connected();
                    this.consensusServer.setMAX_MESSAGES(this.N_COPY * 2);
                    this.consensusServer.receive_handler();
                }
            } catch (Exception e) {
                cleanup();
                LOG.info("InitialSetup: Exception caught " + e.toString());
                throw new IllegalArgumentException("Exception caught " + e.toString());
            }
        }

        @Override
        public void DispersePhase(ConsensusMessage<TransactionBlock> data) throws Exception {
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
                String rec = new String(this.consensusServer.receiveErasureData(), StandardCharsets.UTF_8);
                if (!existed.contains(rec)) {
                    existed.add(rec);
                    unique++;
                    proofs.add(rec);
                }
            }

            if (proofs.size() < N_COPY - F) {
                LOG.info("DispersePhase: Size of validators are not correct Abort");
                data.setStatusType(ConsensusStatusType.ABORT);
                cleanup();
                return;
            }

            if (N_COPY == 0) {
                LOG.info("DispersePhase: None of validators connected abort");
                data.setStatusType(ConsensusStatusType.ABORT);
                cleanup();
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
            MerkleTreeImp tree = new MerkleTreeImp();
            ArrayList<MerkleNode> merkleNodes = new ArrayList<MerkleNode>();
            for (int i = 0; i < n.size(); i++) {
                SerializableErasureObject serializableErasureObject = new SerializableErasureObject(object, n.get(i).asArray(), new ArrayList<byte[]>());
                serializableErasureObjects.add(serializableErasureObject);
                merkleNodes.add(new MerkleNode(HashUtil.sha256_bytetoString(serializableErasureObject.getOriginalPacketChunks())));
            }
            tree.my_generate2(merkleNodes);
            for (int j = 0; j < serializableErasureObjects.size(); j++) {
                tree.build_proofs2(merkleNodes, new MerkleNode(HashUtil.sha256_bytetoString(serializableErasureObjects.get(j).getOriginalPacketChunks())));
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
                LOG.info("DispersePhase: Validators dont send correct header messages abort");
                data.setStatusType(ConsensusStatusType.ABORT);
                cleanup();
                return;
            }

            for (int i = 0; i < valid; i++) {
                this.consensusServer.setErasureMessage(toSend.get(i), identities.get(i));
            }
        }

        @Override
        public void AnnouncePhase(ConsensusMessage<TransactionBlock> data) throws Exception {
            if (!DEBUG) {
                if (this.consensusServer.getPeers_not_connected() > F) {
                    LOG.info("AnnouncePhase: Byzantine network not meet requirements abort " + String.valueOf(this.consensusServer.getPeers_not_connected()));
                    data.setStatusType(ConsensusStatusType.ABORT);
                    cleanup();
                    return;
                }
            }
            data.setData(new TransactionBlock(
                    original_copy.getHash(),
                    original_copy.getHeaderData().getPreviousHash(),
                    original_copy.getSize(), original_copy.getHeight(),
                    original_copy.getZone(), original_copy.getViewID(),
                    original_copy.getHeaderData().getTimestamp(),
                    original_copy.getZone()));
            data.setMessageType(ConsensusMessageType.ANNOUNCE);
            if (DEBUG)
                return;

            this.sizeCalculator.setTransactionBlock(data.getData());
            byte[] message = block_serialize.encode(data.getData(), this.sizeCalculator.TransactionBlockSizeCalculator());
            Signature sig = BLSSignature.sign(message, CachedBLSKeyPair.getInstance().getPrivateKey());
            data.getChecksumData().setBlsPublicKey(CachedBLSKeyPair.getInstance().getPublicKey());
            data.getChecksumData().setSignature(sig);

            //data.setChecksumData(new ConsensusMessage.ChecksumData(sig, CachedBLSKeyPair.getInstance().getPublicKey()));
            byte[] toSend = consensus_serialize.encode(data);
            consensusServer.publishMessage(toSend);
        }

        @Override
        public void PreparePhase(ConsensusMessage<TransactionBlock> data) {
            if (data.getStatusType().equals(ConsensusStatusType.ABORT))
                return;

            if (!DEBUG) {
                int i = N_COPY;
                while (i > 0) {
                    byte[] receive = consensusServer.receiveData();
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
                                data.getSignatures().add(received.getChecksumData());
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
                    LOG.info("PreparePhase: Byzantine network not meet requirements abort " + String.valueOf(N_COPY));
                    data.setStatusType(ConsensusStatusType.ABORT);
                    cleanup();
                    return;
                }
            }

            data.setData(new TransactionBlock(
                    original_copy.getHash(),
                    original_copy.getHeaderData().getPreviousHash(),
                    original_copy.getSize(), original_copy.getHeight(),
                    original_copy.getZone(), original_copy.getViewID(),
                    original_copy.getHeaderData().getTimestamp(),
                    original_copy.getZone()));
            data.setMessageType(ConsensusMessageType.PREPARE);

            List<BLSPublicKey> publicKeys = data.getSignatures().stream().map(ConsensusMessage.ChecksumData::getBlsPublicKey).collect(Collectors.toList());
            List<Signature> signature = data.getSignatures().stream().map(ConsensusMessage.ChecksumData::getSignature).collect(Collectors.toList());

            Signature aggregatedSignature = BLSSignature.aggregate(signature);

            this.sizeCalculator.setTransactionBlock(data.getData());
            Bytes message = Bytes.wrap(block_serialize.encode(data.getData(), this.sizeCalculator.TransactionBlockSizeCalculator()));
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
                BLSSignatureData BLSSignatureData = new BLSSignatureData(blsPublicKey);
                BLSSignatureData.getSignature()[0] = signature.get(pos);
                signatureDataMap.put(blsPublicKey, BLSSignatureData);
                pos++;
            }
            //##############################################################

            this.sizeCalculator.setTransactionBlock(data.getData());
            Signature sig = BLSSignature.sign(block_serialize.encode(data.getData(), this.sizeCalculator.TransactionBlockSizeCalculator()), CachedBLSKeyPair.getInstance().getPrivateKey());
            data.setChecksumData(new ConsensusMessage.ChecksumData(sig, CachedBLSKeyPair.getInstance().getPublicKey()));

            this.N_COPY = (this.N - 1) - consensusServer.getPeers_not_connected();


            byte[] toSend = consensus_serialize.encode(data);
            consensusServer.publishMessage(toSend);
        }

        @Override
        public void CommitPhase(ConsensusMessage<TransactionBlock> data) {
            if (data.getStatusType().equals(ConsensusStatusType.ABORT))
                return;

            if (!DEBUG) {
                int i = N_COPY;
                data.getSignatures().clear();
                while (i > 0) {
                    byte[] receive = consensusServer.receiveData();
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
                                data.getSignatures().add(received.getChecksumData());
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
                    LOG.info("CommitPhase: Byzantine network not meet requirements abort " + String.valueOf(N_COPY));
                    data.setStatusType(ConsensusStatusType.ABORT);
                    cleanup();
                    return;
                }
            }
            data.setMessageType(ConsensusMessageType.COMMIT);

            List<BLSPublicKey> publicKeys = data.getSignatures().stream().map(ConsensusMessage.ChecksumData::getBlsPublicKey).collect(Collectors.toList());
            List<Signature> signature = data.getSignatures().stream().map(ConsensusMessage.ChecksumData::getSignature).collect(Collectors.toList());


            Signature aggregatedSignature = BLSSignature.aggregate(signature);
            this.sizeCalculator.setTransactionBlock(data.getData());
            Bytes message = Bytes.wrap(block_serialize.encode(data.getData(), this.sizeCalculator.TransactionBlockSizeCalculator()));
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
                BLSSignatureData BLSSignatureData = signatureDataMap.get(blsPublicKey);
                BLSSignatureData.getSignature()[1] = signature.get(pos);
                signatureDataMap.put(blsPublicKey, BLSSignatureData);
                pos++;
            }
            //##############################################################

            this.sizeCalculator.setTransactionBlock(data.getData());
            Signature sig = BLSSignature.sign(block_serialize.encode(data.getData(), this.sizeCalculator.TransactionBlockSizeCalculator()), CachedBLSKeyPair.getInstance().getPrivateKey());
            data.setChecksumData(new ConsensusMessage.ChecksumData(sig, CachedBLSKeyPair.getInstance().getPublicKey()));

            this.N_COPY = (this.N - 1) - consensusServer.getPeers_not_connected();
            int i = this.N_COPY;

            byte[] toSend = consensus_serialize.encode(data);
            consensusServer.publishMessage(toSend);

            this.original_copy.setSignatureData(signatureDataMap);
            BlockInvent regural_block = (BlockInvent) factory.getBlock(BlockType.REGULAR);
            regural_block.InventTransactionBlock(this.original_copy);

            BLSPublicKey next_key = blockIndex.getPublicKeyByIndex(CachedZoneIndex.getInstance().getZoneIndex(), CachedLeaderIndex.getInstance().getTransactionPositionLeader());
            CachedLatestBlocks.getInstance().getTransactionBlock().setTransactionProposer(next_key.toRaw());
            CachedLatestBlocks.getInstance().getTransactionBlock().setLeaderPublicKey(next_key);
            LOG.info("Block is finalized with Success");
            cleanup();
        }

        private void cleanup() {
            if (consensusServer != null) {
                consensusServer.close();
                consensusServer = null;
            }
        }
    }

      /*  if (current == CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(1).size() - 1)
                data.getData().setLeaderPublicKey(this.blockIndex.getPublicKeyByIndex(CachedZoneIndex.getInstance().getZoneIndex(), 0));
            else {
                data.getData().setLeaderPublicKey(this.blockIndex.getPublicKeyByIndex(CachedZoneIndex.getInstance().getZoneIndex(), current + 1));
            }*/
    // CachedLatestBlocks.getInstance().setTransactionBlock(data.getData());

     /*while (i > 0) {
                try {
                    consensusServer.receiveStringData();
                } catch (NullPointerException ex) {
                } finally {
                    i--;
                }
            }*/

}
