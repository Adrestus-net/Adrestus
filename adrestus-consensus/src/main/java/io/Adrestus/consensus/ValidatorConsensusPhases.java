package io.Adrestus.consensus;

import com.google.common.base.Preconditions;
import com.google.common.reflect.TypeToken;
import io.Adrestus.config.AdrestusConfiguration;
import io.Adrestus.config.KafkaConfiguration;
import io.Adrestus.core.*;
import io.Adrestus.core.Resourses.*;
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
import io.Adrestus.crypto.vdf.VDFMessage;
import io.Adrestus.crypto.vdf.engine.VdfEngine;
import io.Adrestus.crypto.vdf.engine.VdfEnginePietrzak;
import io.Adrestus.crypto.vrf.VRFMessage;
import io.Adrestus.crypto.vrf.engine.VrfEngine2;
import io.Adrestus.erasure.code.ArrayDataDecoder;
import io.Adrestus.erasure.code.EncodingPacket;
import io.Adrestus.erasure.code.OpenRQ;
import io.Adrestus.erasure.code.decoder.SourceBlockDecoder;
import io.Adrestus.erasure.code.parameters.FECParameterObject;
import io.Adrestus.erasure.code.parameters.FECParameters;
import io.Adrestus.network.CachedEventLoop;
import io.Adrestus.network.ConsensusBrokerInstance;
import io.Adrestus.network.IPFinder;
import io.Adrestus.network.TopicType;
import io.Adrestus.rpc.RpcErasureClient;
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
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static io.Adrestus.config.ConsensusConfiguration.ERASURE_SERVER_PORT;

public class ValidatorConsensusPhases {

    protected final IBlockIndex blockIndex;
    protected CountDownLatch latch;
    protected int current;
    protected BLSPublicKey leader_bls;

    public ValidatorConsensusPhases() {
        this.blockIndex = new BlockIndex();
    }

    protected static class VerifyVDF extends ValidatorConsensusPhases implements BFTConsensusPhase<VDFMessage> {
        private static final Type fluentType = new TypeToken<ConsensusMessage<VDFMessage>>() {
        }.getType();
        private static Logger LOG = LoggerFactory.getLogger(VerifyVDF.class);
        private final VdfEngine vdf;
        private final SerializationUtil<VDFMessage> data_serialize;
        private final OptionalInt position;
        private final ArrayList<String> ips;
        private String LeaderIP;

        public VerifyVDF() {
            this.vdf = new VdfEnginePietrzak(AdrestusConfiguration.PIERRZAK_BIT);
            this.data_serialize = new SerializationUtil<VDFMessage>(VDFMessage.class);
            this.ips = this.blockIndex.getIpList(CachedZoneIndex.getInstance().getZoneIndex());
            KafkaConfiguration.KAFKA_HOST = IPFinder.getLocalIP();
            this.position = IntStream.range(0, this.ips.size()).filter(i -> KafkaConfiguration.KAFKA_HOST.equals(ips.get(i))).findFirst();
            ConsensusBrokerInstance.getInstance(ips, ips.getFirst(), position.getAsInt());
        }

        @Override
        public void InitialSetup() {
            this.current = CachedLeaderIndex.getInstance().getCommitteePositionLeader();
            this.leader_bls = this.blockIndex.getPublicKeyByIndex(0, current);
            this.LeaderIP = this.blockIndex.getIpValue(0, this.leader_bls);
            ConsensusBrokerInstance.getInstance().getConsensusBroker().setLeader_host(this.LeaderIP);
            ConsensusBrokerInstance.getInstance().getConsensusBroker().setLeader_position(this.current);
        }

        @Override
        public void DispersePhase(ConsensusMessage<VDFMessage> data) throws Exception {
            return;
        }

        @Override
        public void AnnouncePhase(ConsensusMessage<VDFMessage> data) {
            try {
                Optional<byte[]> result = ConsensusBrokerInstance.getInstance().getConsensusBroker().receiveMessageFromLeader(TopicType.ANNOUNCE_PHASE, CachedSecurityHeaders.getInstance().getRndSecurityHeaderViewID());
                Preconditions.checkArgument(result.isPresent(), "Initialize: Empty Leader Response from leader");
                byte[] receive = result.get();
                data = (ConsensusMessage<VDFMessage>) SerializationFuryUtil.getInstance().getFury().deserialize(receive);
                if (!data.getChecksumData().getBlsPublicKey().toRaw().equals(leader_bls.toRaw())) {
                    cleanup();
                    LOG.info("AnnouncePhase: This is not the valid leader for this round");
                    data.setStatusType(ConsensusStatusType.ABORT);
                    return;
                } else {
                    byte[] message = data_serialize.encode(data.getData(), data.getData().length());
                    boolean verify = BLSSignature.verify(data.getChecksumData().getSignature(), message, data.getChecksumData().getBlsPublicKey());
                    if (!verify) {
                        cleanup();
                        data.setStatusType(ConsensusStatusType.ABORT);
                        LOG.info("AnnouncePhase: Abort consensus phase BLS leader signature is invalid during announce phase");
                    }
                }

            } catch (IllegalArgumentException | StringIndexOutOfBoundsException e) {
                cleanup();
                LOG.info("AnnouncePhase: Problem at message deserialization Abort");
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            } catch (ArrayIndexOutOfBoundsException e) {
                cleanup();
                LOG.info("AnnouncePhase: Receiving out of bounds response from organizer");
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            } catch (NullPointerException e) {
                cleanup();
                LOG.info("AnnouncePhase: Receiving null response from organizer");
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            }
            if (!data.getMessageType().equals(ConsensusMessageType.ANNOUNCE)) {
                cleanup();
                LOG.info("AnnouncePhase: Organizer not send correct header message expected " + ConsensusMessageType.ANNOUNCE);
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            }

            boolean verify = vdf.verify(CachedSecurityHeaders.getInstance().getSecurityHeader().getPRnd(), CachedLatestBlocks.getInstance().getCommitteeBlock().getDifficulty(), data.getData().getVDFSolution());
            if (!verify) {
                cleanup();
                LOG.info("AnnouncePhase: Abort consensus phase VDF solution is invalid");
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            }
            //CachedSecurityHeaders.getInstance().getSecurityHeader().setRnd(data.getData().getVDFSolution());
            data.setStatusType(ConsensusStatusType.SUCCESS);
            Signature sig = BLSSignature.sign(data.getData().getVDFSolution(), CachedBLSKeyPair.getInstance().getPrivateKey());
            data.setChecksumData(new ChecksumData(sig, CachedBLSKeyPair.getInstance().getPublicKey()));


            byte[] toSend = SerializationFuryUtil.getInstance().getFury().serialize(data);
            ConsensusBrokerInstance.getInstance().getConsensusBroker().produceMessage(TopicType.ANNOUNCE_PHASE, CachedSecurityHeaders.getInstance().getRndSecurityHeaderViewID(), toSend);
        }

        @Override
        public void PreparePhase(ConsensusMessage<VDFMessage> data) {
            if (data.getStatusType().equals(ConsensusStatusType.ABORT))
                return;

            try {
                Optional<byte[]> result = ConsensusBrokerInstance.getInstance().getConsensusBroker().receiveMessageFromLeader(TopicType.PREPARE_PHASE, CachedSecurityHeaders.getInstance().getRndSecurityHeaderViewID());
                Preconditions.checkArgument(result.isPresent(), "Initialize: Empty Leader Response from leader");
                byte[] receive = result.get();
                data = (ConsensusMessage<VDFMessage>) SerializationFuryUtil.getInstance().getFury().deserialize(receive);
                try {
                    if (!data.getChecksumData().getBlsPublicKey().toRaw().equals(leader_bls.toRaw())) {
                        cleanup();
                        LOG.info("PreparePhase: This is not the valid leader for this round");
                        data.setStatusType(ConsensusStatusType.ABORT);
                        return;
                    } else {
                        byte[] message = data_serialize.encode(data.getData(), data.getData().length());
                        boolean verify = BLSSignature.verify(data.getChecksumData().getSignature(), message, data.getChecksumData().getBlsPublicKey());
                        if (!verify) {
                            cleanup();
                            LOG.info("PreparePhase: Abort consensus phase BLS leader signature is invalid during prepare phase");
                            data.setStatusType(ConsensusStatusType.ABORT);
                            return;
                        }
                    }
                } catch (IllegalArgumentException | StringIndexOutOfBoundsException e) {
                    cleanup();
                    LOG.info("PreparePhase: Problem at message deserialization Abort");
                    data.setStatusType(ConsensusStatusType.ABORT);
                    return;
                }
            } catch (IllegalArgumentException | StringIndexOutOfBoundsException e) {
                cleanup();
                LOG.info("PreparePhase: Problem at message deserialization Abort");
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            } catch (ArrayIndexOutOfBoundsException e) {
                cleanup();
                LOG.info("PreparePhase: Receiving out of bounds response from organizer");
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            } catch (NullPointerException e) {
                cleanup();
                LOG.info("PreparePhase: Receiving null response from organizer");
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            }

            if (!data.getMessageType().equals(ConsensusMessageType.PREPARE)) {
                cleanup();
                LOG.info("PreparePhase: Organizer not send correct header message expected " + ConsensusMessageType.PREPARE);
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            }

            List<BLSPublicKey> publicKeys = new ArrayList<>(data.getSignatures().keySet());
            List<Signature> signature = data.getSignatures().values().stream().map(BLSSignatureData::getSignature).map(r -> r[0]).collect(Collectors.toList());


            Signature aggregatedSignature = BLSSignature.aggregate(signature);
            Bytes toVerify = Bytes.wrap(data.getData().getVDFSolution());
            boolean verify = BLSSignature.fastAggregateVerify(publicKeys, toVerify, aggregatedSignature);
            if (!verify) {
                cleanup();
                LOG.info("PreparePhase: Abort consensus phase BLS multi_signature is invalid in prepare phase");
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            }

            // data.clear();
            data.setStatusType(ConsensusStatusType.SUCCESS);


            byte[] message = data_serialize.encode(data.getData(), data.getData().length());
            Signature sig = BLSSignature.sign(message, CachedBLSKeyPair.getInstance().getPrivateKey());
            data.setChecksumData(new ChecksumData(sig, CachedBLSKeyPair.getInstance().getPublicKey()));


            byte[] toSend = SerializationFuryUtil.getInstance().getFury().serialize(data);
            ConsensusBrokerInstance.getInstance().getConsensusBroker().produceMessage(TopicType.PREPARE_PHASE, CachedSecurityHeaders.getInstance().getRndSecurityHeaderViewID(), toSend);
        }


