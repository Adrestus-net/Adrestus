package io.Adrestus.consensus.ChangeView;

import com.google.common.reflect.TypeToken;
import io.Adrestus.consensus.ConsensusMessage;
import io.Adrestus.consensus.ConsensusMessageType;
import io.Adrestus.consensus.ConsensusStatusType;
import io.Adrestus.core.AbstractBlock;
import io.Adrestus.core.Resourses.CachedLatestBlocks;
import io.Adrestus.core.Resourses.CachedLeaderIndex;
import io.Adrestus.core.Resourses.CachedZoneIndex;
import io.Adrestus.crypto.bls.model.BLSPublicKey;
import io.Adrestus.crypto.bls.model.BLSSignature;
import io.Adrestus.crypto.bls.model.CachedBLSKeyPair;
import io.Adrestus.crypto.bls.model.Signature;
import io.Adrestus.network.ConsensusClient;
import org.apache.tuweni.bytes.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Collectors;

import static io.Adrestus.config.ConsensusConfiguration.HEARTBEAT_MESSAGE;

public class ChangeViewValidatorsConsensusPhase extends ChangeViewConsensusPhase {
    protected static Logger LOG = LoggerFactory.getLogger(ChangeViewValidatorsConsensusPhase.class);
    private static final Type fluentType = new TypeToken<ConsensusMessage<ChangeViewData>>() {
    }.getType();

    public ChangeViewValidatorsConsensusPhase(boolean DEBUG) {
        super(DEBUG);
    }


    public static class ChangeViewTransactionBlock extends ChangeViewValidatorsConsensusPhase {

        public ChangeViewTransactionBlock(boolean DEBUG) {
            super(DEBUG);
        }

        @Override
        public void InitialSetup() {
            if (!DEBUG) {
                try {
                    this.current = CachedLeaderIndex.getInstance().getTransactionPositionLeader();
                    this.leader_bls = this.blockIndex.getPublicKeyByIndex(CachedZoneIndex.getInstance().getZoneIndex(), this.current);
                    this.consensusClient = new ConsensusClient(this.blockIndex.getIpValue(CachedZoneIndex.getInstance().getZoneIndex(), this.leader_bls));
                    this.consensusClient.receive_handler();
                    this.consensusClient.send_heartbeat(HEARTBEAT_MESSAGE);
                    this.consensusClient.rec_heartbeat();
                } catch (Exception e) {
                    LOG.info("Initial Setup Exception: " + e.toString());
                }
            }
        }

        @Override
        public void AnnouncePhase(ConsensusMessage<ChangeViewData> data) throws Exception {
            AbstractBlock transactionBlock = CachedLatestBlocks.getInstance().getTransactionBlock();
            transactionBlock.setViewID(transactionBlock.getViewID() + 1);
            ChangeViewData changeViewData = new ChangeViewData(transactionBlock.getHash(), transactionBlock.getViewID());
            data.setData(changeViewData);
            data.setMessageType(ConsensusMessageType.ANNOUNCE);

            byte[] message = change_view_ser.encode(data.getData());
            Signature sig = BLSSignature.sign(message, CachedBLSKeyPair.getInstance().getPrivateKey());
            data.getChecksumData().setBlsPublicKey(CachedBLSKeyPair.getInstance().getPublicKey());
            data.getChecksumData().setSignature(sig);

            if (DEBUG)
                return;

            byte[] toSend = consensus_serialize.encode(data);
            this.consensusClient.pushMessage(toSend);
        }

        @Override
        public void PreparePhase(ConsensusMessage<ChangeViewData> data) {
            if (!DEBUG) {
                byte[] receive = this.consensusClient.deque_message();
                if (receive == null) {
                    cleanup();
                    LOG.info("PreparePhase: Leader is not active fail to send message");
                    data.setStatusType(ConsensusStatusType.ABORT);
                    return;
                } else {
                    try {
                        data = consensus_serialize.decode(receive);
                        if (!data.getChecksumData().getBlsPublicKey().toRaw().equals(leader_bls.toRaw())) {
                            cleanup();
                            LOG.info("PreparePhase: This is not the valid leader for this round");
                            data.setStatusType(ConsensusStatusType.ABORT);
                            return;
                        } else {
                            byte[] message = change_view_ser.encode(data.getData());
                            boolean verify = BLSSignature.verify(data.getChecksumData().getSignature(), message, data.getChecksumData().getBlsPublicKey());
                            if (!verify) {
                                cleanup();
                                LOG.info("PreparePhase: Abort consensus phase BLS leader signature is invalid during prepare phase");
                                data.setStatusType(ConsensusStatusType.ABORT);
                                return;
                            }
                        }
                    } catch (IllegalArgumentException e) {
                        cleanup();
                        LOG.info("PreparePhase: Problem at message deserialization Abort");
                        data.setStatusType(ConsensusStatusType.ABORT);
                        return;
                    } catch (ArrayIndexOutOfBoundsException e) {
                        cleanup();
                        LOG.info("PreparePhase: Receiving null response from organizer");
                        data.setStatusType(ConsensusStatusType.ABORT);
                        return;
                    }
                }
            }


            if (!data.getMessageType().equals(ConsensusMessageType.PREPARE)) {
                cleanup();
                LOG.info("PreparePhase: Organizer not send correct header message expected " + ConsensusMessageType.PREPARE);
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            }
            List<BLSPublicKey> publicKeys = data.getSignatures().stream().map(ConsensusMessage.ChecksumData::getBlsPublicKey).collect(Collectors.toList());
            List<Signature> signature = data.getSignatures().stream().map(ConsensusMessage.ChecksumData::getSignature).collect(Collectors.toList());


            Signature aggregatedSignature = BLSSignature.aggregate(signature);
            Bytes toVerify = Bytes.wrap(change_view_ser.encode(data.getData()));
            boolean verify = BLSSignature.fastAggregateVerify(publicKeys, toVerify, aggregatedSignature);
            if (!verify) {
                cleanup();
                LOG.info("PreparePhase: Abort consensus phase BLS multi_signature is invalid in prepare phase");
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            }

            if (DEBUG)
                return;

            consensusClient.send_heartbeat(HEARTBEAT_MESSAGE);
            CachedLatestBlocks.getInstance().getTransactionBlock().setLeaderPublicKey(this.leader_bls);
            CachedLatestBlocks.getInstance().getTransactionBlock().setViewID(data.getData().getViewID());
            CachedLatestBlocks.getInstance().getTransactionBlock().setTransactionProposer(this.leader_bls.toRaw());
            cleanup();
            LOG.info("Change View is finalized with Success");
        }
    }

