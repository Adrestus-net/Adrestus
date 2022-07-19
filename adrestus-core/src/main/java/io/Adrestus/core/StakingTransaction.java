package io.Adrestus.core;

import io.Adrestus.crypto.elliptic.SignatureData;

import java.util.Objects;

public class StakingTransaction extends Transaction{
    private String Name;
    private String Details;
    private String Website;
    private String Identity;
    private double CommissionRate;
    private String ValidatorAddress;

    public StakingTransaction(TransactionStatus status, int zoneFrom, int zoneTo, int blockNumber, java.sql.Timestamp timestamp, String from, String to, double amount, double transactionFee, int nonce, SignatureData signature, String name, double amount1, String details, String website, String identity, double commissionRate, String validatorAddress) {
        super(status, zoneFrom, zoneTo, blockNumber, timestamp, from, to, amount, transactionFee, nonce, signature);
        Name = name;
        Details = details;
        Website = website;
        Identity = identity;
        CommissionRate = commissionRate;
        ValidatorAddress = validatorAddress;
    }

    public StakingTransaction(TransactionType Type, TransactionStatus status, int zoneFrom, int blockNumber, String from, int Nonce, SignatureData signature, String name, double amount, String details, String website, String identity, double commissionRate, String validatorAddress) {
        super(Type, status, zoneFrom, blockNumber, from,amount, Nonce, signature);
        Name = name;
        Details = details;
        Website = website;
        Identity = identity;
        CommissionRate = commissionRate;
        ValidatorAddress = validatorAddress;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getDetails() {
        return Details;
    }

    public void setDetails(String details) {
        Details = details;
    }

    public String getWebsite() {
        return Website;
    }

    public void setWebsite(String website) {
        Website = website;
    }

    public String getIdentity() {
        return Identity;
    }

    public void setIdentity(String identity) {
        Identity = identity;
    }

    public double getCommissionRate() {
        return CommissionRate;
    }

    public void setCommissionRate(double commissionRate) {
        CommissionRate = commissionRate;
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
        StakingTransaction that = (StakingTransaction) o;
        return Double.compare(that.CommissionRate, CommissionRate) == 0 && Objects.equals(Name, that.Name) && Objects.equals(Details, that.Details) && Objects.equals(Website, that.Website) && Objects.equals(Identity, that.Identity) && Objects.equals(ValidatorAddress, that.ValidatorAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), Name, Details, Website, Identity, CommissionRate, ValidatorAddress);
    }

    @Override
    public String toString() {
        return "StakingTransaction{" +
                "Name='" + Name + '\'' +
                ", Details='" + Details + '\'' +
                ", Website='" + Website + '\'' +
                ", Identity='" + Identity + '\'' +
                ", CommissionRate=" + CommissionRate +
                ", ValidatorAddress='" + ValidatorAddress + '\'' +
                '}';
    }
}
