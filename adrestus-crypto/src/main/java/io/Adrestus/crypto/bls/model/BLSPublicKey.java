package io.Adrestus.crypto.bls.model;

import com.google.common.primitives.Ints;
import io.Adrestus.crypto.HashUtil;
import io.Adrestus.crypto.bls.BLS381.ECP;
import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;
import org.spongycastle.util.encoders.Hex;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BLSPublicKey implements Cloneable, Serializable {

    private G1Point point;

    public BLSPublicKey() {
        this.point = new G1Point();
    }

    public BLSPublicKey(@Deserialize("point") G1Point point) {
        this.point = point;
    }

    public BLSPublicKey(BLSPrivateKey sk, Params params) {
        ECP ecp = params.g.getValue().mul(sk.getX().value);
        this.point = new G1Point(ecp);
    }

    public BLSPublicKey(BLSPrivateKey sk, byte[] g) {
        byte hash[] = HashUtil.Shake256(g);
        ECP ecp = ECP.fromBytes(hash).mul(sk.getX().value);
        this.point = new G1Point(ecp);
    }

    public BLSPublicKey(BLSPrivateKey sk) {
        ECP ecp = ECP.generator().mul(sk.getX().value);
        this.point = new G1Point(ecp);
    }

    public byte[] toBytes() {
        return this.point.toBytes();
    }

    public String toRaw() {
        return Hex.toHexString(toBytes());
    }

    public static BLSPublicKey aggregate(List<BLSPublicKey> keys) {
        List<BLSPublicKey> cloned_keys = new ArrayList<BLSPublicKey>();
        keys.stream().forEach(x -> cloned_keys.add(x.clone()));
        return keys.isEmpty() ? new BLSPublicKey(new G1Point()) : cloned_keys.stream().reduce(BLSPublicKey::combine).get();
    }


    public BLSPublicKey combine(BLSPublicKey pk) {
        point.getValue().add(pk.point.getValue());
        return new BLSPublicKey(new G1Point(point));
    }

    public static BLSPublicKey fromByte(byte[] buf) {
        return new BLSPublicKey(G1Point.fromBytes(buf));
    }


    @Override
    public boolean equals(Object v) {
        boolean retVal = false;

        if (v instanceof BLSPublicKey) {
            BLSPublicKey ptr = (BLSPublicKey) v;
            retVal = Arrays.equals(ptr.toBytes(), this.toBytes());
        }
        return retVal;
    }

    @Override
    public int hashCode() {
        return Ints.fromByteArray(this.toBytes());
    }

    @Serialize
    public G1Point getPoint() {
        return point;
    }

    @Override
    public BLSPublicKey clone() {
        return new BLSPublicKey(new G1Point(point));
    }

    public void setPoint(G1Point point) {
        this.point = point;
    }

    @Override
    public String toString() {
        return "BLSPublicKey{" +
                "point=" + point +
                '}';
    }
}