        @SneakyThrows
        @Override
        public void CommitPhase(ConsensusMessage<VDFMessage> data) {
            if (data.getStatusType().equals(ConsensusStatusType.ABORT))
                return;


            try {
                Optional<byte[]> result = ConsensusBrokerInstance.getInstance().getConsensusBroker().receiveMessageFromLeader(TopicType.COMMITTEE_PHASE, CachedSecurityHeaders.getInstance().getRndSecurityHeaderViewID());
                Preconditions.checkArgument(result.isPresent(), "Initialize: Empty Leader Response from leader");
                byte[] receive = result.get();
                data = (ConsensusMessage<VDFMessage>) SerializationFuryUtil.getInstance().getFury().deserialize(receive);
                try {
                    if (!data.getChecksumData().getBlsPublicKey().toRaw().equals(leader_bls.toRaw())) {
                        LOG.info("CommitPhase: This is not the valid leader for this round");
                        data.setStatusType(ConsensusStatusType.ABORT);
                        return;
                    } else {
                        byte[] message = data_serialize.encode(data.getData(), data.getData().length());
                        boolean verify = BLSSignature.verify(data.getChecksumData().getSignature(), message, data.getChecksumData().getBlsPublicKey());
                        if (!verify)
                            throw new IllegalArgumentException("CommitPhase: Abort consensus phase BLS leader signature is invalid during commit phase");
                    }
                } catch (IllegalArgumentException | StringIndexOutOfBoundsException e) {
                    LOG.info("CommitPhase: Problem at message deserialization Abort");
                    data.setStatusType(ConsensusStatusType.ABORT);
                    return;
                }
            } catch (IllegalArgumentException | StringIndexOutOfBoundsException e) {
                cleanup();
                LOG.info("CommitPhase: Problem at message deserialization Abort");
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            } catch (ArrayIndexOutOfBoundsException e) {
                cleanup();
                LOG.info("CommitPhase: Receiving out of bounds response from organizer");
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            } catch (NullPointerException e) {
                cleanup();
                LOG.info("CommitPhase: Receiving null response from organizer");
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            }

            if (!data.getMessageType().equals(ConsensusMessageType.COMMIT)) {
                cleanup();
                LOG.info("CommitPhase: Organizer not send correct header message expected " + ConsensusMessageType.COMMIT);
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            }

            List<BLSPublicKey> publicKeys = new ArrayList<>(data.getSignatures().keySet());
            List<Signature> signature = data.getSignatures().values().stream().map(BLSSignatureData::getSignature).map(r -> r[1]).collect(Collectors.toList());


            Signature aggregatedSignature = BLSSignature.aggregate(signature);
            byte[] message = data_serialize.encode(data.getData(), data.getData().length());
            Bytes toVerify = Bytes.wrap(message);
            boolean verify = BLSSignature.fastAggregateVerify(publicKeys, toVerify, aggregatedSignature);
            if (!verify) {
                cleanup();
                LOG.info("CommitPhase: Abort consensus phase BLS multi_signature is invalid in commit phase");
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            }


            ConsensusMessage<String> commit = new ConsensusMessage<String>("COMMIT");
            commit.setStatusType(ConsensusStatusType.SUCCESS);
            commit.setMessageType(ConsensusMessageType.COMMIT);

            Signature sig = BLSSignature.sign(commit.getData().getBytes(StandardCharsets.UTF_8), CachedBLSKeyPair.getInstance().getPrivateKey());
            commit.setChecksumData(new ChecksumData(sig, CachedBLSKeyPair.getInstance().getPublicKey()));

            byte[] toSend = SerializationUtils.serialize(commit);
            ConsensusBrokerInstance.getInstance().getConsensusBroker().produceMessage(TopicType.COMMITTEE_PHASE, CachedSecurityHeaders.getInstance().getRndSecurityHeaderViewID(), toSend);

            data.setStatusType(ConsensusStatusType.SUCCESS);
            cleanup();
            Thread.sleep(400);
            CachedSecurityHeaders.getInstance().getSecurityHeader().setRnd(data.getData().getVDFSolution());
            //commit save to db
            //  consensusClient.send_heartbeat(HEARTBEAT_MESSAGE);
            LOG.info("VDF is finalized with Success");
        }

        private void cleanup() {
            if (ConsensusBrokerInstance.getInstance().getConsensusBroker() != null) {
                ConsensusBrokerInstance.getInstance().getConsensusBroker().clear();
            }
        }
    }


    protected static class VerifyVRF extends ValidatorConsensusPhases implements VRFConsensusPhase<VRFMessage> {
        private static Logger LOG = LoggerFactory.getLogger(VerifyVRF.class);
        private static final Type fluentType = new TypeToken<ConsensusMessage<VRFMessage>>() {
        }.getType();
        private final VrfEngine2 group;
        private final SerializationUtil<VRFMessage> serialize;
        private final OptionalInt position;
        private final ArrayList<String> ips;
        private String LeaderIP;
        private byte[] proof;

        public VerifyVRF() {
            this.group = new VrfEngine2();
            this.serialize = new SerializationUtil<VRFMessage>(VRFMessage.class);
            this.ips = this.blockIndex.getIpList(CachedZoneIndex.getInstance().getZoneIndex());
            KafkaConfiguration.KAFKA_HOST = IPFinder.getLocalIP();
            this.position = IntStream.range(0, this.ips.size()).filter(i -> KafkaConfiguration.KAFKA_HOST.equals(ips.get(i))).findFirst();
            ConsensusBrokerInstance.getInstance(ips, ips.getFirst(), position.getAsInt());
        }

        @Override
        public void InitialSetup() {
            this.current = CachedLeaderIndex.getInstance().getCommitteePositionLeader();
            this.leader_bls = this.blockIndex.getPublicKeyByIndex(0, current);
            this.LeaderIP = this.blockIndex.getIpValue(0, this.leader_bls);
            ConsensusBrokerInstance.getInstance().getConsensusBroker().setLeader_host(this.LeaderIP);
            ConsensusBrokerInstance.getInstance().getConsensusBroker().setLeader_position(this.current);
        }

        @Override
        public void DispersePhase(ConsensusMessage<VRFMessage> data) throws Exception {
            return;
        }

        @Override
        public void Initialize(VRFMessage message) throws Exception {
            try {
                Optional<byte[]> result = ConsensusBrokerInstance.getInstance().getConsensusBroker().receiveMessageFromLeader(TopicType.DISPERSE_PHASE2, CachedSecurityHeaders.getInstance().getpRndSecurityHeaderViewID());
                Preconditions.checkArgument(result.isPresent(), "Initialize: Empty Leader Response from leader");
                byte[] receive = result.get();
                message = serialize.decode(receive);
            } catch (IllegalArgumentException | StringIndexOutOfBoundsException e) {
                cleanup();
                LOG.info("Initialize: Problem at message deserialization Abort");
                message.setType(VRFMessage.VRFMessageType.ABORT);
                return;
            } catch (ArrayIndexOutOfBoundsException e) {
                cleanup();
                LOG.info("Initialize: Receiving out of bounds response from organizer");
                message.setType(VRFMessage.VRFMessageType.ABORT);
                return;
            } catch (NullPointerException e) {
                cleanup();
                LOG.info("Initialize: Receiving null response from organizer");
                message.setType(VRFMessage.VRFMessageType.ABORT);
                return;
            }

            if (!message.getType().equals(VRFMessage.VRFMessageType.INIT) || !message.getBlockHash().equals(CachedLatestBlocks.getInstance().getCommitteeBlock().getHash())) {
                cleanup();
                LOG.info("Initialize: Organizer not produce valid vrf request");
                message.setType(VRFMessage.VRFMessageType.ABORT);
                return;
            }

            StringBuilder hashToVerify = new StringBuilder();


            hashToVerify.append(CachedLatestBlocks.getInstance().getCommitteeBlock().getHash());
            hashToVerify.append(CachedLatestBlocks.getInstance().getCommitteeBlock().getViewID());

            byte[] ri = group.prove(CachedBLSKeyPair.getInstance().getPrivateKey().toBytes(), hashToVerify.toString().getBytes(StandardCharsets.UTF_8));
            byte[] pi = group.proofToHash(ri);

            VRFMessage.VRFData data = new VRFMessage.VRFData(CachedBLSKeyPair.getInstance().getPublicKey().toBytes(), ri, pi);
            message.setData(data);


            byte[] toSend = serialize.encode(message, message.length());
            ConsensusBrokerInstance.getInstance().getConsensusBroker().produceMessage(TopicType.DISPERSE_PHASE2, CachedSecurityHeaders.getInstance().getpRndSecurityHeaderViewID(), toSend);
        }

        @Override
        public void AggregateVRF(VRFMessage message) throws Exception {

        }


