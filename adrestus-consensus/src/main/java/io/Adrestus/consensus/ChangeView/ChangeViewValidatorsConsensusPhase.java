package io.Adrestus.consensus.ChangeView;

import com.google.common.reflect.TypeToken;
import io.Adrestus.consensus.ConsensusMessage;
import io.Adrestus.consensus.ConsensusMessageType;
import io.Adrestus.consensus.ConsensusStatusType;
import io.Adrestus.core.AbstractBlock;
import io.Adrestus.core.Resourses.CachedLatestBlocks;
import io.Adrestus.core.Resourses.CachedLeaderIndex;
import io.Adrestus.core.Resourses.CachedZoneIndex;
import io.Adrestus.crypto.bls.BLSSignatureData;
import io.Adrestus.crypto.bls.model.BLSPublicKey;
import io.Adrestus.crypto.bls.model.BLSSignature;
import io.Adrestus.crypto.bls.model.CachedBLSKeyPair;
import io.Adrestus.crypto.bls.model.Signature;
import io.Adrestus.network.ConsensusClient;
import io.Adrestus.util.SerializationFuryUtil;
import org.apache.tuweni.bytes.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static io.Adrestus.config.ConsensusConfiguration.HEARTBEAT_MESSAGE;

public class ChangeViewValidatorsConsensusPhase extends ChangeViewConsensusPhase {
    protected static Logger LOG = LoggerFactory.getLogger(ChangeViewValidatorsConsensusPhase.class);
    private static final Type fluentType = new TypeToken<ConsensusMessage<ChangeViewData>>() {
    }.getType();

    public ChangeViewValidatorsConsensusPhase() {
        super();
    }


    public static class ChangeViewTransactionBlock extends ChangeViewValidatorsConsensusPhase {

        public ChangeViewTransactionBlock() {
            super();
        }

