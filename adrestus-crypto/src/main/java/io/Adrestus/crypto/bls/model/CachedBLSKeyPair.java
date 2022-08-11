package io.Adrestus.crypto.bls.model;


public class CachedBLSKeyPair {
    private static volatile CachedBLSKeyPair instance;

    private BLSPrivateKey PrivateKey;
    private BLSPublicKey PublicKey;


    private CachedBLSKeyPair() {
        if (instance != null) {
            throw new IllegalStateException("Already initialized.");
        }
    }

    public static CachedBLSKeyPair getInstance() {
        var result = instance;
        if (result == null) {
            synchronized (CachedBLSKeyPair.class) {
                result = instance;
                if (result == null) {
                    instance = result = new CachedBLSKeyPair();
                }
            }
        }
        return result;
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
}
