package io.Adrestus.crypto.bls.model;

import io.Adrestus.crypto.HashUtil;
import org.apache.milagro.amcl.BLS381.ECP;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.spec.ECParameterSpec;

public class VerKey {

    public G1 point;
    private static ECParameterSpec curve = ECNamedCurveTable.getParameterSpec("secp256k1");
    private static ECDomainParameters domain = new ECDomainParameters(curve.getCurve(), curve.getG(), curve.getN(), curve.getH());
    public VerKey(G1 point) {
        this.point = point;
    }
    
    public VerKey(SigKey sk, Params params) {
        ECP ecp = params.g.value.mul(sk.x.value);
        this.point = new G1(ecp);
    }

    public VerKey(SigKey sk, byte[] g) {
        byte hash[]= HashUtil.Shake256(g);
        ECP ecp = new G1(hash).value.mul(sk.x.value);
        this.point = new G1(ecp);
    }

    public VerKey(SigKey sk) {
        ECP ecp = ECP.generator().mul(sk.x.value);
        this.point = new G1(ecp);
    }

    public byte[] toBytes() {
        return this.point.toBytes();
    }

    public String toRaw(){
        return this.point.value.toString();
    }
    
    public static VerKey fromByte(byte[] buf) {
        return new VerKey(G1.fromBytes(buf));
    }
}
