package io.Adrestus.core;

import com.google.common.base.Objects;
import io.Adrestus.core.RingBuffer.handler.blocks.DisruptorBlock;
import io.Adrestus.core.RingBuffer.handler.blocks.DisruptorBlockVisitor;
import io.Adrestus.crypto.bls.BLSSignatureData;
import io.Adrestus.crypto.bls.model.BLSPublicKey;
import io.activej.serializer.annotations.Serialize;
import io.activej.serializer.annotations.SerializeNullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TransactionBlock extends AbstractBlock implements BlockFactory, DisruptorBlock, Serializable {
    private int Zone;
    private List<Transaction> TransactionList;
    private List<StakingTransaction> StakingTransactionList;
    private InboundRelay Inbound;
    private OutBoundRelay Outbound;
    private String TransactionProposer;
    private BLSPublicKey LeaderPublicKey;
    private String MerkleRoot;
    private String PatriciaMerkleRoot;

    public TransactionBlock(String previousHash, int height, int Generation, int zone, List<Transaction> transactionList, String transactionProposer) {
        super(previousHash, height, Generation);
        this.Zone = zone;
        this.TransactionList = transactionList;
        this.TransactionProposer = transactionProposer;
    }

    public TransactionBlock(String hash, String previousHash, int size, int height, String timestamp) {
        super(hash, previousHash, size, height, timestamp);
        this.LeaderPublicKey = new BLSPublicKey();
        this.Zone = 0;
        this.TransactionList = new ArrayList<>();
        this.StakingTransactionList = new ArrayList<>();
        this.Inbound = new InboundRelay();
        this.Outbound = new OutBoundRelay();
        this.TransactionProposer = "";
        this.MerkleRoot = "";
        this.PatriciaMerkleRoot = "";
    }

    public TransactionBlock(String hash, String previousHash, int size, int height, int generation, int viewID, String timestamp, int zone) {
        super(hash, previousHash, size, height, generation, viewID, timestamp);
        this.LeaderPublicKey = new BLSPublicKey();
        this.Zone = zone;
        this.TransactionList = new ArrayList<>();
        this.StakingTransactionList = new ArrayList<>();
        this.Inbound = new InboundRelay();
        this.Outbound = new OutBoundRelay();
        this.TransactionProposer = "";
        this.MerkleRoot = "";
        this.PatriciaMerkleRoot = "";
    }

    public TransactionBlock() {
        super();
        this.LeaderPublicKey = new BLSPublicKey();
        this.Zone = 0;
        this.TransactionList = new ArrayList<>();
        this.StakingTransactionList = new ArrayList<>();
        this.Inbound = new InboundRelay();
        this.Outbound = new OutBoundRelay();
        this.TransactionProposer = "";
        this.MerkleRoot = "";
        this.PatriciaMerkleRoot = "";
    }

    @Override
    public void accept(BlockForge visitor) throws Exception {
        visitor.forgeTransactionBlock(this);
    }

    @Override
    public void accept(BlockInvent visitor) throws Exception {
        visitor.InventTransactionBlock(this);
    }

    @Override
    public void accept(DisruptorBlockVisitor disruptorBlockVisitor) {
        disruptorBlockVisitor.visit(this);
    }

    @Serialize
    public int getZone() {
        return Zone;
    }

    public void setZone(int zone) {
        Zone = zone;
    }

    @Serialize
    public List<Transaction> getTransactionList() {
        return TransactionList;
    }

    public void setTransactionList(List<Transaction> transactionList) {
        TransactionList = transactionList;
    }

    @Serialize
    public List<StakingTransaction> getStakingTransactionList() {
        return StakingTransactionList;
    }

    public void setStakingTransactionList(List<StakingTransaction> stakingTransactionList) {
        StakingTransactionList = stakingTransactionList;
    }

    @Serialize
    public InboundRelay getInbound() {
        return Inbound;
    }

    public void setInbound(InboundRelay inbound) {
        Inbound = inbound;
    }

    @Serialize
    public OutBoundRelay getOutbound() {
        return Outbound;
    }

    public void setOutbound(OutBoundRelay outbound) {
        Outbound = outbound;
    }

    @Serialize
    public String getTransactionProposer() {
        return TransactionProposer;
    }

    public void setTransactionProposer(String transactionProposer) {
        TransactionProposer = transactionProposer;
    }

    @Serialize
    public String getMerkleRoot() {
        return MerkleRoot;
    }

    public void setMerkleRoot(String merkleRoot) {
        MerkleRoot = merkleRoot;
    }

    @Serialize
    public BLSPublicKey getLeaderPublicKey() {
        return LeaderPublicKey;
    }

    public void setLeaderPublicKey(BLSPublicKey leaderPublicKey) {
        LeaderPublicKey = leaderPublicKey;
    }

    @Serialize
    public String getPatriciaMerkleRoot() {
        return PatriciaMerkleRoot;
    }

    public void setPatriciaMerkleRoot(String patriciaMerkleRoot) {
        PatriciaMerkleRoot = patriciaMerkleRoot;
    }

    @Override
    public Map<BLSPublicKey, BLSSignatureData> getSignatureData() {
        return super.getSignatureData();
    }

    @Override
    public void setSignatureData(Map<BLSPublicKey, BLSSignatureData> signatureData) {
        super.setSignatureData(signatureData);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        TransactionBlock that = (TransactionBlock) o;
        return Zone == that.Zone && Objects.equal(TransactionList, that.TransactionList) && Objects.equal(StakingTransactionList, that.StakingTransactionList) && Objects.equal(Inbound, that.Inbound) && Objects.equal(Outbound, that.Outbound) && Objects.equal(TransactionProposer, that.TransactionProposer) && Objects.equal(LeaderPublicKey, that.LeaderPublicKey) && Objects.equal(MerkleRoot, that.MerkleRoot) && Objects.equal(PatriciaMerkleRoot, that.PatriciaMerkleRoot);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), Zone, TransactionList, StakingTransactionList, Inbound, Outbound, TransactionProposer, LeaderPublicKey, MerkleRoot, PatriciaMerkleRoot);
    }

    @Override
    public String toString() {
        return "TransactionBlock{" +
                "Zone=" + Zone +
                ", TransactionList=" + TransactionList +
                ", StakingTransactionList=" + StakingTransactionList +
                ", Inbound=" + Inbound +
                ", Outbound=" + Outbound +
                ", TransactionProposer='" + TransactionProposer + '\'' +
                ", LeaderPublicKey=" + LeaderPublicKey +
                ", MerkleRoot='" + MerkleRoot + '\'' +
                ", PatriciaMerkleRoot='" + PatriciaMerkleRoot + '\'' +
                '}';
    }
}