    public static class ChangeCommiteeBlockView extends ChangeViewValidatorsConsensusPhase {

        public ChangeCommiteeBlockView(boolean DEBUG) {
            super(DEBUG);
        }

        @Override
        public void InitialSetup() {
            if (!DEBUG) {
                try {
                    this.current = CachedLeaderIndex.getInstance().getCommitteePositionLeader();
                    this.leader_bls = this.blockIndex.getPublicKeyByIndex(CachedZoneIndex.getInstance().getZoneIndex(), this.current);
                    this.consensusClient = new ConsensusClient(this.blockIndex.getIpValue(CachedZoneIndex.getInstance().getZoneIndex(), this.leader_bls));
                    this.consensusClient.receive_handler();
                    this.consensusClient.send_heartbeat(HEARTBEAT_MESSAGE);
                    this.consensusClient.rec_heartbeat();
                } catch (Exception e) {
                    LOG.info("Initial Setup Exception: " + e.toString());
                }
            }
        }

        @Override
        public void AnnouncePhase(ConsensusMessage<ChangeViewData> data) throws Exception {
            AbstractBlock committeeBlock = CachedLatestBlocks.getInstance().getCommitteeBlock();
            committeeBlock.setViewID(committeeBlock.getViewID() + 1);
            ChangeViewData changeViewData = new ChangeViewData(committeeBlock.getHash(), committeeBlock.getViewID());
            data.setData(changeViewData);
            data.setMessageType(ConsensusMessageType.ANNOUNCE);

            byte[] message = change_view_ser.encode(data.getData());
            Signature sig = BLSSignature.sign(message, CachedBLSKeyPair.getInstance().getPrivateKey());
            data.getChecksumData().setBlsPublicKey(CachedBLSKeyPair.getInstance().getPublicKey());
            data.getChecksumData().setSignature(sig);

            if (DEBUG)
                return;

            byte[] toSend = consensus_serialize.encode(data);
            this.consensusClient.pushMessage(toSend);
        }

        @Override
        public void PreparePhase(ConsensusMessage<ChangeViewData> data) {
            if (!DEBUG) {
                byte[] receive = this.consensusClient.deque_message();
                if (receive == null) {
                    cleanup();
                    LOG.info("PreparePhase: Leader is not active fail to send message");
                    data.setStatusType(ConsensusStatusType.ABORT);
                    return;
                } else {
                    try {
                        data = consensus_serialize.decode(receive);
                        if (!data.getChecksumData().getBlsPublicKey().toRaw().equals(leader_bls.toRaw())) {
                            cleanup();
                            LOG.info("PreparePhase: This is not the valid leader for this round");
                            data.setStatusType(ConsensusStatusType.ABORT);
                            return;
                        } else {
                            byte[] message = change_view_ser.encode(data.getData());
                            boolean verify = BLSSignature.verify(data.getChecksumData().getSignature(), message, data.getChecksumData().getBlsPublicKey());
                            if (!verify) {
                                cleanup();
                                LOG.info("PreparePhase: Abort consensus phase BLS leader signature is invalid during prepare phase");
                                data.setStatusType(ConsensusStatusType.ABORT);
                                return;
                            }
                        }
                    } catch (IllegalArgumentException e) {
                        cleanup();
                        LOG.info("PreparePhase: Problem at message deserialization Abort");
                        data.setStatusType(ConsensusStatusType.ABORT);
                        return;
                    } catch (ArrayIndexOutOfBoundsException e) {
                        cleanup();
                        LOG.info("PreparePhase: Receiving null response from organizer");
                        data.setStatusType(ConsensusStatusType.ABORT);
                        return;
                    }
                }
            }


            if (!data.getMessageType().equals(ConsensusMessageType.PREPARE)) {
                cleanup();
                LOG.info("PreparePhase: Organizer not send correct header message expected " + ConsensusMessageType.PREPARE);
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            }
            List<BLSPublicKey> publicKeys = data.getSignatures().stream().map(ConsensusMessage.ChecksumData::getBlsPublicKey).collect(Collectors.toList());
            List<Signature> signature = data.getSignatures().stream().map(ConsensusMessage.ChecksumData::getSignature).collect(Collectors.toList());


            Signature aggregatedSignature = BLSSignature.aggregate(signature);
            Bytes toVerify = Bytes.wrap(change_view_ser.encode(data.getData()));
            boolean verify = BLSSignature.fastAggregateVerify(publicKeys, toVerify, aggregatedSignature);
            if (!verify) {
                cleanup();
                LOG.info("PreparePhase: Abort consensus phase BLS multi_signature is invalid in prepare phase");
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            }

            if (DEBUG)
                return;

            consensusClient.send_heartbeat(HEARTBEAT_MESSAGE);
            LOG.info("Change View is finalized with Success");
        }
    }
}
