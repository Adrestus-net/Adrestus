package io.Adrestus.consensus.ChangeView;

import com.google.common.reflect.TypeToken;
import io.Adrestus.config.ConsensusConfiguration;
import io.Adrestus.consensus.ConsensusMessage;
import io.Adrestus.consensus.ConsensusMessageType;
import io.Adrestus.consensus.ConsensusStatusType;
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
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

public class ChangeViewSupervisorConsensusPhase extends ChangeViewConsensusPhase {
    protected static Logger LOG = LoggerFactory.getLogger(ChangeViewSupervisorConsensusPhase.class);

    private ConsensusServer consensusServer;
    private int N;
    private int F;
    private CountDownLatch latch;
    private static final Type fluentType = new TypeToken<ConsensusMessage<ChangeViewData>>() {
    }.getType();

    public ChangeViewSupervisorConsensusPhase(boolean DEBUG) {
        super(DEBUG);
    }

    @Override
    public void InitialSetup() {
        if (!DEBUG) {
            this.N = CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(CachedZoneIndex.getInstance().getZoneIndex()).size() - 1;
            this.F = (this.N - 1) / 3;
            this.latch = new CountDownLatch(N);
            this.current = CachedLeaderIndex.getInstance().getCommitteePositionLeader();
            this.leader_bls = this.blockIndex.getPublicKeyByIndex(CachedZoneIndex.getInstance().getZoneIndex(), this.current);
            this.consensusServer = new ConsensusServer(this.blockIndex.getIpValue(CachedZoneIndex.getInstance().getZoneIndex(), this.leader_bls), latch, ConsensusConfiguration.CHANGE_VIEW_COLLECTOR_TIMEOUT,ConsensusConfiguration.CHANGE_VIEW_CONNECTED_TIMEOUT);
        }
    }

    @Override
    public void AnnouncePhase(ConsensusMessage<ChangeViewData> data) throws Exception {
        if (!DEBUG) {
            int i = N;
            while (i > 0) {
                byte[] receive = consensusServer.receiveData();
                try {
                    if (receive == null) {
                        LOG.info("AnnouncePhase: Null message from validators");
                        i--;
                    } else {
                        ConsensusMessage<ChangeViewData> received = consensus_serialize.decode(receive);
                        data.setData(received.getData());
                        if (!data.getMessageType().equals(ConsensusMessageType.ANNOUNCE)||
                                !data.getData().getPrev_hash().equals(CachedLatestBlocks.getInstance().getCommitteeBlock().getHash())||
                                data.getData().getViewID()!=CachedLatestBlocks.getInstance().getCommitteeBlock().getViewID()+1) {
                            LOG.info("AnnouncePhase: Problem at message validation");
                            i--;
                        }
                        if (!CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(CachedZoneIndex.getInstance().getZoneIndex()).containsKey(received.getChecksumData().getBlsPublicKey())) {
                            LOG.info("AnnouncePhase: Validator does not exist on consensus... Ignore");
                            i--;
                        } else {
                            data.getSignatures().add(received.getChecksumData());
                            N--;
                            i--;
                        }
                    }
                } catch (IllegalArgumentException e) {
                    LOG.info("AnnouncePhase: Problem at message deserialization");
                    data.setStatusType(ConsensusStatusType.ABORT);
                    cleanup();
                    return;
                }
            }


            if (N > F) {
                LOG.info("AnnouncePhase: Byzantine network not meet requirements abort " + String.valueOf(N));
                data.setStatusType(ConsensusStatusType.ABORT);
                cleanup();
                return;
            }

            data.setMessageType(ConsensusMessageType.PREPARE);

            List<BLSPublicKey> publicKeys = data.getSignatures().stream().map(ConsensusMessage.ChecksumData::getBlsPublicKey).collect(Collectors.toList());
            List<Signature> signature = data.getSignatures().stream().map(ConsensusMessage.ChecksumData::getSignature).collect(Collectors.toList());

            Signature aggregatedSignature = BLSSignature.aggregate(signature);

            Bytes message = Bytes.wrap(change_view_ser.encode(data.getData()));
            boolean verify = BLSSignature.fastAggregateVerify(publicKeys, message, aggregatedSignature);
            if (!verify) {
                LOG.info("Abort consensus phase BLS multi_signature is invalid during announce phase");
                data.setStatusType(ConsensusStatusType.ABORT);
                cleanup();
                return;
            }

            if (DEBUG)
                return;

            Signature sig = BLSSignature.sign(change_view_ser.encode(data.getData()), CachedBLSKeyPair.getInstance().getPrivateKey());
            data.setChecksumData(new ConsensusMessage.ChecksumData(sig, CachedBLSKeyPair.getInstance().getPublicKey()));

            this.N = CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(CachedZoneIndex.getInstance().getZoneIndex()).size() - 1;
            this.F = (this.N - 1) / 3;


            byte[] toSend = consensus_serialize.encode(data);
            consensusServer.publishMessage(toSend);
        }
    }

    @Override
    public void PreparePhase(ConsensusMessage<ChangeViewData> data) throws InterruptedException {
        super.PreparePhase(data);
        super.cleanup();
    }
}
