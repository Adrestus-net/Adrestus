package io.Adrestus.core;

import io.Adrestus.crypto.elliptic.SignatureData;
import io.activej.serializer.annotations.Serialize;


public class RewardsTransaction extends Transaction {
    private String RecipientAddress;

    public RewardsTransaction(String RecipientAddress) {
        this.RecipientAddress = RecipientAddress;
    }

    public RewardsTransaction(String hash, TransactionType type, StatusType status, int zoneFrom, int zoneTo, String timestamp, int blockNumber, String from, String to, double amount, double transactionFee, int nonce, SignatureData signature, String RecipientAddress) {
        super(hash, type, status, zoneFrom, zoneTo, timestamp, blockNumber, from, to, amount, transactionFee, nonce, signature);
        this.RecipientAddress = RecipientAddress;
    }

    public RewardsTransaction() {
    }

    @Serialize
    public String getRecipientAddress() {
        return RecipientAddress;
    }

    public void setRecipientAddress(String RecipientAddress) {
        this.RecipientAddress = RecipientAddress;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public String toString() {
        return "RewardsTransaction{" +
                "RecipientAddress='" + RecipientAddress + '\'' +
                ", Type=" + Type +
                ", Status=" + Status +
                ", ZoneFrom=" + ZoneFrom +
                ", ZoneTo=" + ZoneTo +
                ", timestamp='" + timestamp + '\'' +
                ", BlockNumber=" + BlockNumber +
                ", From='" + From + '\'' +
                ", To='" + To + '\'' +
                ", Amount=" + Amount +
                ", AmountWithTransactionFee=" + AmountWithTransactionFee +
                ", Nonce=" + Nonce +
                ", Signature=" + Signature +
                '}';
    }
}
