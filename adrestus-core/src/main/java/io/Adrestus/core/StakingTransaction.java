package io.Adrestus.core;

import io.activej.serializer.annotations.Serialize;

import java.util.Objects;

public class StakingTransaction extends Transaction {
    private String Name;
    private String Details;
    private String Website;
    private String Identity;
    private double CommissionRate;
    private String ValidatorAddress;

    public StakingTransaction() {
    }

    @Serialize
    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    @Serialize
    public String getDetails() {
        return Details;
    }

    public void setDetails(String details) {
        Details = details;
    }

    @Serialize
    public String getWebsite() {
        return Website;
    }

    public void setWebsite(String website) {
        Website = website;
    }

    @Serialize
    public String getIdentity() {
        return Identity;
    }

    public void setIdentity(String identity) {
        Identity = identity;
    }

    @Serialize
    public double getCommissionRate() {
        return CommissionRate;
    }

    public void setCommissionRate(double commissionRate) {
        CommissionRate = commissionRate;
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
