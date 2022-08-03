package io.Adrestus.crypto.elliptic;

import io.Adrestus.crypto.HashUtil;
import io.Adrestus.crypto.PrimitiveUtil;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.signers.HMacDSAKCalculator;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.encoders.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;

public class ECDSASign implements SignInterface {

    private static final Logger logger = LoggerFactory.getLogger(ECDSASign.class);

    private static final BigInteger curveN = new BigInteger(1, Hex.decode("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEBAAEDCE6AF48A03BBFD25E8CD0364141"));
    private static final BigInteger halfCurveN = curveN.shiftRight(1);

    /**
     * Sign the message with ECDSA algorithm without add 27 to v
     *
     * @param message
     * @param keyPair
     * @return
     */
    public SignatureData secp256SignMessage(byte[] message, ECKeyPair keyPair) {
        SignatureData signatureData = signMessage(message, keyPair);
        return new SignatureData(
                (byte) (signatureData.getV() - 27), signatureData.getR(), signatureData.getS());
    }


    public boolean secp256Verify(
            byte[] hash, BigInteger publicKey, SignatureData signatureData) {
        return verify(
                hash,
                publicKey,
                new SignatureData(
                        (byte) (signatureData.getV() + 27),
                        signatureData.getR(),
                        signatureData.getS()));
    }


    @Override
    public SignatureData signMessage(byte[] message, ECKeyPair keyPair) {
        BigInteger privateKey = keyPair.getPrivateKey();
        BigInteger publicKey = keyPair.getPublicKey();

        byte[] messageHash = HashUtil.sha256(message);

        ECDSASignature sig = sign(messageHash, privateKey);

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

        return new SignatureData(v, r, s);
    }


    public boolean verify(byte[] hash, BigInteger publicKey, SignatureData signatureData) {
        ECDSASignature sig =
                new ECDSASignature(
                        PrimitiveUtil.toBigInt(signatureData.getR()),
                        PrimitiveUtil.toBigInt(signatureData.getS()));

        BigInteger k = Sign.recoverFromSignature(signatureData.getV() - 27, sig, hash);
        return publicKey.equals(k);
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
