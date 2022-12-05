package io.Adrestus.consensus;

import com.google.common.reflect.TypeToken;
import io.Adrestus.config.AdrestusConfiguration;
import io.Adrestus.core.*;
import io.Adrestus.core.Resourses.CachedLatestBlocks;
import io.Adrestus.core.Resourses.CachedLeaderIndex;
import io.Adrestus.core.Resourses.CachedSecurityAuditProofs;
import io.Adrestus.core.Resourses.CachedSecurityHeaders;
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
import io.Adrestus.crypto.vdf.VDFMessage;
import io.Adrestus.crypto.vdf.engine.VdfEngine;
import io.Adrestus.crypto.vdf.engine.VdfEnginePietrzak;
import io.Adrestus.crypto.vrf.VRFMessage;
import io.Adrestus.crypto.vrf.engine.VrfEngine2;
import io.Adrestus.network.ConsensusServer;
import io.Adrestus.p2p.kademlia.node.DHTCachedNodes;
import io.Adrestus.util.ByteUtil;
import io.Adrestus.util.SerializationUtil;
import io.distributedLedger.DatabaseFactory;
import io.distributedLedger.DatabaseType;
import io.distributedLedger.IDatabase;
import org.apache.tuweni.bytes.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

public class SupervisorConsensusPhases {
    protected boolean DEBUG;
    protected CountDownLatch latch;
    protected int N;
    protected int F;
    protected int current;
    protected ConsensusServer consensusServer;
    protected BLSPublicKey leader_bls;

    protected static class ProposeVDF extends SupervisorConsensusPhases implements BFTConsensusPhase<VDFMessage> {
        private static final Type fluentType = new TypeToken<ConsensusMessage<VDFMessage>>() {
        }.getType();
        private static Logger LOG = LoggerFactory.getLogger(ProposeVDF.class);
        private final VdfEngine vdf;
        private final SerializationUtil<ConsensusMessage> consensus_serialize;
        private final SerializationUtil<VDFMessage> data_serialize;


        public ProposeVDF(boolean DEBUG) {
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
                this.N = 1;
                //this.N = CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(1).size() - 1;
                this.F = (this.N - 1) / 3;
                this.latch = new CountDownLatch(N);
                this.current = CachedLeaderIndex.getInstance().getCommitteePositionLeader();
                this.leader_bls = CachedLatestBlocks.getInstance().getCommitteeBlock().getPublicKeyByIndex(0, current);
                this.consensusServer = new ConsensusServer(CachedLatestBlocks.getInstance().getCommitteeBlock().getValue(0, this.leader_bls), latch);
            }
        }

        @Override
        public void AnnouncePhase(ConsensusMessage<VDFMessage> data) {
            data.setMessageType(ConsensusMessageType.ANNOUNCE);
            byte[] solution = vdf.solve(CachedSecurityHeaders.getInstance().getSecurityHeader().getpRnd(), CachedLatestBlocks.getInstance().getCommitteeBlock().getDifficulty());
            data.getData().setVDFSolution(solution);

            if (DEBUG)
                return;

            byte[] message = data_serialize.encode(data.getData());
            Signature sig = BLSSignature.sign(message, CachedBLSKeyPair.getInstance().getPrivateKey());
            data.getChecksumData().setBlsPublicKey(CachedBLSKeyPair.getInstance().getPublicKey());
            data.getChecksumData().setSignature(sig);

            byte[] toSend = consensus_serialize.encode(data);
            consensusServer.publishMessage(toSend);

        }

