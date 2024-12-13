package io.Adrestus.crypto.elliptic;

import io.Adrestus.config.AdrestusConfiguration;
import io.activej.serializer.annotations.Serialize;
import lombok.SneakyThrows;
import org.apache.commons.codec.binary.Hex;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.ECPublicKeySpec;
import java.util.Arrays;
import java.util.Objects;

public class ECKeyPair {
    //    private final BigInteger privKey;
//    private final BigInteger pubKey;
    private final PrivateKey privateKey;
    private final PublicKey publicKey;

    private final BigInteger XpubAxis;
    private final BigInteger YpubAxis;

    private ECPublicKeySpec ecPublicKeySpec;

    public ECKeyPair() {
        this.privateKey = null;
        this.publicKey = null;
        this.ecPublicKeySpec = null;
        this.XpubAxis = null;
        this.YpubAxis = null;
    }

    @SneakyThrows
    public ECKeyPair(PrivateKey privateKey, PublicKey publicKey) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.ecPublicKeySpec = (ECPublicKeySpec) KeyFactory.getInstance(AdrestusConfiguration.SIGN_ALGORITHM, AdrestusConfiguration.SIGN_PROVIDER).getKeySpec(publicKey, ECPublicKeySpec.class);
        this.XpubAxis = ecPublicKeySpec.getW().getAffineX();
        this.YpubAxis = ecPublicKeySpec.getW().getAffineY();
    }


    @Serialize
    public BigInteger getXpubAxis() {
        return XpubAxis;
    }

    @Serialize
    public BigInteger getYpubAxis() {
        return YpubAxis;
    }

    @Serialize
    public ECPublicKeySpec getEcPublicKeySpec() {
        return ecPublicKeySpec;
    }

    @Serialize
    public PublicKey getPublicKey() {
        return publicKey;
    }

    @Serialize
    public PrivateKey getPrivateKey() {
        return privateKey;
    }


    @SneakyThrows
    public BigInteger getPrivKey() {
        BCECPrivateKey priv = (BCECPrivateKey) privateKey;
        return priv.getD();
    }

    @SneakyThrows
    public BigInteger getPubKey() {
        BCECPublicKey pub = (BCECPublicKey) publicKey;
        byte[] publicKeyBytes = pub.getQ().getEncoded(false);
        return new BigInteger(1, Arrays.copyOfRange(publicKeyBytes, 1, publicKeyBytes.length));
    }
//    //compressed pub used to generate the address
//    public static ECKeyPair create(KeyPair keyPair) {
//        BCECPrivateKey privateKey = (BCECPrivateKey) keyPair.getPrivate();
//        BCECPublicKey publicKey = (BCECPublicKey) keyPair.getPublic();
//
//        BigInteger privateKeyValue = privateKey.getD();
//
//        byte[] publicKeyBytes = publicKey.getQ().getEncoded(false);
//        BigInteger publicKeyValue =
//                new BigInteger(1, Arrays.copyOfRange(publicKeyBytes, 1, publicKeyBytes.length));
//
//        ECKeyPair ECKeyPair = new ECKeyPair(privateKeyValue, publicKeyValue);
//        return ECKeyPair;
//    }

    private static String compress(PublicKey publicKey) {
        StringBuilder sb = new StringBuilder(Hex.encodeHexString(publicKey.getEncoded()));
        return sb.delete(0, 46).toString();
    }

//    public ECDSASignature sign(byte[] hash) {
//
//        org.bouncycastle.crypto.signers.ECDSASigner signer = new org.bouncycastle.crypto.signers.ECDSASigner(new HMacDSAKCalculator(new SHA256Digest()));
//
//        ECPrivateKeyParameters privKey = new ECPrivateKeyParameters(getPrivKey(), Sign.CURVE);
//        signer.init(true, privKey);
//        BigInteger[] components = signer.generateSignature(hash);
//
//        return new ECDSASignature(components[0], components[1]).toCanonicalised();
//    }


//    public boolean verify(byte[] hash, ECDSASignature signature) {
//        org.bouncycastle.crypto.signers.ECDSASigner signer = new ECDSASigner();
//        signer.init(
//                false,
//                new ECPublicKeyParameters(
//                        Sign.publicPointFromPrivate(getPrivKey()), Sign.CURVE));
//        return signer.verifySignature(hash, signature.getR(), signature.getS());
//    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ECKeyPair ecKeyPair = (ECKeyPair) o;
        return Objects.equals(privateKey, ecKeyPair.privateKey) && Objects.equals(publicKey, ecKeyPair.publicKey) && Objects.equals(XpubAxis, ecKeyPair.XpubAxis) && Objects.equals(YpubAxis, ecKeyPair.YpubAxis) && Objects.equals(ecPublicKeySpec, ecKeyPair.ecPublicKeySpec);
    }

    @Override
    public int hashCode() {
        return Objects.hash(privateKey, publicKey, XpubAxis, YpubAxis, ecPublicKeySpec);
    }

    @Override
    public String toString() {
        return "ECKeyPair{" +
                "privateKey=" + privateKey +
                ", publicKey=" + publicKey +
                ", XpubAxis=" + XpubAxis +
                ", YpubAxis=" + YpubAxis +
                ", ecPublicKeySpec=" + ecPublicKeySpec +
                '}';
    }
}
