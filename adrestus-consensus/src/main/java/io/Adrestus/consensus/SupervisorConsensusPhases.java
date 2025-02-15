package io.Adrestus.consensus;

import com.google.common.base.Preconditions;
import com.google.common.reflect.TypeToken;
import io.Adrestus.Trie.MerkleNode;
import io.Adrestus.Trie.MerkleTreeOptimizedImp;
import io.Adrestus.config.AdrestusConfiguration;
import io.Adrestus.config.KafkaConfiguration;
import io.Adrestus.core.*;
import io.Adrestus.core.Resourses.*;
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
import io.Adrestus.crypto.vdf.VDFMessage;
import io.Adrestus.crypto.vdf.engine.VdfEngine;
import io.Adrestus.crypto.vdf.engine.VdfEnginePietrzak;
import io.Adrestus.crypto.vrf.VRFMessage;
import io.Adrestus.crypto.vrf.engine.VrfEngine2;
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
import io.Adrestus.util.ByteUtil;
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
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SupervisorConsensusPhases {
    protected int N;
    protected int F;
    protected int current;
    protected BLSPublicKey leader_bls;

    protected static class ProposeVDF extends SupervisorConsensusPhases implements BFTConsensusPhase<VDFMessage> {
        private static final Type fluentType = new TypeToken<ConsensusMessage<VDFMessage>>() {
        }.getType();
        private static Logger LOG = LoggerFactory.getLogger(ProposeVDF.class);
        private final VdfEngine vdf;
        private final SerializationUtil<VDFMessage> data_serialize;
        private final IBlockIndex blockIndex;
        private final OptionalInt position;
        private final ArrayList<String> ips;
        private String LeaderIP;

        public ProposeVDF() {
            this.blockIndex = new BlockIndex();
            this.vdf = new VdfEnginePietrzak(AdrestusConfiguration.PIERRZAK_BIT);
            this.data_serialize = new SerializationUtil<VDFMessage>(VDFMessage.class);
            this.ips = new ArrayList<>(this.blockIndex.getIpList(CachedZoneIndex.getInstance().getZoneIndex()));
            KafkaConfiguration.KAFKA_HOST = IPFinder.getLocalIP();
            this.position = IntStream.range(0, this.ips.size()).filter(i -> KafkaConfiguration.KAFKA_HOST.equals(ips.get(i))).findFirst();
            ConsensusBrokerInstance.getInstance(ips, ips.getFirst(), position.getAsInt());
            ErasureServerInstance.getInstance();
        }

        @Override
        public void InitialSetup() {
            try {
                this.N = CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(0).size();
                this.F = (this.N - 1) / 3;
                this.current = CachedLeaderIndex.getInstance().getCommitteePositionLeader();
                this.leader_bls = this.blockIndex.getPublicKeyByIndex(0, current);
                this.LeaderIP = this.blockIndex.getIpValue(0, this.leader_bls);
                ConsensusBrokerInstance.getInstance().getConsensusBroker().setLeader_host(this.LeaderIP);
                ConsensusBrokerInstance.getInstance().getConsensusBroker().setLeader_position(this.current);
            } catch (Exception e) {
                throw new IllegalArgumentException("Exception caught " + e.toString());
            }
        }

        @Override
        public void DispersePhase(ConsensusMessage<VDFMessage> data) throws Exception {
            return;
        }

        @Override
        public void AnnouncePhase(ConsensusMessage<VDFMessage> data) {
            data.setMessageType(ConsensusMessageType.ANNOUNCE);
            byte[] solution = vdf.solve(CachedSecurityHeaders.getInstance().getSecurityHeader().getPRnd(), CachedLatestBlocks.getInstance().getCommitteeBlock().getDifficulty());
            data.getData().setVDFSolution(solution);


            data.setStatusType(ConsensusStatusType.SUCCESS);

            byte[] message = data_serialize.encode(data.getData(), data.getData().length());
            Signature sig = BLSSignature.sign(message, CachedBLSKeyPair.getInstance().getPrivateKey());
            data.getChecksumData().setBlsPublicKey(CachedBLSKeyPair.getInstance().getPublicKey());
            data.getChecksumData().setSignature(sig);

            byte[] toSend = SerializationFuryUtil.getInstance().getFury().serialize(data);
            ConsensusBrokerInstance.getInstance().getConsensusBroker().produceMessage(TopicType.ANNOUNCE_PHASE, CachedSecurityHeaders.getInstance().getRndSecurityHeaderViewID(), toSend);

        }

        @Override
        public void PreparePhase(ConsensusMessage<VDFMessage> data) {
            if (data.getStatusType().equals(ConsensusStatusType.ABORT)) return;

            List<byte[]> result = ConsensusBrokerInstance.getInstance().getConsensusBroker().receiveMessageFromValidators(TopicType.ANNOUNCE_PHASE, CachedSecurityHeaders.getInstance().getRndSecurityHeaderViewID());
            if (result == null || result.isEmpty()) {
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
                        ConsensusMessage<VDFMessage> received = (ConsensusMessage<VDFMessage>) SerializationFuryUtil.getInstance().getFury().deserialize(receive);
                        if (!CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(0).containsKey(received.getChecksumData().getBlsPublicKey())) {
                            LOG.info("PreparePhase: Validator does not exist on consensus... Ignore");
                        } else {
                            data.getSignatures().put(received.getChecksumData().getBlsPublicKey(), new BLSSignatureData());
                            data.getSignatures().get(received.getChecksumData().getBlsPublicKey()).getSignature()[0] = received.getChecksumData().getSignature();
                            N_COPY--;
                        }
                    }

                } catch (IllegalArgumentException | StringIndexOutOfBoundsException e) {
                    LOG.info("PreparePhase: Problem at message deserialization");
                } catch (ArrayIndexOutOfBoundsException e) {
                    LOG.info("PreparePhase: Receiving out of bounds response from organizer");
                } catch (NullPointerException e) {
                    LOG.info("PreparePhase: Receiving null pointer Exception");
                }

            }


            if (N_COPY > F) {
                LOG.info("PreparePhase: Byzantine network not meet requirements abort " + String.valueOf(N));
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            }
            data.setMessageType(ConsensusMessageType.PREPARE);

            List<BLSPublicKey> publicKeys = new ArrayList<>(data.getSignatures().keySet());
            List<Signature> signature = data.getSignatures().values().stream().map(BLSSignatureData::getSignature).map(r -> r[0]).collect(Collectors.toList());


            Signature aggregatedSignature = BLSSignature.aggregate(signature);
            Bytes message = Bytes.wrap(data.getData().getVDFSolution());
            boolean verify = BLSSignature.fastAggregateVerify(publicKeys, message, aggregatedSignature);
            if (!verify) {
                LOG.info("PreparePhase: Abort consensus phase BLS multi_signature is invalid during prepare phase");
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            }


            data.setStatusType(ConsensusStatusType.SUCCESS);


            Signature sig = BLSSignature.sign(data_serialize.encode(data.getData(), data.getData().length()), CachedBLSKeyPair.getInstance().getPrivateKey());
            data.setChecksumData(new ChecksumData(sig, CachedBLSKeyPair.getInstance().getPublicKey()));


            byte[] toSend = SerializationFuryUtil.getInstance().getFury().serialize(data);
            ConsensusBrokerInstance.getInstance().getConsensusBroker().produceMessage(TopicType.PREPARE_PHASE, CachedSecurityHeaders.getInstance().getRndSecurityHeaderViewID(), toSend);
        }


        @SneakyThrows
        @Override
        public void CommitPhase(ConsensusMessage<VDFMessage> data) {
            if (data.getStatusType().equals(ConsensusStatusType.ABORT)) return;

            List<byte[]> res = ConsensusBrokerInstance.getInstance().getConsensusBroker().receiveMessageFromValidators(TopicType.PREPARE_PHASE, CachedSecurityHeaders.getInstance().getRndSecurityHeaderViewID());
            if (res == null || res.isEmpty()) {
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
                        ConsensusMessage<VDFMessage> received = (ConsensusMessage<VDFMessage>) SerializationFuryUtil.getInstance().getFury().deserialize(receive);
                        if (!CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(0).containsKey(received.getChecksumData().getBlsPublicKey())) {
                            LOG.info("CommitPhase: Validator does not exist on consensus... Ignore");
                            // i--;
                        } else {
                            data.getSignatures().get(received.getChecksumData().getBlsPublicKey()).getSignature()[1] = received.getChecksumData().getSignature();
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
                LOG.info("CommitPhase: Byzantine network not meet requirements abort " + String.valueOf(N));
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            }

            data.setMessageType(ConsensusMessageType.COMMIT);

            List<BLSPublicKey> publicKeys = new ArrayList<>(data.getSignatures().keySet());
            List<Signature> signature = data.getSignatures().values().stream().map(BLSSignatureData::getSignature).map(r -> r[1]).collect(Collectors.toList());


            Signature aggregatedSignature = BLSSignature.aggregate(signature);
            byte[] wrapp = data_serialize.encode(data.getData(), data.getData().length());
            Bytes message = Bytes.wrap(wrapp);
            boolean verify = BLSSignature.fastAggregateVerify(publicKeys, message, aggregatedSignature);
            if (!verify) {
                LOG.info("CommitPhase: Abort consensus phase BLS multi_signature is invalid during commit phase");
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            }

            //commit save to db
            data.setStatusType(ConsensusStatusType.SUCCESS);

            Signature sig = BLSSignature.sign(data_serialize.encode(data.getData(), data.getData().length()), CachedBLSKeyPair.getInstance().getPrivateKey());
            data.setChecksumData(new ChecksumData(sig, CachedBLSKeyPair.getInstance().getPublicKey()));


            byte[] toSend = SerializationFuryUtil.getInstance().getFury().serialize(data);
            ConsensusBrokerInstance.getInstance().getConsensusBroker().produceMessage(TopicType.COMMITTEE_PHASE, CachedSecurityHeaders.getInstance().getRndSecurityHeaderViewID(), toSend);

            TreeMap<BLSPublicKey, BLSSignatureData> commitSignatures = new TreeMap<BLSPublicKey, BLSSignatureData>(new SortSignatureMapByBlsPublicKey());
            List<byte[]> result = ConsensusBrokerInstance.getInstance().getConsensusBroker().receiveMessageFromValidators(TopicType.COMMITTEE_PHASE, CachedSecurityHeaders.getInstance().getRndSecurityHeaderViewID());
            Preconditions.checkArgument(!result.isEmpty(), "CommitPhase: Abort Empty Validators Response from leader");
            Preconditions.checkArgument(result != null, "CommitPhase: Abort Empty Validators Response contains null values");
            result.stream()
                    .filter(receive -> receive != null && receive.length > 0)
                    .forEach(receive -> {
                        try {
                            ConsensusMessage<String> received = SerializationUtils.deserialize(receive);
                            BLSSignatureData blsSignatureData = new BLSSignatureData();
                            blsSignatureData.getSignature()[0] = received.getChecksumData().getSignature();
                            commitSignatures.put(received.getChecksumData().getBlsPublicKey(), blsSignatureData);
                        } catch (IllegalArgumentException | StringIndexOutOfBoundsException e) {
                            LOG.info("CommitPhase: Problem at message deserialization");
                        } catch (ArrayIndexOutOfBoundsException e) {
                            LOG.info("CommitPhase: Receiving out of bounds response from organizer");
                        } catch (NullPointerException e) {
                            LOG.info("CommitPhase: Receiving null response from organizer");
                        }
                    });

            List<BLSPublicKey> pubKeys = new ArrayList<>(commitSignatures.keySet());
            List<Signature> sigs = commitSignatures.values().stream().map(BLSSignatureData::getSignature).map(r -> r[0]).collect(Collectors.toList());

            Signature aggregate = BLSSignature.aggregate(sigs);

            boolean isCommit = BLSSignature.fastAggregateVerify(pubKeys, Bytes.wrap("COMMIT".getBytes(StandardCharsets.UTF_8)), aggregate);

            if (!isCommit) {
                LOG.info("CommitPhase: Abort validators dont commit the block with success revert");
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            }

            Thread.sleep(400);
            CachedSecurityHeaders.getInstance().getSecurityHeader().setRnd(data.getData().getVDFSolution().clone());
            LOG.info("VDF is finalized with Success");

        }
    }


    protected static class ProposeVRF extends SupervisorConsensusPhases implements VRFConsensusPhase<VRFMessage> {
        private static Logger LOG = LoggerFactory.getLogger(ProposeVRF.class);
        private static final Type fluentType = new TypeToken<ConsensusMessage<VRFMessage>>() {
        }.getType();
        private VrfEngine2 group;
        private final SerializationUtil<VRFMessage> serialize;
        private final IBlockIndex blockIndex;
        private final OptionalInt position;
        private final SerializationUtil<ArrayList<byte[]>> serenc_rpc;
        private final ArrayList<String> ips;

        private String LeaderIP;

        public ProposeVRF() {
            this.blockIndex = new BlockIndex();
            this.group = new VrfEngine2();
            this.serialize = new SerializationUtil<VRFMessage>(VRFMessage.class);
            this.serenc_rpc = new SerializationUtil<ArrayList<byte[]>>(new TypeToken<List<byte[]>>() {
            }.getType());
            this.ips = new ArrayList<>(this.blockIndex.getIpList(CachedZoneIndex.getInstance().getZoneIndex()));
            KafkaConfiguration.KAFKA_HOST = IPFinder.getLocalIP();
            this.position = IntStream.range(0, this.ips.size()).filter(i -> KafkaConfiguration.KAFKA_HOST.equals(ips.get(i))).findFirst();
            ConsensusBrokerInstance.getInstance(ips, ips.getFirst(), position.getAsInt());
            ErasureServerInstance.getInstance();
        }


        @Override
        public void InitialSetup() {
            try {
                this.N = CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(0).size();
                this.F = (this.N - 1) / 3;
                this.current = CachedLeaderIndex.getInstance().getCommitteePositionLeader();
                this.leader_bls = this.blockIndex.getPublicKeyByIndex(0, current);
                this.LeaderIP = this.blockIndex.getIpValue(0, this.leader_bls);
                ConsensusBrokerInstance.getInstance().getConsensusBroker().setLeader_host(this.LeaderIP);
                ConsensusBrokerInstance.getInstance().getConsensusBroker().setLeader_position(this.current);
            } catch (Exception e) {
                throw new IllegalArgumentException("Exception caught " + e.toString());
            }
        }


        @Override
        public void Initialize(VRFMessage message) {
            try {
                message.setBlockHash(CachedLatestBlocks.getInstance().getCommitteeBlock().getHash());
                message.setType(VRFMessage.VRFMessageType.INIT);

                Thread.sleep(200);
                byte[] toSend = serialize.encode(message, 4096);
                System.out.println("panos " + " donbe");
                ConsensusBrokerInstance.getInstance().getConsensusBroker().produceMessage(TopicType.DISPERSE_PHASE2, CachedSecurityHeaders.getInstance().getpRndSecurityHeaderViewID(), toSend);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        public void AggregateVRF(VRFMessage message) throws Exception {
            List<byte[]> result = ConsensusBrokerInstance.getInstance().getConsensusBroker().receiveMessageFromValidators(TopicType.DISPERSE_PHASE2, CachedSecurityHeaders.getInstance().getpRndSecurityHeaderViewID());
            if (result == null || result.isEmpty()) {
                LOG.info("PreparePhase: Not Receiving from Validators");
                message.setType(VRFMessage.VRFMessageType.ABORT);
                return;
            }
            int N_COPY = N - 1;
            for (int START = 0; START < result.size(); START++) {
                byte[] receive = result.get(START);
                try {
                    if (receive == null || receive.length <= 0) {
                        LOG.info("AggregateVRF: Null message from validator");
                    } else {
                        VRFMessage received = serialize.decode(receive);
                        message.getSigners().add(received.getData());
                        N_COPY--;
                    }
                } catch (IllegalArgumentException | StringIndexOutOfBoundsException e) {
                    LOG.info("AggregateVRF: Problem at message deserialization");
                } catch (ArrayIndexOutOfBoundsException e) {
                    LOG.info("AggregateVRF: Receiving out of bounds response from organizer");
                } catch (NullPointerException e) {
                    LOG.info("AggregateVRF: Receiving null response from organizer");
                }

            }
            if (N_COPY > F) {
                LOG.info("AggregateVRF: Byzantine network not meet requirements abort " + String.valueOf(N));
                message.setType(VRFMessage.VRFMessageType.ABORT);
                return;
            }
            List<VRFMessage.VRFData> list = message.getSigners();

            if (list.isEmpty()) {
                LOG.info("Validators not produce valid vrf inputs and list is empty");
                message.setType(VRFMessage.VRFMessageType.ABORT);
                return;
            }

            StringBuilder hashToVerify = new StringBuilder();


            hashToVerify.append(CachedLatestBlocks.getInstance().getCommitteeBlock().getHash());
            hashToVerify.append(CachedLatestBlocks.getInstance().getCommitteeBlock().getViewID());


            for (int i = 0; i < list.size(); i++) {

                byte[] prove = group.verify(list.get(i).getBls_pubkey(), list.get(i).getRi(), hashToVerify.toString().getBytes(StandardCharsets.UTF_8));
                boolean retval = Arrays.equals(prove, list.get(i).getPi());

                if (!retval) {
                    LOG.info("VRF computation is not valid for this validator");
                    list.remove(i);
                }
            }


            byte[] res = list.get(0).getRi();
            for (int i = 0; i < list.size(); i++) {
                if (i == list.size() - 1) {
                    message.setPrnd(res);
                    break;
                }
                res = ByteUtil.xor(res, list.get(i + 1).getRi());
            }
        }


        @Override
        public void DispersePhase(ConsensusMessage<VRFMessage> data) throws Exception {
            return;
        }

        @Override
        public void AnnouncePhase(ConsensusMessage<VRFMessage> data) {
            if (data.getStatusType().equals(ConsensusStatusType.ABORT)) return;

            data.setMessageType(ConsensusMessageType.ANNOUNCE);
            data.setStatusType(ConsensusStatusType.SUCCESS);


            byte[] message = serialize.encode(data.getData(), data.getData().length());
            Signature sig = BLSSignature.sign(message, CachedBLSKeyPair.getInstance().getPrivateKey());
            data.getChecksumData().setBlsPublicKey(CachedBLSKeyPair.getInstance().getPublicKey());
            data.getChecksumData().setSignature(sig);

            byte[] toSend = SerializationFuryUtil.getInstance().getFury().serialize(data);
            ConsensusBrokerInstance.getInstance().getConsensusBroker().produceMessage(TopicType.ANNOUNCE_PHASE, CachedSecurityHeaders.getInstance().getpRndSecurityHeaderViewID(), toSend);
        }

        @Override
        public void PreparePhase(ConsensusMessage<VRFMessage> data) {
            if (data.getStatusType().equals(ConsensusStatusType.ABORT)) return;

            List<byte[]> result = ConsensusBrokerInstance.getInstance().getConsensusBroker().receiveMessageFromValidators(TopicType.ANNOUNCE_PHASE, CachedSecurityHeaders.getInstance().getpRndSecurityHeaderViewID());
            if (result == null || result.isEmpty()) {
                LOG.info("PreparePhase: Not Receiving from Validators");
                data.getData().setType(VRFMessage.VRFMessageType.ABORT);
                return;
            }
            int N_COPY = N - 1;
            for (int START = 0; START < result.size(); START++) {
                byte[] receive = result.get(START);
                try {
                    if (receive == null || receive.length <= 0) {
                        LOG.info("PreparePhase: Null message from validators");
                    } else {
                        ConsensusMessage<VRFMessage> received = (ConsensusMessage<VRFMessage>) SerializationFuryUtil.getInstance().getFury().deserialize(receive);
                        if (!CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(0).containsKey(received.getChecksumData().getBlsPublicKey())) {
                            LOG.info("PreparePhase: Validator does not exist on consensus... Ignore");
                        } else {
                            data.getSignatures().put(received.getChecksumData().getBlsPublicKey(), new BLSSignatureData());
                            data.getSignatures().get(received.getChecksumData().getBlsPublicKey()).getSignature()[0] = received.getChecksumData().getSignature();
                            N_COPY--;
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
                LOG.info("PreparePhase: Byzantine network not meet requirements abort " + String.valueOf(N));
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            }

            data.setMessageType(ConsensusMessageType.PREPARE);

            List<BLSPublicKey> publicKeys = new ArrayList<>(data.getSignatures().keySet());
            List<Signature> signature = data.getSignatures().values().stream().map(BLSSignatureData::getSignature).map(r -> r[0]).collect(Collectors.toList());


            Signature aggregatedSignature = BLSSignature.aggregate(signature);
            byte[] message = serialize.encode(data.getData(), data.getData().length());
            boolean verify = BLSSignature.fastAggregateVerify(publicKeys, Bytes.wrap(message), aggregatedSignature);
            if (!verify) {
                LOG.info("PreparePhase: Abort consensus phase BLS multi_signature is invalid during prepare phase");
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            }


            data.setStatusType(ConsensusStatusType.SUCCESS);


            Signature sig = BLSSignature.sign(serialize.encode(data.getData(), data.getData().length()), CachedBLSKeyPair.getInstance().getPrivateKey());
            data.setChecksumData(new ChecksumData(sig, CachedBLSKeyPair.getInstance().getPublicKey()));


            byte[] toSend = SerializationFuryUtil.getInstance().getFury().serialize(data);
            ConsensusBrokerInstance.getInstance().getConsensusBroker().produceMessage(TopicType.PREPARE_PHASE, CachedSecurityHeaders.getInstance().getpRndSecurityHeaderViewID(), toSend);
        }


        @SneakyThrows
        @Override
        public void CommitPhase(ConsensusMessage<VRFMessage> data) {
            if (data.getStatusType().equals(ConsensusStatusType.ABORT)) return;

            List<byte[]> res = ConsensusBrokerInstance.getInstance().getConsensusBroker().receiveMessageFromValidators(TopicType.PREPARE_PHASE, CachedSecurityHeaders.getInstance().getpRndSecurityHeaderViewID());
            if (res == null || res.isEmpty()) {
                LOG.info("PreparePhase: Not Receiving from Validators");
                data.getData().setType(VRFMessage.VRFMessageType.ABORT);
                return;
            }
            int N_COPY = N - 1;
            for (int START = 0; START < res.size(); START++) {
                byte[] receive = res.get(START);
                try {
                    if (receive == null || receive.length <= 0) {
                        LOG.info("CommitPhase: Not Receiving from Validators");
                    } else {
                        ConsensusMessage<VRFMessage> received = (ConsensusMessage<VRFMessage>) SerializationFuryUtil.getInstance().getFury().deserialize(receive);
                        if (!CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(0).containsKey(received.getChecksumData().getBlsPublicKey())) {
                            LOG.info("CommitPhase: Validator does not exist on consensus... Ignore");
                        } else {
                            data.getSignatures().get(received.getChecksumData().getBlsPublicKey()).getSignature()[1] = received.getChecksumData().getSignature();
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
                LOG.info("CommitPhase: Byzantine network not meet requirements abort " + String.valueOf(N));
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            }

            data.setMessageType(ConsensusMessageType.COMMIT);

            List<BLSPublicKey> publicKeys = new ArrayList<>(data.getSignatures().keySet());
            List<Signature> signature = data.getSignatures().values().stream().map(BLSSignatureData::getSignature).map(r -> r[1]).collect(Collectors.toList());


            Signature aggregatedSignature = BLSSignature.aggregate(signature);
            byte[] wrapp = serialize.encode(data.getData(), data.getData().length());
            Bytes message = Bytes.wrap(wrapp);
            boolean verify = BLSSignature.fastAggregateVerify(publicKeys, message, aggregatedSignature);
            if (!verify) {
                LOG.info("CommitPhase: Abort consensus phase BLS multi_signature is invalid during commit phase");
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            }

            //commit save to db
            data.setStatusType(ConsensusStatusType.SUCCESS);

            Signature sig = BLSSignature.sign(serialize.encode(data.getData(), data.getData().length()), CachedBLSKeyPair.getInstance().getPrivateKey());
            data.setChecksumData(new ChecksumData(sig, CachedBLSKeyPair.getInstance().getPublicKey()));


            byte[] toSend = SerializationFuryUtil.getInstance().getFury().serialize(data);
            ConsensusBrokerInstance.getInstance().getConsensusBroker().produceMessage(TopicType.COMMITTEE_PHASE, CachedSecurityHeaders.getInstance().getpRndSecurityHeaderViewID(), toSend);

            TreeMap<BLSPublicKey, BLSSignatureData> commitSignatures = new TreeMap<BLSPublicKey, BLSSignatureData>(new SortSignatureMapByBlsPublicKey());
            List<byte[]> result = ConsensusBrokerInstance.getInstance().getConsensusBroker().receiveMessageFromValidators(TopicType.COMMITTEE_PHASE, CachedSecurityHeaders.getInstance().getpRndSecurityHeaderViewID());
            Preconditions.checkArgument(!result.isEmpty(), "CommitPhase: Abort Empty Validators Response from leader");
            Preconditions.checkArgument(result != null, "CommitPhase: Abort Empty Validators Response contains null values");
            result.stream()
                    .filter(receive -> receive != null && receive.length > 0)
                    .forEach(receive -> {
                        try {
                            ConsensusMessage<String> received = SerializationUtils.deserialize(receive);
                            BLSSignatureData blsSignatureData = new BLSSignatureData();
                            blsSignatureData.getSignature()[0] = received.getChecksumData().getSignature();
                            commitSignatures.put(received.getChecksumData().getBlsPublicKey(), blsSignatureData);
                        } catch (IllegalArgumentException | StringIndexOutOfBoundsException e) {
                            LOG.info("CommitPhase: Problem at message deserialization");
                        } catch (ArrayIndexOutOfBoundsException e) {
                            LOG.info("CommitPhase: Receiving out of bounds response from organizer");
                        } catch (NullPointerException e) {
                            LOG.info("CommitPhase: Receiving null response from organizer");
                        }
                    });

            List<BLSPublicKey> pubKeys = new ArrayList<>(commitSignatures.keySet());
            List<Signature> sigs = commitSignatures.values().stream().map(BLSSignatureData::getSignature).map(r -> r[0]).collect(Collectors.toList());

            Signature aggregate = BLSSignature.aggregate(sigs);

            boolean isCommit = BLSSignature.fastAggregateVerify(pubKeys, Bytes.wrap("COMMIT".getBytes(StandardCharsets.UTF_8)), aggregate);

            if (!isCommit) {
                LOG.info("CommitPhase: Abort validators dont commit the block with success revert");
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            }

            CachedSecurityHeaders.getInstance().getSecurityHeader().setPRnd(data.getData().getPrnd());


            LOG.info("VRF is finalized with Success");
            Thread.sleep(100);
        }


    }

    protected static class ProposeCommitteeBlock extends SupervisorConsensusPhases implements BFTConsensusPhase<CommitteeBlock> {
        private static final Type fluentType = new TypeToken<ConsensusMessage<CommitteeBlock>>() {
        }.getType();
        private static Logger LOG = LoggerFactory.getLogger(ProposeCommitteeBlock.class);
        private final SerializationUtil<AbstractBlock> block_serialize;
        private final DefaultFactory factory;
        private final IBlockIndex blockIndex;
        private final BlockSizeCalculator sizeCalculator;
        private final SerializationUtil<SerializableErasureObject> serenc_erasure;

        private OptionalInt position;
        private ArrayList<String> ips;
        private CommitteeBlock original_copy;
        private MerkleTreeOptimizedImp tree;
        private String leader_host;

        public ProposeCommitteeBlock() {
            this.blockIndex = new BlockIndex();
            this.factory = new DefaultFactory();
            List<SerializationUtil.Mapping> list = new ArrayList<>();
            list.add(new SerializationUtil.Mapping(ECP.class, ctx -> new ECPmapper()));
            list.add(new SerializationUtil.Mapping(ECP2.class, ctx -> new ECP2mapper()));
            list.add(new SerializationUtil.Mapping(BigDecimal.class, ctx -> new BigDecimalSerializer()));
            list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
            list.add(new SerializationUtil.Mapping(TreeMap.class, ctx -> new CustomSerializerTreeMap()));
            this.block_serialize = new SerializationUtil<AbstractBlock>(AbstractBlock.class, list);
            this.serenc_erasure = new SerializationUtil<SerializableErasureObject>(SerializableErasureObject.class, list);
            this.sizeCalculator = new BlockSizeCalculator();
            ErasureServerInstance.getInstance();
        }

        @Override
        public void InitialSetup() {
            try {
                this.N = CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(CachedZoneIndex.getInstance().getZoneIndex()).size();
                this.F = (this.N - 1) / 3;
                this.current = CachedLeaderIndex.getInstance().getCommitteePositionLeader();
                this.leader_host = this.blockIndex.getIpValue(CachedZoneIndex.getInstance().getZoneIndex(), CachedBLSKeyPair.getInstance().getPublicKey());
                this.ips = new ArrayList<>(this.blockIndex.getIpList(CachedZoneIndex.getInstance().getZoneIndex()));
                KafkaConfiguration.KAFKA_HOST = IPFinder.getLocalIP();
                this.position = IntStream.range(0, this.ips.size()).filter(i -> KafkaConfiguration.KAFKA_HOST.equals(ips.get(i))).findFirst();
                ConsensusBrokerInstance.getInstance(ips, ips.getFirst(), position.getAsInt());
                ConsensusBrokerInstance.getInstance().getConsensusBroker().setLeader_host(this.leader_host);
                ConsensusBrokerInstance.getInstance().getConsensusBroker().setLeader_position(CachedLeaderIndex.getInstance().getCommitteePositionLeader());
            } catch (Exception e) {
                e.printStackTrace();
                throw new IllegalArgumentException("Exception caught " + e.toString());
            }
        }

        @Override
        public void DispersePhase(ConsensusMessage<CommitteeBlock> data) throws Exception {
            var regural_block = factory.getBlock(BlockType.REGULAR);
            regural_block.forgeCommitteBlock(data.getData());

            this.original_copy = (CommitteeBlock) data.getData().clone();

            data.setMessageType(ConsensusMessageType.ANNOUNCE);
            data.setStatusType(ConsensusStatusType.SUCCESS);


            try {
                BlockSizeCalculator sizeCalculator = new BlockSizeCalculator();
                sizeCalculator.setCommitteeBlock(this.original_copy);
                byte[] buffer = block_serialize.encode(this.original_copy, sizeCalculator.CommitteeBlockSizeCalculator());

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
                        throw new IllegalArgumentException("Size of to Send is 0");
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
        public void AnnouncePhase(ConsensusMessage<CommitteeBlock> data) {
            data.setData(null);
            data.setMessageType(ConsensusMessageType.ANNOUNCE);

            this.sizeCalculator.setCommitteeBlock(original_copy);
            byte[] message = block_serialize.encode(original_copy, this.sizeCalculator.CommitteeBlockSizeCalculator());
            data.setHash(HashUtil.sha256_bytetoString(message));
            Signature sig = BLSSignature.sign(message, CachedBLSKeyPair.getInstance().getPrivateKey());
            data.getChecksumData().setBlsPublicKey(CachedBLSKeyPair.getInstance().getPublicKey());
            data.getChecksumData().setSignature(sig);

            BLSSignatureData BLSLeaderSignatureData = new BLSSignatureData(2);
            BLSLeaderSignatureData.getSignature()[0] = Signature.fromByte(sig.toBytes());
            BLSLeaderSignatureData.getMessageHash()[0] = BLSSignature.GetMessageHashAsBase64String(message);
            this.original_copy.getSignatureData().put(BLSPublicKey.fromByte(CachedBLSKeyPair.getInstance().getPublicKey().toBytes()), BLSLeaderSignatureData);


            byte[] toSend = SerializationFuryUtil.getInstance().getFury().serialize(data);
            ConsensusBrokerInstance.getInstance().getConsensusBroker().produceMessage(TopicType.ANNOUNCE_PHASE, String.valueOf(this.original_copy.getViewID()), toSend);
        }

        @Override
        public void PreparePhase(ConsensusMessage<CommitteeBlock> data) {
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

            this.sizeCalculator.setCommitteeBlock(original_copy);
            byte[] toVerify = block_serialize.encode(original_copy, this.sizeCalculator.CommitteeBlockSizeCalculator());
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

            this.sizeCalculator.setCommitteeBlock(this.original_copy);
            byte[] toSign = block_serialize.encode(this.original_copy, this.sizeCalculator.CommitteeBlockSizeCalculator());
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


        @SneakyThrows
        @Override
        public void CommitPhase(ConsensusMessage<CommitteeBlock> data) {
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
            this.sizeCalculator.setCommitteeBlock(original_copy);
            byte[] toVerify = block_serialize.encode(original_copy, this.sizeCalculator.CommitteeBlockSizeCalculator());
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

            data.setStatusType(ConsensusStatusType.SUCCESS);


            this.sizeCalculator.setCommitteeBlock(this.original_copy);
            byte[] toSign = block_serialize.encode(this.original_copy, this.sizeCalculator.CommitteeBlockSizeCalculator());
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

            CachedStartHeightRewards.getInstance().setRewardsCommitteeEnabled(true);
            BlockInvent regural_block = (BlockInvent) factory.getBlock(BlockType.REGULAR);
            regural_block.InventCommitteBlock(this.original_copy);

            BLSPublicKey next_key = blockIndex.getPublicKeyByIndex(CachedZoneIndex.getInstance().getZoneIndex(), 0);
            CachedLatestBlocks.getInstance().getTransactionBlock().setBlockProposer(next_key.toRaw());
            CachedLatestBlocks.getInstance().getCommitteeBlock().setBlockProposer(next_key.toRaw());
            CachedLatestBlocks.getInstance().getTransactionBlock().setLeaderPublicKey(next_key);
            CachedLatestBlocks.getInstance().getCommitteeBlock().setLeaderPublicKey(next_key);
            CachedLeaderIndex.getInstance().setCommitteePositionLeader(0);

            if (tree != null)
                tree.clear();
            LOG.info("Committee is finalized with Success");
            Thread.sleep(1000);
            cleanup();
            //Make sure you give enough time for nodes to sync that not existed or existed
        }

        private void cleanup() {
            if (ConsensusBrokerInstance.getInstance().getConsensusBroker() != null) {
                ConsensusBrokerInstance.getInstance().close();
            }
        }
    }
}
