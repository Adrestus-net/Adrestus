package io.Adrestus.crypto.elliptic;

import java.security.Provider;
import java.security.Security;

public class ProviderInstance {
    private static Provider CRYPTO_PROVIDER;
    private static volatile ProviderInstance instance;
    static {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        CRYPTO_PROVIDER = Security.getProvider("BC");
        CRYPTO_PROVIDER.put("MessageDigest.ETH-KECCAK-256", "org.ethereum.crypto.cryptohash.Keccak256");
        CRYPTO_PROVIDER.put("MessageDigest.ETH-KECCAK-512", "org.ethereum.crypto.cryptohash.Keccak512");
    }

    private ProviderInstance() {
        // Protect against instantiation via reflection
        if (instance != null) {
            throw new IllegalStateException("Already initialized.");
        }
    }
    public static synchronized ProviderInstance getInstance() {
        if (instance == null) {
            synchronized (ProviderInstance.class) {
                if (instance == null) {
                    instance = new ProviderInstance();
                }
            }
        }
        return instance;
    }
    public static Provider getCryptoProvider() {
        return CRYPTO_PROVIDER;
    }

}
