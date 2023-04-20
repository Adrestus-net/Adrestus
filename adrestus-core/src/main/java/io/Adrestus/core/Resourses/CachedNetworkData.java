package io.Adrestus.core.Resourses;

import com.google.common.base.Objects;
import io.Adrestus.core.CommitteeBlock;
import io.Adrestus.core.TransactionBlock;
import io.Adrestus.crypto.SecurityHeader;
import io.activej.serializer.annotations.Serialize;

public class CachedNetworkData {

    private boolean consensus_state;
    private int epoch_counter;
    private CommitteeBlock committeeBlock;
    private TransactionBlock transactionBlock;

    private int CommitteePositionLeader;
    private int TransactionPositionLeader;
    private SecurityHeader securityHeader;

    private int ZoneIndex;


    public CachedNetworkData(boolean consensus_state, int epoch_counter, CommitteeBlock committeeBlock, TransactionBlock transactionBlock, int committeePositionLeader, int transactionPositionLeader, SecurityHeader securityHeader, int zoneIndex) {
        this.consensus_state = consensus_state;
        this.epoch_counter = epoch_counter;
        this.committeeBlock = committeeBlock;
        this.transactionBlock = transactionBlock;
        CommitteePositionLeader = committeePositionLeader;
        TransactionPositionLeader = transactionPositionLeader;
        this.securityHeader = securityHeader;
        ZoneIndex = zoneIndex;
    }

    public CachedNetworkData(int epoch_counter, CommitteeBlock committeeBlock, TransactionBlock transactionBlock, int committeePositionLeader, int transactionPositionLeader, SecurityHeader securityHeader, int zoneIndex) {
        this.epoch_counter = epoch_counter;
        this.committeeBlock = committeeBlock;
        this.transactionBlock = transactionBlock;
        this.CommitteePositionLeader = committeePositionLeader;
        this.TransactionPositionLeader = transactionPositionLeader;
        this.securityHeader = securityHeader;
        this.ZoneIndex = zoneIndex;
    }


    public CachedNetworkData() {
        this.consensus_state=false;
        this.epoch_counter = 0;
        this.committeeBlock = new CommitteeBlock();
        this.transactionBlock = new TransactionBlock();
        this.CommitteePositionLeader = 0;
        this.TransactionPositionLeader = 0;
        this.securityHeader = new SecurityHeader();
        this.ZoneIndex = 0;
    }

    @Serialize
    public int getEpoch_counter() {
        return epoch_counter;
    }

    public void setEpoch_counter(int epoch_counter) {
        this.epoch_counter = epoch_counter;
    }

    @Serialize
    public CommitteeBlock getCommitteeBlock() {
        return committeeBlock;
    }

    public void setCommitteeBlock(CommitteeBlock committeeBlock) {
        this.committeeBlock = committeeBlock;
    }

    @Serialize
    public TransactionBlock getTransactionBlock() {
        return transactionBlock;
    }

    public void setTransactionBlock(TransactionBlock transactionBlock) {
        this.transactionBlock = transactionBlock;
    }

    @Serialize
    public SecurityHeader getSecurityHeader() {
        return securityHeader;
    }

    public void setSecurityHeader(SecurityHeader securityHeader) {
        this.securityHeader = securityHeader;
    }

    @Serialize
    public int getZoneIndex() {
        return ZoneIndex;
    }

    public void setZoneIndex(int zoneIndex) {
        this.ZoneIndex = zoneIndex;
    }

    @Serialize
    public int getCommitteePositionLeader() {
        return CommitteePositionLeader;
    }

    public void setCommitteePositionLeader(int committeePositionLeader) {
        CommitteePositionLeader = committeePositionLeader;
    }

    @Serialize
    public int getTransactionPositionLeader() {
        return TransactionPositionLeader;
    }

    public void setTransactionPositionLeader(int transactionPositionLeader) {
        TransactionPositionLeader = transactionPositionLeader;
    }

    @Serialize
    public boolean isConsensus_state() {
        return consensus_state;
    }

    public void setConsensus_state(boolean consensus_state) {
        this.consensus_state = consensus_state;
    }
    public void SetCacheData() {
        CachedEpochGeneration.getInstance().setEpoch_counter(this.epoch_counter);
        CachedLatestBlocks.getInstance().setCommitteeBlock(this.committeeBlock);
        CachedLatestBlocks.getInstance().setTransactionBlock(this.transactionBlock);
        CachedSecurityHeaders.getInstance().setSecurityHeader(this.securityHeader);
        CachedZoneIndex.getInstance().setZoneIndex(this.ZoneIndex);
        CachedLeaderIndex.getInstance().setCommitteePositionLeader(this.CommitteePositionLeader);
        CachedLeaderIndex.getInstance().setTransactionPositionLeader(this.TransactionPositionLeader);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CachedNetworkData that = (CachedNetworkData) o;
        return consensus_state == that.consensus_state && epoch_counter == that.epoch_counter && CommitteePositionLeader == that.CommitteePositionLeader && TransactionPositionLeader == that.TransactionPositionLeader && ZoneIndex == that.ZoneIndex && Objects.equal(committeeBlock, that.committeeBlock) && Objects.equal(transactionBlock, that.transactionBlock) && Objects.equal(securityHeader, that.securityHeader);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(consensus_state, epoch_counter, committeeBlock, transactionBlock, CommitteePositionLeader, TransactionPositionLeader, securityHeader, ZoneIndex);
    }

    @Override
    public String toString() {
        return "CachedNetworkData{" +
                "consensus_state=" + consensus_state +
                ", epoch_counter=" + epoch_counter +
                ", committeeBlock=" + committeeBlock +
                ", transactionBlock=" + transactionBlock +
                ", CommitteePositionLeader=" + CommitteePositionLeader +
                ", TransactionPositionLeader=" + TransactionPositionLeader +
                ", securityHeader=" + securityHeader +
                ", ZoneIndex=" + ZoneIndex +
                '}';
    }
}
