package io.Adrestus.core;

import io.Adrestus.crypto.elliptic.SignatureData;
import io.activej.serializer.annotations.Serialize;

import java.util.Objects;

public class DelegateTransaction extends Transaction {

    private String DelegatorAddress;
    private String ValidatorAddress;


    public DelegateTransaction(String delegatorAddress, String validatorAddress) {
        DelegatorAddress = delegatorAddress;
        ValidatorAddress = validatorAddress;
    }

    public DelegateTransaction(String hash, TransactionType type, TransactionStatus status, int zoneFrom, int zoneTo, String timestamp, int blockNumber, String from, String to, double amount, double transactionFee, int nonce, SignatureData signature, String delegatorAddress, String validatorAddress) {
        super(hash, type, status, zoneFrom, zoneTo, timestamp, blockNumber, from, to, amount, transactionFee, nonce, signature);
        DelegatorAddress = delegatorAddress;
        ValidatorAddress = validatorAddress;
    }

    public DelegateTransaction() {
        DelegatorAddress = "";
        ValidatorAddress = "";
    }

    @Serialize
    public String getDelegatorAddress() {
        return DelegatorAddress;
    }

    public void setDelegatorAddress(String delegatorAddress) {
        DelegatorAddress = delegatorAddress;
    }

    @Serialize
    public String getValidatorAddress() {
        return ValidatorAddress;
    }

    public void setValidatorAddress(String validatorAddress) {
        ValidatorAddress = validatorAddress;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        DelegateTransaction that = (DelegateTransaction) o;
        return Objects.equals(DelegatorAddress, that.DelegatorAddress) && Objects.equals(ValidatorAddress, that.ValidatorAddress);
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), DelegatorAddress, ValidatorAddress);
    }

    @Override
    public String toString() {
        return "DelegateTransaction{" +
                "DelegatorAddress='" + DelegatorAddress + '\'' +
                ", ValidatorAddress='" + ValidatorAddress + '\'' +
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