        @Override
        public void PreparePhase(ConsensusMessage<VDFMessage> data) {
            if (!DEBUG) {
                int i = N;
                while (i > 0) {
                    byte[] receive = consensusServer.receiveData();
                    try {
                        if (receive == null) {
                            LOG.info("PreparePhase: Null message from validators");
                            i--;
                        } else {
                            ConsensusMessage<VDFMessage> received = consensus_serialize.decode(receive);
                            if (!CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(0).containsKey(received.getChecksumData().getBlsPublicKey())) {
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
            Bytes message = Bytes.wrap(data.getData().getVDFSolution());
            boolean verify = BLSSignature.fastAggregateVerify(publicKeys, message, aggregatedSignature);
            if (!verify)
                throw new IllegalArgumentException("Abort consensus phase BLS multi_signature is invalid during prepare phase");


            if (DEBUG)
                return;


            Signature sig = BLSSignature.sign(data_serialize.encode(data.getData()), CachedBLSKeyPair.getInstance().getPrivateKey());
            data.setChecksumData(new ConsensusMessage.ChecksumData(sig, CachedBLSKeyPair.getInstance().getPublicKey()));

            this.N = 1;
            this.F = (this.N - 1) / 3;


            byte[] toSend = consensus_serialize.encode(data);
            consensusServer.publishMessage(toSend);
        }

        @Override
        public void CommitPhase(ConsensusMessage<VDFMessage> data) {
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
                            if (!CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(0).containsKey(received.getChecksumData().getBlsPublicKey())) {
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
            byte[] wrapp = data_serialize.encode(data.getData());
            Bytes message = Bytes.wrap(wrapp);
            boolean verify = BLSSignature.fastAggregateVerify(publicKeys, message, aggregatedSignature);
            if (!verify)
                throw new IllegalArgumentException("Abort consensus phase BLS multi_signature is invalid during commit phase");

            //commit save to db

            if (DEBUG)
                return;

            Signature sig = BLSSignature.sign(data_serialize.encode(data.getData()), CachedBLSKeyPair.getInstance().getPrivateKey());
            data.setChecksumData(new ConsensusMessage.ChecksumData(sig, CachedBLSKeyPair.getInstance().getPublicKey()));

            this.N = 1;
            this.F = (this.N - 1) / 3;
            int i = N;

            byte[] toSend = consensus_serialize.encode(data);
            consensusServer.publishMessage(toSend);

            CachedSecurityHeaders.getInstance().getSecurityHeader().setRnd(data.getData().getVDFSolution());

            while (i > 0) {
                try {
                    consensusServer.receiveStringData();
                } catch (NullPointerException ex) {
                } finally {
                    i--;
                }
            }
            cleanup();
            LOG.info("VDF is finalized with Success");
        }
    }


    protected static class ProposeVRF extends SupervisorConsensusPhases implements VRFConsensusPhase<VRFMessage> {
        private static Logger LOG = LoggerFactory.getLogger(ProposeVRF.class);
        private static final Type fluentType = new TypeToken<ConsensusMessage<VRFMessage>>() {
        }.getType();
        private VrfEngine2 group;
        private final SerializationUtil<VRFMessage> serialize;
        private final SerializationUtil<ConsensusMessage> consensus_serialize;

        public ProposeVRF(boolean DEBUG) {
            this.DEBUG = DEBUG;
            this.group = new VrfEngine2();
            List<SerializationUtil.Mapping> list = new ArrayList<>();
            list.add(new SerializationUtil.Mapping(ECP.class, ctx -> new ECPmapper()));
            list.add(new SerializationUtil.Mapping(ECP2.class, ctx -> new ECP2mapper()));
            this.serialize = new SerializationUtil<VRFMessage>(VRFMessage.class);
            this.consensus_serialize = new SerializationUtil<ConsensusMessage>(fluentType, list);
        }


        @Override
        public void InitialSetup() {
            if (!DEBUG) {
                this.N = 1;
                //this.N = CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(1).size() - 1;
                this.F = (this.N - 1) / 3;
                this.latch = new CountDownLatch(N);
                this.current = CachedLeaderIndex.getInstance().getCommitteePositionLeader();
                this.leader_bls = CachedLatestBlocks.getInstance().getCommitteeBlock().getPublicKeyByIndex(0, current);
                this.consensusServer = new ConsensusServer(CachedLatestBlocks.getInstance().getCommitteeBlock().getValue(0, this.leader_bls), latch);
            }
        }

        @Override
        public void Initialize(VRFMessage message) {
            message.setBlockHash(CachedLatestBlocks.getInstance().getCommitteeBlock().getHash());
            message.setType(VRFMessage.vrfMessageType.INIT);

            if (DEBUG)
                return;

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            byte[] toSend = serialize.encode(message);
            consensusServer.publishMessage(toSend);

        }


        public void AggregateVRF(VRFMessage message) throws Exception {
            if (!DEBUG) {
                int i = N;
                while (i > 0) {
                    byte[] receive = consensusServer.receiveData();
                    try {
                        if (receive == null) {
                            LOG.info("AggregateVRF: Null message from validators");
                            i--;
                        } else {
                            VRFMessage received = serialize.decode(receive);
                            message.getSigners().add(received.getData());
                            N--;
                            i--;
                        }
                    } catch (IllegalArgumentException e) {
                        LOG.info("AggregateVRF: Problem at message deserialization");
                        message.setType(VRFMessage.vrfMessageType.ABORT);
                        cleanup();
                        return;
                    }
                }


                if (N > F) {
                    LOG.info("AggregateVRF: Byzantine network not meet requirements abort " + String.valueOf(N));
                    message.setType(VRFMessage.vrfMessageType.ABORT);
                    cleanup();
                    return;
                }
            }
            List<VRFMessage.VRFData> list = message.getSigners();

            if (list.isEmpty())
                throw new IllegalArgumentException("Validators not produce valid vrf inputs and list is empty");

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

            this.N = 1;
            this.F = (this.N - 1) / 3;
        }


        @Override
        public void AnnouncePhase(ConsensusMessage<VRFMessage> data) {
            data.setMessageType(ConsensusMessageType.ANNOUNCE);

            if (DEBUG)
                return;

            byte[] message = serialize.encode(data.getData());
            Signature sig = BLSSignature.sign(message, CachedBLSKeyPair.getInstance().getPrivateKey());
            data.getChecksumData().setBlsPublicKey(CachedBLSKeyPair.getInstance().getPublicKey());
            data.getChecksumData().setSignature(sig);

            byte[] toSend = consensus_serialize.encode(data);
            consensusServer.publishMessage(toSend);
        }

        @Override
        public void PreparePhase(ConsensusMessage<VRFMessage> data) {

            if (!DEBUG) {
                int i = N;
                while (i > 0) {
                    byte[] receive = consensusServer.receiveData();
                    try {
                        if (receive == null) {
                            LOG.info("PreparePhase: Null message from validators");
                            i--;
                        } else {
                            ConsensusMessage<VRFMessage> received = consensus_serialize.decode(receive);
                            if (!CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(0).containsKey(received.getChecksumData().getBlsPublicKey())) {
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
            byte[] message = serialize.encode(data.getData());
            boolean verify = BLSSignature.fastAggregateVerify(publicKeys, Bytes.wrap(message), aggregatedSignature);
            if (!verify)
                throw new IllegalArgumentException("Abort consensus phase BLS multi_signature is invalid during prepare phase");


            if (DEBUG)
                return;


            Signature sig = BLSSignature.sign(serialize.encode(data.getData()), CachedBLSKeyPair.getInstance().getPrivateKey());
            data.setChecksumData(new ConsensusMessage.ChecksumData(sig, CachedBLSKeyPair.getInstance().getPublicKey()));

            this.N = 1;
            this.F = (this.N - 1) / 3;


            byte[] toSend = consensus_serialize.encode(data);
            consensusServer.publishMessage(toSend);
        }

        @Override
        public void CommitPhase(ConsensusMessage<VRFMessage> data) {
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
                            ConsensusMessage<VRFMessage> received = consensus_serialize.decode(receive);
                            if (!CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(0).containsKey(received.getChecksumData().getBlsPublicKey())) {
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
            byte[] wrapp = serialize.encode(data.getData());
            Bytes message = Bytes.wrap(wrapp);
            boolean verify = BLSSignature.fastAggregateVerify(publicKeys, message, aggregatedSignature);
            if (!verify)
                throw new IllegalArgumentException("Abort consensus phase BLS multi_signature is invalid during commit phase");

            //commit save to db

            if (DEBUG)
                return;

            Signature sig = BLSSignature.sign(serialize.encode(data.getData()), CachedBLSKeyPair.getInstance().getPrivateKey());
            data.setChecksumData(new ConsensusMessage.ChecksumData(sig, CachedBLSKeyPair.getInstance().getPublicKey()));

            this.N = 1;
            this.F = (this.N - 1) / 3;
            int i = N;

            byte[] toSend = consensus_serialize.encode(data);
            consensusServer.publishMessage(toSend);

            CachedSecurityHeaders.getInstance().getSecurityHeader().setpRnd(data.getData().getPrnd());

            while (i > 0) {
                try {
                    consensusServer.receiveStringData();
                } catch (NullPointerException ex) {
                } finally {
                    i--;
                }
            }
            cleanup();
            LOG.info("VRF is finalized with Success");
        }


    }

    protected static class ProposeCommitteeBlock extends SupervisorConsensusPhases implements BFTConsensusPhase<CommitteeBlock> {
        private static final Type fluentType = new TypeToken<ConsensusMessage<CommitteeBlock>>() {
        }.getType();
        private static Logger LOG = LoggerFactory.getLogger(ProposeCommitteeBlock.class);

        private final IDatabase<String, CommitteeBlock> database;
        private final SerializationUtil<AbstractBlock> block_serialize;
        private final SerializationUtil<ConsensusMessage> consensus_serialize;
        private final DefaultFactory factory;

        public ProposeCommitteeBlock(boolean DEBUG) {
            this.DEBUG = DEBUG;
            this.factory = new DefaultFactory();
            this.database = new DatabaseFactory(String.class, CommitteeBlock.class).getDatabase(DatabaseType.ROCKS_DB);
            List<SerializationUtil.Mapping> list = new ArrayList<>();
            list.add(new SerializationUtil.Mapping(ECP.class, ctx -> new ECPmapper()));
            list.add(new SerializationUtil.Mapping(ECP2.class, ctx -> new ECP2mapper()));
            list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
            list.add(new SerializationUtil.Mapping(TreeMap.class, ctx -> new CustomSerializerTreeMap()));
            this.block_serialize = new SerializationUtil<AbstractBlock>(AbstractBlock.class, list);
            this.consensus_serialize = new SerializationUtil<ConsensusMessage>(fluentType, list);
        }

        @Override
        public void InitialSetup() {
            if (!DEBUG) {
                this.N = 1;
                //this.N = CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(1).size() - 1;
                this.F = (this.N - 1) / 3;
                this.latch = new CountDownLatch(N);
                this.current = CachedLeaderIndex.getInstance().getCommitteePositionLeader();
                this.leader_bls = CachedLatestBlocks.getInstance().getCommitteeBlock().getPublicKeyByIndex(0, current);
                this.consensusServer = new ConsensusServer(CachedLatestBlocks.getInstance().getCommitteeBlock().getValue(0, this.leader_bls), latch);
            }
        }

        @Override
        public void AnnouncePhase(ConsensusMessage<CommitteeBlock> block) {
            var regural_block = factory.getBlock(BlockType.REGULAR);

            // this line is incorrect need to change in future please revisit
            block.getData().setStructureMap(CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap());

            if (DHTCachedNodes.getInstance().getDhtBootstrapNode() != null) {
                CachedSecurityAuditProofs
                        .getInstance()
                        .setSecurityAuditProofs(DHTCachedNodes
                                .getInstance()
                                .getDhtBootstrapNode()
                                .getActiveNodes()
                                .stream()
                                .collect(Collectors.toList()));
            }
            regural_block.forgeCommitteBlock(block.getData());
            block.setMessageType(ConsensusMessageType.ANNOUNCE);
            if (DEBUG)
                return;

            byte[] message = block_serialize.encode(block.getData());
            Signature sig = BLSSignature.sign(message, CachedBLSKeyPair.getInstance().getPrivateKey());
            block.getChecksumData().setBlsPublicKey(CachedBLSKeyPair.getInstance().getPublicKey());
            block.getChecksumData().setSignature(sig);


            byte[] toSend = consensus_serialize.encode(block);
            consensusServer.publishMessage(toSend);
        }

        @Override
        public void PreparePhase(ConsensusMessage<CommitteeBlock> block) {
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
                            if (!CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(0).containsKey(received.getChecksumData().getBlsPublicKey())) {
                                LOG.info("PreparePhase: Validator does not exist on consensus... Ignore");
                                i--;
                            } else {
                                block.getSignatures().add(received.getChecksumData());
                                N--;
                                i--;
                            }
                        }
                    } catch (IllegalArgumentException e) {
                        LOG.info("PreparePhase: Problem at message deserialization");
                        block.setStatusType(ConsensusStatusType.ABORT);
                        cleanup();
                        return;
                    }
                }


                if (N > F) {
                    LOG.info("PreparePhase: Byzantine network not meet requirements abort " + String.valueOf(N));
                    block.setStatusType(ConsensusStatusType.ABORT);
                    cleanup();
                    return;
                }
            }
            block.setMessageType(ConsensusMessageType.PREPARE);

            List<BLSPublicKey> publicKeys = block.getSignatures().stream().map(ConsensusMessage.ChecksumData::getBlsPublicKey).collect(Collectors.toList());
            List<Signature> signature = block.getSignatures().stream().map(ConsensusMessage.ChecksumData::getSignature).collect(Collectors.toList());

            Signature aggregatedSignature = BLSSignature.aggregate(signature);

            Bytes message = Bytes.wrap(block_serialize.encode(block.getData()));
            boolean verify = BLSSignature.fastAggregateVerify(publicKeys, message, aggregatedSignature);
            if (!verify)
                throw new IllegalArgumentException("Abort consensus phase BLS multi_signature is invalid during prepare phase");

            if (DEBUG)
                return;


            Signature sig = BLSSignature.sign(block_serialize.encode(block.getData()), CachedBLSKeyPair.getInstance().getPrivateKey());
            block.setChecksumData(new ConsensusMessage.ChecksumData(sig, CachedBLSKeyPair.getInstance().getPublicKey()));

            this.N = 1;
            this.F = (this.N - 1) / 3;


            byte[] toSend = consensus_serialize.encode(block);
            consensusServer.publishMessage(toSend);
        }

        @Override
        public void CommitPhase(ConsensusMessage<CommitteeBlock> block) {
            if (!DEBUG) {
                int i = N;
                block.getSignatures().clear();
                while (i > 0) {
                    byte[] receive = consensusServer.receiveData();
                    try {
                        if (receive == null) {
                            LOG.info("CommitPhase: Not Receiving from Validators");
                            i--;
                        } else {
                            ConsensusMessage<TransactionBlock> received = consensus_serialize.decode(receive);
                            if (!CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(0).containsKey(received.getChecksumData().getBlsPublicKey())) {
                                LOG.info("CommitPhase: Validator does not exist on consensus... Ignore");
                                i--;
                            } else {
                                block.getSignatures().add(received.getChecksumData());
                                N--;
                                i--;
                            }
                        }
                    } catch (IllegalArgumentException e) {
                        LOG.info("CommitPhase: Problem at message deserialization");
                        block.setStatusType(ConsensusStatusType.ABORT);
                        cleanup();
                        return;
                    }
                }


                if (N > F) {
                    LOG.info("CommitPhase: Byzantine network not meet requirements abort " + String.valueOf(N));
                    block.setStatusType(ConsensusStatusType.ABORT);
                    cleanup();
                    return;
                }
            }
            block.setMessageType(ConsensusMessageType.COMMIT);

            List<BLSPublicKey> publicKeys = block.getSignatures().stream().map(ConsensusMessage.ChecksumData::getBlsPublicKey).collect(Collectors.toList());
            List<Signature> signature = block.getSignatures().stream().map(ConsensusMessage.ChecksumData::getSignature).collect(Collectors.toList());


            Signature aggregatedSignature = BLSSignature.aggregate(signature);
            Bytes message = Bytes.wrap(block_serialize.encode(block.getData()));
            boolean verify = BLSSignature.fastAggregateVerify(publicKeys, message, aggregatedSignature);
            if (!verify)
                throw new IllegalArgumentException("CommitPhase: Abort consensus phase BLS multi_signature is invalid during commit phase");

            //commit save to db

            if (DEBUG)
                return;

            Signature sig = BLSSignature.sign(block_serialize.encode(block.getData()), CachedBLSKeyPair.getInstance().getPrivateKey());
            block.setChecksumData(new ConsensusMessage.ChecksumData(sig, CachedBLSKeyPair.getInstance().getPublicKey()));

            this.N = 1;
            this.F = (this.N - 1) / 3;
            int i = N;

            byte[] toSend = consensus_serialize.encode(block);
            consensusServer.publishMessage(toSend);


            while (i > 0) {
                try {
                    consensusServer.receiveStringData();
                } catch (NullPointerException ex) {
                } finally {
                    i--;
                }
            }
            CachedLatestBlocks.getInstance().setCommitteeBlock(block.getData());
            database.save(block.getData().getHash(), block.getData());
            cleanup();
            LOG.info("Committee is finalized with Success");
        }


    }

    protected void cleanup() {
        consensusServer.close();
    }
}
