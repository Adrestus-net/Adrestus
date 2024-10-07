package io.Adrestus.consensus;

import com.google.common.reflect.TypeToken;
import io.Adrestus.config.AdrestusConfiguration;
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
import io.Adrestus.network.ConsensusClient;
import io.Adrestus.rpc.CachedSerializableErasureObject;
import io.Adrestus.rpc.RpcErasureClient;
import io.Adrestus.util.ByteUtil;
import io.Adrestus.util.GetTime;
import io.Adrestus.util.SerializationUtil;
import lombok.SneakyThrows;
import org.apache.commons.codec.binary.Hex;
import org.apache.tuweni.bytes.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import static io.Adrestus.config.ConsensusConfiguration.*;

public class ValidatorConsensusPhases {

    protected boolean DEBUG;
    protected final IBlockIndex blockIndex;

    protected CountDownLatch latch;
    protected int N;
    protected int F;
    protected int current;
    protected ConsensusClient consensusClient;
    protected BLSPublicKey leader_bls;

    public ValidatorConsensusPhases() {
        this.blockIndex = new BlockIndex();
    }

    protected static class VerifyVDF extends ValidatorConsensusPhases implements BFTConsensusPhase<VDFMessage> {
        private static final Type fluentType = new TypeToken<ConsensusMessage<VDFMessage>>() {
        }.getType();
        private static Logger LOG = LoggerFactory.getLogger(VerifyVDF.class);
        private final VdfEngine vdf;
        private final SerializationUtil<ConsensusMessage> consensus_serialize;
        private final SerializationUtil<VDFMessage> data_serialize;
        private RpcErasureClient<byte[]> collector_client;

        public VerifyVDF(boolean DEBUG) {
            this.DEBUG = DEBUG;
            this.vdf = new VdfEnginePietrzak(AdrestusConfiguration.PIERRZAK_BIT);
            List<SerializationUtil.Mapping> list = new ArrayList<>();
            list.add(new SerializationUtil.Mapping(ECP.class, ctx -> new ECPmapper()));
            list.add(new SerializationUtil.Mapping(ECP2.class, ctx -> new ECP2mapper()));
            this.data_serialize = new SerializationUtil<VDFMessage>(VDFMessage.class);
            this.consensus_serialize = new SerializationUtil<ConsensusMessage>(fluentType, list);
        }

        @Override
        public void InitialSetup() {
            if (!DEBUG) {
                this.current = CachedLeaderIndex.getInstance().getCommitteePositionLeader();
                this.leader_bls = this.blockIndex.getPublicKeyByIndex(0, current);
                String LeaderIP = this.blockIndex.getIpValue(0, this.leader_bls);
                this.consensusClient = new ConsensusClient(LeaderIP, 0);
                this.consensusClient.receive_handler();
                this.collector_client = new RpcErasureClient<byte[]>(LeaderIP, ERASURE_SERVER_PORT, COMMITTEE_ERASURE_CLIENT_TIMEOUT, CachedEventLoop.getInstance().getEventloop());
                this.collector_client.connect();
            }
        }

        @Override
        public void DispersePhase(ConsensusMessage<VDFMessage> data) throws Exception {
            return;
        }

