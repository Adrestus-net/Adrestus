package io.Adrestus.crypto.elliptic;

import org.apache.commons.codec.binary.Hex;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.signers.ECDSASigner;
import org.bouncycastle.crypto.signers.HMacDSAKCalculator;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Objects;

public class ECKeyPair {
    private final BigInteger privateKey;
    private final BigInteger publicKey;

    public ECKeyPair(BigInteger privateKey, BigInteger publicKey) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }

    public ECKeyPair(BigInteger publicKey) {
        this.privateKey=null;
        this.publicKey = publicKey;
    }

    public BigInteger getPrivateKey() {
        return privateKey;
    }

    public BigInteger getPublicKey() {
        return publicKey;
    }


    //compressed pub used to generate the address
    public static ECKeyPair create(KeyPair keyPair) {
        BCECPrivateKey privateKey = (BCECPrivateKey) keyPair.getPrivate();
        BCECPublicKey publicKey = (BCECPublicKey) keyPair.getPublic();

        BigInteger privateKeyValue = privateKey.getD();

        byte[] publicKeyBytes = publicKey.getQ().getEncoded(false);
        BigInteger publicKeyValue =
                new BigInteger(1, Arrays.copyOfRange(publicKeyBytes, 1, publicKeyBytes.length));

        ECKeyPair ECKeyPair = new ECKeyPair(privateKeyValue, publicKeyValue);
        return ECKeyPair;
    }


    private static String compress(PublicKey publicKey) {
        StringBuilder sb = new StringBuilder(Hex.encodeHexString(publicKey.getEncoded()));
        return sb.delete(0, 46).toString();
    }

    public ECDSASignature sign(byte[] hash) {

        ECDSASigner signer = new ECDSASigner(new HMacDSAKCalculator(new SHA256Digest()));

        ECPrivateKeyParameters privKey = new ECPrivateKeyParameters(privateKey, Sign.CURVE);
        signer.init(true, privKey);
        BigInteger[] components = signer.generateSignature(hash);

        return new ECDSASignature(components[0], components[1]).toCanonicalised();
    }


    public boolean verify(byte[] hash, ECDSASignature signature) {
        ECDSASigner signer = new ECDSASigner();
        signer.init(
                false,
                new ECPublicKeyParameters(
                        Sign.publicPointFromPrivate(getPrivateKey()), Sign.CURVE));
        return signer.verifySignature(hash, signature.getR(), signature.getS());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ECKeyPair ECKeyPair = (ECKeyPair) o;
        return Objects.equals(privateKey, ECKeyPair.privateKey)
                && Objects.equals(publicKey, ECKeyPair.publicKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(privateKey, publicKey);
    }
}
