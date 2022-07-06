package io.Adrestus.crypto.bls.model;

import org.apache.milagro.amcl.BLS381.BIG;
import org.apache.milagro.amcl.RAND;

public class SigKey {

    public FieldElement x;
        
    public SigKey(FieldElement x) {
        this.x = x;
    }
    
    public SigKey(RAND r) {
        this.x = new FieldElement(r);
    }
    
    public SigKey(int r) {
        this.x = new FieldElement(new BIG(r));
    }
    
    public byte[] toBytes() {
        return this.x.toBytes();
    }
    
    public SigKey fromBytes(byte[] buf) {
        return new SigKey(FieldElement.fromBytes(buf));
    }
}
