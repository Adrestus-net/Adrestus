package io.Adrestus.consensus.ChangeView;

import com.google.common.reflect.TypeToken;
import io.Adrestus.consensus.ConsensusMessage;
import io.Adrestus.core.BlockIndex;
import io.Adrestus.core.IBlockIndex;
import io.Adrestus.crypto.bls.model.BLSPublicKey;
import io.Adrestus.network.ConsensusServer;
import io.Adrestus.util.SerializationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.concurrent.CountDownLatch;

public abstract class ChangeViewConsensusPhase {
    protected static Logger LOG = LoggerFactory.getLogger(ChangeViewConsensusPhase.class);
    protected final boolean DEBUG;
    protected final IBlockIndex blockIndex;
    protected final SerializationUtil<ChangeViewData> change_view_ser;
    protected int current;
    protected BLSPublicKey leader_bls;

    protected final SerializationUtil<ConsensusMessage> consensus_serialize;

    protected ConsensusServer consensusServer;
    protected int N;
    protected int F;
    protected CountDownLatch latch;
    protected static final Type fluentType = new TypeToken<ConsensusMessage<ChangeViewData>>() {
    }.getType();

    public ChangeViewConsensusPhase(boolean DEBUG) {
        this.DEBUG = DEBUG;
        this.blockIndex = new BlockIndex();
        this.change_view_ser = new SerializationUtil<ChangeViewData>(ChangeViewData.class);
        this.consensus_serialize = new SerializationUtil<ConsensusMessage>(fluentType);
    }

    public void InitialSetup() throws Exception {
    }


    public void AnnouncePhase(ConsensusMessage<ChangeViewData> data) throws Exception {
    }

    public void PreparePhase(ConsensusMessage<ChangeViewData> data) throws InterruptedException {
        int i = N;
        while (i > 0) {
            try {
                consensusServer.receiveStringData();
            } catch (NullPointerException ex) {
            } finally {
                i--;
            }
        }
        cleanup();
        LOG.info("Change View is finalized with Success");
    }

    protected void cleanup() {
        consensusServer.close();
    }
}