        @Override
        public void AnnouncePhase(ConsensusMessage<VRFMessage> data) throws Exception {
            if (data.getStatusType().equals(ConsensusStatusType.ABORT))
                return;

            try {
                Optional<byte[]> result = ConsensusBrokerInstance.getInstance().getConsensusBroker().receiveMessageFromLeader(TopicType.ANNOUNCE_PHASE, CachedSecurityHeaders.getInstance().getpRndSecurityHeaderViewID());
                Preconditions.checkArgument(result.isPresent(), "Initialize: Empty Leader Response from leader");
                byte[] receive = result.get();
                data = (ConsensusMessage<VRFMessage>) SerializationFuryUtil.getInstance().getFury().deserialize(receive);
                try {
                    if (!data.getChecksumData().getBlsPublicKey().toRaw().equals(leader_bls.toRaw())) {
                        cleanup();
                        LOG.info("AnnouncePhase: This is not the valid leader for this round");
                        data.setStatusType(ConsensusStatusType.ABORT);
                        return;
                    } else {
                        byte[] message = serialize.encode(data.getData(), data.getData().length());
                        boolean verify = BLSSignature.verify(data.getChecksumData().getSignature(), message, data.getChecksumData().getBlsPublicKey());
                        if (!verify) {
                            cleanup();
                            LOG.info("AnnouncePhase: Abort consensus phase BLS leader signature is invalid during prepare phase");
                            data.setStatusType(ConsensusStatusType.ABORT);
                            return;
                        }
                    }
                } catch (IllegalArgumentException | StringIndexOutOfBoundsException e) {
                    cleanup();
                    LOG.info("AnnouncePhase: Problem at message deserialization Abort");
                    data.setStatusType(ConsensusStatusType.ABORT);
                    return;
                }
            } catch (IllegalArgumentException | StringIndexOutOfBoundsException e) {
                cleanup();
                LOG.info("AnnouncePhase: Problem at message deserialization Abort");
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            } catch (ArrayIndexOutOfBoundsException e) {
                cleanup();
                LOG.info("AnnouncePhase: Receiving out of bounds response from organizer");
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            } catch (NullPointerException e) {
                cleanup();
                LOG.info("AnnouncePhase: Receiving null response from organizer");
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            }
            if (!data.getMessageType().equals(ConsensusMessageType.ANNOUNCE)) {
                cleanup();
                LOG.info("AnnouncePhase: Organizer not send correct header message expected " + ConsensusMessageType.ANNOUNCE);
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            }


            List<VRFMessage.VRFData> list = data.getData().getSigners();

            if (list.isEmpty()) {
                cleanup();
                LOG.info("AnnouncePhase: Validators not produce valid vrf inputs and list is empty");
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            }

            StringBuilder hashToVerify = new StringBuilder();


            hashToVerify.append(CachedLatestBlocks.getInstance().getCommitteeBlock().getHash());
            hashToVerify.append(CachedLatestBlocks.getInstance().getCommitteeBlock().getViewID());


            for (int i = 0; i < list.size(); i++) {

                byte[] prove = group.verify(list.get(i).getBls_pubkey(), list.get(i).getRi(), hashToVerify.toString().getBytes(StandardCharsets.UTF_8));
                boolean retval = Arrays.equals(prove, list.get(i).getPi());

                if (!retval) {
                    LOG.info("AnnouncePhase: VRF computation is not valid for this validator");
                    list.remove(i);
                }
            }


            byte[] res = list.get(0).getRi();
            for (int i = 0; i < list.size(); i++) {
                if (i == list.size() - 1) {
                    boolean retval = Arrays.equals(data.getData().getPrnd(), res);
                    if (!retval) {
                        cleanup();
                        LOG.info("AnnouncePhase: pRnd is not the same leader failure change view protocol");
                        data.setStatusType(ConsensusStatusType.ABORT);
                        return;
                    }
                    proof = data.getData().getPrnd().clone();
                    break;
                }
                res = ByteUtil.xor(res, list.get(i + 1).getRi());
            }
            byte[] message = serialize.encode(data.getData(), data.getData().length());
            Signature sig = BLSSignature.sign(message, CachedBLSKeyPair.getInstance().getPrivateKey());
            data.setChecksumData(new ChecksumData(sig, CachedBLSKeyPair.getInstance().getPublicKey()));

            data.setStatusType(ConsensusStatusType.SUCCESS);

            byte[] toSend = SerializationFuryUtil.getInstance().getFury().serialize(data);
            ConsensusBrokerInstance.getInstance().getConsensusBroker().produceMessage(TopicType.ANNOUNCE_PHASE, CachedSecurityHeaders.getInstance().getpRndSecurityHeaderViewID(), toSend);
        }

        @Override
        public void PreparePhase(ConsensusMessage<VRFMessage> data) {
            if (data.getStatusType().equals(ConsensusStatusType.ABORT))
                return;


            try {
                Optional<byte[]> result = ConsensusBrokerInstance.getInstance().getConsensusBroker().receiveMessageFromLeader(TopicType.PREPARE_PHASE, CachedSecurityHeaders.getInstance().getpRndSecurityHeaderViewID());
                Preconditions.checkArgument(result.isPresent(), "Initialize: Empty Leader Response from leader");
                byte[] receive = result.get();
                data = (ConsensusMessage<VRFMessage>) SerializationFuryUtil.getInstance().getFury().deserialize(receive);
                try {
                    if (!data.getChecksumData().getBlsPublicKey().toRaw().equals(leader_bls.toRaw())) {
                        cleanup();
                        LOG.info("PreparePhase: This is not the valid leader for this round");
                        data.setStatusType(ConsensusStatusType.ABORT);
                        return;
                    } else {
                        byte[] message = serialize.encode(data.getData(), data.getData().length());
                        boolean verify = BLSSignature.verify(data.getChecksumData().getSignature(), message, data.getChecksumData().getBlsPublicKey());
                        if (!verify)
                            throw new IllegalArgumentException("PreparePhase: Abort consensus phase BLS leader signature is invalid during prepare phase");
                    }
                } catch (IllegalArgumentException e) {
                    cleanup();
                    LOG.info("PreparePhase: Problem at message deserialization Abort");
                    data.setStatusType(ConsensusStatusType.ABORT);
                    return;
                }
            } catch (IllegalArgumentException | StringIndexOutOfBoundsException e) {
                cleanup();
                LOG.info("PreparePhase: Problem at message deserialization Abort");
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            } catch (ArrayIndexOutOfBoundsException e) {
                cleanup();
                LOG.info("PreparePhase: Receiving out of bounds response from organizer");
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            } catch (NullPointerException e) {
                cleanup();
                LOG.info("PreparePhase: Receiving null response from organizer");
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            }

            if (!data.getMessageType().equals(ConsensusMessageType.PREPARE) || !Arrays.equals(data.getData().getPrnd(), proof)) {
                cleanup();
                LOG.info("PreparePhase: Organizer not send correct header message expected " + ConsensusMessageType.PREPARE);
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            }
            List<BLSPublicKey> publicKeys = new ArrayList<>(data.getSignatures().keySet());
            List<Signature> signature = data.getSignatures().values().stream().map(BLSSignatureData::getSignature).map(r -> r[0]).collect(Collectors.toList());

            Signature aggregatedSignature = BLSSignature.aggregate(signature);
            Bytes toVerify = Bytes.wrap(serialize.encode(data.getData(), data.getData().length()));
            boolean verify = BLSSignature.fastAggregateVerify(publicKeys, toVerify, aggregatedSignature);
            if (!verify) {
                cleanup();
                LOG.info("PreparePhase: Abort consensus phase BLS multi_signature is invalid in prepare phase");
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            }

            // data.clear();
            data.setStatusType(ConsensusStatusType.SUCCESS);


            byte[] message = serialize.encode(data.getData(), data.getData().length());
            Signature sig = BLSSignature.sign(message, CachedBLSKeyPair.getInstance().getPrivateKey());
            data.setChecksumData(new ChecksumData(sig, CachedBLSKeyPair.getInstance().getPublicKey()));


            byte[] toSend = SerializationFuryUtil.getInstance().getFury().serialize(data);
            ConsensusBrokerInstance.getInstance().getConsensusBroker().produceMessage(TopicType.PREPARE_PHASE, CachedSecurityHeaders.getInstance().getpRndSecurityHeaderViewID(), toSend);
        }

        @SneakyThrows
        @Override
        public void CommitPhase(ConsensusMessage<VRFMessage> data) {
            if (data.getStatusType().equals(ConsensusStatusType.ABORT))
                return;

            try {
                Optional<byte[]> result = ConsensusBrokerInstance.getInstance().getConsensusBroker().receiveMessageFromLeader(TopicType.COMMITTEE_PHASE, CachedSecurityHeaders.getInstance().getpRndSecurityHeaderViewID());
                Preconditions.checkArgument(result.isPresent(), "Initialize: Empty Leader Response from leader");
                byte[] receive = result.get();
                data = (ConsensusMessage<VRFMessage>) SerializationFuryUtil.getInstance().getFury().deserialize(receive);
                try {
                    if (!data.getChecksumData().getBlsPublicKey().toRaw().equals(leader_bls.toRaw())) {
                        cleanup();
                        LOG.info("CommitPhase: This is not the valid leader for this round");
                        data.setStatusType(ConsensusStatusType.ABORT);
                        return;
                    } else {
                        byte[] message = serialize.encode(data.getData(), data.getData().length());
                        boolean verify = BLSSignature.verify(data.getChecksumData().getSignature(), message, data.getChecksumData().getBlsPublicKey());
                        if (!verify) {
                            cleanup();
                            LOG.info("CommitPhase: Abort consensus phase BLS leader signature is invalid during commit phase");
                            data.setStatusType(ConsensusStatusType.ABORT);
                            return;
                        }
                    }
                } catch (IllegalArgumentException e) {
                    cleanup();
                    LOG.info("CommitPhase: Problem at message deserialization Abort");
                    data.setStatusType(ConsensusStatusType.ABORT);
                    return;
                }
            } catch (IllegalArgumentException | StringIndexOutOfBoundsException e) {
                cleanup();
                LOG.info("CommitPhase: Problem at message deserialization Abort");
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            } catch (ArrayIndexOutOfBoundsException e) {
                cleanup();
                LOG.info("CommitPhase: Receiving out of bounds response from organizer");
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            } catch (NullPointerException e) {
                cleanup();
                LOG.info("CommitPhase: Receiving null response from organizer");
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            }

            if (!data.getMessageType().equals(ConsensusMessageType.COMMIT) || !Arrays.equals(data.getData().getPrnd(), proof)) {
                cleanup();
                LOG.info("CommitPhase: Organizer not send correct header message expected " + ConsensusMessageType.COMMIT);
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            }

            List<BLSPublicKey> publicKeys = new ArrayList<>(data.getSignatures().keySet());
            List<Signature> signature = data.getSignatures().values().stream().map(BLSSignatureData::getSignature).map(r -> r[1]).collect(Collectors.toList());


            Signature aggregatedSignature = BLSSignature.aggregate(signature);
            byte[] message = serialize.encode(data.getData(), data.getData().length());
            Bytes toVerify = Bytes.wrap(message);
            boolean verify = BLSSignature.fastAggregateVerify(publicKeys, toVerify, aggregatedSignature);
            if (!verify) {
                cleanup();
                LOG.info("CommitPhase: Abort consensus phase BLS multi_signature is invalid in prepare phase");
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            }

            data.setStatusType(ConsensusStatusType.SUCCESS);
            //commit save to db


            ConsensusMessage<String> commit = new ConsensusMessage<String>("COMMIT");
            commit.setStatusType(ConsensusStatusType.SUCCESS);
            commit.setMessageType(ConsensusMessageType.COMMIT);

            Signature sig = BLSSignature.sign(commit.getData().getBytes(StandardCharsets.UTF_8), CachedBLSKeyPair.getInstance().getPrivateKey());
            commit.setChecksumData(new ChecksumData(sig, CachedBLSKeyPair.getInstance().getPublicKey()));

            byte[] toSend = SerializationUtils.serialize(commit);
            ConsensusBrokerInstance.getInstance().getConsensusBroker().produceMessage(TopicType.COMMITTEE_PHASE, CachedSecurityHeaders.getInstance().getpRndSecurityHeaderViewID(), toSend);

            CachedSecurityHeaders.getInstance().getSecurityHeader().setPRnd(this.proof);
            //commit save to db

            // consensusClient.send_heartbeat(HEARTBEAT_MESSAGE);
            LOG.info("VRF is finalized with Success");
            cleanup();
            Thread.sleep(100);
        }

