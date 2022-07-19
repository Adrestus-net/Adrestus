package io.Adrestus.core;

import io.Adrestus.crypto.elliptic.SignatureData;

import java.util.Objects;

public class RewardsTransaction extends Transaction{
    private String DelegatorAddress;

    public RewardsTransaction(TransactionStatus status, int zoneFrom, int blockNumber, String from, double Amount, int Nonce, SignatureData signature, String delegatorAddress) {
        super(TransactionType.REWARDS, status, zoneFrom, blockNumber, from, Amount, Nonce, signature);
        DelegatorAddress = delegatorAddress;
    }

    public String getDelegatorAddress() {
        return DelegatorAddress;
    }

    public void setDelegatorAddress(String delegatorAddress) {
        DelegatorAddress = delegatorAddress;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        RewardsTransaction that = (RewardsTransaction) o;
        return Objects.equals(DelegatorAddress, that.DelegatorAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), DelegatorAddress);
    }

    @Override
    public String toString() {
        return "RewardsTransaction{" +
                "DelegatorAddress='" + DelegatorAddress + '\'' +
                '}';
    }
}
