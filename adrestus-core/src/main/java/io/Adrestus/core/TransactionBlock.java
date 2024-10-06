package io.Adrestus.core;

import io.Adrestus.core.RingBuffer.handler.blocks.DisruptorBlock;
import io.Adrestus.core.RingBuffer.handler.blocks.DisruptorBlockVisitor;
import io.Adrestus.crypto.bls.BLSSignatureData;
import io.Adrestus.crypto.bls.model.BLSPublicKey;
import io.activej.serializer.annotations.Serialize;

import java.io.*;
import java.util.*;

public class TransactionBlock extends AbstractBlock implements BlockFactory, DisruptorBlock, Serializable {
    private int Zone;
    private List<Transaction> TransactionList;
    private List<StakingTransaction> StakingTransactionList;
    private InboundRelay Inbound;
    private OutBoundRelay Outbound;
    private String MerkleRoot;
    private String PatriciaMerkleRoot;


    public TransactionBlock(String previousHash, int height, int Generation, int zone, List<Transaction> transactionList, String blockProposer) {
        super(previousHash, height, Generation,blockProposer);
        this.Zone = zone;
        this.TransactionList = transactionList;
    }

    public TransactionBlock(String hash, String previousHash, int size, int height, String timestamp) {
        super(hash, previousHash, size, height, timestamp);
        this.Zone = 0;
        this.TransactionList = new ArrayList<>();
        this.StakingTransactionList = new ArrayList<>();
        this.Inbound = new InboundRelay();
        this.Outbound = new OutBoundRelay();
        this.MerkleRoot = "";
        this.PatriciaMerkleRoot = "";
    }

    public TransactionBlock(String hash, String previousHash, int size, int height, int generation, int viewID, String timestamp, int zone) {
        super(hash, previousHash, size, height, generation, viewID, timestamp);
        this.Zone = zone;
        this.TransactionList = new ArrayList<>();
        this.StakingTransactionList = new ArrayList<>();
        this.Inbound = new InboundRelay();
        this.Outbound = new OutBoundRelay();
        this.MerkleRoot = "";
        this.PatriciaMerkleRoot = "";
    }

    public TransactionBlock() {
        super();
        this.Zone = 0;
        this.TransactionList = new ArrayList<>();
        this.StakingTransactionList = new ArrayList<>();
        this.Inbound = new InboundRelay();
        this.Outbound = new OutBoundRelay();
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
    public String getBlockProposer() {
        return super.getBlockProposer();
    }

    public void setBlockProposer(String blockProposer) {
        super.setBlockProposer(blockProposer);
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
        return super.getLeaderPublicKey();
    }

    public void setLeaderPublicKey(BLSPublicKey leaderPublicKey) {
        super.setLeaderPublicKey(leaderPublicKey);
    }

    @Serialize
    public String getPatriciaMerkleRoot() {
        return PatriciaMerkleRoot;
    }

    public void setPatriciaMerkleRoot(String patriciaMerkleRoot) {
        PatriciaMerkleRoot = patriciaMerkleRoot;
    }

    @Override
    public TreeMap<BLSPublicKey, BLSSignatureData> getSignatureData() {
        return super.getSignatureData();
    }

    @Override
    public void setSignatureData(TreeMap<BLSPublicKey, BLSSignatureData> signatureData) {
        super.setSignatureData(signatureData);
    }

    @Override
    public void AddAllSignatureData(HashMap<BLSPublicKey, BLSSignatureData> signatureData) {
        super.AddAllSignatureData(signatureData);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        TransactionBlock that = (TransactionBlock) o;
        return Zone == that.Zone && Objects.equals(TransactionList, that.TransactionList) && Objects.equals(StakingTransactionList, that.StakingTransactionList) && Objects.equals(Inbound, that.Inbound) && Objects.equals(Outbound, that.Outbound) && Objects.equals(MerkleRoot, that.MerkleRoot) && Objects.equals(PatriciaMerkleRoot, that.PatriciaMerkleRoot);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), Zone, TransactionList, StakingTransactionList, Inbound, Outbound, MerkleRoot, PatriciaMerkleRoot);
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        try {
            ByteArrayOutputStream bOut = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bOut);
            out.writeObject(super.clone());
            out.close();

            ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bOut.toByteArray()));
            TransactionBlock copy = (TransactionBlock) in.readObject();
            in.close();

            return copy;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return "TransactionBlock{" +
                "Zone=" + Zone +
                ", TransactionList=" + TransactionList +
                ", StakingTransactionList=" + StakingTransactionList +
                ", Inbound=" + Inbound +
                ", Outbound=" + Outbound +
                ", MerkleRoot='" + MerkleRoot + '\'' +
                ", PatriciaMerkleRoot='" + PatriciaMerkleRoot + '\'' +
                '}';
    }
}