        private void cleanup() {
            if (ConsensusBrokerInstance.getInstance().getConsensusBroker() != null) {
                ConsensusBrokerInstance.getInstance().getConsensusBroker().clear();
            }
        }
    }


    protected static class VerifyTransactionBlock extends ValidatorConsensusPhases implements BFTConsensusPhase<TransactionBlock> {
        private static Logger LOG = LoggerFactory.getLogger(VerifyTransactionBlock.class);
        private static final Type fluentType = new TypeToken<ConsensusMessage<TransactionBlock>>() {
        }.getType();
        private final SerializationUtil<AbstractBlock> block_serialize;
        private final SerializationUtil<Signature> signatureMapper;
        private final SerializationUtil<SerializableErasureObject> serenc_erasure;
        private final DefaultFactory factory;
        private final BlockSizeCalculator sizeCalculator;
        private final String[] Shake256Hash;
        private final List<byte[]> prevAgreegation;
        private final OptionalInt position;
        private final SerializationUtil<ArrayList<byte[]>> serenc_rpc;
        private final ArrayList<String> ips;

        private RpcErasureClient client;
        private TransactionBlock original_copy;
        private String LeaderIP;

        public VerifyTransactionBlock() {
            this.factory = new DefaultFactory();
            List<SerializationUtil.Mapping> list = new ArrayList<>();
            list.add(new SerializationUtil.Mapping(ECP.class, ctx -> new ECPmapper()));
            list.add(new SerializationUtil.Mapping(ECP2.class, ctx -> new ECP2mapper()));
            list.add(new SerializationUtil.Mapping(BigDecimal.class, ctx -> new BigDecimalSerializer()));
            list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
            list.add(new SerializationUtil.Mapping(TreeMap.class, ctx -> new CustomSerializerTreeMap()));
            this.ips = this.blockIndex.getIpList(CachedZoneIndex.getInstance().getZoneIndex());
            this.block_serialize = new SerializationUtil<AbstractBlock>(AbstractBlock.class, list);
            this.serenc_rpc = new SerializationUtil<ArrayList<byte[]>>(new TypeToken<List<byte[]>>() {
            }.getType());
            this.sizeCalculator = new BlockSizeCalculator();
            this.signatureMapper = new SerializationUtil<Signature>(Signature.class, list);
            this.serenc_erasure = new SerializationUtil<SerializableErasureObject>(SerializableErasureObject.class, list);
            this.Shake256Hash = new String[2];
            this.prevAgreegation = new ArrayList<>(2);
            KafkaConfiguration.KAFKA_HOST = IPFinder.getLocalIP();
            this.position = IntStream.range(0, this.ips.size()).filter(i -> KafkaConfiguration.KAFKA_HOST.equals(ips.get(i))).findFirst();
            ConsensusBrokerInstance.getInstance(ips, ips.getFirst(), position.getAsInt());
            ErasureServerInstance.getInstance();
        }

        @Override
        public void InitialSetup() {
            this.current = CachedLeaderIndex.getInstance().getTransactionPositionLeader();
            this.leader_bls = this.blockIndex.getPublicKeyByIndex(CachedZoneIndex.getInstance().getZoneIndex(), this.current);
            if (current == CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(CachedZoneIndex.getInstance().getZoneIndex()).size() - 1) {
                this.LeaderIP = this.blockIndex.getIpValue(CachedZoneIndex.getInstance().getZoneIndex(), this.leader_bls);
                this.client = new RpcErasureClient(LeaderIP, ERASURE_SERVER_PORT, CachedEventLoop.getInstance().getEventloop());
                ConsensusBrokerInstance.getInstance().getConsensusBroker().setLeader_host(this.LeaderIP);
                ConsensusBrokerInstance.getInstance().getConsensusBroker().setLeader_position(this.current);
                CachedLeaderIndex.getInstance().setTransactionPositionLeader(0);
            } else {
                this.LeaderIP = this.blockIndex.getIpValue(CachedZoneIndex.getInstance().getZoneIndex(), this.leader_bls);
                this.client = new RpcErasureClient(LeaderIP, ERASURE_SERVER_PORT, CachedEventLoop.getInstance().getEventloop());
                ConsensusBrokerInstance.getInstance().getConsensusBroker().setLeader_host(this.LeaderIP);
                ConsensusBrokerInstance.getInstance().getConsensusBroker().setLeader_position(this.current);
                CachedLeaderIndex.getInstance().setTransactionPositionLeader(current + 1);
            }
        }

        @Override
        public void DispersePhase(ConsensusMessage<TransactionBlock> data) throws Exception {
            try {
                ArrayList<byte[]> fromLeaderReceive = ConsensusBrokerInstance.getInstance().getConsensusBroker().receiveDisperseHandledMessageFromLeader(TopicType.DISPERSE_PHASE1, String.valueOf(data.getData().getViewID()) + position.getAsInt());

                Preconditions.checkArgument(!fromLeaderReceive.isEmpty(), "DispersePhase: Empty Leader Response from leader");
                Preconditions.checkArgument(!fromLeaderReceive.contains(null), "DispersePhase: The Leader Response contains null values");


                ConsensusBrokerInstance.getInstance().getConsensusBroker().distributeDisperseMessageToValidators(fromLeaderReceive, String.valueOf(data.getData().getViewID()));
                HashMap<String, ArrayList<byte[]>> finalList = ConsensusBrokerInstance.getInstance().getConsensusBroker().retrieveDisperseMessageFromValidatorsAndConcatResponse(fromLeaderReceive, String.valueOf(data.getData().getViewID()));

                Preconditions.checkArgument(!finalList.isEmpty(), "DispersePhase: Empty Validators Response from leader");
                Preconditions.checkArgument(finalList.containsKey(this.LeaderIP), "DispersePhase: Abort LeaderIP is not in the final list");
                Preconditions.checkArgument(!finalList.get(this.LeaderIP).isEmpty(), "DispersePhase: Empty Response from leader to get root SerializableErasureObject");
                Preconditions.checkArgument(!fromLeaderReceive.contains(null), "DispersePhase: Null Response from leader to get root SerializableErasureObject");

                ArrayList<SerializableErasureObject> recserializableErasureObjects = new ArrayList<SerializableErasureObject>();


                SerializableErasureObject root = serenc_erasure.decode(finalList.get(this.LeaderIP).getFirst());

                Preconditions.checkArgument(root != null, "DispersePhase: Null Response root SerializableErasureObject is null");
                Preconditions.checkArgument(!root.getRootMerkleHash().isEmpty() || root.getOriginalPacketChunks() != null || root.getFecParameterObject() != null || root.getOriginalPacketChunks().length != 0, "DispersePhase: Empty Response root SerializableErasureObject is empty");

                Set<String> keysToRetrieve = new HashSet<>();
                for (Map.Entry<String, ArrayList<byte[]>> entry : finalList.entrySet()) {
                    try {
                        if (entry.getValue().isEmpty()) {
                            keysToRetrieve.add(entry.getKey());
                            continue;
                        }
                        for (byte[] rec_buff : entry.getValue()) {
                            SerializableErasureObject object = serenc_erasure.decode(rec_buff);
                            if (!object.getRootMerkleHash().equals(root.getRootMerkleHash())) {
                                LOG.info("DispersePhase: Meerklee Hash is not valid");
                                keysToRetrieve.add(entry.getKey());
                            } else {
                                recserializableErasureObjects.add(serenc_erasure.decode(rec_buff));
                            }
                        }
                    } catch (Exception e) {
                        LOG.info("DispersePhase: Serialization Block Exception need retrieve mored data");
                        keysToRetrieve.add(entry.getKey());
                    }
                }
                if (!keysToRetrieve.isEmpty()) {
                    this.client.connect();
                    Map<String, byte[]> serializableErasureObject = client.getErasureChunks(new ArrayList<>(keysToRetrieve));
                    for (Map.Entry<String, byte[]> entry : serializableErasureObject.entrySet()) {
                        ArrayList<byte[]> res = new ArrayList<>(serenc_rpc.decode(entry.getValue()));
                        for (byte[] rec_buff : res) {
                            SerializableErasureObject object = serenc_erasure.decode(rec_buff);
                            if (!object.getRootMerkleHash().equals(root.getRootMerkleHash())) {
                                throw new IllegalArgumentException("DispersePhase: Meerklee Hash is not valid");
                            } else {
                                recserializableErasureObjects.add(serenc_erasure.decode(rec_buff));
                            }
                        }
                    }
                }

                recserializableErasureObjects.add(root);
                Collections.shuffle(recserializableErasureObjects);
                FECParameterObject recobject = recserializableErasureObjects.get(0).getFecParameterObject();
                FECParameters recfecParams = FECParameters.newParameters(recobject.getDataLen(), recobject.getSymbolSize(), recobject.getNumberOfSymbols());
                final ArrayDataDecoder dec = OpenRQ.newDecoder(recfecParams, recobject.getSymbolOverhead());

                for (int i = 0; i < recserializableErasureObjects.size(); i++) {
                    EncodingPacket encodingPacket = dec.parsePacket(ByteBuffer.wrap(recserializableErasureObjects.get(i).getOriginalPacketChunks()), false).value();
                    final SourceBlockDecoder sbDec = dec.sourceBlock(encodingPacket.sourceBlockNumber());
                    sbDec.putEncodingPacket(encodingPacket);
                }

                this.original_copy = (TransactionBlock) block_serialize.decode(dec.dataArray());


            } catch (IllegalArgumentException | StringIndexOutOfBoundsException e) {
                cleanup();
                LOG.info("DispersePhase: Problem at message deserialization Abort " + e.getMessage());
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            } catch (ArrayIndexOutOfBoundsException e) {
                cleanup();
                LOG.info("DispersePhase: Out of bounds response from organizer " + e.getMessage());
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            } catch (NullPointerException e) {
                cleanup();
                LOG.info("DispersePhase: Receiving null response from organizer " + e.getMessage());
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            } catch (Exception e) {
                cleanup();
                LOG.info("DispersePhase: Disperse failed cannot decode block need more chunks abort " + e.getMessage());
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            }

            data.setStatusType(ConsensusStatusType.SUCCESS);
        }

