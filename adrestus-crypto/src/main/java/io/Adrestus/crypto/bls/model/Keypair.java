package io.Adrestus.crypto.bls.model;

import org.apache.milagro.amcl.RAND;

public class Keypair {

    public SigKey sigKey;
    public VerKey verKey;
    
    public Keypair(SigKey sigKey, VerKey verKey) {
        this.sigKey = sigKey;
        this.verKey = verKey;
    }
    
    public Keypair(RAND r, Params params) {
        this.sigKey = new SigKey(r);
        this.verKey = new VerKey(this.sigKey, params);
    }
}
