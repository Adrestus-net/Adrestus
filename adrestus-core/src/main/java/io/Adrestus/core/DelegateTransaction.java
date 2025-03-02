package io.Adrestus.core;

import io.Adrestus.crypto.elliptic.ECDSASignatureData;
import io.activej.serializer.annotations.Serialize;

import java.math.BigDecimal;
import java.util.Objects;

public class DelegateTransaction extends Transaction {

    private String DelegatorAddress;
    private String ValidatorAddress;


    public DelegateTransaction(String delegatorAddress, String validatorAddress) {
        this.DelegatorAddress = delegatorAddress;
        this.ValidatorAddress = validatorAddress;
    }

    public DelegateTransaction(String hash, TransactionType type, StatusType status, int zoneFrom, int zoneTo, String timestamp, int blockNumber, String from, String to, BigDecimal amount, BigDecimal transactionFee, int nonce, ECDSASignatureData signature, String delegatorAddress, String validatorAddress) {
        super(hash, type, status, zoneFrom, zoneTo, timestamp, blockNumber, from, to, amount, transactionFee, nonce, signature);
        this.DelegatorAddress = delegatorAddress;
        this.ValidatorAddress = validatorAddress;
    }

    public DelegateTransaction() {
        super();
        this.DelegatorAddress = "";
        this.ValidatorAddress = "";
    }

    @Override
    public void accept(TransactionUnitVisitor visitor) {
        visitor.visit(this);
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
    public DelegateTransaction clone() {
        return (DelegateTransaction) super.clone();
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