        @Override
        public void AnnouncePhase(ConsensusMessage<TransactionBlock> data) throws InterruptedException {
            if (data.getStatusType().equals(ConsensusStatusType.ABORT))
                return;

            byte[] bytesBlock = null;
            try {
                Optional<byte[]> message = ConsensusBrokerInstance.getInstance().getConsensusBroker().receiveMessageFromLeader(TopicType.ANNOUNCE_PHASE, String.valueOf(data.getData().getViewID()));
                Preconditions.checkArgument(message.isPresent(), "AnnouncePhase: Empty Leader Response from leader");
                byte[] receive = message.get();
                data = (ConsensusMessage<TransactionBlock>) SerializationFuryUtil.getInstance().getFury().deserialize(receive);
                if (!data.getChecksumData().getBlsPublicKey().toRaw().equals(leader_bls.toRaw())) {
                    cleanup();
                    LOG.info("AnnouncePhase: This is not the valid leader for this round");
                    data.setStatusType(ConsensusStatusType.ABORT);
                    return;
                }
            } catch (IllegalArgumentException | StringIndexOutOfBoundsException e) {
                e.printStackTrace();
                cleanup();
                LOG.info("AnnouncePhase: Problem at message deserialization Abort " + e.getMessage());
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            } catch (ArrayIndexOutOfBoundsException e) {
                e.printStackTrace();
                cleanup();
                LOG.info("AnnouncePhase: Out of bounds response from organizer " + e.getMessage());
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            } catch (NullPointerException e) {
                e.printStackTrace();
                cleanup();
                LOG.info("AnnouncePhase: Receiving null response from organizer " + e.getMessage());
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            }
            if (!data.getMessageType().equals(ConsensusMessageType.ANNOUNCE)) {
                cleanup();
                LOG.info("AnnouncePhase: Organizer not send correct header message expected " + ConsensusMessageType.ANNOUNCE);
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            }


            long Criticalstart = System.currentTimeMillis();
            CachedTransactionBlockEventPublisher.getInstance().publish(this.original_copy);
            CachedTransactionBlockEventPublisher.getInstance().WaitUntilRemainingCapacityZero();
            long Criticalfinish = System.currentTimeMillis();
            long CriticaltimeElapsed = Criticalfinish - Criticalstart;
            System.out.println("CriticaltimeElapsed " + CriticaltimeElapsed);

            if (this.original_copy.getStatustype().equals(StatusType.ABORT)) {
                cleanup();
                LOG.info("AnnouncePhase: Block is not valid marked as ABORT");
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            }
            this.sizeCalculator.setTransactionBlock(this.original_copy);
            bytesBlock = block_serialize.encode(this.original_copy, this.sizeCalculator.TransactionBlockSizeCalculator());
            if (bytesBlock == null) {
                cleanup();
                LOG.info("AnnouncePhase: failed  to receive correct bytes block: Abort");
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            }
            String hash = HashUtil.sha256_bytetoString(bytesBlock);
            if (!hash.equals(data.getHash())) {
                cleanup();
                LOG.info("AnnouncePhase: Abort consensus phase BLS leader hash is invalid according to block");
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            }
            boolean verify = BLSSignature.verify(data.getChecksumData().getSignature(), bytesBlock, data.getChecksumData().getBlsPublicKey());
            if (!verify) {
                cleanup();
                LOG.info("AnnouncePhase: Abort consensus phase BLS leader signature is invalid during announce phase");
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            }

            data.setStatusType(ConsensusStatusType.SUCCESS);
            data.setData(null);

            BLSSignatureData BLSLeaderSignatureData = new BLSSignatureData(2);
            BLSLeaderSignatureData.getSignature()[0] = Signature.fromByte(data.getChecksumData().getSignature().toBytes());
            BLSLeaderSignatureData.getMessageHash()[0] = BLSSignature.GetMessageHashAsBase64String(bytesBlock);
            this.original_copy.getSignatureData().put(BLSPublicKey.fromByte(data.getChecksumData().getBlsPublicKey().toBytes()), BLSLeaderSignatureData);

            this.sizeCalculator.setTransactionBlock(original_copy);
            byte[] message = block_serialize.encode(original_copy, this.sizeCalculator.TransactionBlockSizeCalculator());
            this.Shake256Hash[0] = BLSSignature.GetMessageHashAsBase64String(message);
            this.prevAgreegation.add(0, message);
            Signature sig = BLSSignature.sign(message, CachedBLSKeyPair.getInstance().getPrivateKey());
            data.setChecksumData(new ChecksumData(sig, CachedBLSKeyPair.getInstance().getPublicKey()));


            byte[] toSend = SerializationFuryUtil.getInstance().getFury().serialize(data);
            ConsensusBrokerInstance.getInstance().getConsensusBroker().produceMessage(TopicType.ANNOUNCE_PHASE, String.valueOf(this.original_copy.getViewID()), toSend);
        }

        @Override
        public void PreparePhase(ConsensusMessage<TransactionBlock> data) throws InterruptedException {
            if (data.getStatusType().equals(ConsensusStatusType.ABORT))
                return;

            byte[] bytesBlock = null;
            try {
                Optional<byte[]> message = ConsensusBrokerInstance.getInstance().getConsensusBroker().receiveMessageFromLeader(TopicType.PREPARE_PHASE, String.valueOf(this.original_copy.getViewID()));
                Preconditions.checkArgument(message.isPresent(), "PreparePhase: Empty Leader Response from leader");
                byte[] receive = message.get();
                data = (ConsensusMessage<TransactionBlock>) SerializationFuryUtil.getInstance().getFury().deserialize(receive);

                if (!this.blockIndex.containsAll(data.getSignatures().keySet(), CachedLatestBlocks
                        .getInstance()
                        .getCommitteeBlock()
                        .getStructureMap()
                        .get(CachedZoneIndex.getInstance().getZoneIndex()).keySet())) {
                    cleanup();
                    LOG.info("PreparePhase: Abort Some Validators Public keys are not exist on Committee Block");
                    data.setStatusType(ConsensusStatusType.ABORT);
                    return;
                }
                if (!data.getChecksumData().getBlsPublicKey().toRaw().equals(leader_bls.toRaw())) {
                    cleanup();
                    LOG.info("PreparePhase: This is not the valid leader for this round");
                    data.setStatusType(ConsensusStatusType.ABORT);
                    return;
                }
            } catch (IllegalArgumentException | StringIndexOutOfBoundsException e) {
                cleanup();
                LOG.info("PreparePhase: Problem at message deserialization Abort");
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            } catch (ArrayIndexOutOfBoundsException e) {
                cleanup();
                LOG.info("PreparePhase: Out of bounds response from organizer");
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            } catch (NullPointerException e) {
                cleanup();
                LOG.info("PreparePhase: Receiving null response from organizer");
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            }


            if (!data.getMessageType().equals(ConsensusMessageType.PREPARE)) {
                LOG.info("PreparePhase: Organizer not send correct header message expected " + ConsensusMessageType.PREPARE);
                cleanup();
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            }

            //##############################################################
            String messageHashAsBase64String = Shake256Hash[0];
            for (Map.Entry<BLSPublicKey, BLSSignatureData> entry : data.getSignatures().entrySet()) {
                BLSSignatureData BLSSignatureData = new BLSSignatureData();
                BLSSignatureData.getSignature()[0] = entry.getValue().getSignature()[0];
                BLSSignatureData.getMessageHash()[0] = messageHashAsBase64String;
                this.original_copy.getSignatureData().put(entry.getKey(), BLSSignatureData);
            }
            //##############################################################

            this.sizeCalculator.setTransactionBlock(this.original_copy);
            bytesBlock = block_serialize.encode(this.original_copy, this.sizeCalculator.TransactionBlockSizeCalculator());
            if (bytesBlock == null) {
                cleanup();
                LOG.info("PreparePhase: failed  to receive correct bytes block: Abort");
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            }
            String hash = HashUtil.sha256_bytetoString(bytesBlock);
            if (!hash.equals(data.getHash())) {
                cleanup();
                LOG.info("PreparePhase: Abort consensus phase BLS leader hash is invalid according to block");
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            }
            boolean verifyLeader = BLSSignature.verify(data.getChecksumData().getSignature(), bytesBlock, data.getChecksumData().getBlsPublicKey());
            if (!verifyLeader) {
                cleanup();
                LOG.info("PreparePhase: Abort consensus phase BLS leader signature is invalid during prepare phase");
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            }
            //##############################################################

            List<BLSPublicKey> publicKeys = new ArrayList<>(data.getSignatures().keySet());
            List<Signature> signature = data.getSignatures().values().stream().map(BLSSignatureData::getSignature).map(r -> r[0]).collect(Collectors.toList());


            Signature aggregatedSignature = BLSSignature.aggregate(signature);

            Bytes toVerify = Bytes.wrap(prevAgreegation.get(0));
            boolean verify = BLSSignature.fastAggregateVerify(publicKeys, toVerify, aggregatedSignature);
            if (!verify) {
                cleanup();
                LOG.info("PreparePhase: Abort consensus phase BLS multi_signature is invalid in prepare phase");
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            }


            BLSSignatureData BLSLeaderSignatureData = this.original_copy.getSignatureData().get(data.getChecksumData().getBlsPublicKey());
            BLSLeaderSignatureData.getSignature()[1] = Signature.fromByte(data.getChecksumData().getSignature().toBytes());
            BLSLeaderSignatureData.getMessageHash()[1] = BLSSignature.GetMessageHashAsBase64String(bytesBlock);
            this.original_copy.getSignatureData().put(BLSPublicKey.fromByte(data.getChecksumData().getBlsPublicKey().toBytes()), BLSLeaderSignatureData);

            // data.clear();
            data.setStatusType(ConsensusStatusType.SUCCESS);


            this.sizeCalculator.setTransactionBlock(original_copy);
            byte[] message = block_serialize.encode(original_copy, this.sizeCalculator.TransactionBlockSizeCalculator());
            data.setHash(HashUtil.sha256_bytetoString(message));
            Shake256Hash[1] = BLSSignature.GetMessageHashAsBase64String(message);
            this.prevAgreegation.add(1, message);
            Signature sig = BLSSignature.sign(message, CachedBLSKeyPair.getInstance().getPrivateKey());
            data.setChecksumData(new ChecksumData(sig, CachedBLSKeyPair.getInstance().getPublicKey()));


            byte[] toSend = SerializationFuryUtil.getInstance().getFury().serialize(data);
            ConsensusBrokerInstance.getInstance().getConsensusBroker().produceMessage(TopicType.PREPARE_PHASE, String.valueOf(this.original_copy.getViewID()), toSend);
        }

