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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.security.AlgorithmParameters;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPublicKeySpec;
import java.util.Arrays;

public class ECDSASign implements SignInterface {


    private static final Logger logger = LoggerFactory.getLogger(ECDSASign.class);

    private static final BigInteger curveN = new BigInteger(1, Hex.decode("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEBAAEDCE6AF48A03BBFD25E8CD0364141"));
    private static final BigInteger halfCurveN = curveN.shiftRight(1);


    public ECDSASign() {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }

    /**
     * Sign the message with ECDSA algorithm without add 27 to v
     *
     * @param message
     * @param keyPair
     * @return
     */
    public ECDSASignatureData secp256SignMessage(byte[] message, ECKeyPair keyPair) {
        ECDSASignatureData signatureData = signMessage(message, keyPair);
        return new ECDSASignatureData(
                (byte) (signatureData.getV() - 27), signatureData.getR(), signatureData.getS());
    }


    public boolean secp256Verify(byte[] hash, BigInteger publicKey, ECDSASignatureData signatureData) {
        return verify(
                hash,
                publicKey,
                new ECDSASignatureData(
                        (byte) (signatureData.getV() + 27),
                        signatureData.getR(),
                        signatureData.getS()));
    }

    public boolean secp256Verify(byte[] hash, String publicKey, ECDSASignatureData signatureData) {
        return verify(
                hash,
                publicKey,
                new ECDSASignatureData(
                        (byte) (signatureData.getV() + 27),
                        signatureData.getR(),
                        signatureData.getS()));
    }

    public boolean secp256Verify(byte[] hash, String address, BigInteger recovered_key, ECDSASignatureData signatureData) {
        return verify(
                hash,
                address,
                recovered_key, new ECDSASignatureData(
                        (byte) (signatureData.getV() + 27),
                        signatureData.getR(),
                        signatureData.getS()));
    }


    @Override
    public ECDSASignatureData signMessage(byte[] message, ECKeyPair keyPair) {
        BigInteger privateKey = keyPair.getPrivateKey();
        BigInteger publicKey = keyPair.getPublicKey();

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
    public BigInteger recoverPublicKeyValue(BigInteger x, BigInteger y) {
        java.security.spec.ECPoint point = new java.security.spec.ECPoint(x, y);

        KeyFactory kfBc = KeyFactory.getInstance(AdrestusConfiguration.SIGN_ALGORITHM, AdrestusConfiguration.SIGN_PROVIDER);
        AlgorithmParameters parameters = AlgorithmParameters.getInstance(AdrestusConfiguration.SIGN_ALGORITHM, AdrestusConfiguration.SIGN_PROVIDER);
        parameters.init(new ECGenParameterSpec(AdrestusConfiguration.SIGN_CURVE));
        ECParameterSpec ecParamSpec = parameters.getParameterSpec(ECParameterSpec.class);
        PublicKey pubKey = kfBc.generatePublic(new ECPublicKeySpec(point, ecParamSpec));
        BCECPublicKey publicKeys = (BCECPublicKey) pubKey;

        byte[] publicKeyBytes = publicKeys.getQ().getEncoded(false);
        BigInteger publicKeyValue = new BigInteger(1, Arrays.copyOfRange(publicKeyBytes, 1, publicKeyBytes.length));
        return publicKeyValue;
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
