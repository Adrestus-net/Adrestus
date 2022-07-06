package io.Adrestus.crypto.bls.model;

import org.apache.milagro.amcl.BLS381.BIG;
import org.apache.milagro.amcl.RAND;

import java.security.SecureRandom;

public class SigKey {

    public FieldElement x;
        
    public SigKey(FieldElement x) {
        this.x = x;
    }
    
    public SigKey(int entropy) {
        this.x = new FieldElement(entropy);
    }
    public SigKey(SecureRandom random) {
        this.x = new FieldElement(random);
    }
    public SigKey(byte [] buff){
        this.x=FieldElement.fromBytes(buff);
    }

    
    public byte[] toBytes() {
        return this.x.toBytes();
    }
    
    public SigKey fromBytes(byte[] buf) {
        return new SigKey(FieldElement.fromBytes(buf));
    }
}
