package io.Adrestus.crypto.elliptic;

import io.Adrestus.config.AdrestusConfiguration;
import io.Adrestus.crypto.JavascriptWalletAddress;
import io.Adrestus.crypto.PrimitiveUtil;
import io.Adrestus.crypto.WalletAddress;
import lombok.SneakyThrows;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.signers.HMacDSAKCalculator;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.encoders.Hex;
import org.conscrypt.Conscrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPublicKeySpec;
import java.util.Arrays;

public class ECDSASign implements SignInterface {


    private static final Logger logger = LoggerFactory.getLogger(ECDSASign.class);

    private static final BigInteger curveN = new BigInteger(1, Hex.decode("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEBAAEDCE6AF48A03BBFD25E8CD0364141"));
    private static final BigInteger halfCurveN = curveN.shiftRight(1);
    private static ECParameterSpec Secp256k1ecParamSpec, secp256r1ecParamSpec;
    private static KeyFactory kfBc;

    public ECDSASign() {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        Security.insertProviderAt(Conscrypt.newProvider(), 0);
        this.Init();
    }


    @SneakyThrows
    private void Init() {
        this.kfBc = KeyFactory.getInstance(AdrestusConfiguration.SIGN_ALGORITHM, AdrestusConfiguration.SIGN_PROVIDER);
        AlgorithmParameters Secp256k1parameters = AlgorithmParameters.getInstance(AdrestusConfiguration.SIGN_ALGORITHM, AdrestusConfiguration.SIGN_PROVIDER);
        AlgorithmParameters secp256r1parameters = AlgorithmParameters.getInstance(AdrestusConfiguration.SIGN_ALGORITHM, AdrestusConfiguration.SIGN_PROVIDER);
        Secp256k1parameters.init(new ECGenParameterSpec(AdrestusConfiguration.SIGN_CURVE_256k1));
        secp256r1parameters.init(new ECGenParameterSpec(AdrestusConfiguration.SIGN_CURVE_256r1));
        this.Secp256k1ecParamSpec = Secp256k1parameters.getParameterSpec(ECParameterSpec.class);
        this.secp256r1ecParamSpec = secp256r1parameters.getParameterSpec(ECParameterSpec.class);
    }

    /**
     * Sign the message with ECDSA algorithm without add 27 to v
     *
     * @param message
     * @param keyPair
     * @return
     */
    public ECDSASignatureData signSecp256k1Message(byte[] message, ECKeyPair keyPair) {
        ECDSASignatureData signatureData = signMessage(message, keyPair);
        return new ECDSASignatureData(
                (byte) (signatureData.getV() - 27), signatureData.getR(), signatureData.getS());
    }

    @SneakyThrows
    @Override
    public ECDSASignatureData signSecp256r1Message(byte[] message, ECKeyPair keyPair) {
        Signature ecdsaSign = Signature.getInstance(AdrestusConfiguration.SIGN_ALGORITHM_ECDSA, AdrestusConfiguration.CONSCRYPT_PROVIDER);
        ecdsaSign.initSign(keyPair.getPrivateKey());
        ecdsaSign.update(message);
        byte[] signature = ecdsaSign.sign();
        return new ECDSASignatureData(keyPair.getPublicKey().getEncoded(), signature);
    }


    public boolean secp256k1Verify(byte[] hash, BigInteger publicKey, ECDSASignatureData signatureData) {
        return verify(
                hash,
                publicKey,
                new ECDSASignatureData(
                        (byte) (signatureData.getV() + 27),
                        signatureData.getR(),
                        signatureData.getS()));
    }

    public boolean secp256k1Verify(byte[] hash, String publicKey, ECDSASignatureData signatureData) {
        return verify(
                hash,
                publicKey,
                new ECDSASignatureData(
                        (byte) (signatureData.getV() + 27),
                        signatureData.getR(),
                        signatureData.getS()));
    }

    public boolean secp256k1Verify(byte[] hash, String address, BigInteger recovered_key, ECDSASignatureData signatureData) {
        return verify(
                hash,
                address,
                recovered_key, new ECDSASignatureData(
                        (byte) (signatureData.getV() + 27),
                        signatureData.getR(),
                        signatureData.getS()));
    }

    @SneakyThrows
    @Override
    public boolean secp256r1Verify(byte[] hash, BigInteger x_axis, BigInteger y_axis, ECDSASignatureData signatureData) {
        if (x_axis == null || y_axis == null || signatureData.getSig() == null) {
            throw new IllegalArgumentException("Invalid parameters secp256r1Verify");
        }
        Signature ecdsaVerify = Signature.getInstance(AdrestusConfiguration.SIGN_ALGORITHM_ECDSA, AdrestusConfiguration.CONSCRYPT_PROVIDER);
        ecdsaVerify.initVerify(this.recoversecp256r1PublicKeyValue(x_axis, y_axis));
        ecdsaVerify.update(hash);
        return ecdsaVerify.verify(signatureData.getSig());
    }

