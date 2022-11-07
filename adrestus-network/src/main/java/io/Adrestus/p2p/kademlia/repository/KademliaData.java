package io.Adrestus.p2p.kademlia.repository;

import com.google.common.base.Objects;
import io.Adrestus.crypto.bls.model.BLSPublicKey;
import io.Adrestus.crypto.elliptic.SignatureData;
import io.Adrestus.p2p.kademlia.common.NettyConnectionInfo;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Arrays;

public class KademliaData implements Serializable, Cloneable {
    private String Hash;
    private BLSPublicKey ValidatorBlSPublicKey;
    private ValidatorAddressData addressData;
    private NettyConnectionInfo nettyConnectionInfo;

    public KademliaData() {
    }

    public KademliaData(String hash, NettyConnectionInfo nettyConnectionInfo) {
        this.Hash = hash;
        this.nettyConnectionInfo = nettyConnectionInfo;
        this.ValidatorBlSPublicKey = new BLSPublicKey();
    }


    public KademliaData(String hash, ValidatorAddressData addressData, NettyConnectionInfo nettyConnectionInfo) {
        this.Hash = hash;
        this.addressData = addressData;
        this.nettyConnectionInfo = nettyConnectionInfo;
        this.ValidatorBlSPublicKey = new BLSPublicKey();
    }

    public KademliaData(ValidatorAddressData addressData, NettyConnectionInfo nettyConnectionInfo) {
        this.Hash = "";
        this.addressData = addressData;
        this.nettyConnectionInfo = nettyConnectionInfo;
        this.ValidatorBlSPublicKey = new BLSPublicKey();
    }

    public KademliaData(ValidatorAddressData addressData) {
        this.Hash = "";
        this.addressData = addressData;
        this.nettyConnectionInfo = new NettyConnectionInfo("", 0);
    }


    public String getHash() {
        return Hash;
    }

    public void setHash(String hash) {
        this.Hash = hash;
    }

    public ValidatorAddressData getAddressData() {
        return addressData;
    }

    public void setAddressData(ValidatorAddressData addressData) {
        this.addressData = addressData;
    }


    public NettyConnectionInfo getNettyConnectionInfo() {
        return nettyConnectionInfo;
    }

    public void setNettyConnectionInfo(NettyConnectionInfo nettyConnectionInfo) {
        this.nettyConnectionInfo = nettyConnectionInfo;
    }

    public BLSPublicKey getValidatorBlSPublicKey() {
        return ValidatorBlSPublicKey;
    }

    public void setValidatorBlSPublicKey(BLSPublicKey validatorBlSPublicKey) {
        ValidatorBlSPublicKey = validatorBlSPublicKey;
    }

    public static final class ValidatorAddressData implements Serializable {
        private String Address;
        private BigInteger ECDSAPublicKey;
        private SignatureData ECDSASignature;

        public ValidatorAddressData() {
        }

        public ValidatorAddressData(String address, BigInteger ECDSAPublicKey, SignatureData ECDSASignature) {
            Address = address;
            this.ECDSAPublicKey = ECDSAPublicKey;
            this.ECDSASignature = ECDSASignature;
        }

        public String getAddress() {
            return Address;
        }

        public void setAddress(String address) {
            Address = address;
        }

        public BigInteger getECDSAPublicKey() {
            return ECDSAPublicKey;
        }

        public void setECDSAPublicKey(BigInteger ECDSAPublicKey) {
            this.ECDSAPublicKey = ECDSAPublicKey;
        }

        public SignatureData getECDSASignature() {
            return ECDSASignature;
        }

        public void setECDSASignature(SignatureData ECDSASignature) {
            this.ECDSASignature = ECDSASignature;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ValidatorAddressData that = (ValidatorAddressData) o;
            return Objects.equal(Address, that.Address) && Objects.equal(ECDSAPublicKey, that.ECDSAPublicKey)
                    && Arrays.equals(ECDSASignature.getR(), that.ECDSASignature.getR())
                    && Objects.equal(ECDSASignature.getV(), that.ECDSASignature.getV())
                    && Arrays.equals(ECDSASignature.getS(), that.ECDSASignature.getS());
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(Address, ECDSAPublicKey, ECDSASignature);
        }

        @Override
        public String toString() {
            return "ValidatorAddressData{" +
                    "Address='" + Address + '\'' +
                    ", ECDSAPublicKey=" + ECDSAPublicKey +
                    ", ECDSASignature=" + ECDSASignature +
                    '}';
        }
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KademliaData that = (KademliaData) o;
        return Hash.equals(that.Hash) && Objects.equal(addressData, that.addressData);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(Hash, addressData);
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public String toString() {
        return "KademliaData{" +
                "Hash='" + Hash + '\'' +
                ", addressData=" + addressData +
                ", nettyConnectionInfo=" + nettyConnectionInfo +
                '}';
    }
}
