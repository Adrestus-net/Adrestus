package io.Adrestus.crypto.bls.model;

import io.Adrestus.crypto.bls.constants.*;
import io.Adrestus.crypto.bls.utils.*;
import org.apache.milagro.amcl.BLS381.*;

public class GT {

    public FP12 value;
    
    public GT(FP12 value) {
        this.value = value;
    }
    
    public static GT one() {
        FP4 zero = new FP4(0);
        FP4 one = new FP4(1);
        FP12 value = new FP12(one, zero, zero);
        return new GT(value);
    }
    
    public static GT atePairing(G1 g1, G2 g2) {
        if(g1.value.is_infinity() || g2.value.is_infinity()) {
            return GT.one();
        }
        FP12 fp12 = PAIR.ate(new ECP2(g2.value), new ECP(g1.value));
        return new GT(PAIR.fexp(fp12));
     }
    
    public static GT ate2Pairing(G1 g1, G2 g2, G1 h1, G2 h2) {
        if(g1.value.is_infinity() || g2.value.is_infinity()) {
            return atePairing(h1, h2);
        }
        if(h1.value.is_infinity() || h2.value.is_infinity()) {
            return atePairing(g1, g2);
        }
        
        FP12 fp12 =  PAIR.ate2(new ECP2(g2.value), new ECP(g1.value), new ECP2(h2.value), new ECP(h1.value));
        return new GT(PAIR.fexp(fp12));
    }
    
    public static GT ateMultiPairing(G1[] g1Arr, G2[] g2Arr) {
        FP12[] accum = new FP12[Constants.ATE_BITS];
        for(int i = 0; i < accum.length; i++) {
            accum[i] = new FP12(1);
        }
        
        for(int i = 0; i < g1Arr.length; i++) {
            G1 g1 = g1Arr[i];
            G2 g2 = g2Arr[i];
            if(g1.value.is_infinity() || g2.value.is_infinity()) {
                continue;
            }
            BigNumberUtils.another(accum, new ECP2(g2.value), new ECP(g1.value));
        }
        FP12 e = BigNumberUtils.miller(accum);
        return new GT(e);
    }
}
