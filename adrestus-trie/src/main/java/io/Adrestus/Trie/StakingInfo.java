package io.Adrestus.Trie;

import io.activej.serializer.annotations.Serialize;

import java.io.Serializable;
import java.util.Objects;

public class StakingInfo implements Serializable, Cloneable {
    private String Name;
    private String Details;
    private String Website;
    private String Identity;
    private double CommissionRate;

    public StakingInfo() {
        this.Name = "";
        this.CommissionRate = 0;
        this.Identity = "";
        this.Website = "";
        this.Details = "";
    }

    public StakingInfo(String name, double commissionRate, String identity, String website, String details) {
        this.Name = name;
        this.CommissionRate = commissionRate;
        this.Identity = identity;
        this.Website = website;
        this.Details = details;
    }

    @Serialize
    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    @Serialize
    public String getIdentity() {
        return Identity;
    }

    public void setIdentity(String identity) {
        Identity = identity;
    }

    @Serialize
    public String getWebsite() {
        return Website;
    }

    public void setWebsite(String website) {
        Website = website;
    }

    @Serialize
    public String getDetails() {
        return Details;
    }

    public void setDetails(String details) {
        Details = details;
    }

    @Serialize
    public double getCommissionRate() {
        return CommissionRate;
    }

    public void setCommissionRate(double commissionRate) {
        CommissionRate = commissionRate;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StakingInfo that = (StakingInfo) o;
        return Double.compare(CommissionRate, that.CommissionRate) == 0 && Objects.equals(Name, that.Name) && Objects.equals(Details, that.Details) && Objects.equals(Website, that.Website) && Objects.equals(Identity, that.Identity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Name, Details, Website, Identity, CommissionRate);
    }

    @Override
    public String toString() {
        return "StakingInfo{" +
                "Name='" + Name + '\'' +
                ", Details='" + Details + '\'' +
                ", Website='" + Website + '\'' +
                ", Identity='" + Identity + '\'' +
                ", CommissionRate=" + CommissionRate +
                '}';
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
