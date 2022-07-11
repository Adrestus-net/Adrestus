package io.Adrestus.crypto.bls.model;

import io.Adrestus.crypto.HashUtil;
import org.apache.milagro.amcl.BLS381.ECP;

import java.util.List;

public class BLSPublicKey {

    private G1Point point;
    public BLSPublicKey(G1Point point) {
        this.point = point;
    }
    
    public BLSPublicKey(BLSPrivateKey sk, Params params) {
        ECP ecp = params.g.getValue().mul(sk.getX().value);
        this.point = new G1Point(ecp);
    }

    public BLSPublicKey(BLSPrivateKey sk, byte[] g) {
        byte hash[]= HashUtil.Shake256(g);
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

    public String toRaw(){
        return this.point.getValue().toString();
    }

    public static BLSPublicKey aggregate(List<BLSPublicKey> keys) {
        return keys.isEmpty()
                ? new BLSPublicKey(new G1Point())
                : keys.stream().reduce(BLSPublicKey::combine).get();
    }
    public BLSPublicKey combine(BLSPublicKey pk) {
        point.getValue().add(pk.point.getValue());
        return new BLSPublicKey(new G1Point(point));
    }
    public static BLSPublicKey fromByte(byte[] buf) {
        return new BLSPublicKey(G1Point.fromBytes(buf));
    }


    public G1Point getPoint() {
        return point;
    }

    public void setPoint(G1Point point) {
        this.point = point;
    }
}