        @Override
        public void AnnouncePhase(ConsensusMessage<VDFMessage> data) {
            if (!DEBUG) {
                try {
                    consensusClient.send_heartbeat(HEARTBEAT_MESSAGE);
                    String heartbeat = consensusClient.rec_heartbeat();
                    if (heartbeat == null) {
                        LOG.info("AnnouncePhase: heartbeat message is null");
                        cleanup();
                        data.setStatusType(ConsensusStatusType.ABORT);
                        return;
                    }
                    byte[] receive = this.consensusClient.deque_message();
                    if (receive == null || receive.length <= 0) {
                        LOG.info("AnnouncePhase: Slow Subscriber is detected");
                        Optional<byte[]> res = collector_client.getAnnounceConsensusChunks(ANNOUNCE_MESSAGE);
                        if (res.isEmpty()) {
                            cleanup();
                            LOG.info("AnnouncePhase: Collector Leader is not active fail to send message");
                            data.setStatusType(ConsensusStatusType.ABORT);
                            return;
                        } else
                            receive = res.get();

                        if (receive.length == 1) {
                            cleanup();
                            LOG.info("AnnouncePhase:  Collector Leader fails to send message the correct message");
                            data.setStatusType(ConsensusStatusType.ABORT);
                            return;
                        }
                    }
                    data = consensus_serialize.decode(receive);
                    if (!data.getChecksumData().getBlsPublicKey().toRaw().equals(leader_bls.toRaw())) {
                        cleanup();
                        LOG.info("AnnouncePhase: This is not the valid leader for this round");
                        data.setStatusType(ConsensusStatusType.ABORT);
                        return;
                    } else {
                        byte[] message = data_serialize.encode(data.getData());
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
            data.setChecksumData(new ConsensusMessage.ChecksumData(sig, CachedBLSKeyPair.getInstance().getPublicKey()));

            if (DEBUG)
                return;

            byte[] toSend = consensus_serialize.encode(data);
            consensusClient.pushMessage(toSend);
        }

        @Override
        public void PreparePhase(ConsensusMessage<VDFMessage> data) {
            if (data.getStatusType().equals(ConsensusStatusType.ABORT))
                return;

            if (!DEBUG) {
                try {
                    byte[] receive;
                    int max_iterate = 2;
                    int counter = 0;
                    while (counter < max_iterate) {
                        receive = this.consensusClient.deque_message();
                        if (receive == null || receive.length <= 0) {
                            LOG.info("PreparePhase: Slow Subscriber is detected");
                            Optional<byte[]> res = collector_client.getPrepareConsensusChunks(PREPARE_MESSAGE);
                            if (res.isEmpty()) {
                                cleanup();
                                LOG.info("PreparePhase: Collector Leader is not active fail to send message");
                                data.setStatusType(ConsensusStatusType.ABORT);
                                return;
                            } else {
                                receive = res.get();
                                if (receive.length == 1) {
                                    cleanup();
                                    LOG.info("PreparePhase: Collector Leader fails to send message the correct message");
                                    data.setStatusType(ConsensusStatusType.ABORT);
                                    return;
                                }
                            }
                        }
                        counter++;
                        data = consensus_serialize.decode(receive);
                        if (data.getMessageType().equals(ConsensusMessageType.PREPARE))
                            break;
                    }
                    try {
                        if (!data.getChecksumData().getBlsPublicKey().toRaw().equals(leader_bls.toRaw())) {
                            cleanup();
                            LOG.info("PreparePhase: This is not the valid leader for this round");
                            data.setStatusType(ConsensusStatusType.ABORT);
                            return;
                        } else {
                            byte[] message = data_serialize.encode(data.getData());
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


            byte[] message = data_serialize.encode(data.getData());
            Signature sig = BLSSignature.sign(message, CachedBLSKeyPair.getInstance().getPrivateKey());
            data.setChecksumData(new ConsensusMessage.ChecksumData(sig, CachedBLSKeyPair.getInstance().getPublicKey()));

            if (DEBUG)
                return;

            byte[] toSend = consensus_serialize.encode(data);
            consensusClient.pushMessage(toSend);
        }


        @SneakyThrows
        @Override
        public void CommitPhase(ConsensusMessage<VDFMessage> data) {
            if (data.getStatusType().equals(ConsensusStatusType.ABORT))
                return;


            if (!DEBUG) {
                try {
                    byte[] receive;
                    int max_iterate = 3;
                    int counter = 0;
                    while (counter < max_iterate) {
                        receive = this.consensusClient.deque_message();
                        if (receive == null || receive.length <= 0) {
                            LOG.info("CommitPhase: Slow Subscriber is detected");
                            Optional<byte[]> res = collector_client.getCommitConsensusChunks(COMMITTEE_MESSAGE);
                            if (res.isEmpty()) {
                                cleanup();
                                LOG.info("CommitPhase: Collector Leader is not active fail to send message");
                                data.setStatusType(ConsensusStatusType.ABORT);
                                return;
                            } else {
                                receive = res.get();
                                if (receive.length == 1) {
                                    cleanup();
                                    LOG.info("CommitPhase: Collector Leader fails to send message the correct message");
                                    data.setStatusType(ConsensusStatusType.ABORT);
                                    return;
                                }
                            }
                        }
                        counter++;
                        data = consensus_serialize.decode(receive);
                        if (data.getMessageType().equals(ConsensusMessageType.COMMIT))
                            break;
                    }
                    try {
                        if (!data.getChecksumData().getBlsPublicKey().toRaw().equals(leader_bls.toRaw())) {
                            LOG.info("CommitPhase: This is not the valid leader for this round");
                            data.setStatusType(ConsensusStatusType.ABORT);
                            return;
                        } else {
                            byte[] message = data_serialize.encode(data.getData());
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
            byte[] message = data_serialize.encode(data.getData());
            Bytes toVerify = Bytes.wrap(message);
            boolean verify = BLSSignature.fastAggregateVerify(publicKeys, toVerify, aggregatedSignature);
            if (!verify) {
                cleanup();
                LOG.info("CommitPhase: Abort consensus phase BLS multi_signature is invalid in commit phase");
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            }

            //commit save to db

            if (DEBUG)
                return;


            consensusClient.send_heartbeat(HEARTBEAT_MESSAGE);
            String heartbeat = consensusClient.rec_heartbeat();
            if (heartbeat == null) {
                cleanup();
                LOG.info("CommitPhase: heartbeat message before end is null abort");
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            }

            data.setStatusType(ConsensusStatusType.SUCCESS);
            cleanup();
            Thread.sleep(400);
            CachedSecurityHeaders.getInstance().getSecurityHeader().setRnd(data.getData().getVDFSolution());
            //commit save to db
            //  consensusClient.send_heartbeat(HEARTBEAT_MESSAGE);
            LOG.info("VDF is finalized with Success");
        }

        private void cleanup() {
            if (collector_client != null) {
                collector_client.close();
                collector_client = null;
            }
            if (consensusClient != null) {
                consensusClient.close();
                consensusClient = null;
            }
        }
    }


    protected static class VerifyVRF extends ValidatorConsensusPhases implements VRFConsensusPhase<VRFMessage> {
        private static Logger LOG = LoggerFactory.getLogger(VerifyVRF.class);
        private static final Type fluentType = new TypeToken<ConsensusMessage<VRFMessage>>() {
        }.getType();
        private final VrfEngine2 group;
        private final SerializationUtil<ConsensusMessage> consensus_serialize;
        private final SerializationUtil<VRFMessage> serialize;

        private RpcErasureClient<byte[]> collector_client;

        public VerifyVRF(boolean DEBUG) {
            this.DEBUG = DEBUG;
            this.group = new VrfEngine2();
            this.serialize = new SerializationUtil<VRFMessage>(VRFMessage.class);
            List<SerializationUtil.Mapping> list = new ArrayList<>();
            list.add(new SerializationUtil.Mapping(ECP.class, ctx -> new ECPmapper()));
            list.add(new SerializationUtil.Mapping(ECP2.class, ctx -> new ECP2mapper()));
            this.consensus_serialize = new SerializationUtil<ConsensusMessage>(fluentType, list);
        }

        @Override
        public void InitialSetup() {
            if (!DEBUG) {
                this.current = CachedLeaderIndex.getInstance().getCommitteePositionLeader();
                this.leader_bls = this.blockIndex.getPublicKeyByIndex(0, current);
                String LeaderIP = this.blockIndex.getIpValue(0, this.leader_bls);
                this.consensusClient = new ConsensusClient(LeaderIP);
                this.consensusClient.receive_handler();
                this.collector_client = new RpcErasureClient<byte[]>(LeaderIP, ERASURE_SERVER_PORT, ERASURE_CLIENT_TIMEOUT, CachedEventLoop.getInstance().getEventloop());
                this.collector_client.connect();
            }
        }

        @Override
        public void DispersePhase(ConsensusMessage<VRFMessage> data) throws Exception {
            return;
        }

        @Override
        public void Initialize(VRFMessage message) throws Exception {
            if (!DEBUG) {
                try {
                    consensusClient.send_heartbeat(HEARTBEAT_MESSAGE);
                    String heartbeat = consensusClient.rec_heartbeat();
                    if (heartbeat == null) {
                        CommitteeBlock c = CachedLatestBlocks.getInstance().getCommitteeBlock();
                        cleanup();
                        LOG.info("Initialize: heartbeat message is null");
                        message.setType(VRFMessage.vrfMessageType.ABORT);
                        return;
                    }
                    byte[] receive = this.consensusClient.deque_message();
                    if (receive == null || receive.length <= 0) {
                        LOG.info("Initialize: Slow Subscriber is detected");
                        Optional<byte[]> res = collector_client.getVrfAggregate_chunks(VRF_AGGREGATE_MESSAGE);
                        if (res.isEmpty()) {
                            cleanup();
                            LOG.info("Initialize: Leader is not active fail to send message");
                            message.setType(VRFMessage.vrfMessageType.ABORT);
                            return;
                        } else
                            receive = res.get();

                        if (receive.length == 1) {
                            cleanup();
                            LOG.info("Initialize:  Collector Leader fails to send message the correct message");
                            message.setType(VRFMessage.vrfMessageType.ABORT);
                            return;
                        }
                    }
                    message = serialize.decode(receive);
                } catch (IllegalArgumentException | StringIndexOutOfBoundsException e) {
                    cleanup();
                    LOG.info("Initialize: Problem at message deserialization Abort");
                    message.setType(VRFMessage.vrfMessageType.ABORT);
                    return;
                } catch (ArrayIndexOutOfBoundsException e) {
                    cleanup();
                    LOG.info("Initialize: Receiving out of bounds response from organizer");
                    message.setType(VRFMessage.vrfMessageType.ABORT);
                    return;
                } catch (NullPointerException e) {
                    cleanup();
                    LOG.info("Initialize: Receiving null response from organizer");
                    message.setType(VRFMessage.vrfMessageType.ABORT);
                    return;
                }
            }
            if (!message.getType().equals(VRFMessage.vrfMessageType.INIT) ||
                    !message.getBlockHash().equals(CachedLatestBlocks.getInstance().getCommitteeBlock().getHash())) {
                cleanup();
                LOG.info("Initialize: Organizer not produce valid vrf request");
                message.setType(VRFMessage.vrfMessageType.ABORT);
                return;
            }

            StringBuilder hashToVerify = new StringBuilder();


            hashToVerify.append(CachedLatestBlocks.getInstance().getCommitteeBlock().getHash());
            hashToVerify.append(CachedLatestBlocks.getInstance().getCommitteeBlock().getViewID());

            byte[] ri = group.prove(CachedBLSKeyPair.getInstance().getPrivateKey().toBytes(), hashToVerify.toString().getBytes(StandardCharsets.UTF_8));
            byte[] pi = group.proofToHash(ri);

            VRFMessage.VRFData data = new VRFMessage.VRFData(CachedBLSKeyPair.getInstance().getPublicKey().toBytes(), ri, pi);
            message.setData(data);
            if (DEBUG)
                return;

            byte[] toSend = serialize.encode(message);
            consensusClient.pushMessage(toSend);
        }

        @Override
        public void AggregateVRF(VRFMessage message) throws Exception {

        }


        @Override
        public void AnnouncePhase(ConsensusMessage<VRFMessage> data) throws Exception {
            if (data.getStatusType().equals(ConsensusStatusType.ABORT))
                return;

            if (!DEBUG) {
                try {
                    byte[] receive;
                    int max_iterate = 2;
                    int counter = 0;
                    while (counter < max_iterate) {
                        receive = this.consensusClient.deque_message();
                        if (receive == null || receive.length <= 0) {
                            LOG.info("AnnouncePhase: Slow Subscriber is detected");
                            Optional<byte[]> res = collector_client.getAnnounceConsensusChunks(ANNOUNCE_MESSAGE);
                            if (res.isEmpty()) {
                                cleanup();
                                LOG.info("AnnouncePhase: Collector Leader is not active fail to send message");
                                data.setStatusType(ConsensusStatusType.ABORT);
                                return;
                            } else {
                                receive = res.get();
                                if (receive.length == 1) {
                                    cleanup();
                                    LOG.info("AnnouncePhase: Collector Leader fails to send message the correct message");
                                    data.setStatusType(ConsensusStatusType.ABORT);
                                    return;
                                }
                            }
                        }
                        counter++;
                        data = consensus_serialize.decode(receive);
                        if (data.getMessageType().equals(ConsensusMessageType.ANNOUNCE))
                            break;
                    }
                    try {
                        if (!data.getChecksumData().getBlsPublicKey().toRaw().equals(leader_bls.toRaw())) {
                            cleanup();
                            LOG.info("AnnouncePhase: This is not the valid leader for this round");
                            data.setStatusType(ConsensusStatusType.ABORT);
                            return;
                        } else {
                            byte[] message = serialize.encode(data.getData());
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
                    CachedSecurityHeaders.getInstance().getSecurityHeader().setPRnd(data.getData().getPrnd());
                    break;
                }
                res = ByteUtil.xor(res, list.get(i + 1).getRi());
            }
            byte[] message = serialize.encode(data.getData());
            Signature sig = BLSSignature.sign(message, CachedBLSKeyPair.getInstance().getPrivateKey());
            data.setChecksumData(new ConsensusMessage.ChecksumData(sig, CachedBLSKeyPair.getInstance().getPublicKey()));

            data.setStatusType(ConsensusStatusType.SUCCESS);
            if (DEBUG)
                return;

            byte[] toSend = consensus_serialize.encode(data);
            consensusClient.pushMessage(toSend);
        }

        @Override
        public void PreparePhase(ConsensusMessage<VRFMessage> data) {
            if (data.getStatusType().equals(ConsensusStatusType.ABORT))
                return;


            if (!DEBUG) {
                try {
                    byte[] receive;
                    int max_iterate = 3;
                    int counter = 0;
                    while (counter < max_iterate) {
                        receive = this.consensusClient.deque_message();
                        if (receive == null || receive.length <= 0) {
                            LOG.info("PreparePhase: Slow Subscriber is detected");
                            Optional<byte[]> res = collector_client.getPrepareConsensusChunks(PREPARE_MESSAGE);
                            if (res.isEmpty()) {
                                cleanup();
                                LOG.info("PreparePhase: Collector Leader is not active fail to send message");
                                data.setStatusType(ConsensusStatusType.ABORT);
                                return;
                            } else {
                                receive = res.get();
                                if (receive.length == 1) {
                                    cleanup();
                                    LOG.info("PreparePhase: Collector Leader fails to send message the correct message");
                                    data.setStatusType(ConsensusStatusType.ABORT);
                                    return;
                                }
                            }
                        }
                        counter++;
                        data = consensus_serialize.decode(receive);
                        if (data.getMessageType().equals(ConsensusMessageType.PREPARE)) {
                            break;
                        }
                    }
                    try {
                        if (!data.getChecksumData().getBlsPublicKey().toRaw().equals(leader_bls.toRaw())) {
                            cleanup();
                            LOG.info("PreparePhase: This is not the valid leader for this round");
                            data.setStatusType(ConsensusStatusType.ABORT);
                            return;
                        } else {
                            byte[] message = serialize.encode(data.getData());
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
            Bytes toVerify = Bytes.wrap(serialize.encode(data.getData()));
            boolean verify = BLSSignature.fastAggregateVerify(publicKeys, toVerify, aggregatedSignature);
            if (!verify) {
                cleanup();
                LOG.info("PreparePhase: Abort consensus phase BLS multi_signature is invalid in prepare phase");
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            }

            // data.clear();
            data.setStatusType(ConsensusStatusType.SUCCESS);


            byte[] message = serialize.encode(data.getData());
            Signature sig = BLSSignature.sign(message, CachedBLSKeyPair.getInstance().getPrivateKey());
            data.setChecksumData(new ConsensusMessage.ChecksumData(sig, CachedBLSKeyPair.getInstance().getPublicKey()));


            if (DEBUG)
                return;


            byte[] toSend = consensus_serialize.encode(data);
            consensusClient.pushMessage(toSend);
        }

        @SneakyThrows
        @Override
        public void CommitPhase(ConsensusMessage<VRFMessage> data) {
            if (data.getStatusType().equals(ConsensusStatusType.ABORT))
                return;

            if (!DEBUG) {
                try {
                    byte[] receive;
                    int max_iterate = 2;
                    int counter = 0;
                    while (counter < max_iterate) {
                        receive = this.consensusClient.deque_message();
                        if (receive == null || receive.length <= 0 || counter == 1) {
                            LOG.info("CommitPhase: Slow Subscriber is detected");
                            Optional<byte[]> res = collector_client.getCommitConsensusChunks(COMMITTEE_MESSAGE);
                            if (res.isEmpty()) {
                                cleanup();
                                LOG.info("CommitPhase: Collector Leader is not active fail to send message");
                                data.setStatusType(ConsensusStatusType.ABORT);
                                return;
                            } else {
                                receive = res.get();
                                if (receive.length == 1) {
                                    cleanup();
                                    LOG.info("CommitPhase: Collector Leader fails to send message the correct message");
                                    data.setStatusType(ConsensusStatusType.ABORT);
                                    return;
                                }
                            }
                        }
                        counter++;
                        data = consensus_serialize.decode(receive);
                        if (data.getMessageType().equals(ConsensusMessageType.COMMIT))
                            break;
                    }
                    try {
                        if (!data.getChecksumData().getBlsPublicKey().toRaw().equals(leader_bls.toRaw())) {
                            cleanup();
                            LOG.info("CommitPhase: This is not the valid leader for this round");
                            data.setStatusType(ConsensusStatusType.ABORT);
                            return;
                        } else {
                            byte[] message = serialize.encode(data.getData());
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
            byte[] message = serialize.encode(data.getData());
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

            if (DEBUG)
                return;


            CachedSecurityHeaders.getInstance().getSecurityHeader().setPRnd(data.getData().getPrnd());
            //commit save to db

            // consensusClient.send_heartbeat(HEARTBEAT_MESSAGE);
            LOG.info("VRF is finalized with Success");
            cleanup();
            Thread.sleep(100);
        }

        private void cleanup() {
            if (collector_client != null) {
                collector_client.close();
                collector_client = null;
            }
            if (consensusClient != null) {
                consensusClient.close();
                consensusClient = null;
            }
        }
    }


    protected static class VerifyTransactionBlock extends ValidatorConsensusPhases implements BFTConsensusPhase<TransactionBlock> {
        private static Logger LOG = LoggerFactory.getLogger(VerifyTransactionBlock.class);
        private static final Type fluentType = new TypeToken<ConsensusMessage<TransactionBlock>>() {
        }.getType();
        private static final String delimeter = "||";
        private final SerializationUtil<AbstractBlock> block_serialize;
        private final SerializationUtil<ConsensusMessage> consensus_serialize;
        private final SerializationUtil<Signature> signatureMapper;
        private final SerializationUtil<SerializableErasureObject> serenc_erasure;
        private final DefaultFactory factory;
        private final BlockSizeCalculator sizeCalculator;
        private final String[] Shake256Hash;
        private final List<byte[]> prevAgreegation;

        private RpcErasureClient<byte[]> collector_client;

        private TransactionBlock original_copy;

        private String erasure_data;

        public VerifyTransactionBlock(boolean DEBUG) {
            this.DEBUG = DEBUG;
            this.factory = new DefaultFactory();
            List<SerializationUtil.Mapping> list = new ArrayList<>();
            list.add(new SerializationUtil.Mapping(ECP.class, ctx -> new ECPmapper()));
            list.add(new SerializationUtil.Mapping(ECP2.class, ctx -> new ECP2mapper()));
            list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
            list.add(new SerializationUtil.Mapping(TreeMap.class, ctx -> new CustomSerializerTreeMap()));
            this.block_serialize = new SerializationUtil<AbstractBlock>(AbstractBlock.class, list);
            this.consensus_serialize = new SerializationUtil<ConsensusMessage>(fluentType, list);
            this.sizeCalculator = new BlockSizeCalculator();
            this.signatureMapper = new SerializationUtil<Signature>(Signature.class, list);
            this.serenc_erasure = new SerializationUtil<SerializableErasureObject>(SerializableErasureObject.class);
            this.Shake256Hash = new String[2];
            this.prevAgreegation = new ArrayList<>(2);
            ErasureServerInstance.getInstance();
        }

        @Override
        public void InitialSetup() {
            if (!DEBUG) {
                String clientID = retrieveIdentity();
                this.current = CachedLeaderIndex.getInstance().getTransactionPositionLeader();
                if (current == CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(CachedZoneIndex.getInstance().getZoneIndex()).size() - 1) {
                    this.leader_bls = this.blockIndex.getPublicKeyByIndex(CachedZoneIndex.getInstance().getZoneIndex(), this.current);
                    String LeaderIP = this.blockIndex.getIpValue(CachedZoneIndex.getInstance().getZoneIndex(), this.leader_bls);
                    this.consensusClient = new ConsensusClient(LeaderIP, clientID);
                    CachedLeaderIndex.getInstance().setTransactionPositionLeader(0);
                    this.collector_client = new RpcErasureClient<byte[]>(LeaderIP, ERASURE_SERVER_PORT, ERASURE_CLIENT_TIMEOUT, CachedEventLoop.getInstance().getEventloop());
                } else {
                    this.leader_bls = this.blockIndex.getPublicKeyByIndex(CachedZoneIndex.getInstance().getZoneIndex(), this.current);
                    String LeaderIP = this.blockIndex.getIpValue(CachedZoneIndex.getInstance().getZoneIndex(), this.leader_bls);
                    this.consensusClient = new ConsensusClient(LeaderIP, clientID);
                    CachedLeaderIndex.getInstance().setTransactionPositionLeader(current + 1);
                    this.collector_client = new RpcErasureClient<byte[]>(LeaderIP, ERASURE_SERVER_PORT, ERASURE_CLIENT_TIMEOUT, CachedEventLoop.getInstance().getEventloop());
                }
            }
        }

        private String retrieveIdentity() {
            List<String> list = new ArrayList<>();
            StringJoiner joiner = new StringJoiner(delimeter);
            String timeStampInString = GetTime.GetTimeStampInString();
            String pubkey = Hex.encodeHexString(CachedBLSKeyPair.getInstance().getPublicKey().toBytes());

            String toSign = joiner.add(pubkey).add(timeStampInString).toString();
            Signature bls_sig = BLSSignature.sign(toSign.getBytes(StandardCharsets.UTF_8), CachedBLSKeyPair.getInstance().getPrivateKey());
            String sig = Hex.encodeHexString(signatureMapper.encode(bls_sig));

            list.add(pubkey);
            list.add(timeStampInString);
            list.add(sig);
            erasure_data = String.join(delimeter, list);
            return toSign;
        }

        @Override
        public void DispersePhase(ConsensusMessage<TransactionBlock> data) throws Exception {
            long Dispersestart = System.currentTimeMillis();
            long Dispersestartstarta = System.currentTimeMillis();
            if (!DEBUG) {
                try {
                    long Dispersefinisha = System.currentTimeMillis();
                    long DispersetimeElapseda = Dispersefinisha - Dispersestartstarta;
                    System.out.println("Validator Dispersea: " + DispersetimeElapseda);
                    long Dispersestartstartb = System.currentTimeMillis();
                    byte[] rec_buff = consensusClient.SendRetrieveErasureData(erasure_data.getBytes(StandardCharsets.UTF_8));
                    if (rec_buff == null) {
                        cleanup();
                        LOG.info("DispersePhase: Received null from server abort");
                        data.setStatusType(ConsensusStatusType.ABORT);
                        return;
                    }
                    long Dispersefinishb = System.currentTimeMillis();
                    long DispersetimeElapsedb = Dispersefinishb - Dispersestartstartb;
                    System.out.println("Validator Disperseb: " + DispersetimeElapsedb);
                    long Dispersestartstartc = System.currentTimeMillis();
                    SerializableErasureObject rootObj = serenc_erasure.decode(rec_buff);
                    CachedSerializableErasureObject.getInstance().setSerializableErasureObject(rootObj);
                    ErasureServerInstance.getInstance().getServer().setSerializable_length(rec_buff.length);
                    List<String> ips = CachedLatestBlocks.getInstance()
                            .getCommitteeBlock()
                            .getStructureMap()
                            .get(CachedZoneIndex.getInstance().getZoneIndex()).values()
                            .stream()
                            .filter(val -> !val.equals(this.blockIndex.getIpValue(CachedZoneIndex.getInstance().getZoneIndex(), CachedBLSKeyPair.getInstance().getPublicKey())) && !val.equals(this.blockIndex.getIpValue(CachedZoneIndex.getInstance().getZoneIndex(), this.leader_bls)))
                            .collect(Collectors.toList());

                    ArrayList<InetSocketAddress> list_ip = new ArrayList<>();
                    for (String ip : ips) {
                        InetSocketAddress address = new InetSocketAddress(InetAddress.getByName(ip), ERASURE_SERVER_PORT);
                        list_ip.add(address);
                    }

                    ArrayList<SerializableErasureObject> recserializableErasureObjects = new ArrayList<>();
                    long Dispersefinishc = System.currentTimeMillis();
                    long DispersetimeElapsedc = Dispersefinishc - Dispersestartstartc;
                    System.out.println("Validator Dispersec: " + DispersetimeElapsedc);
                    if (list_ip.isEmpty()) {
                        LOG.info("DispersePhase: Erasure connect list is empty");
                    } else {
                        RpcErasureClient<SerializableErasureObject> client = new RpcErasureClient<SerializableErasureObject>(new SerializableErasureObject(), list_ip, ERASURE_SERVER_PORT, CachedEventLoop.getInstance().getEventloop());
                        client.connect();
                        recserializableErasureObjects = (ArrayList<SerializableErasureObject>) client.getErasureChunks(new byte[0]);
                        client.close();

                        for (SerializableErasureObject obj : recserializableErasureObjects) {
                            if (!obj.CheckChunksValidity(rootObj.getRootMerkleHash())) {
                                LOG.info("Merkle Hash is not valid");
                                recserializableErasureObjects.remove(obj);
                            }
                        }

                        if (recserializableErasureObjects.isEmpty()) {
                            cleanup();
                            LOG.info("DispersePhase: Received null or incorrect chunks from validators abort");
                            data.setStatusType(ConsensusStatusType.ABORT);
                            return;
                        }
                    }
                    recserializableErasureObjects.add(rootObj);

                    FECParameterObject recobject = recserializableErasureObjects.get(0).getFecParameterObject();
                    FECParameters recfecParams = FECParameters.newParameters(recobject.getDataLen(), recobject.getSymbolSize(), recobject.getNumberOfSymbols());
                    final ArrayDataDecoder dec = OpenRQ.newDecoder(recfecParams, recobject.getSymbolOverhead());

                    for (int i = 0; i < recserializableErasureObjects.size(); i++) {
                        EncodingPacket encodingPacket = dec.parsePacket(ByteBuffer.wrap(recserializableErasureObjects.get(i).getOriginalPacketChunks()), false).value();
                        final SourceBlockDecoder sbDec = dec.sourceBlock(encodingPacket.sourceBlockNumber());
                        sbDec.putEncodingPacket(encodingPacket);
                    }

                    try {
                        this.original_copy = (TransactionBlock) block_serialize.decode(dec.dataArray());
                    } catch (Exception e) {
                        cleanup();
                        LOG.info("DispersePhase: Disperse failed cannot decode block need more chunks abort");
                        data.setStatusType(ConsensusStatusType.ABORT);
                        return;
                    }


                } catch (IllegalArgumentException | StringIndexOutOfBoundsException e) {
                    cleanup();
                    LOG.info("DispersePhase: Problem at message deserialization Abort");
                    data.setStatusType(ConsensusStatusType.ABORT);
                    return;
                } catch (ArrayIndexOutOfBoundsException e) {
                    cleanup();
                    LOG.info("DispersePhase: Out of bounds response from organizer");
                    data.setStatusType(ConsensusStatusType.ABORT);
                    return;
                } catch (NullPointerException e) {
                    cleanup();
                    LOG.info("DispersePhase: Receiving null response from organizer");
                    data.setStatusType(ConsensusStatusType.ABORT);
                    return;
                }
            } else
                this.original_copy = (TransactionBlock) data.getData().clone();

            data.setStatusType(ConsensusStatusType.SUCCESS);
            long Dispersefinish = System.currentTimeMillis();
            long DispersetimeElapsed = Dispersefinish - Dispersestart;
            System.out.println("Validator Disperse: " + DispersetimeElapsed);
        }

        @Override
        public void AnnouncePhase(ConsensusMessage<TransactionBlock> data) throws InterruptedException {
            long Announcestart = System.currentTimeMillis();
            if (data.getStatusType().equals(ConsensusStatusType.ABORT))
                return;

            byte[] bytesBlock = null;
            if (!DEBUG) {
                try {
//                    this.consensusClient.send_heartbeat(ConsensusConfiguration.HEARTBEAT_MESSAGE);
//                    String heartbeat = consensusClient.rec_heartbeat();
//                    if (heartbeat == null) {
//                        cleanup();
//                        LOG.info("AnnouncePhase: Heartbeat message is null");
//                        data.setStatusType(ConsensusStatusType.ABORT);
//                        return;
//                    }
                    this.consensusClient.receive_handler();
                    this.collector_client.connect();
                    byte[] receive = this.consensusClient.deque_message();
                    if (receive == null || receive.length <= 0) {
                        LOG.info("AnnouncePhase: Slow Subscriber is detected");
                        Optional<byte[]> res = collector_client.getAnnounceConsensusChunks(ANNOUNCE_MESSAGE);
                        if (res.isEmpty()) {
                            cleanup();
                            LOG.info("AnnouncePhase: Collector Leader is not active fail to send message");
                            data.setStatusType(ConsensusStatusType.ABORT);
                            return;
                        } else
                            receive = res.get();

                        if (receive.length == 1) {
                            cleanup();
                            LOG.info("AnnouncePhase:  Collector Leader fails to send message the correct message");
                            data.setStatusType(ConsensusStatusType.ABORT);
                            return;
                        }
                    }
                    data = consensus_serialize.decode(receive);
                    if (!data.getChecksumData().getBlsPublicKey().toRaw().equals(leader_bls.toRaw())) {
                        cleanup();
                        LOG.info("AnnouncePhase: This is not the valid leader for this round");
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
                    LOG.info("AnnouncePhase: Out of bounds response from organizer");
                    data.setStatusType(ConsensusStatusType.ABORT);
                    return;
                } catch (NullPointerException e) {
                    cleanup();
                    LOG.info("AnnouncePhase: Receiving null response from organizer");
                    data.setStatusType(ConsensusStatusType.ABORT);
                    return;
                }
            }
            if (!data.getMessageType().equals(ConsensusMessageType.ANNOUNCE)) {
                cleanup();
                LOG.info("AnnouncePhase: Organizer not send correct header message expected " + ConsensusMessageType.ANNOUNCE);
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            }


            if (!DEBUG) {
                long Criticalstart = System.currentTimeMillis();
                CachedTransactionBlockEventPublisher.getInstance().publish(this.original_copy);
                CachedTransactionBlockEventPublisher.getInstance().WaitUntilRemainingCapacityZero();
                long Criticalfinish = System.currentTimeMillis();
                long CriticaltimeElapsed = Criticalfinish - Criticalstart;
                System.out.println("CriticaltimeElapsed " + CriticaltimeElapsed);
            }

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
            BLSLeaderSignatureData.getSignature()[0] = new Signature(data.getChecksumData().getSignature().getPoint());
            BLSLeaderSignatureData.getMessageHash()[0] = BLSSignature.GetMessageHashAsBase64String(bytesBlock);
            this.original_copy.getSignatureData().put(data.getChecksumData().getBlsPublicKey(), BLSLeaderSignatureData);

            this.sizeCalculator.setTransactionBlock(original_copy);
            byte[] message = block_serialize.encode(original_copy, this.sizeCalculator.TransactionBlockSizeCalculator());
            this.Shake256Hash[0] = BLSSignature.GetMessageHashAsBase64String(message);
            this.prevAgreegation.add(0, message);
            Signature sig = BLSSignature.sign(message, CachedBLSKeyPair.getInstance().getPrivateKey());
            data.setChecksumData(new ConsensusMessage.ChecksumData(sig, CachedBLSKeyPair.getInstance().getPublicKey()));

            if (DEBUG)
                return;

            byte[] toSend = consensus_serialize.encode(data);
            consensusClient.pushMessage(toSend);

            long Announcefinish = System.currentTimeMillis();
            long timeElapsed = Announcefinish - Announcestart;
            System.out.println("Announce phase " + timeElapsed);

        }

        @Override
        public void PreparePhase(ConsensusMessage<TransactionBlock> data) throws InterruptedException {
            long Preparestart = System.currentTimeMillis();
            if (data.getStatusType().equals(ConsensusStatusType.ABORT))
                return;

            byte[] bytesBlock = null;
            if (!DEBUG) {
                try {
                    byte[] receive;
                    int max_iterate = 2;
                    int counter = 0;
                    while (counter < max_iterate) {
                        receive = this.consensusClient.deque_message();
                        if (receive == null || receive.length <= 0) {
                            LOG.info("PreparePhase: Slow Subscriber is detected");
                            Optional<byte[]> res = collector_client.getPrepareConsensusChunks(PREPARE_MESSAGE);
                            if (res.isEmpty()) {
                                cleanup();
                                LOG.info("PreparePhase: Collector Leader is not active fail to send message");
                                data.setStatusType(ConsensusStatusType.ABORT);
                                return;
                            } else {
                                receive = res.get();
                                if (receive.length == 1) {
                                    cleanup();
                                    LOG.info("PreparePhase: Collector Leader fails to send message the correct message");
                                    data.setStatusType(ConsensusStatusType.ABORT);
                                    return;
                                }
                            }
                        }
                        counter++;
                        data = consensus_serialize.decode(receive);
                        if (data.getMessageType().equals(ConsensusMessageType.PREPARE))
                            break;
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
                BLSSignatureData.getSignature()[0] = new Signature(entry.getValue().getSignature()[0].getPoint());
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
            BLSLeaderSignatureData.getSignature()[1] = new Signature(data.getChecksumData().getSignature().getPoint());
            BLSLeaderSignatureData.getMessageHash()[1] = BLSSignature.GetMessageHashAsBase64String(bytesBlock);
            this.original_copy.getSignatureData().put(data.getChecksumData().getBlsPublicKey(), BLSLeaderSignatureData);

            // data.clear();
            data.setStatusType(ConsensusStatusType.SUCCESS);


            this.sizeCalculator.setTransactionBlock(original_copy);
            byte[] message = block_serialize.encode(original_copy, this.sizeCalculator.TransactionBlockSizeCalculator());
            data.setHash(HashUtil.sha256_bytetoString(message));
            Shake256Hash[1] = BLSSignature.GetMessageHashAsBase64String(message);
            this.prevAgreegation.add(1, message);
            Signature sig = BLSSignature.sign(message, CachedBLSKeyPair.getInstance().getPrivateKey());
            data.setChecksumData(new ConsensusMessage.ChecksumData(sig, CachedBLSKeyPair.getInstance().getPublicKey()));

            if (DEBUG)
                return;


            byte[] toSend = consensus_serialize.encode(data);
            consensusClient.pushMessage(toSend);

            long Preparefinish = System.currentTimeMillis();
            long PreparetimeElapsed = Preparefinish - Preparestart;
            System.out.println("Validators Prepare " + PreparetimeElapsed);
        }

        @Override
        public void CommitPhase(ConsensusMessage<TransactionBlock> data) throws InterruptedException {
            long Commiteerestart = System.currentTimeMillis();
            if (data.getStatusType().equals(ConsensusStatusType.ABORT))
                return;

            byte[] bytesBlock = null;
            if (!DEBUG) {
                try {
                    byte[] receive;
                    int max_iterate = 3;
                    int counter = 0;
                    while (counter < max_iterate) {
                        receive = this.consensusClient.deque_message();
                        if (receive == null || receive.length <= 0) {
                            LOG.info("CommitPhase: Slow Subscriber is detected");
                            Optional<byte[]> res = collector_client.getCommitConsensusChunks(COMMITTEE_MESSAGE);
                            if (res.isEmpty()) {
                                cleanup();
                                LOG.info("CommitPhase: Collector Leader is not active fail to send message");
                                data.setStatusType(ConsensusStatusType.ABORT);
                                return;
                            } else {
                                receive = res.get();
                                if (receive.length == 1) {
                                    cleanup();
                                    LOG.info("CommitPhase: Collector Leader fails to send message the correct message");
                                    data.setStatusType(ConsensusStatusType.ABORT);
                                    return;
                                }
                            }
                        }
                        counter++;
                        data = consensus_serialize.decode(receive);
                        if (data.getMessageType().equals(ConsensusMessageType.COMMIT))
                            break;

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
            }

            if (!data.getMessageType().equals(ConsensusMessageType.COMMIT)) {
                cleanup();
                LOG.info("CommitPhase: Organizer not send correct header message expected " + ConsensusMessageType.COMMIT);
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            }

            String messageHashAsBase64String = Shake256Hash[1];
            for (Map.Entry<BLSPublicKey, BLSSignatureData> entry : data.getSignatures().entrySet()) {
                BLSSignatureData BLSSignatureData = new BLSSignatureData();
                BLSSignatureData.getSignature()[1] = new Signature(entry.getValue().getSignature()[1].getPoint());
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


            if (DEBUG)
                return;


            //##############################################################
           /* if (current == CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(1).size() - 1)
                data.getData().setLeaderPublicKey(this.blockIndex.getPublicKeyByIndex(CachedZoneIndex.getInstance().getZoneIndex(), 0));
            else {
                data.getData().setLeaderPublicKey(this.blockIndex.getPublicKeyByIndex(CachedZoneIndex.getInstance().getZoneIndex(), current + 1));
            }*/
            //CachedLatestBlocks.getInstance().setTransactionBlock(data.getData());
            //commit save to db
            BlockInvent regural_block = (BlockInvent) factory.getBlock(BlockType.REGULAR);
            regural_block.InventTransactionBlock(this.original_copy);
            BLSPublicKey next_key = blockIndex.getPublicKeyByIndex(CachedZoneIndex.getInstance().getZoneIndex(), CachedLeaderIndex.getInstance().getTransactionPositionLeader());
            CachedLatestBlocks.getInstance().getTransactionBlock().setBlockProposer(next_key.toRaw());
            CachedLatestBlocks.getInstance().getTransactionBlock().setLeaderPublicKey(next_key);
            // consensusClient.send_heartbeat(HEARTBEAT_MESSAGE);
            CachedSerializableErasureObject.getInstance().setSerializableErasureObject(null);
            long Commiteefinish = System.currentTimeMillis();
            long timeElapsed = Commiteefinish - Commiteerestart;
            System.out.println("Calidators commite " + timeElapsed);
            LOG.info("Block is finalized with Success");
            cleanup();
            LOG.info("12");
        }

        private void cleanup() {
            if (collector_client != null) {
                collector_client.close();
                collector_client = null;
            }
            if (consensusClient != null) {
                consensusClient.close();
                consensusClient = null;
            }
        }
    }

    protected static class VerifyCommitteeBlock extends ValidatorConsensusPhases implements BFTConsensusPhase<CommitteeBlock> {

        private static Logger LOG = LoggerFactory.getLogger(VerifyCommitteeBlock.class);
        private static final Type fluentType = new TypeToken<ConsensusMessage<CommitteeBlock>>() {
        }.getType();
        private final SerializationUtil<AbstractBlock> block_serialize;
        private final SerializationUtil<ConsensusMessage> consensus_serialize;
        private final DefaultFactory factory;

        private final BlockSizeCalculator sizeCalculator;

        private RpcErasureClient<byte[]> collector_client;

        public VerifyCommitteeBlock(boolean DEBUG) {
            this.DEBUG = DEBUG;
            this.factory = new DefaultFactory();
            List<SerializationUtil.Mapping> list = new ArrayList<>();
            list.add(new SerializationUtil.Mapping(ECP.class, ctx -> new ECPmapper()));
            list.add(new SerializationUtil.Mapping(ECP2.class, ctx -> new ECP2mapper()));
            list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
            list.add(new SerializationUtil.Mapping(TreeMap.class, ctx -> new CustomSerializerTreeMap()));
            this.block_serialize = new SerializationUtil<AbstractBlock>(AbstractBlock.class, list);
            this.consensus_serialize = new SerializationUtil<ConsensusMessage>(fluentType, list);
            this.sizeCalculator = new BlockSizeCalculator();
        }

        @Override
        public void InitialSetup() {
            if (!DEBUG) {
                this.current = CachedLeaderIndex.getInstance().getCommitteePositionLeader();
                this.leader_bls = this.blockIndex.getPublicKeyByIndex(0, current);
                String LeaderIP = this.blockIndex.getIpValue(0, this.leader_bls);
                this.consensusClient = new ConsensusClient(LeaderIP, 0);
                this.consensusClient.receive_handler();
                this.collector_client = new RpcErasureClient<byte[]>(LeaderIP, ERASURE_SERVER_PORT, COMMITTEE_ERASURE_CLIENT_TIMEOUT, CachedEventLoop.getInstance().getEventloop());
            }
        }

        @Override
        public void DispersePhase(ConsensusMessage<CommitteeBlock> data) throws Exception {
            return;
        }


        @SneakyThrows
        @Override
        public void AnnouncePhase(ConsensusMessage<CommitteeBlock> block) {
            if (!DEBUG) {
                try {
                    consensusClient.send_heartbeat(HEARTBEAT_MESSAGE);
                    String heartbeat = consensusClient.rec_heartbeat();
                    if (heartbeat == null) {
                        cleanup();
                        LOG.info("AnnouncePhase: heartbeat message is null");
                        block.setStatusType(ConsensusStatusType.ABORT);
                        return;
                    }
                    this.collector_client.connect();
                    byte[] receive = this.consensusClient.deque_message();
                    if (receive == null || receive.length <= 0) {
                        LOG.info("AnnouncePhase: Slow Subscriber is detected");
                        Optional<byte[]> res = collector_client.getAnnounceConsensusChunks(ANNOUNCE_MESSAGE);
                        if (res.isEmpty()) {
                            cleanup();
                            LOG.info("AnnouncePhase: Collector Leader is not active fail to send message");
                            block.setStatusType(ConsensusStatusType.ABORT);
                            return;
                        } else
                            receive = res.get();

                        if (receive.length == 1) {
                            cleanup();
                            LOG.info("AnnouncePhase:  Collector Leader fails to send message the correct message");
                            block.setStatusType(ConsensusStatusType.ABORT);
                            return;
                        }
                    }
                    block = consensus_serialize.decode(receive);
                    if (!block.getChecksumData().getBlsPublicKey().toRaw().equals(leader_bls.toRaw())) {
                        cleanup();
                        LOG.info("AnnouncePhase: This is not the valid leader for this round");
                        block.setStatusType(ConsensusStatusType.ABORT);
                        return;
                    } else {
                        this.sizeCalculator.setCommitteeBlock(block.getData());
                        byte[] message = block_serialize.encode(block.getData(), this.sizeCalculator.CommitteeBlockSizeCalculator());
                        boolean verify = BLSSignature.verify(block.getChecksumData().getSignature(), message, block.getChecksumData().getBlsPublicKey());
                        if (!verify) {
                            cleanup();
                            LOG.info("AnnouncePhase: Abort consensus phase BLS leader signature is invalid during announce phase");
                            block.setStatusType(ConsensusStatusType.ABORT);
                            return;
                        }
                    }
                } catch (IllegalArgumentException | StringIndexOutOfBoundsException e) {
                    cleanup();
                    LOG.info("AnnouncePhase: Problem at message deserialization Abort");
                    block.setStatusType(ConsensusStatusType.ABORT);
                    return;
                } catch (ArrayIndexOutOfBoundsException e) {
                    cleanup();
                    LOG.info("AnnouncePhase: Receiving out of bounds response from organizer");
                    block.setStatusType(ConsensusStatusType.ABORT);
                    return;
                } catch (NullPointerException e) {
                    cleanup();
                    LOG.info("AnnouncePhase: Receiving null response from organizer");
                    block.setStatusType(ConsensusStatusType.ABORT);
                    return;
                }
            }
            if (!block.getMessageType().equals(ConsensusMessageType.ANNOUNCE)) {
                LOG.info("AnnouncePhase: Organizer not send correct header message expected " + ConsensusMessageType.ANNOUNCE);
                cleanup();
                block.setStatusType(ConsensusStatusType.ABORT);
                return;
            }


            CachedCommitteeBlockEventPublisher.getInstance().publish(block.getData());
            CachedCommitteeBlockEventPublisher.getInstance().WaitUntilRemainingCapacityZero();
            System.out.println("Done");
            if (block.getData().getStatustype().equals(StatusType.ABORT)) {
                cleanup();
                LOG.info("AnnouncePhase: Block is not valid marked as ABORT");
                return;
            }
            block.setStatusType(ConsensusStatusType.SUCCESS);
            this.sizeCalculator.setCommitteeBlock(block.getData());
            Signature sig = BLSSignature.sign(block_serialize.encode(block.getData(), this.sizeCalculator.CommitteeBlockSizeCalculator()), CachedBLSKeyPair.getInstance().getPrivateKey());
            block.setChecksumData(new ConsensusMessage.ChecksumData(sig, CachedBLSKeyPair.getInstance().getPublicKey()));

            if (DEBUG)
                return;

            byte[] toSend = consensus_serialize.encode(block);
            consensusClient.pushMessage(toSend);
        }

        @Override
        public void PreparePhase(ConsensusMessage<CommitteeBlock> block) {
            if (block.getStatusType().equals(ConsensusStatusType.ABORT))
                return;

            if (!DEBUG) {
                try {
                    byte[] receive;
                    int max_iterate = 2;
                    int counter = 0;
                    while (counter < max_iterate) {
                        receive = this.consensusClient.deque_message();
                        if (receive == null || receive.length <= 0) {
                            LOG.info("PreparePhase: Slow Subscriber is detected");
                            Optional<byte[]> res = collector_client.getPrepareConsensusChunks(PREPARE_MESSAGE);
                            if (res.isEmpty()) {
                                cleanup();
                                LOG.info("PreparePhase: Collector Leader is not active fail to send message");
                                block.setStatusType(ConsensusStatusType.ABORT);
                                return;
                            } else {
                                receive = res.get();
                                if (receive.length == 1) {
                                    cleanup();
                                    LOG.info("PreparePhase: Collector Leader fails to send message the correct message");
                                    block.setStatusType(ConsensusStatusType.ABORT);
                                    return;
                                }
                            }
                        }
                        counter++;
                        block = consensus_serialize.decode(receive);
                        if (block.getMessageType().equals(ConsensusMessageType.PREPARE))
                            break;
                    }
                    if (!block.getChecksumData().getBlsPublicKey().toRaw().equals(leader_bls.toRaw())) {
                        cleanup();
                        LOG.info("PreparePhase: This is not the valid leader for this round");
                        block.setStatusType(ConsensusStatusType.ABORT);
                        return;
                    } else {
                        this.sizeCalculator.setCommitteeBlock(block.getData());
                        byte[] message = block_serialize.encode(block.getData(), this.sizeCalculator.CommitteeBlockSizeCalculator());
                        boolean verify = BLSSignature.verify(block.getChecksumData().getSignature(), message, block.getChecksumData().getBlsPublicKey());
                        if (!verify) {
                            LOG.info("PreparePhase: Abort consensus phase BLS leader signature is invalid during prepare phase");
                            cleanup();
                            block.setStatusType(ConsensusStatusType.ABORT);
                            return;
                        }
                    }
                } catch (IllegalArgumentException | StringIndexOutOfBoundsException e) {
                    cleanup();
                    LOG.info("PreparePhase: Problem at message deserialization Abort");
                    block.setStatusType(ConsensusStatusType.ABORT);
                    return;
                } catch (ArrayIndexOutOfBoundsException e) {
                    cleanup();
                    LOG.info("PreparePhase: Receiving out of bounds response from organizer");
                    block.setStatusType(ConsensusStatusType.ABORT);
                    return;
                } catch (NullPointerException e) {
                    cleanup();
                    LOG.info("PreparePhase: Receiving null response from organizer");
                    block.setStatusType(ConsensusStatusType.ABORT);
                    return;
                }
            }

            if (!block.getMessageType().equals(ConsensusMessageType.PREPARE)) {
                LOG.info("PreparePhase: Organizer not send correct header message expected " + ConsensusMessageType.PREPARE);
                cleanup();
                block.setStatusType(ConsensusStatusType.ABORT);
                return;
            }
            List<BLSPublicKey> publicKeys = new ArrayList<>(block.getSignatures().keySet());
            List<Signature> signature = block.getSignatures().values().stream().map(BLSSignatureData::getSignature).map(r -> r[0]).collect(Collectors.toList());


            Signature aggregatedSignature = BLSSignature.aggregate(signature);
            this.sizeCalculator.setCommitteeBlock(block.getData());
            Bytes toVerify = Bytes.wrap(block_serialize.encode(block.getData(), this.sizeCalculator.CommitteeBlockSizeCalculator()));
            boolean verify = BLSSignature.fastAggregateVerify(publicKeys, toVerify, aggregatedSignature);
            if (!verify) {
                cleanup();
                LOG.info("PreparePhase: Abort consensus phase BLS multi_signature is invalid in prepare phase");
                block.setStatusType(ConsensusStatusType.ABORT);
                return;
            }

            // data.clear();
            block.setStatusType(ConsensusStatusType.SUCCESS);


            this.sizeCalculator.setCommitteeBlock(block.getData());
            Signature sig = BLSSignature.sign(block_serialize.encode(block.getData(), this.sizeCalculator.CommitteeBlockSizeCalculator()), CachedBLSKeyPair.getInstance().getPrivateKey());
            block.setChecksumData(new ConsensusMessage.ChecksumData(sig, CachedBLSKeyPair.getInstance().getPublicKey()));

            if (DEBUG)
                return;

            byte[] toSend = consensus_serialize.encode(block);
            consensusClient.pushMessage(toSend);
        }

        @SneakyThrows
        @Override
        public void CommitPhase(ConsensusMessage<CommitteeBlock> block) {
            if (block.getStatusType().equals(ConsensusStatusType.ABORT))
                return;

            if (!DEBUG) {
                try {
                    byte[] receive;
                    int max_iterate = 3;
                    int counter = 0;
                    while (counter < max_iterate) {
                        receive = this.consensusClient.deque_message();
                        if (receive == null || receive.length <= 0) {
                            LOG.info("CommitPhase: Slow Subscriber is detected");
                            Optional<byte[]> res = collector_client.getCommitConsensusChunks(COMMITTEE_MESSAGE);
                            if (res.isEmpty()) {
                                cleanup();
                                LOG.info("CommitPhase: Collector Leader is not active fail to send message");
                                block.setStatusType(ConsensusStatusType.ABORT);
                                return;
                            } else {
                                receive = res.get();
                                if (receive.length == 1) {
                                    cleanup();
                                    LOG.info("CommitPhase: Collector Leader fails to send message the correct message");
                                    block.setStatusType(ConsensusStatusType.ABORT);
                                    return;
                                }
                            }
                        }
                        counter++;
                        block = consensus_serialize.decode(receive);
                        if (block.getMessageType().equals(ConsensusMessageType.COMMIT))
                            break;
                    }
                    if (!block.getChecksumData().getBlsPublicKey().toRaw().equals(leader_bls.toRaw())) {
                        cleanup();
                        LOG.info("CommitPhase: This is not the valid leader for this round");
                        block.setStatusType(ConsensusStatusType.ABORT);
                        return;
                    } else {
                        this.sizeCalculator.setCommitteeBlock(block.getData());
                        byte[] message = block_serialize.encode(block.getData(), this.sizeCalculator.CommitteeBlockSizeCalculator());
                        boolean verify = BLSSignature.verify(block.getChecksumData().getSignature(), message, block.getChecksumData().getBlsPublicKey());
                        if (!verify) {
                            LOG.info("CommitPhase: Abort consensus phase BLS leader signature is invalid during commit phase");
                            cleanup();
                            block.setStatusType(ConsensusStatusType.ABORT);
                            return;
                        }
                    }
                } catch (IllegalArgumentException | StringIndexOutOfBoundsException e) {
                    cleanup();
                    LOG.info("CommitPhase: Problem at message deserialization Abort");
                    block.setStatusType(ConsensusStatusType.ABORT);
                    return;
                } catch (ArrayIndexOutOfBoundsException e) {
                    cleanup();
                    LOG.info("CommitPhase: Receiving out of bounds response from organizer");
                    block.setStatusType(ConsensusStatusType.ABORT);
                    return;
                } catch (NullPointerException e) {
                    cleanup();
                    LOG.info("CommitPhase: Receiving null response from organizer");
                    block.setStatusType(ConsensusStatusType.ABORT);
                    return;
                }
            }

            if (!block.getMessageType().equals(ConsensusMessageType.COMMIT)) {
                cleanup();
                LOG.info("CommitPhase: Organizer not send correct header message expected " + ConsensusMessageType.COMMIT);
                block.setStatusType(ConsensusStatusType.ABORT);
                return;
            }

            List<BLSPublicKey> publicKeys = new ArrayList<>(block.getSignatures().keySet());
            List<Signature> signature = block.getSignatures().values().stream().map(BLSSignatureData::getSignature).map(r -> r[1]).collect(Collectors.toList());


            Signature aggregatedSignature = BLSSignature.aggregate(signature);
            this.sizeCalculator.setCommitteeBlock(block.getData());
            byte[] message = block_serialize.encode(block.getData(), this.sizeCalculator.CommitteeBlockSizeCalculator());
            Bytes toVerify = Bytes.wrap(message);
            boolean verify = BLSSignature.fastAggregateVerify(publicKeys, toVerify, aggregatedSignature);
            if (!verify) {
                LOG.info("CommitPhase: Abort consensus phase BLS multi_signature is invalid in prepare phase");
                block.setStatusType(ConsensusStatusType.ABORT);
            }
            if (DEBUG)
                return;


            consensusClient.send_heartbeat(HEARTBEAT_MESSAGE);
            String heartbeat = consensusClient.rec_heartbeat();
            if (heartbeat == null) {
                cleanup();
                LOG.info("CommitPhase: heartbeat message before end is null abort");
                block.setStatusType(ConsensusStatusType.ABORT);
                return;
            }

            CachedStartHeightRewards.getInstance().setRewardsCommitteeEnabled(false);
            BlockInvent regural_block = (BlockInvent) factory.getBlock(BlockType.REGULAR);
            regural_block.InventCommitteBlock(block.getData());
            //commit save to db

            // consensusClient.send_heartbeat(HEARTBEAT_MESSAGE);
            LOG.info("Committee is finalized with Success");
            cleanup();
            Thread.sleep(700);
            //Make sure you give enough time for nodes to sync that not existed or existed
        }

        private void cleanup() {
            if (collector_client != null) {
                collector_client.close();
                collector_client = null;
            }
            if (consensusClient != null) {
                consensusClient.close();
                consensusClient = null;
            }
        }
    }

}