        @Override
        public void CommitPhase(ConsensusMessage<TransactionBlock> data) throws InterruptedException {
            if (data.getStatusType().equals(ConsensusStatusType.ABORT))
                return;

            byte[] bytesBlock = null;
            try {
                Optional<byte[]> message = ConsensusBrokerInstance.getInstance().getConsensusBroker().receiveMessageFromLeader(TopicType.COMMITTEE_PHASE, String.valueOf(this.original_copy.getViewID()));
                Preconditions.checkArgument(message.isPresent(), "CommitPhase: Empty Leader Response from leader");
                byte[] receive = message.get();
                data = (ConsensusMessage<TransactionBlock>) SerializationFuryUtil.getInstance().getFury().deserialize(receive);

                if (!this.blockIndex.containsAll(data.getSignatures().keySet(), CachedLatestBlocks
                        .getInstance()
                        .getCommitteeBlock()
                        .getStructureMap()
                        .get(CachedZoneIndex.getInstance().getZoneIndex()).keySet())) {
                    cleanup();
                    LOG.info("CommitPhase: Abort Some Validators Public keys are not exist on Committee Block");
                    data.setStatusType(ConsensusStatusType.ABORT);
                    return;
                }
                if (!data.getChecksumData().getBlsPublicKey().toRaw().equals(leader_bls.toRaw())) {
                    cleanup();
                    LOG.info("CommitPhase: This is not the valid leader for this round");
                    data.setStatusType(ConsensusStatusType.ABORT);
                    return;
                }
            } catch (IllegalArgumentException | StringIndexOutOfBoundsException e) {
                cleanup();
                LOG.info("CommitPhase: Problem at message deserialization Abort");
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            } catch (ArrayIndexOutOfBoundsException e) {
                cleanup();
                LOG.info("CommitPhase: Receiving out of bounds response from organizer");
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            } catch (NullPointerException e) {
                cleanup();
                LOG.info("CommitPhase: Receiving null response from organizer");
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            }

            if (!data.getMessageType().equals(ConsensusMessageType.COMMIT)) {
                cleanup();
                LOG.info("CommitPhase: Organizer not send correct header message expected " + ConsensusMessageType.COMMIT);
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            }

            String messageHashAsBase64String = Shake256Hash[1];
            for (Map.Entry<BLSPublicKey, BLSSignatureData> entry : data.getSignatures().entrySet()) {
                BLSSignatureData BLSSignatureData = this.original_copy.getSignatureData().get(entry.getKey());
                BLSSignatureData.getSignature()[1] = entry.getValue().getSignature()[1];
                BLSSignatureData.getMessageHash()[1] = messageHashAsBase64String;
                this.original_copy.getSignatureData().put(entry.getKey(), BLSSignatureData);
            }
            this.sizeCalculator.setTransactionBlock(this.original_copy);
            bytesBlock = block_serialize.encode(this.original_copy, this.sizeCalculator.TransactionBlockSizeCalculator());
            if (bytesBlock == null) {
                cleanup();
                LOG.info("CommitPhase: failed  to receive correct bytes block: Abort");
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            }
            String hash = HashUtil.sha256_bytetoString(bytesBlock);
            if (!hash.equals(data.getHash())) {
                cleanup();
                LOG.info("CommitPhase: Abort consensus phase BLS leader hash is invalid according to block");
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            }
            boolean verifyLeader = BLSSignature.verify(data.getChecksumData().getSignature(), bytesBlock, data.getChecksumData().getBlsPublicKey());
            if (!verifyLeader) {
                cleanup();
                LOG.info("CommitPhase: Abort consensus phase BLS leader signature is invalid during commit phase");
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            }

            List<BLSPublicKey> publicKeys = new ArrayList<>(data.getSignatures().keySet());
            List<Signature> signature = data.getSignatures().values().stream().map(BLSSignatureData::getSignature).map(r -> r[1]).collect(Collectors.toList());


            Signature aggregatedSignature = BLSSignature.aggregate(signature);

            Bytes toVerify = Bytes.wrap(prevAgreegation.get(1));
            boolean verify = BLSSignature.fastAggregateVerify(publicKeys, toVerify, aggregatedSignature);
            if (!verify) {
                cleanup();
                LOG.info("CommitPhase: Abort consensus phase BLS multi_signature is invalid in prepare phase");
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            }


            ConsensusMessage<String> commit = new ConsensusMessage<String>("COMMIT");
            commit.setStatusType(ConsensusStatusType.SUCCESS);
            commit.setMessageType(ConsensusMessageType.COMMIT);

            Signature sig = BLSSignature.sign(commit.getData().getBytes(StandardCharsets.UTF_8), CachedBLSKeyPair.getInstance().getPrivateKey());
            commit.setChecksumData(new ChecksumData(sig, CachedBLSKeyPair.getInstance().getPublicKey()));

            byte[] toSend = SerializationUtils.serialize(commit);
            ConsensusBrokerInstance.getInstance().getConsensusBroker().produceMessage(TopicType.COMMITTEE_PHASE, String.valueOf(this.original_copy.getViewID()), toSend);

            //commit save to db
            BlockInvent regural_block = (BlockInvent) factory.getBlock(BlockType.REGULAR);
            regural_block.InventTransactionBlock(this.original_copy);
            BLSPublicKey next_key = blockIndex.getPublicKeyByIndex(CachedZoneIndex.getInstance().getZoneIndex(), CachedLeaderIndex.getInstance().getTransactionPositionLeader());
            CachedLatestBlocks.getInstance().getTransactionBlock().setBlockProposer(next_key.toRaw());
            CachedLatestBlocks.getInstance().getTransactionBlock().setLeaderPublicKey(next_key);
            // consensusClient.send_heartbeat(HEARTBEAT_MESSAGE);
            //CachedSerializableErasureObject.getInstance().setSerializableErasureObject(null);
            cleanup();
            LOG.info("Block is finalized with Success");
            Thread.sleep(500);
            ConsensusBrokerInstance.getInstance().getConsensusBroker().seekAllOffsetToEnd();
        }

        private void cleanup() {
            if (ConsensusBrokerInstance.getInstance().getConsensusBroker() != null) {
                ConsensusBrokerInstance.getInstance().getConsensusBroker().clear();
            }
        }
    }

    protected static class VerifyCommitteeBlock extends ValidatorConsensusPhases implements BFTConsensusPhase<CommitteeBlock> {

        private static Logger LOG = LoggerFactory.getLogger(VerifyCommitteeBlock.class);
        private static final Type fluentType = new TypeToken<ConsensusMessage<CommitteeBlock>>() {
        }.getType();
        private final SerializationUtil<AbstractBlock> block_serialize;
        private final SerializationUtil<ConsensusMessage> consensus_serialize;
        private final SerializationUtil<Signature> signatureMapper;
        private final SerializationUtil<SerializableErasureObject> serenc_erasure;
        private final DefaultFactory factory;
        private final BlockSizeCalculator sizeCalculator;
        private final String[] Shake256Hash;
        private final List<byte[]> prevAgreegation;
        private final SerializationUtil<ArrayList<byte[]>> serenc_rpc;

        private RpcErasureClient client;
        private CommitteeBlock original_copy;
        private String LeaderIP;
        private OptionalInt position;
        private ArrayList<String> ips;

        public VerifyCommitteeBlock() {
            this.factory = new DefaultFactory();
            List<SerializationUtil.Mapping> list = new ArrayList<>();
            list.add(new SerializationUtil.Mapping(ECP.class, ctx -> new ECPmapper()));
            list.add(new SerializationUtil.Mapping(ECP2.class, ctx -> new ECP2mapper()));
            list.add(new SerializationUtil.Mapping(BigDecimal.class, ctx -> new BigDecimalSerializer()));
            list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
            list.add(new SerializationUtil.Mapping(TreeMap.class, ctx -> new CustomSerializerTreeMap()));
            this.block_serialize = new SerializationUtil<AbstractBlock>(AbstractBlock.class, list);
            this.consensus_serialize = new SerializationUtil<ConsensusMessage>(fluentType, list);
            this.serenc_rpc = new SerializationUtil<ArrayList<byte[]>>(new TypeToken<List<byte[]>>() {
            }.getType());
            this.sizeCalculator = new BlockSizeCalculator();
            this.signatureMapper = new SerializationUtil<Signature>(Signature.class, list);
            this.serenc_erasure = new SerializationUtil<SerializableErasureObject>(SerializableErasureObject.class, list);
            this.Shake256Hash = new String[2];
            this.prevAgreegation = new ArrayList<>(2);
            ErasureServerInstance.getInstance();
        }

        @Override
        public void InitialSetup() {
            this.current = CachedLeaderIndex.getInstance().getCommitteePositionLeader();
            this.leader_bls = this.blockIndex.getPublicKeyByIndex(0, current);
            this.LeaderIP = this.blockIndex.getIpValue(0, this.leader_bls);
            this.ips = this.blockIndex.getIpList(CachedZoneIndex.getInstance().getZoneIndex());
            KafkaConfiguration.KAFKA_HOST = IPFinder.getLocalIP();
            this.position = IntStream.range(0, this.ips.size()).filter(i -> KafkaConfiguration.KAFKA_HOST.equals(ips.get(i))).findFirst();
            ConsensusBrokerInstance.getInstance(ips, ips.getFirst(), position.getAsInt());
            ConsensusBrokerInstance.getInstance().getConsensusBroker().setLeader_host(this.LeaderIP);
            ConsensusBrokerInstance.getInstance().getConsensusBroker().setLeader_position(this.current);
        }

