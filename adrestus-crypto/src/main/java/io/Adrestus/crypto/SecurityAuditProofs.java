package io.Adrestus.crypto;

import com.google.common.base.Objects;
import io.Adrestus.crypto.bls.model.BLSPublicKey;
import io.Adrestus.crypto.elliptic.SignatureData;
import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Arrays;

public final class SecurityAuditProofs implements Serializable {
    private String address;
    private BLSPublicKey validatorBlSPublicKey;
    private BigInteger eCDSAPublicKey;
    private SignatureData eCDSASignature;

    public SecurityAuditProofs() {
        this.address = "";
        this.validatorBlSPublicKey = new BLSPublicKey();
        this.eCDSAPublicKey = new BigInteger("0");
        this.eCDSASignature = new SignatureData();
    }


    public SecurityAuditProofs(@Deserialize("validatorBlSPublicKey") BLSPublicKey validatorBlSPublicKey) {
        this.validatorBlSPublicKey = validatorBlSPublicKey;
    }

    public SecurityAuditProofs(
            @Deserialize("validatorBlSPublicKey") BLSPublicKey validatorBlSPublicKey,
            @Deserialize("address") String address,
            @Deserialize("eCDSAPublicKey") BigInteger eCDSAPublicKey,
            @Deserialize("eCDSASignature") SignatureData eCDSASignature) {
        this.validatorBlSPublicKey = validatorBlSPublicKey;
        this.address = address;
        this.eCDSAPublicKey = eCDSAPublicKey;
        this.eCDSASignature = eCDSASignature;
    }

    public SecurityAuditProofs(@Deserialize("address") String address,
                               @Deserialize("eCDSAPublicKey") BigInteger eCDSAPublicKey,
                               @Deserialize("eCDSASignature") SignatureData eCDSASignature) {
        this.address = address;
        this.eCDSAPublicKey = eCDSAPublicKey;
        this.eCDSASignature = eCDSASignature;
        this.validatorBlSPublicKey = new BLSPublicKey();
    }

    public SecurityAuditProofs(@Deserialize("address") String address,
                               @Deserialize("validatorBlSPublicKey") BLSPublicKey validatorBlSPublicKey,
                               @Deserialize("eCDSAPublicKey") BigInteger eCDSAPublicKey,
                               @Deserialize("eCDSASignature") SignatureData eCDSASignature) {
        this.address = address;
        this.validatorBlSPublicKey = validatorBlSPublicKey;
        this.eCDSAPublicKey = eCDSAPublicKey;
        this.eCDSASignature = eCDSASignature;
    }

    @Serialize
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Serialize
    public BigInteger getECDSAPublicKey() {
        return eCDSAPublicKey;
    }

    public void setECDSAPublicKey(BigInteger eCDSAPublicKey) {
        this.eCDSAPublicKey = eCDSAPublicKey;
    }

    @Serialize
    public SignatureData getECDSASignature() {
        return eCDSASignature;
    }

    public void setECDSASignature(SignatureData eCDSASignature) {
        this.eCDSASignature = eCDSASignature;
    }


    @Serialize
    public BLSPublicKey getValidatorBlSPublicKey() {
        return validatorBlSPublicKey;
    }

    public void setValidatorBlSPublicKey(BLSPublicKey validatorBlSPublicKey) {
        this.validatorBlSPublicKey = validatorBlSPublicKey;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SecurityAuditProofs that = (SecurityAuditProofs) o;
        return Objects.equal(address, that.address) &&
                Objects.equal(validatorBlSPublicKey, that.validatorBlSPublicKey)
                && Objects.equal(eCDSAPublicKey, that.eCDSAPublicKey)
                && Arrays.equals(eCDSASignature.getR(), that.eCDSASignature.getR())
                && Objects.equal(eCDSASignature.getV(), that.eCDSASignature.getV())
                && Arrays.equals(eCDSASignature.getS(), that.eCDSASignature.getS());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(address, validatorBlSPublicKey, eCDSAPublicKey, eCDSASignature);
    }

    @Override
    public String toString() {
        return "ValidatorAddressData{" +
                ", address='" + address + '\'' +
                ", validatorBlSPublicKey=" + validatorBlSPublicKey +
                ", eCDSAPublicKey=" + eCDSAPublicKey +
                ", eCDSASignature=" + eCDSASignature +
                '}';
    }
}
