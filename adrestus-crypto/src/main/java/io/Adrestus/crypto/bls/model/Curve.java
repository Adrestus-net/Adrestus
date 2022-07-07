package io.Adrestus.crypto.bls.model;

import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.spec.ECParameterSpec;

public class Curve {
    private  static ECParameterSpec curveParams;

    public static ECParameterSpec getCurveParams() {
        curveParams = ECNamedCurveTable.getParameterSpec("secp256k1");
        return curveParams;
    }

}
