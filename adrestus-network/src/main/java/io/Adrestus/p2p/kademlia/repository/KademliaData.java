package io.Adrestus.p2p.kademlia.repository;

import com.google.common.base.Objects;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import io.Adrestus.crypto.bls.model.BLSPublicKey;
import io.Adrestus.crypto.bls.model.Signature;
import io.Adrestus.crypto.elliptic.SignatureData;
import jdk.swing.interop.SwingInterOpUtils;
import org.identityconnectors.common.StringUtil;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.Arrays;

public class KademliaData implements Serializable,Cloneable{
    private String Hash;
    private BLSPublicKey ValidatorBlSPublicKey;
    private ValidatorAddressData addressData;
    private BootstrapNodeProofs bootstrapNodeProofs;


    public KademliaData() {
    }

    public KademliaData(String hash) {
        this.Hash = hash;
    }

    public KademliaData(String hash, BLSPublicKey validatorBlSPublicKey) {
        Hash = hash;
        ValidatorBlSPublicKey = validatorBlSPublicKey;
    }

    public KademliaData(String hash, ValidatorAddressData addressData, BootstrapNodeProofs bootstrapNodeProofs) {
        this.Hash = hash;
        this.addressData = addressData;
        this.bootstrapNodeProofs = bootstrapNodeProofs;
    }

    public KademliaData(String hash, BLSPublicKey validatorBlSPublicKey, ValidatorAddressData addressData) {
        Hash = hash;
        ValidatorBlSPublicKey = validatorBlSPublicKey;
        this.addressData = addressData;
    }

    public KademliaData(ValidatorAddressData addressData, BootstrapNodeProofs bootstrapNodeProofs) {
        this.addressData = addressData;
        this.bootstrapNodeProofs = bootstrapNodeProofs;
    }

    public KademliaData(ValidatorAddressData addressData) {
        this.addressData = addressData;
        this.Hash="";
        this.bootstrapNodeProofs=new BootstrapNodeProofs();
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

    public BootstrapNodeProofs getBootstrapNodeProofs() {
        return bootstrapNodeProofs;
    }

    public void setAddressData(ValidatorAddressData addressData) {
        this.addressData = addressData;
    }

    public void setBootstrapNodeProofs(BootstrapNodeProofs bootstrapNodeProofs) {
        this.bootstrapNodeProofs = bootstrapNodeProofs;
    }

    public BLSPublicKey getValidatorBlSPublicKey() {
        return ValidatorBlSPublicKey;
    }

    public void setValidatorBlSPublicKey(BLSPublicKey validatorBlSPublicKey) {
        ValidatorBlSPublicKey = validatorBlSPublicKey;
    }

    public static final class ValidatorAddressData implements Serializable{
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

    public static final class BootstrapNodeProofs implements Serializable,Cloneable{
        private BLSPublicKey blsPublicKey;
        private Signature signature;

        public BootstrapNodeProofs(BLSPublicKey blsPublicKey, Signature signature) {
            this.blsPublicKey = blsPublicKey;
            this.signature = signature;
        }

        public BootstrapNodeProofs() {
            this.blsPublicKey=new BLSPublicKey();
            this.signature=new Signature();
        }

        public BLSPublicKey getBlsPublicKey() {
            return blsPublicKey;
        }


        public void InitEmpty() {
            this.blsPublicKey=new BLSPublicKey();
            this.signature=new Signature();
        }
        public void setBlsPublicKey(BLSPublicKey blsPublicKey) {
            this.blsPublicKey = blsPublicKey;
        }

        public Signature getSignature() {
            return signature;
        }

        public void setSignature(Signature signature) {
            this.signature = signature;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            BootstrapNodeProofs that = (BootstrapNodeProofs) o;
            return Objects.equal(blsPublicKey, that.blsPublicKey) && Objects.equal(signature, that.signature);
        }

        @Override
        public Object clone() throws CloneNotSupportedException
        {
            return super.clone();
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(blsPublicKey, signature);
        }


        @Override
        public String toString() {
            return "BootstrapNodeProofs{" +
                    "blsPublicKey=" + blsPublicKey +
                    ", signature=" + signature +
                    '}';
        }
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KademliaData that = (KademliaData) o;
        return Hash.equals(that.Hash) && Objects.equal(addressData, that.addressData) && Objects.equal(bootstrapNodeProofs, that.bootstrapNodeProofs);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(Hash, addressData, bootstrapNodeProofs);
    }

    @Override
    public Object clone() throws CloneNotSupportedException
    {
        return super.clone();
    }

    @Override
    public String toString() {
        return "KademliaData{" +
                "Hash='" + Hash + '\'' +
                ", ValidatorBlSPublicKey=" + ValidatorBlSPublicKey.toString() +
                ", addressData=" + addressData +
                ", bootstrapNodeProofs=" + bootstrapNodeProofs +
                '}';
    }
}