        @Override
        public void DispersePhase(ConsensusMessage<CommitteeBlock> data) throws Exception {
            try {
                ArrayList<byte[]> fromLeaderReceive = ConsensusBrokerInstance.getInstance().getConsensusBroker().receiveDisperseHandledMessageFromLeader(TopicType.DISPERSE_PHASE1, String.valueOf(data.getData().getViewID()) + position.getAsInt());

                Preconditions.checkArgument(!fromLeaderReceive.isEmpty(), "DispersePhase: Empty Leader Response from leader");
                Preconditions.checkArgument(!fromLeaderReceive.contains(null), "DispersePhase: The Leader Response contains null values");


                ConsensusBrokerInstance.getInstance().getConsensusBroker().distributeDisperseMessageToValidators(fromLeaderReceive, String.valueOf(data.getData().getViewID()));
                HashMap<String, ArrayList<byte[]>> finalList = ConsensusBrokerInstance.getInstance().getConsensusBroker().retrieveDisperseMessageFromValidatorsAndConcatResponse(fromLeaderReceive, String.valueOf(data.getData().getViewID()));

                Preconditions.checkArgument(!finalList.isEmpty(), "DispersePhase: Empty Validators Response from leader");
                Preconditions.checkArgument(finalList.containsKey(this.LeaderIP), "DispersePhase: Abort LeaderIP is not in the final list");
                Preconditions.checkArgument(!finalList.get(this.LeaderIP).isEmpty(), "DispersePhase: Empty Response from leader to get root SerializableErasureObject");
                Preconditions.checkArgument(!fromLeaderReceive.contains(null), "DispersePhase: Null Response from leader to get root SerializableErasureObject");

                ArrayList<SerializableErasureObject> recserializableErasureObjects = new ArrayList<SerializableErasureObject>();


                SerializableErasureObject root = serenc_erasure.decode(finalList.get(this.LeaderIP).getFirst());

                Preconditions.checkArgument(root != null, "DispersePhase: Null Response root SerializableErasureObject is null");
                Preconditions.checkArgument(!root.getRootMerkleHash().isEmpty() || root.getOriginalPacketChunks() != null || root.getFecParameterObject() != null || root.getOriginalPacketChunks().length != 0, "DispersePhase: Empty Response root SerializableErasureObject is empty");

                Set<String> keysToRetrieve = new HashSet<>();
                for (Map.Entry<String, ArrayList<byte[]>> entry : finalList.entrySet()) {
                    try {
                        if (entry.getValue().isEmpty()) {
                            keysToRetrieve.add(entry.getKey());
                            continue;
                        }
                        for (byte[] rec_buff : entry.getValue()) {
                            SerializableErasureObject object = serenc_erasure.decode(rec_buff);
                            if (!object.getRootMerkleHash().equals(root.getRootMerkleHash())) {
                                LOG.info("DispersePhase: Meerklee Hash is not valid");
                                keysToRetrieve.add(entry.getKey());
                            } else {
                                recserializableErasureObjects.add(serenc_erasure.decode(rec_buff));
                            }
                        }
                    } catch (Exception e) {
                        LOG.info("DispersePhase: Serialization Block Exception need retrieve mored data");
                        keysToRetrieve.add(entry.getKey());
                    }
                }
                if (!keysToRetrieve.isEmpty()) {
                    this.client.connect();
                    Map<String, byte[]> serializableErasureObject = client.getErasureChunks(new ArrayList<>(keysToRetrieve));
                    for (Map.Entry<String, byte[]> entry : serializableErasureObject.entrySet()) {
                        ArrayList<byte[]> res = new ArrayList<>(serenc_rpc.decode(entry.getValue()));
                        for (byte[] rec_buff : res) {
                            SerializableErasureObject object = serenc_erasure.decode(rec_buff);
                            if (!object.getRootMerkleHash().equals(root.getRootMerkleHash())) {
                                throw new IllegalArgumentException("DispersePhase: Meerklee Hash is not valid");
                            } else {
                                recserializableErasureObjects.add(serenc_erasure.decode(rec_buff));
                            }
                        }
                    }
                }

                recserializableErasureObjects.add(root);
                Collections.shuffle(recserializableErasureObjects);
                FECParameterObject recobject = recserializableErasureObjects.get(0).getFecParameterObject();
                FECParameters recfecParams = FECParameters.newParameters(recobject.getDataLen(), recobject.getSymbolSize(), recobject.getNumberOfSymbols());
                final ArrayDataDecoder dec = OpenRQ.newDecoder(recfecParams, recobject.getSymbolOverhead());

                for (int i = 0; i < recserializableErasureObjects.size(); i++) {
                    EncodingPacket encodingPacket = dec.parsePacket(ByteBuffer.wrap(recserializableErasureObjects.get(i).getOriginalPacketChunks()), false).value();
                    final SourceBlockDecoder sbDec = dec.sourceBlock(encodingPacket.sourceBlockNumber());
                    sbDec.putEncodingPacket(encodingPacket);
                }

                this.original_copy = (CommitteeBlock) block_serialize.decode(dec.dataArray());


            } catch (IllegalArgumentException | StringIndexOutOfBoundsException e) {
                cleanup();
                LOG.info("DispersePhase: Problem at message deserialization Abort " + e.getMessage());
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            } catch (ArrayIndexOutOfBoundsException e) {
                cleanup();
                LOG.info("DispersePhase: Out of bounds response from organizer " + e.getMessage());
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            } catch (NullPointerException e) {
                cleanup();
                LOG.info("DispersePhase: Receiving null response from organizer " + e.getMessage());
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            } catch (Exception e) {
                cleanup();
                LOG.info("DispersePhase: Disperse failed cannot decode block need more chunks abort " + e.getMessage());
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            }

            data.setStatusType(ConsensusStatusType.SUCCESS);
        }


        @SneakyThrows
        @Override
        public void AnnouncePhase(ConsensusMessage<CommitteeBlock> data) {
            if (data.getStatusType().equals(ConsensusStatusType.ABORT))
                return;

            byte[] bytesBlock = null;
            try {
                Optional<byte[]> message = ConsensusBrokerInstance.getInstance().getConsensusBroker().receiveMessageFromLeader(TopicType.ANNOUNCE_PHASE, String.valueOf(data.getData().getViewID()));
                Preconditions.checkArgument(message.isPresent(), "AnnouncePhase: Empty Leader Response from leader");
                byte[] receive = message.get();
                data = (ConsensusMessage<CommitteeBlock>) SerializationFuryUtil.getInstance().getFury().deserialize(receive);
                if (!data.getChecksumData().getBlsPublicKey().toRaw().equals(leader_bls.toRaw())) {
                    cleanup();
                    LOG.info("AnnouncePhase: This is not the valid leader for this round");
                    data.setStatusType(ConsensusStatusType.ABORT);
                    return;
                }
            } catch (IllegalArgumentException | StringIndexOutOfBoundsException e) {
                e.printStackTrace();
                cleanup();
                LOG.info("AnnouncePhase: Problem at message deserialization Abort " + e.getMessage());
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            } catch (ArrayIndexOutOfBoundsException e) {
                e.printStackTrace();
                cleanup();
                LOG.info("AnnouncePhase: Out of bounds response from organizer " + e.getMessage());
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            } catch (NullPointerException e) {
                e.printStackTrace();
                cleanup();
                LOG.info("AnnouncePhase: Receiving null response from organizer " + e.getMessage());
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            }
            if (!data.getMessageType().equals(ConsensusMessageType.ANNOUNCE)) {
                cleanup();
                LOG.info("AnnouncePhase: Organizer not send correct header message expected " + ConsensusMessageType.ANNOUNCE);
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            }


            CachedCommitteeBlockEventPublisher.getInstance().publish(this.original_copy);
            CachedCommitteeBlockEventPublisher.getInstance().WaitUntilRemainingCapacityZero();

            if (this.original_copy.getStatustype().equals(StatusType.ABORT)) {
                cleanup();
                LOG.info("AnnouncePhase: Block is not valid marked as ABORT");
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            }
            this.sizeCalculator.setCommitteeBlock(this.original_copy);
            bytesBlock = block_serialize.encode(this.original_copy, this.sizeCalculator.CommitteeBlockSizeCalculator());
            if (bytesBlock == null) {
                cleanup();
                LOG.info("AnnouncePhase: failed  to receive correct bytes block: Abort");
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            }
            String hash = HashUtil.sha256_bytetoString(bytesBlock);
            if (!hash.equals(data.getHash())) {
                cleanup();
                LOG.info("AnnouncePhase: Abort consensus phase BLS leader hash is invalid according to block");
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            }
            boolean verify = BLSSignature.verify(data.getChecksumData().getSignature(), bytesBlock, data.getChecksumData().getBlsPublicKey());
            if (!verify) {
                cleanup();
                LOG.info("AnnouncePhase: Abort consensus phase BLS leader signature is invalid during announce phase");
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            }

            data.setStatusType(ConsensusStatusType.SUCCESS);
            data.setData(null);

            BLSSignatureData BLSLeaderSignatureData = new BLSSignatureData(2);
            BLSLeaderSignatureData.getSignature()[0] = Signature.fromByte(data.getChecksumData().getSignature().toBytes());
            BLSLeaderSignatureData.getMessageHash()[0] = BLSSignature.GetMessageHashAsBase64String(bytesBlock);
            this.original_copy.getSignatureData().put(BLSPublicKey.fromByte(data.getChecksumData().getBlsPublicKey().toBytes()), BLSLeaderSignatureData);

            this.sizeCalculator.setCommitteeBlock(original_copy);
            byte[] message = block_serialize.encode(original_copy, this.sizeCalculator.CommitteeBlockSizeCalculator());
            this.Shake256Hash[0] = BLSSignature.GetMessageHashAsBase64String(message);
            this.prevAgreegation.add(0, message);
            Signature sig = BLSSignature.sign(message, CachedBLSKeyPair.getInstance().getPrivateKey());
            data.setChecksumData(new ChecksumData(sig, CachedBLSKeyPair.getInstance().getPublicKey()));


            byte[] toSend = SerializationFuryUtil.getInstance().getFury().serialize(data);
            ConsensusBrokerInstance.getInstance().getConsensusBroker().produceMessage(TopicType.ANNOUNCE_PHASE, String.valueOf(this.original_copy.getViewID()), toSend);
        }

