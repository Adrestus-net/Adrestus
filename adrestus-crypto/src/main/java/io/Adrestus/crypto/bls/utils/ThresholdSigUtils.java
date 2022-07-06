package io.Adrestus.crypto.bls.utils;

import io.Adrestus.crypto.bls.model.*;
import org.apache.milagro.amcl.BLS381.BIG;
import org.apache.milagro.amcl.RAND;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class ThresholdSigUtils {
    
    public static Signature aggregateSignature(int threshold, List<Integer> ids, List<Signature> sigs) {
        
        G2[] sBases = new G2[threshold];
        FieldElement[] sExps = new FieldElement[threshold];
        
        for(int i = 0; i < threshold; i++) {
            Signature sig = sigs.get(i);
            int id  = ids.get(i);
            FieldElement l = Polynomial.lagrangeBasisAt0(new HashSet<>(ids.subList(0, threshold)), id);
            sBases[i] = new G2(sig.point);
            sExps[i] = l;
        }
        G2 point = G2.multiScalarMulConstTime(sBases, sExps);
        return new Signature(point);
    }
    
    public static VerKey aggregateVerkey(int threshold,  List<Integer> ids, List<VerKey> verkeys) {
        
        G1[] vkBases = new G1[threshold];
        FieldElement[] vkExps = new FieldElement[threshold];
        
        for(int i = 0; i < threshold; i++) {
            int id = ids.get(i);
            VerKey vk = verkeys.get(i);
            FieldElement l = Polynomial.lagrangeBasisAt0(new HashSet<>(ids.subList(0, threshold)), id);
            vkBases[i] = new G1(vk.point);
            vkExps[i] = new FieldElement(l);
        }
        G1 point = G1.multiScalarMulVarTime(vkBases, vkExps);
        return new VerKey(point);
    }
    
    private static Object[] getSharedSecretWithPolynomial(int threshold, int total) {
        RAND r = new RAND();
        Polynomial randomPoly = new Polynomial(r, threshold-1);
        FieldElement secret = randomPoly.eval(FieldElement.zero());
        Map<Integer, FieldElement> shares = new HashMap<>();
        for(int x = 1; x <= total; x++) {
            shares.put(x, randomPoly.eval(new FieldElement(new BIG(x))));
        }
        
        return new Object[] {secret, shares, randomPoly};        
    }
    
    @SuppressWarnings("unchecked")
    public static Object[] trustedPartySSSKeygen(int threshold, int total, Params params) {
        Object[] objs = getSharedSecretWithPolynomial(threshold, total);
        FieldElement secretX = (FieldElement) objs[0];
        Map<Integer, FieldElement> xShares = (Map<Integer, FieldElement>)objs[1];
        List<Signer> signers = Signer.keygenFromShares(total, xShares, params);
        return new Object[] {secretX, signers};
    }
}
