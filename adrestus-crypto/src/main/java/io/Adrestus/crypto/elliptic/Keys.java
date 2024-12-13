package io.Adrestus.crypto.elliptic;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.*;
import java.security.spec.ECGenParameterSpec;

public class Keys {

    static final int PRIVATE_KEY_SIZE = 32;
    static final int PUBLIC_KEY_SIZE = 64;

    public static final int ADDRESS_SIZE = 160;
    public static final int ADDRESS_LENGTH_IN_HEX = ADDRESS_SIZE >> 2;

    public static final int PUBLIC_KEY_LENGTH_IN_HEX = PUBLIC_KEY_SIZE << 1;
    public static final int PRIVATE_KEY_LENGTH_IN_HEX = PRIVATE_KEY_SIZE << 1;

    public static final String ALGORITHM = "ECDSA";
    public static final String PARAM_SPEC_1 = "secp256k1";
    public static final String PARAM_SPEC_2 = "secp256r1";

    static {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }


    private static KeyPair createSecp256k1KeyPair(SecureRandom random)
            throws NoSuchProviderException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException {

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM, ProviderInstance.getCryptoProvider());
        ECGenParameterSpec ecGenParameterSpec = new ECGenParameterSpec(PARAM_SPEC_1);
        if (random != null) {
            keyPairGenerator.initialize(ecGenParameterSpec, random);
        } else {
            keyPairGenerator.initialize(ecGenParameterSpec);
        }
        return keyPairGenerator.generateKeyPair();
    }

    private static KeyPair createSecp256r1KeyPair(SecureRandom random)
            throws NoSuchProviderException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException {

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM, ProviderInstance.getCryptoProvider());
        ECGenParameterSpec ecGenParameterSpec = new ECGenParameterSpec(PARAM_SPEC_2);
        if (random != null) {
            keyPairGenerator.initialize(ecGenParameterSpec, random);
        } else {
            keyPairGenerator.initialize(ecGenParameterSpec);
        }
        return keyPairGenerator.generateKeyPair();
    }

    public static ECKeyPair create256k1KeyPair(SecureRandom random)
            throws InvalidAlgorithmParameterException, NoSuchAlgorithmException,
            NoSuchProviderException {
        KeyPair keyPair = createSecp256k1KeyPair(random);
        return new ECKeyPair(keyPair.getPrivate(), keyPair.getPublic());
    }

    public static ECKeyPair create256r1KeyPair(SecureRandom random)
            throws InvalidAlgorithmParameterException, NoSuchAlgorithmException,
            NoSuchProviderException {
        KeyPair keyPair = createSecp256r1KeyPair(random);
        return new ECKeyPair(keyPair.getPrivate(), keyPair.getPublic());
    }
}
