package io.Adrestus.consensus.ChangeView;

import com.google.common.reflect.TypeToken;
import io.Adrestus.config.ConsensusConfiguration;
import io.Adrestus.consensus.ConsensusMessage;
import io.Adrestus.consensus.ConsensusStatusType;
import io.Adrestus.core.BlockIndex;
import io.Adrestus.core.IBlockIndex;
import io.Adrestus.crypto.bls.BLS381.ECP;
import io.Adrestus.crypto.bls.BLS381.ECP2;
import io.Adrestus.crypto.bls.mapper.ECP2mapper;
import io.Adrestus.crypto.bls.mapper.ECPmapper;
import io.Adrestus.crypto.bls.model.BLSPublicKey;
import io.Adrestus.crypto.elliptic.mapper.BigDecimalSerializer;
import io.Adrestus.crypto.elliptic.mapper.BigIntegerSerializer;
import io.Adrestus.crypto.elliptic.mapper.CustomSerializerTreeMap;
import io.Adrestus.network.ConsensusClient;
import io.Adrestus.network.ConsensusServer;
import io.Adrestus.util.SerializationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;

public abstract class ChangeViewConsensusPhase {
    protected static Logger LOG = LoggerFactory.getLogger(ChangeViewConsensusPhase.class);
    protected final boolean DEBUG;
    protected final IBlockIndex blockIndex;
    protected final SerializationUtil<ChangeViewData> change_view_ser;
    protected int current;
    protected BLSPublicKey leader_bls;

    protected final SerializationUtil<ConsensusMessage> consensus_serialize;

    protected ConsensusClient consensusClient;
    protected int N, N_COPY;
    protected int F;
    protected CountDownLatch latch;
    protected static final Type fluentType = new TypeToken<ConsensusMessage<ChangeViewData>>() {
    }.getType();

    public ChangeViewConsensusPhase(boolean DEBUG) {
        this.DEBUG = DEBUG;
        this.blockIndex = new BlockIndex();
        this.change_view_ser = new SerializationUtil<ChangeViewData>(ChangeViewData.class);
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(ECP.class, ctx -> new ECPmapper()));
        list.add(new SerializationUtil.Mapping(ECP2.class, ctx -> new ECP2mapper()));
        list.add(new SerializationUtil.Mapping(BigDecimal.class, ctx -> new BigDecimalSerializer()));
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        list.add(new SerializationUtil.Mapping(TreeMap.class, ctx -> new CustomSerializerTreeMap()));
        this.consensus_serialize = new SerializationUtil<ConsensusMessage>(fluentType, list);
    }

    public void InitialSetup() throws Exception {
    }


    public void AnnouncePhase(ConsensusMessage<ChangeViewData> data) throws Exception {
    }

    public void PreparePhase(ConsensusMessage<ChangeViewData> data) throws InterruptedException {
        int i = N_COPY;
        while (i > 0) {
            try {
                String heartbeat = ConsensusServer.getInstance().receiveStringData();
                if (heartbeat.equals(ConsensusConfiguration.HEARTBEAT_MESSAGE))
                    N_COPY--;
            } catch (NullPointerException ex) {
            } finally {
                i--;
            }
        }
        if (N_COPY > F) {
            LOG.info("Prepare phase not meet byzantine requirement at the end");
            data.setStatusType(ConsensusStatusType.ABORT);
            cleanup();
            return;
        }
    }

    protected void cleanup() {
        if (consensusClient != null) {
            consensusClient.close();
            consensusClient = null;
        }
    }
}
