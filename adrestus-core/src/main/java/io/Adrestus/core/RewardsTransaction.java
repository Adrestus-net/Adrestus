package io.Adrestus.core;

import io.activej.serializer.annotations.Serialize;


public class RewardsTransaction extends Transaction {
    private String DelegatorAddress;

    public RewardsTransaction(String DelegatorAddress) {
        this.DelegatorAddress = DelegatorAddress;
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
