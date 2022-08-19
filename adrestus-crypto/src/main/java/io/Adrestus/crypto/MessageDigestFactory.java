package io.Adrestus.crypto;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class MessageDigestFactory {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    @SuppressWarnings("DoNotInvokeMessageDigestDirectly")
    public static MessageDigest create(final String algorithm) throws NoSuchAlgorithmException {
        return MessageDigest.getInstance(algorithm);
    }
}