        @Override
        public void InitialSetup() {
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

        @Override
        public void AnnouncePhase(ConsensusMessage<ChangeViewData> data) throws Exception {
            AbstractBlock transactionBlock = CachedLatestBlocks.getInstance().getTransactionBlock();
            transactionBlock.setViewID(transactionBlock.getViewID() + 1);
            ChangeViewData changeViewData = new ChangeViewData(transactionBlock.getHash(), transactionBlock.getViewID());
            data.setData(changeViewData);
            data.setMessageType(ConsensusMessageType.ANNOUNCE);

            byte[] message = change_view_ser.encode(data.getData(), data.getData().length());
            Signature sig = BLSSignature.sign(message, CachedBLSKeyPair.getInstance().getPrivateKey());
            data.getChecksumData().setBlsPublicKey(CachedBLSKeyPair.getInstance().getPublicKey());
            data.getChecksumData().setSignature(sig);


            byte[] toSend = SerializationFuryUtil.getInstance().getFury().serialize(data);
            this.consensusClient.pushMessage(toSend);
        }

        @Override
        public void PreparePhase(ConsensusMessage<ChangeViewData> data) {
            byte[] receive = this.consensusClient.deque_message();
            if (receive == null) {
                cleanup();
                LOG.info("PreparePhase: Leader is not active fail to send message");
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            } else {
                try {
                    data = (ConsensusMessage<ChangeViewData>) SerializationFuryUtil.getInstance().getFury().deserialize(receive);
                    if (!data.getChecksumData().getBlsPublicKey().toRaw().equals(leader_bls.toRaw())) {
                        cleanup();
                        LOG.info("PreparePhase: This is not the valid leader for this round");
                        data.setStatusType(ConsensusStatusType.ABORT);
                        return;
                    } else {
                        byte[] message = change_view_ser.encode(data.getData(), data.getData().length());
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


            if (!data.getMessageType().equals(ConsensusMessageType.PREPARE)) {
                cleanup();
                LOG.info("PreparePhase: Organizer not send correct header message expected " + ConsensusMessageType.PREPARE);
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            }
            List<BLSPublicKey> publicKeys = new ArrayList<>(data.getSignatures().keySet());
            List<Signature> signature = data.getSignatures().values().stream().map(BLSSignatureData::getSignature).map(r -> r[0]).collect(Collectors.toList());


            Signature aggregatedSignature = BLSSignature.aggregate(signature);
            Bytes toVerify = Bytes.wrap(change_view_ser.encode(data.getData(), data.getData().length()));
            boolean verify = BLSSignature.fastAggregateVerify(publicKeys, toVerify, aggregatedSignature);
            if (!verify) {
                cleanup();
                LOG.info("PreparePhase: Abort consensus phase BLS multi_signature is invalid in prepare phase");
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            }


            consensusClient.send_heartbeat(HEARTBEAT_MESSAGE);
            CachedLatestBlocks.getInstance().getTransactionBlock().setLeaderPublicKey(this.leader_bls);
            CachedLatestBlocks.getInstance().getTransactionBlock().setViewID(data.getData().getViewID());
            CachedLatestBlocks.getInstance().getTransactionBlock().setBlockProposer(this.leader_bls.toRaw());
            cleanup();
            LOG.info("Change View is finalized with Success");
        }
    }

    public static class ChangeCommiteeBlockView extends ChangeViewValidatorsConsensusPhase {

        public ChangeCommiteeBlockView() {
            super();
        }

        @Override
        public void InitialSetup() {
            try {
                this.current = CachedLeaderIndex.getInstance().getCommitteePositionLeader();
                this.leader_bls = this.blockIndex.getPublicKeyByIndex(CachedZoneIndex.getInstance().getZoneIndex(), this.current);
                this.consensusClient = new ConsensusClient(this.blockIndex.getIpValue(CachedZoneIndex.getInstance().getZoneIndex(), this.leader_bls));
                this.consensusClient.receive_handler();
                this.consensusClient.send_heartbeat(HEARTBEAT_MESSAGE);
                String heartbeat = consensusClient.rec_heartbeat();
                if (heartbeat == null)
                    throw new IllegalArgumentException("Heartbeat message is null no response from leader");
                if (!heartbeat.equals("1"))
                    throw new IllegalArgumentException("Heartbeat message is not valid");

            } catch (Exception e) {
                LOG.info("Initial Setup Exception: " + e.toString());
            }
        }

        @Override
        public void AnnouncePhase(ConsensusMessage<ChangeViewData> data) throws Exception {
            AbstractBlock committeeBlock = CachedLatestBlocks.getInstance().getCommitteeBlock();
            committeeBlock.setViewID(committeeBlock.getViewID() + 1);
            ChangeViewData changeViewData = new ChangeViewData(committeeBlock.getHash(), committeeBlock.getViewID());
            data.setData(changeViewData);
            data.setMessageType(ConsensusMessageType.ANNOUNCE);

            byte[] message = change_view_ser.encode(data.getData(), data.getData().length());
            Signature sig = BLSSignature.sign(message, CachedBLSKeyPair.getInstance().getPrivateKey());
            data.getChecksumData().setBlsPublicKey(CachedBLSKeyPair.getInstance().getPublicKey());
            data.getChecksumData().setSignature(sig);


            byte[] toSend = SerializationFuryUtil.getInstance().getFury().serialize(data);
            this.consensusClient.pushMessage(toSend);
        }

        @Override
        public void PreparePhase(ConsensusMessage<ChangeViewData> data) {
            byte[] receive = this.consensusClient.deque_message();
            if (receive == null) {
                cleanup();
                LOG.info("PreparePhase: Leader is not active fail to send message");
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            } else {
                try {
                    data = (ConsensusMessage<ChangeViewData>) SerializationFuryUtil.getInstance().getFury().deserialize(receive);
                    if (!data.getChecksumData().getBlsPublicKey().toRaw().equals(leader_bls.toRaw())) {
                        cleanup();
                        LOG.info("PreparePhase: This is not the valid leader for this round");
                        data.setStatusType(ConsensusStatusType.ABORT);
                        return;
                    } else {
                        byte[] message = change_view_ser.encode(data.getData(), data.getData().length());
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
                    LOG.info("PreparePhase: Problem at message deserialization Abort " + e.toString());
                    data.setStatusType(ConsensusStatusType.ABORT);
                    return;
                } catch (ArrayIndexOutOfBoundsException e) {
                    cleanup();
                    LOG.info("PreparePhase: Receiving out of bounds from organizer " + e.toString());
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
            Bytes toVerify = Bytes.wrap(change_view_ser.encode(data.getData(), data.getData().length()));
            boolean verify = BLSSignature.fastAggregateVerify(publicKeys, toVerify, aggregatedSignature);
            if (!verify) {
                cleanup();
                LOG.info("PreparePhase: Abort consensus phase BLS multi_signature is invalid in prepare phase");
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            }


            CachedLatestBlocks.getInstance().getCommitteeBlock().setViewID(data.getData().getViewID());
            consensusClient.send_heartbeat(HEARTBEAT_MESSAGE);
            cleanup();
            LOG.info("Change View is finalized with Success");
        }
    }
}
