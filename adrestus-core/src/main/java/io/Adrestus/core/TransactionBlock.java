package io.Adrestus.core;

import io.Adrestus.crypto.bls.model.BLSPublicKey;
import io.Adrestus.crypto.bls.model.Signature;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TransactionBlock extends AbstractBlock implements BlockFactory {
    private int Zone;
    private List<Transaction> TransactionList;
    private List<StakingTransaction> StakingTransactionList;
    private Map<Integer, InboundRelay> Inbound;
    private Map<Integer, OutBoundRelay> Outbound;
    private String TransactionProposer;
    private String MerkleRoot;

    private List<BLSPublicKey> BLSPubKeyList;
    private List<Signature> SignatureList;


    public TransactionBlock(String previousHash, int height, int Generation, int zone, List<Transaction> transactionList, String transactionProposer) {
        super(previousHash, height, Generation);
        this.Zone = zone;
        this.TransactionList = transactionList;
        this.TransactionProposer = transactionProposer;
    }

    @Override
    public void accept(BlockForge visitor) {
        visitor.forgeTransactionBlock(this);
    }


    public int getZone() {
        return Zone;
    }

    public void setZone(int zone) {
        Zone = zone;
    }

    public List<Transaction> getTransactionList() {
        return TransactionList;
    }

    public void setTransactionList(List<Transaction> transactionList) {
        TransactionList = transactionList;
    }

    public List<StakingTransaction> getStakingTransactionList() {
        return StakingTransactionList;
    }

    public void setStakingTransactionList(List<StakingTransaction> stakingTransactionList) {
        StakingTransactionList = stakingTransactionList;
    }

    public Map<Integer, InboundRelay> getInbound() {
        return Inbound;
    }

    public void setInbound(Map<Integer, InboundRelay> inbound) {
        Inbound = inbound;
    }

    public Map<Integer, OutBoundRelay> getOutbound() {
        return Outbound;
    }

    public void setOutbound(Map<Integer, OutBoundRelay> outbound) {
        Outbound = outbound;
    }

    public String getTransactionProposer() {
        return TransactionProposer;
    }

    public void setTransactionProposer(String transactionProposer) {
        TransactionProposer = transactionProposer;
    }

    public String getMerkleRoot() {
        return MerkleRoot;
    }

    public void setMerkleRoot(String merkleRoot) {
        MerkleRoot = merkleRoot;
    }

    public List<BLSPublicKey> getBLSPubKeyList() {
        return BLSPubKeyList;
    }

    public void setBLSPubKeyList(List<BLSPublicKey> BLSPubKeyList) {
        this.BLSPubKeyList = BLSPubKeyList;
    }

    public List<Signature> getSignatureList() {
        return SignatureList;
    }

    public void setSignatureList(List<Signature> signatureList) {
        SignatureList = signatureList;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        TransactionBlock that = (TransactionBlock) o;
        return Zone == that.Zone && Objects.equals(TransactionList, that.TransactionList) && Objects.equals(StakingTransactionList, that.StakingTransactionList) && Objects.equals(Inbound, that.Inbound) && Objects.equals(Outbound, that.Outbound) && Objects.equals(TransactionProposer, that.TransactionProposer) && Objects.equals(MerkleRoot, that.MerkleRoot) && Objects.equals(BLSPubKeyList, that.BLSPubKeyList) && Objects.equals(SignatureList, that.SignatureList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), Zone, TransactionList, StakingTransactionList, Inbound, Outbound, TransactionProposer, MerkleRoot, BLSPubKeyList, SignatureList);
    }

    private class InboundRelay {
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

    private class OutBoundRelay {
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