    @SneakyThrows
    @Override
    public boolean secp256r1Verify(byte[] hash, PublicKey publicKey, ECDSASignatureData signatureData) {
        if (publicKey == null || signatureData.getSig() == null) {
            throw new IllegalArgumentException("Invalid parameters secp256r1Verify");
        }
        ECPublicKeySpec ecPublicKeySpec = KeyFactory.getInstance(AdrestusConfiguration.SIGN_ALGORITHM, AdrestusConfiguration.SIGN_PROVIDER).getKeySpec(publicKey, ECPublicKeySpec.class);
        BigInteger affineX = ecPublicKeySpec.getW().getAffineX();
        BigInteger affineY = ecPublicKeySpec.getW().getAffineY();

        Signature ecdsaVerify = Signature.getInstance(AdrestusConfiguration.SIGN_ALGORITHM_ECDSA, AdrestusConfiguration.CONSCRYPT_PROVIDER);
        ecdsaVerify.initVerify(this.recoversecp256r1PublicKeyValue(affineX, affineY));
        ecdsaVerify.update(hash);
        return ecdsaVerify.verify(signatureData.getSig());
    }

    @SneakyThrows
    @Override
    public ECDSASignatureData signMessage(byte[] message, ECKeyPair keyPair) {
        BigInteger privateKey = keyPair.getPrivKey();
        BigInteger publicKey = keyPair.getPubKey();

        //check this if need
        //byte[] messageHash = HashUtil.sha256(message);

        ECDSASignature sig = sign(message, privateKey);

        // Now we have to work backwards to figure out the recId needed to recover the signature.

        /** Optimize the algorithm for calculating the recId value */
        ECPoint ecPoint = sig.p;
        BigInteger affineXCoordValue = ecPoint.normalize().getAffineXCoord().toBigInteger();
        BigInteger affineYCoordValue = ecPoint.normalize().getAffineYCoord().toBigInteger();

        int recId = affineYCoordValue.and(BigInteger.ONE).intValue();
        recId |= (affineXCoordValue.compareTo(sig.getR()) != 0 ? 2 : 0);
        if (sig.getS().compareTo(halfCurveN) > 0) {
            sig.setS(Sign.CURVE.getN().subtract(sig.getS()));
            recId = recId ^ 1;
        }


        int headerByte = recId + 27;

        byte v = (byte) headerByte;
        byte[] r = PrimitiveUtil.toBytesPadded(sig.getR(), 32);
        byte[] s = PrimitiveUtil.toBytesPadded(sig.getS(), 32);

        return new ECDSASignatureData(v, r, s);
    }

    @SneakyThrows
    @Override
    public BigInteger recoverSecp256k1PublicKeyValue(BigInteger x, BigInteger y) {
        java.security.spec.ECPoint point = new java.security.spec.ECPoint(x, y);

        PublicKey pubKey = kfBc.generatePublic(new ECPublicKeySpec(point, Secp256k1ecParamSpec));
        BCECPublicKey publicKeys = (BCECPublicKey) pubKey;

        byte[] publicKeyBytes = publicKeys.getQ().getEncoded(false);
        BigInteger publicKeyValue = new BigInteger(1, Arrays.copyOfRange(publicKeyBytes, 1, publicKeyBytes.length));
        return publicKeyValue;
    }

    @SneakyThrows
    @Override
    public PublicKey recoversecp256r1PublicKeyValue(BigInteger x, BigInteger y) {
        java.security.spec.ECPoint point = new java.security.spec.ECPoint(x, y);


        java.security.spec.ECPoint ecPoint = new java.security.spec.ECPoint(x, y);
        ECPublicKeySpec publicKeySpec = new ECPublicKeySpec(ecPoint, secp256r1ecParamSpec);

        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
        return publicKey;
    }


    public boolean verify(byte[] hash, BigInteger publicKey, ECDSASignatureData signatureData) {
        ECDSASignature sig =
                new ECDSASignature(
                        PrimitiveUtil.toBigInt(signatureData.getR()),
                        PrimitiveUtil.toBigInt(signatureData.getS()));

        BigInteger k = Sign.recoverFromSignature(signatureData.getV() - 27, sig, hash);
        return publicKey.equals(k);
    }

    public boolean verify(byte[] hash, String given_address, ECDSASignatureData signatureData) {
        ECDSASignature sig =
                new ECDSASignature(
                        PrimitiveUtil.toBigInt(signatureData.getR()),
                        PrimitiveUtil.toBigInt(signatureData.getS()));

        BigInteger pubkey = Sign.recoverFromSignature(signatureData.getV() - 27, sig, hash);
        String adddress = WalletAddress.generate_address((byte) AdrestusConfiguration.version, pubkey);
        return given_address.equals(adddress);
    }

    public boolean verify(byte[] hash, String given_address, BigInteger recovered_key, ECDSASignatureData signatureData) {
        ECDSASignature sig =
                new ECDSASignature(
                        PrimitiveUtil.toBigInt(signatureData.getR()),
                        PrimitiveUtil.toBigInt(signatureData.getS()));

        BigInteger pubkey = Sign.recoverFromSignature(signatureData.getV() - 27, sig, hash);
        String adddress = JavascriptWalletAddress.generate_address((byte) AdrestusConfiguration.version, recovered_key.toString());
        return given_address.equals(adddress) && pubkey.equals(recovered_key);
    }

    public static ECDSASignature sign(byte[] transactionHash, BigInteger privateKey) {
        ECDSASigner signer = new ECDSASigner(new HMacDSAKCalculator(new SHA256Digest()));

        ECPrivateKeyParameters privKey = new ECPrivateKeyParameters(privateKey, Sign.CURVE);
        signer.init(true, privKey);
        Object[] components = signer.generateSignature2(transactionHash);
        return new ECDSASignature(
                (BigInteger) components[0], (BigInteger) components[1], (ECPoint) components[2]);
    }
}
