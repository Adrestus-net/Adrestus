package io.Adrestus.core;

import io.Adrestus.crypto.elliptic.ECDSASignatureData;
import io.activej.serializer.annotations.Serialize;

import java.util.Objects;


public class RewardsTransaction extends Transaction {
    private String RecipientAddress;


    public RewardsTransaction() {
        super();
    }

    public RewardsTransaction(String RecipientAddress) {
        this.RecipientAddress = RecipientAddress;
    }

    public RewardsTransaction(String hash, TransactionType type, StatusType status, int zoneFrom, int zoneTo, String timestamp, int blockNumber, String from, String to, double amount, double transactionFee, int nonce, ECDSASignatureData signature, String RecipientAddress) {
        super(hash, type, status, zoneFrom, zoneTo, timestamp, blockNumber, from, to, amount, transactionFee, nonce, signature);
        this.RecipientAddress = RecipientAddress;
    }

    public RewardsTransaction(TransactionType type, String RecipientAddress, double amount) {
        super(type, amount);
        this.RecipientAddress = RecipientAddress;
    }


    @Override
    public void accept(TransactionUnitVisitor visitor) {
        visitor.visit(this);
    }

    @Serialize
    public String getRecipientAddress() {
        return RecipientAddress;
    }

    public void setRecipientAddress(String RecipientAddress) {
        this.RecipientAddress = RecipientAddress;
    }

    @Override
    public void infos(String value) {
        if (this.transactionCallback == null)
            return;
        this.transactionCallback.call(value);
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        RewardsTransaction that = (RewardsTransaction) o;
        return Objects.equals(RecipientAddress, that.RecipientAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), RecipientAddress);
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