        @Override
        public void PreparePhase(ConsensusMessage<CommitteeBlock> data) {
            if (data.getStatusType().equals(ConsensusStatusType.ABORT))
                return;

            byte[] bytesBlock = null;
            try {
                Optional<byte[]> message = ConsensusBrokerInstance.getInstance().getConsensusBroker().receiveMessageFromLeader(TopicType.PREPARE_PHASE, String.valueOf(this.original_copy.getViewID()));
                Preconditions.checkArgument(message.isPresent(), "PreparePhase: Empty Leader Response from leader");
                byte[] receive = message.get();
                data = (ConsensusMessage<CommitteeBlock>) SerializationFuryUtil.getInstance().getFury().deserialize(receive);

                if (!this.blockIndex.containsAll(data.getSignatures().keySet(), CachedLatestBlocks
                        .getInstance()
                        .getCommitteeBlock()
                        .getStructureMap()
                        .get(CachedZoneIndex.getInstance().getZoneIndex()).keySet())) {
                    cleanup();
                    LOG.info("PreparePhase: Abort Some Validators Public keys are not exist on Committee Block");
                    data.setStatusType(ConsensusStatusType.ABORT);
                    return;
                }
                if (!data.getChecksumData().getBlsPublicKey().toRaw().equals(leader_bls.toRaw())) {
                    cleanup();
                    LOG.info("PreparePhase: This is not the valid leader for this round");
                    data.setStatusType(ConsensusStatusType.ABORT);
                    return;
                }
            } catch (IllegalArgumentException | StringIndexOutOfBoundsException e) {
                cleanup();
                LOG.info("PreparePhase: Problem at message deserialization Abort");
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            } catch (ArrayIndexOutOfBoundsException e) {
                cleanup();
                LOG.info("PreparePhase: Out of bounds response from organizer");
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            } catch (NullPointerException e) {
                cleanup();
                LOG.info("PreparePhase: Receiving null response from organizer");
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            }


            if (!data.getMessageType().equals(ConsensusMessageType.PREPARE)) {
                LOG.info("PreparePhase: Organizer not send correct header message expected " + ConsensusMessageType.PREPARE);
                cleanup();
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            }

            //##############################################################
            String messageHashAsBase64String = Shake256Hash[0];
            for (Map.Entry<BLSPublicKey, BLSSignatureData> entry : data.getSignatures().entrySet()) {
                BLSSignatureData BLSSignatureData = new BLSSignatureData();
                BLSSignatureData.getSignature()[0] = entry.getValue().getSignature()[0];
                BLSSignatureData.getMessageHash()[0] = messageHashAsBase64String;
                this.original_copy.getSignatureData().put(entry.getKey(), BLSSignatureData);
            }
            //##############################################################

            this.sizeCalculator.setCommitteeBlock(this.original_copy);
            bytesBlock = block_serialize.encode(this.original_copy, this.sizeCalculator.CommitteeBlockSizeCalculator());
            if (bytesBlock == null) {
                cleanup();
                LOG.info("PreparePhase: failed  to receive correct bytes block: Abort");
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            }
            String hash = HashUtil.sha256_bytetoString(bytesBlock);
            if (!hash.equals(data.getHash())) {
                cleanup();
                LOG.info("PreparePhase: Abort consensus phase BLS leader hash is invalid according to block");
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            }
            boolean verifyLeader = BLSSignature.verify(data.getChecksumData().getSignature(), bytesBlock, data.getChecksumData().getBlsPublicKey());
            if (!verifyLeader) {
                cleanup();
                LOG.info("PreparePhase: Abort consensus phase BLS leader signature is invalid during prepare phase");
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            }
            //##############################################################

            List<BLSPublicKey> publicKeys = new ArrayList<>(data.getSignatures().keySet());
            List<Signature> signature = data.getSignatures().values().stream().map(BLSSignatureData::getSignature).map(r -> r[0]).collect(Collectors.toList());


            Signature aggregatedSignature = BLSSignature.aggregate(signature);

            Bytes toVerify = Bytes.wrap(prevAgreegation.get(0));
            boolean verify = BLSSignature.fastAggregateVerify(publicKeys, toVerify, aggregatedSignature);
            if (!verify) {
                cleanup();
                LOG.info("PreparePhase: Abort consensus phase BLS multi_signature is invalid in prepare phase");
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            }


            BLSSignatureData BLSLeaderSignatureData = this.original_copy.getSignatureData().get(data.getChecksumData().getBlsPublicKey());
            BLSLeaderSignatureData.getSignature()[1] = Signature.fromByte(data.getChecksumData().getSignature().toBytes());
            BLSLeaderSignatureData.getMessageHash()[1] = BLSSignature.GetMessageHashAsBase64String(bytesBlock);
            this.original_copy.getSignatureData().put(BLSPublicKey.fromByte(data.getChecksumData().getBlsPublicKey().toBytes()), BLSLeaderSignatureData);

            // data.clear();
            data.setStatusType(ConsensusStatusType.SUCCESS);


            this.sizeCalculator.setCommitteeBlock(original_copy);
            byte[] message = block_serialize.encode(original_copy, this.sizeCalculator.CommitteeBlockSizeCalculator());
            data.setHash(HashUtil.sha256_bytetoString(message));
            Shake256Hash[1] = BLSSignature.GetMessageHashAsBase64String(message);
            this.prevAgreegation.add(1, message);
            Signature sig = BLSSignature.sign(message, CachedBLSKeyPair.getInstance().getPrivateKey());
            data.setChecksumData(new ChecksumData(sig, CachedBLSKeyPair.getInstance().getPublicKey()));


            byte[] toSend = SerializationFuryUtil.getInstance().getFury().serialize(data);
            ConsensusBrokerInstance.getInstance().getConsensusBroker().produceMessage(TopicType.PREPARE_PHASE, String.valueOf(this.original_copy.getViewID()), toSend);
        }

        @SneakyThrows
        @Override
        public void CommitPhase(ConsensusMessage<CommitteeBlock> data) {
            if (data.getStatusType().equals(ConsensusStatusType.ABORT))
                return;

            byte[] bytesBlock = null;
            try {
                Optional<byte[]> message = ConsensusBrokerInstance.getInstance().getConsensusBroker().receiveMessageFromLeader(TopicType.COMMITTEE_PHASE, String.valueOf(this.original_copy.getViewID()));
                Preconditions.checkArgument(message.isPresent(), "CommitPhase: Empty Leader Response from leader");
                byte[] receive = message.get();
                data = (ConsensusMessage<CommitteeBlock>) SerializationFuryUtil.getInstance().getFury().deserialize(receive);

                if (!this.blockIndex.containsAll(data.getSignatures().keySet(), CachedLatestBlocks
                        .getInstance()
                        .getCommitteeBlock()
                        .getStructureMap()
                        .get(CachedZoneIndex.getInstance().getZoneIndex()).keySet())) {
                    cleanup();
                    LOG.info("CommitPhase: Abort Some Validators Public keys are not exist on Committee Block");
                    data.setStatusType(ConsensusStatusType.ABORT);
                    return;
                }
                if (!data.getChecksumData().getBlsPublicKey().toRaw().equals(leader_bls.toRaw())) {
                    cleanup();
                    LOG.info("CommitPhase: This is not the valid leader for this round");
                    data.setStatusType(ConsensusStatusType.ABORT);
                    return;
                }
            } catch (IllegalArgumentException | StringIndexOutOfBoundsException e) {
                cleanup();
                LOG.info("CommitPhase: Problem at message deserialization Abort");
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            } catch (ArrayIndexOutOfBoundsException e) {
                cleanup();
                LOG.info("CommitPhase: Receiving out of bounds response from organizer");
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            } catch (NullPointerException e) {
                cleanup();
                LOG.info("CommitPhase: Receiving null response from organizer");
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            }

            if (!data.getMessageType().equals(ConsensusMessageType.COMMIT)) {
                cleanup();
                LOG.info("CommitPhase: Organizer not send correct header message expected " + ConsensusMessageType.COMMIT);
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            }

            String messageHashAsBase64String = Shake256Hash[1];
            for (Map.Entry<BLSPublicKey, BLSSignatureData> entry : data.getSignatures().entrySet()) {
                BLSSignatureData BLSSignatureData = this.original_copy.getSignatureData().get(entry.getKey());
                BLSSignatureData.getSignature()[1] = entry.getValue().getSignature()[1];
                BLSSignatureData.getMessageHash()[1] = messageHashAsBase64String;
                this.original_copy.getSignatureData().put(entry.getKey(), BLSSignatureData);
            }
            this.sizeCalculator.setCommitteeBlock(this.original_copy);
            bytesBlock = block_serialize.encode(this.original_copy, this.sizeCalculator.CommitteeBlockSizeCalculator());
            if (bytesBlock == null) {
                cleanup();
                LOG.info("CommitPhase: failed  to receive correct bytes block: Abort");
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            }
            String hash = HashUtil.sha256_bytetoString(bytesBlock);
            if (!hash.equals(data.getHash())) {
                cleanup();
                LOG.info("CommitPhase: Abort consensus phase BLS leader hash is invalid according to block");
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            }
            boolean verifyLeader = BLSSignature.verify(data.getChecksumData().getSignature(), bytesBlock, data.getChecksumData().getBlsPublicKey());
            if (!verifyLeader) {
                cleanup();
                LOG.info("CommitPhase: Abort consensus phase BLS leader signature is invalid during commit phase");
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            }

            List<BLSPublicKey> publicKeys = new ArrayList<>(data.getSignatures().keySet());
            List<Signature> signature = data.getSignatures().values().stream().map(BLSSignatureData::getSignature).map(r -> r[1]).collect(Collectors.toList());


            Signature aggregatedSignature = BLSSignature.aggregate(signature);

            Bytes toVerify = Bytes.wrap(prevAgreegation.get(1));
            boolean verify = BLSSignature.fastAggregateVerify(publicKeys, toVerify, aggregatedSignature);
            if (!verify) {
                cleanup();
                LOG.info("CommitPhase: Abort consensus phase BLS multi_signature is invalid in prepare phase");
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            }


            ConsensusMessage<String> commit = new ConsensusMessage<String>("COMMIT");
            commit.setStatusType(ConsensusStatusType.SUCCESS);
            commit.setMessageType(ConsensusMessageType.COMMIT);

            Signature sig = BLSSignature.sign(commit.getData().getBytes(StandardCharsets.UTF_8), CachedBLSKeyPair.getInstance().getPrivateKey());
            commit.setChecksumData(new ChecksumData(sig, CachedBLSKeyPair.getInstance().getPublicKey()));

            byte[] toSend = SerializationUtils.serialize(commit);
            ConsensusBrokerInstance.getInstance().getConsensusBroker().produceMessage(TopicType.COMMITTEE_PHASE, String.valueOf(this.original_copy.getViewID()), toSend);

            CachedStartHeightRewards.getInstance().setRewardsCommitteeEnabled(true);
            BlockInvent regural_block = (BlockInvent) factory.getBlock(BlockType.REGULAR);
            regural_block.InventCommitteBlock(this.original_copy);
            //commit save to db

            BLSPublicKey next_key = blockIndex.getPublicKeyByIndex(CachedZoneIndex.getInstance().getZoneIndex(), 0);
            CachedLatestBlocks.getInstance().getTransactionBlock().setBlockProposer(next_key.toRaw());
            CachedLatestBlocks.getInstance().getCommitteeBlock().setBlockProposer(next_key.toRaw());
            CachedLatestBlocks.getInstance().getTransactionBlock().setLeaderPublicKey(next_key);
            CachedLatestBlocks.getInstance().getCommitteeBlock().setLeaderPublicKey(next_key);
            CachedLeaderIndex.getInstance().setCommitteePositionLeader(0);
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
