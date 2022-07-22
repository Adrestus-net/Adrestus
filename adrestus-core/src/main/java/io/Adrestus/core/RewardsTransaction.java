package io.Adrestus.core;

import io.Adrestus.crypto.elliptic.ECDSASignature;
import io.activej.serializer.annotations.Serialize;


public class RewardsTransaction extends Transaction {
    private String DelegatorAddress;

    public RewardsTransaction(String DelegatorAddress) {
        this.DelegatorAddress = DelegatorAddress;
    }

    public RewardsTransaction(String hash, TransactionType type, TransactionStatus status, int zoneFrom, int zoneTo, String timestamp, int blockNumber, String from, String to, double amount, double transactionFee, int nonce, ECDSASignature signature, String delegatorAddress) {
        super(hash, type, status, zoneFrom, zoneTo, timestamp, blockNumber, from, to, amount, transactionFee, nonce, signature);
        DelegatorAddress = delegatorAddress;
    }

    public RewardsTransaction() {
    }

    @Serialize
    public String getDelegatorAddress() {
        return DelegatorAddress;
    }

    public void setDelegatorAddress(String delegatorAddress) {
        DelegatorAddress = delegatorAddress;
    }
    public void d(){
        System.out.println("reward");
    }
    @Override
    public String toString() {
        return "RewardsTransaction{" +
                "DelegatorAddress='" + DelegatorAddress + '\'' +
                ", Type=" + Type +
                ", Status=" + Status +
                ", ZoneFrom=" + ZoneFrom +
                ", ZoneTo=" + ZoneTo +
                ", timestamp='" + timestamp + '\'' +
                ", BlockNumber=" + BlockNumber +
                ", From='" + From + '\'' +
                ", To='" + To + '\'' +
                ", Amount=" + Amount +
                ", TransactionFee=" + TransactionFee +
                ", Nonce=" + Nonce +
                ", Signature=" + Signature +
                '}';
    }
}
