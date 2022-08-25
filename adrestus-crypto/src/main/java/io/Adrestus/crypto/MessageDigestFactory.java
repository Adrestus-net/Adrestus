package io.Adrestus.crypto;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;

public class MessageDigestFactory {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    @SuppressWarnings("DoNotInvokeMessageDigestDirectly")
    public static MessageDigest create(final String algorithm) throws NoSuchAlgorithmException {
        return MessageDigest.getInstance(algorithm);
    }
}
