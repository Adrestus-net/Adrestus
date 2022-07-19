package io.Adrestus.core;

import io.Adrestus.crypto.elliptic.SignatureData;
import java.util.Objects;


public class DelegateTransaction extends Transaction{

    private String DelegatorAddress;
    private String ValidatorAddress;

    public DelegateTransaction(TransactionStatus status, int zoneFrom, int zoneTo, int blockNumber, java.sql.Timestamp timestamp, String from, String to, double amount, double transactionFee, int nonce, SignatureData signature, String delegatorAddress, String validatorAddress, double amount1) {
        super(status, zoneFrom, zoneTo, blockNumber, timestamp, from, to, amount, transactionFee, nonce, signature);
        DelegatorAddress = delegatorAddress;
        ValidatorAddress = validatorAddress;
    }

    public DelegateTransaction(TransactionStatus status, int zoneFrom, int zoneTo, int blockNumber, String from, String to, double amount, SignatureData signature, String delegatorAddress, String validatorAddress, double amount1) {
        super(status, zoneFrom, zoneTo, blockNumber, from, to, amount, signature);
        DelegatorAddress = delegatorAddress;
        ValidatorAddress = validatorAddress;
    }


    public String getDelegatorAddress() {
        return DelegatorAddress;
    }

    public void setDelegatorAddress(String delegatorAddress) {
        DelegatorAddress = delegatorAddress;
    }

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
    public int hashCode() {
        return Objects.hash(super.hashCode(), DelegatorAddress, ValidatorAddress);
    }

    @Override
    public String toString() {
        return "DelegateTransaction{" +
                "DelegatorAddress='" + DelegatorAddress + '\'' +
                ", ValidatorAddress='" + ValidatorAddress + '\'' +
                '}';
    }
}
