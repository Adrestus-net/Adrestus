package io.Adrestus.crypto.bls.model;

import java.security.SecureRandom;
import java.util.Objects;

public class BLSKeyPair {

    private BLSPrivateKey PrivateKey;
    private BLSPublicKey PublicKey;
    
    
    public BLSKeyPair(int entropy){
        this.PrivateKey=new BLSPrivateKey(entropy);
        this.PublicKey=new BLSPublicKey(this.PrivateKey);
    }
    public BLSKeyPair(SecureRandom random){
        this.PrivateKey=new BLSPrivateKey(random);
        this.PublicKey=new BLSPublicKey(this.PrivateKey);
    }
    
    public BLSKeyPair(BLSPrivateKey privateKey, BLSPublicKey PublicKey) {
        this.PrivateKey = privateKey;
        this.PublicKey = PublicKey;
    }

    public BLSPrivateKey getPrivateKey() {
        return PrivateKey;
    }

    public void setPrivateKey(BLSPrivateKey PrivateKey) {
        this.PrivateKey = PrivateKey;
    }

    public BLSPublicKey getPublicKey() {
        return PublicKey;
    }

    public void setPublicKey(BLSPublicKey PublicKey) {
        this.PublicKey = PublicKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BLSKeyPair keypair = (BLSKeyPair) o;
        return Objects.equals(PrivateKey, keypair.PrivateKey) && Objects.equals(PublicKey, keypair.PublicKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(PrivateKey, PublicKey);
    }
}
