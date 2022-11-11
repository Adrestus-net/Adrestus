package io.Adrestus.core;

import com.google.common.base.Objects;
import io.Adrestus.crypto.bls.model.BLSPublicKey;
import io.Adrestus.crypto.elliptic.SignatureData;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Arrays;

public final class ValidatorAddressData implements Serializable {
    private String Ip;
    private String Address;
    private BLSPublicKey ValidatorBlSPublicKey;
    private BigInteger ECDSAPublicKey;
    private SignatureData ECDSASignature;

    public ValidatorAddressData() {
    }

    public ValidatorAddressData(BLSPublicKey ValidatorBlSPublicKey, String address, BigInteger ECDSAPublicKey, SignatureData ECDSASignature) {
        this.ValidatorBlSPublicKey = ValidatorBlSPublicKey;
        this.Address = address;
        this.ECDSAPublicKey = ECDSAPublicKey;
        this.ECDSASignature = ECDSASignature;
    }

    public ValidatorAddressData(String address, BigInteger ECDSAPublicKey, SignatureData ECDSASignature) {
        this.Address = address;
        this.ECDSAPublicKey = ECDSAPublicKey;
        this.ECDSASignature = ECDSASignature;
        this.ValidatorBlSPublicKey = new BLSPublicKey();
    }

    public ValidatorAddressData(String ip,BLSPublicKey validatorBlSPublicKey,String address, BigInteger ECDSAPublicKey, SignatureData ECDSASignature) {
        this.Ip = ip;
        this.Address = address;
        this.ValidatorBlSPublicKey = validatorBlSPublicKey;
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

    public BLSPublicKey getValidatorBlSPublicKey() {
        return ValidatorBlSPublicKey;
    }

    public void setValidatorBlSPublicKey(BLSPublicKey validatorBlSPublicKey) {
        ValidatorBlSPublicKey = validatorBlSPublicKey;
    }

    public String getIp() {
        return Ip;
    }

    public void setIp(String ip) {
        Ip = ip;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ValidatorAddressData that = (ValidatorAddressData) o;
        return Objects.equal(Address, that.Address) &&
                Objects.equal(Ip, that.Ip)&&
                Objects.equal(ValidatorBlSPublicKey, that.ValidatorBlSPublicKey)
                && Objects.equal(ECDSAPublicKey, that.ECDSAPublicKey)
                && Arrays.equals(ECDSASignature.getR(), that.ECDSASignature.getR())
                && Objects.equal(ECDSASignature.getV(), that.ECDSASignature.getV())
                && Arrays.equals(ECDSASignature.getS(), that.ECDSASignature.getS());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(Address, Ip,ValidatorBlSPublicKey, ECDSAPublicKey, ECDSASignature);
    }

    @Override
    public String toString() {
        return "ValidatorAddressData{" +
                "Ip='" + Ip + '\'' +
                ", Address='" + Address + '\'' +
                ", ValidatorBlSPublicKey=" + ValidatorBlSPublicKey +
                ", ECDSAPublicKey=" + ECDSAPublicKey +
                ", ECDSASignature=" + ECDSASignature +
                '}';
    }
}
