package io.Adrestus.crypto.bls.model;

import io.Adrestus.crypto.HashUtil;
import io.Adrestus.crypto.bls.constants.Constants;
import org.apache.milagro.amcl.BLS381.ECP2;

import java.nio.charset.StandardCharsets;

public class Signature {

    public G2 point;
    
    public Signature(G2 point) {
        this.point = point;
    }
    
    private G2 hashMessage(byte[] msg) {
        byte[] tmp = new byte[Constants.MESSAGE_DOMAIN_PREFIX.length + msg.length];
        System.arraycopy(Constants.MESSAGE_DOMAIN_PREFIX, 0, tmp, 0, Constants.MESSAGE_DOMAIN_PREFIX.length);
        System.arraycopy(msg, 0, tmp, Constants.MESSAGE_DOMAIN_PREFIX.length, msg.length);
        return new G2(HashUtil.Shake256(tmp));
    }
    
    public Signature(byte[] msg, SigKey sigKey) {
        G2 hashPoint = hashMessage(msg);
        ECP2 ecp2 = hashPoint.value.mul(sigKey.x.value);
        this.point = new G2(ecp2);
    }              
    
    public boolean verify(byte[] msg, VerKey verKey, Params params) {
        if(this.point.value.is_infinity()) {
            return false;
        }
        G2 hashPoint = hashMessage(msg);
        G1 g = new G1(params.g);
        g.neg();
        GT gt = GT.ate2Pairing(verKey.point, hashPoint, g, this.point);
        return gt.value.isunity();
    }

    public boolean verify(byte[] msg, VerKey verKey, byte [] param) {
        if(this.point.value.is_infinity()) {
            return false;
        }
        G2 hashPoint = hashMessage(msg);
        G1 g=new G1(HashUtil.Shake256(param));
        g.neg();
        GT gt = GT.ate2Pairing(verKey.point, hashPoint, g, this.point);
        return gt.value.isunity();
    }

    public boolean verify(byte[] msg, VerKey verKey) {
        if(this.point.value.is_infinity()) {
            return false;
        }
        G2 hashPoint = hashMessage(msg);
        G1 g=new G1(Curve.getCurveParams().getG().getEncoded(false));
        g.neg();
        GT gt = GT.ate2Pairing(verKey.point, hashPoint, g, this.point);
        return gt.value.isunity();
    }
    
}
