package io.Adrestus.crypto.bls.model;

import org.apache.milagro.amcl.BLS381.ECP;

public class VerKey {

    public G1 point;
    
    public VerKey(G1 point) {
        this.point = point;
    }
    
    public VerKey(SigKey sk, Params params) {
        ECP ecp = params.g.value.mul(sk.x.value);
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
