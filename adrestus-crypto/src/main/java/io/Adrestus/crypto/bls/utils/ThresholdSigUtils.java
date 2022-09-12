package io.Adrestus.crypto.bls.utils;

import io.Adrestus.crypto.bls.BLS381.BIG;
import io.Adrestus.crypto.bls.model.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class ThresholdSigUtils {

    public static Signature aggregateSignature(int threshold, List<Integer> ids, List<Signature> sigs) {

        G2Point[] sBases = new G2Point[threshold];
        FieldElement[] sExps = new FieldElement[threshold];

        for (int i = 0; i < threshold; i++) {
            Signature sig = sigs.get(i);
            int id = ids.get(i);
            FieldElement l = Polynomial.lagrangeBasisAt0(new HashSet<>(ids.subList(0, threshold)), id);
            sBases[i] = new G2Point(sig.getPoint());
            sExps[i] = l;
        }
        G2Point point = G2Point.multiScalarMulConstTime(sBases, sExps);
        return new Signature(point);
    }

    public static BLSPublicKey aggregateVerkey(int threshold, List<Integer> ids, List<BLSPublicKey> verkeys) {

        G1Point[] vkBases = new G1Point[threshold];
        FieldElement[] vkExps = new FieldElement[threshold];

        for (int i = 0; i < threshold; i++) {
            int id = ids.get(i);
            BLSPublicKey vk = verkeys.get(i);
            FieldElement l = Polynomial.lagrangeBasisAt0(new HashSet<>(ids.subList(0, threshold)), id);
            vkBases[i] = new G1Point(vk.getPoint());
            vkExps[i] = new FieldElement(l);
        }
        G1Point point = G1Point.multiScalarMulVarTime(vkBases, vkExps);
        return new BLSPublicKey(point);
    }

    private static Object[] getSharedSecretWithPolynomial(int threshold, int total) {
        Polynomial randomPoly = new Polynomial(42, threshold - 1);
        FieldElement secret = randomPoly.eval(FieldElement.zero());
        Map<Integer, FieldElement> shares = new HashMap<>();
        for (int x = 1; x <= total; x++) {
            shares.put(x, randomPoly.eval(new FieldElement(new BIG(x))));
        }

        return new Object[]{secret, shares, randomPoly};
    }

    @SuppressWarnings("unchecked")
    public static Object[] trustedPartySSSKeygen(int threshold, int total, Params params) {
        Object[] objs = getSharedSecretWithPolynomial(threshold, total);
        FieldElement secretX = (FieldElement) objs[0];
        Map<Integer, FieldElement> xShares = (Map<Integer, FieldElement>) objs[1];
        List<Signer> signers = Signer.keygenFromShares(total, xShares, params);
        return new Object[]{secretX, signers};
    }
}
