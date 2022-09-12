package io.Adrestus.core;

import com.google.common.base.Objects;
import io.Adrestus.core.RingBuffer.handler.blocks.DisruptorBlock;
import io.Adrestus.core.RingBuffer.handler.blocks.DisruptorBlockVisitor;
import io.Adrestus.crypto.bls.model.BLSPublicKey;
import io.activej.serializer.annotations.Serialize;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransactionBlock extends AbstractBlock implements BlockFactory, DisruptorBlock {
    private int Zone;
    private List<Transaction> TransactionList;
    private List<StakingTransaction> StakingTransactionList;
    private Map<Integer, InboundRelay> Inbound;
    private Map<Integer, OutBoundRelay> Outbound;
    private String TransactionProposer;
    private BLSPublicKey LeaderPublicKey;
    private String MerkleRoot;


    public TransactionBlock(String previousHash, int height, int Generation, int zone, List<Transaction> transactionList, String transactionProposer) {
        super(previousHash, height, Generation);
        this.Zone = zone;
        this.TransactionList = transactionList;
        this.TransactionProposer = transactionProposer;
    }

    public TransactionBlock(String hash, String previousHash, int size, int height, String timestamp) {
        super(hash, previousHash, size, height, timestamp);
    }

    public TransactionBlock() {
        super();
        this.LeaderPublicKey = new BLSPublicKey();
        this.Zone = 0;
        this.TransactionList = new ArrayList<>();
        this.StakingTransactionList = new ArrayList<>();
        this.Inbound = new HashMap<>();
        this.Outbound = new HashMap<>();
        this.TransactionProposer = "";
        this.MerkleRoot = "";
    }

    @Override
    public void accept(BlockForge visitor) throws Exception {
        visitor.forgeTransactionBlock(this);
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
    public Map<Integer, InboundRelay> getInbound() {
        return Inbound;
    }

    public void setInbound(Map<Integer, InboundRelay> inbound) {
        Inbound = inbound;
    }

    @Serialize
    public Map<Integer, OutBoundRelay> getOutbound() {
        return Outbound;
    }

    public void setOutbound(Map<Integer, OutBoundRelay> outbound) {
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


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        TransactionBlock that = (TransactionBlock) o;
        return Zone == that.Zone && Objects.equal(TransactionList, that.TransactionList) && Objects.equal(StakingTransactionList, that.StakingTransactionList) && Objects.equal(Inbound, that.Inbound) && Objects.equal(Outbound, that.Outbound) && Objects.equal(TransactionProposer, that.TransactionProposer) && Objects.equal(LeaderPublicKey.toRaw(), that.LeaderPublicKey.toRaw()) && Objects.equal(MerkleRoot, that.MerkleRoot);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), Zone, TransactionList, StakingTransactionList, Inbound, Outbound, TransactionProposer, LeaderPublicKey, MerkleRoot);
    }

    @Override
    public String toString() {
        return super.toString() + " " +
                "TransactionBlock{" +
                "Zone=" + Zone +
                ", TransactionList=" + TransactionList +
                ", StakingTransactionList=" + StakingTransactionList +
                ", Inbound=" + Inbound +
                ", Outbound=" + Outbound +
                ", TransactionProposer='" + TransactionProposer + '\'' +
                ", LeaderPublicKey=" + LeaderPublicKey +
                ", MerkleRoot='" + MerkleRoot + '\'' +
                '}';
    }

    public static class InboundRelay {
        private List<Receipt> Receipt;
        private String InboundMerkleRoot;

        public InboundRelay(List<Receipt> receipt, String inboundMerkleRoot) {
            Receipt = receipt;
            InboundMerkleRoot = inboundMerkleRoot;
        }

        public InboundRelay() {
        }

        public List<Receipt> getReceipt() {
            return Receipt;
        }

        public void setReceipt(List<Receipt> receipt) {
            Receipt = receipt;
        }

        public String getInboundMerkleRoot() {
            return InboundMerkleRoot;
        }

        public void setInboundMerkleRoot(String inboundMerkleRoot) {
            InboundMerkleRoot = inboundMerkleRoot;
        }
    }

    public static class OutBoundRelay {
        private List<Integer> Outbound;
        private String OutboundMerkleRoot;

        public OutBoundRelay(List<Integer> outbound, String outboundMerkleRoot) {
            Outbound = outbound;
            OutboundMerkleRoot = outboundMerkleRoot;
        }

        public OutBoundRelay() {
        }

        public List<Integer> getOutbound() {
            return Outbound;
        }

        public void setOutbound(List<Integer> outbound) {
            Outbound = outbound;
        }

        public String getOutboundMerkleRoot() {
            return OutboundMerkleRoot;
        }

        public void setOutboundMerkleRoot(String outboundMerkleRoot) {
            OutboundMerkleRoot = outboundMerkleRoot;
        }
    }
}
