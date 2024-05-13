package io.Adrestus.core;

import io.Adrestus.crypto.elliptic.ECDSASignatureData;

public class UnstakingTransaction extends StakingTransaction {

    public UnstakingTransaction(String hash, TransactionType type, StatusType status, int zoneFrom, int zoneTo, String timestamp, int blockNumber, String from, String to, double amount, double transactionFee, int nonce, ECDSASignatureData signature, String name, String details, String website, String identity, double commissionRate, String validatorAddress) {
        super(hash, type, status, zoneFrom, zoneTo, timestamp, blockNumber, from, to, amount, transactionFee, nonce, signature, name, details, website, identity, commissionRate, validatorAddress);
    }

    public UnstakingTransaction(String name, String details, String website, String identity, double commissionRate, String validatorAddress) {
        super(name, details, website, identity, commissionRate, validatorAddress);
    }

    public UnstakingTransaction() {
        super();
    }

    @Override
    public void accept(TransactionUnitVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public void setValidatorAddress(String validatorAddress) {
        super.setValidatorAddress(validatorAddress);
    }

    @Override
    public String getValidatorAddress() {
        return super.getValidatorAddress();
    }

    @Override
    public void setCommissionRate(double commissionRate) {
        super.setCommissionRate(commissionRate);
    }

    @Override
    public double getCommissionRate() {
        return super.getCommissionRate();
    }

    @Override
    public void setIdentity(String identity) {
        super.setIdentity(identity);
    }

    @Override
    public String getIdentity() {
        return super.getIdentity();
    }

    @Override
    public void setWebsite(String website) {
        super.setWebsite(website);
    }

    @Override
    public String getWebsite() {
        return super.getWebsite();
    }

    @Override
    public void setDetails(String details) {
        super.setDetails(details);
    }

    @Override
    public String getDetails() {
        return super.getDetails();
    }

    @Override
    public void setName(String name) {
        super.setName(name);
    }

    @Override
    public String getName() {
        return super.getName();
    }

    @Override
    public String toString() {
        return "UnstakingTransaction{" +
                "Signature=" + Signature +
                ", YAxis=" + YAxis +
                ", XAxis=" + XAxis +
                ", Nonce=" + Nonce +
                ", AmountWithTransactionFee=" + AmountWithTransactionFee +
                ", Amount=" + Amount +
                ", To='" + To + '\'' +
                ", From='" + From + '\'' +
                ", BlockNumber=" + BlockNumber +
                ", timestamp='" + timestamp + '\'' +
                ", ZoneTo=" + ZoneTo +
                ", ZoneFrom=" + ZoneFrom +
                ", Status=" + Status +
                ", Type=" + Type +
                ", Hash='" + Hash + '\'' +
                '}';
    }
}
