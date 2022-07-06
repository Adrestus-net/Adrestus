package io.Adrestus.crypto.bls.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Signer {

    public int id;
    public VerKey verKey;
    public SigKey sigKey;
    
    public Signer(int id, VerKey verKey, SigKey sigKey) {
        this.id = id;
        this.verKey = verKey;
        this.sigKey = sigKey;
    }
        
    public static List<Signer> keygenFromShares(int numSigners, Map<Integer, FieldElement> xShares, Params params) {
        List<Signer> signers = new ArrayList<>();
        for(int i = 0; i < numSigners; i++) {
            int id = i + 1;
            FieldElement xi = xShares.remove(id);
            G1 gxi = new G1(params.g.value.mul(xi.value));
            signers.add(new Signer(id, new VerKey(gxi), new SigKey(xi)));
        }
        return signers;
    }
}
