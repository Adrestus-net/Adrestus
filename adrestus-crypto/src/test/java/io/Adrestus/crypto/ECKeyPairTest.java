package io.Adrestus.crypto;

import io.Adrestus.config.AdrestusConfiguration;
import io.Adrestus.crypto.elliptic.*;
import org.apache.hc.core5.util.ByteArrayBuffer;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.ECPublicKey;
import java.security.spec.*;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ECKeyPairTest {
    static SecureRandom random;
    private static int version = 0x00;

    @BeforeAll
    public static void Setup() throws NoSuchAlgorithmException, NoSuchProviderException {
        String mnemonic_code = "fd8cee9c1a3f3f57ab51b25740b24341ae093c8f697fde4df948050d3acd1700f6379d716104d2159e4912509c40ac81714d833e93b822e5ba0fadd68d5568a2";
        random = SecureRandom.getInstance(AdrestusConfiguration.ALGORITHM, AdrestusConfiguration.PROVIDER);
        random.setSeed(Hex.decode(mnemonic_code));
    }

    @Test
    public void verifyECKeyPairTest() throws Exception {
        ECKeyPair ecKeyPair = Keys.createEcKeyPair(random);
        String message = "verify test";
        ECDSASignature signature = ecKeyPair.sign(message.getBytes());
        boolean verify = ecKeyPair.verify(message.getBytes(StandardCharsets.UTF_8), signature);

        assertEquals(verify, true);
    }

    @Test
    public void verifySecp256ECDSASignTest() throws Exception {
        ECKeyPair ecKeyPair = Keys.createEcKeyPair(random);
        ECDSASign ecdsaSign = new ECDSASign();
        String message = "message";


        ECDSASignatureData signatureData = ecdsaSign.secp256SignMessage(message.getBytes(), ecKeyPair);
        String r = Hex.toHexString(signatureData.getR());
        String s = Hex.toHexString(signatureData.getS());

        boolean verify = ecdsaSign.secp256Verify(message.getBytes(StandardCharsets.UTF_8), ecKeyPair.getPublicKey(), signatureData);

        assertEquals(verify, true);

    }

    @Test
    public void verifySecp256ECDSASignFromeNodJStest() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException, InvalidParameterSpecException {
        KeyPair keyPair = Keys.createSecp256k1KeyPair(random);
        ECPublicKey publicKey = (ECPublicKey) keyPair.getPublic();
        byte[] otherPub = Hex.decode("04104afaf5a596a835d6d2039059a6a864af698a1611b6667b6bbaa68887af6f362b20bbac2b35be4bdd72bb6c2bd6b9e9820ab6e3ec9288c72471f7a9c4b69a65");
        ByteArrayBuffer xBytes = new ByteArrayBuffer(33);
        ByteArrayBuffer yBytes = new ByteArrayBuffer(33);

        byte[] zero = {(byte)0x00};
        xBytes.append(zero, 0, 1);
        xBytes.append(otherPub, 0, 32);
        yBytes.append(zero, 0, 1);
        yBytes.append(otherPub, 32, 32);

        //BigInteger x = new BigInteger(xBytes.array());
        //BigInteger y = new BigInteger(yBytes.array());

        // generate the public key point
        BigInteger x = new BigInteger("7369484319337998596972647547379871491753219237714478965059054797133358001974");
        BigInteger y = new BigInteger("19507286863381576855511297146826386734262360006212439790426001023900868581989");
        String curveName = "secp256k1";

        ECPoint point = new ECPoint(x, y);

        KeyFactory kfBc = KeyFactory.getInstance("EC", "BC");
        AlgorithmParameters parameters = AlgorithmParameters.getInstance("EC", "BC");
        parameters.init(new ECGenParameterSpec("secp256k1"));
        ECParameterSpec ecParamSpec = parameters.getParameterSpec(ECParameterSpec.class);
        PublicKey pubKey = kfBc.generatePublic(new ECPublicKeySpec(point, ecParamSpec));
        BCECPublicKey publicKeys = (BCECPublicKey) pubKey;

        byte[] publicKeyBytes = publicKeys.getQ().getEncoded(false);
        BigInteger  publicKeyValue= new BigInteger(1, Arrays.copyOfRange(publicKeyBytes, 1, publicKeyBytes.length));
        String gfd=Hex.toHexString(publicKeyValue.toByteArray());
        ECDSASign ecdsaSign = new ECDSASign();
        String message = "dd6d5849a507fc670db1c9ce77fea2166658e1c9b697b33ee9e1d07c03290da3";
        ECDSASignatureData signatureData = new ECDSASignatureData();
        signatureData.setV((byte) 1);
        signatureData.setR(new BigInteger("72206948797534088208614803801283696908693199660360101710869586509783673281188").toByteArray());
        signatureData.setS(new BigInteger("25862194351575536146049253133699227087619381433572936560523349568599918570974").toByteArray());

        boolean verify = ecdsaSign.secp256Verify(HashUtil.sha256(message.getBytes(StandardCharsets.UTF_8)), publicKeyValue, signatureData);
        assertEquals(verify, true);

    }

     @Test
    public void verifySecp256ECDSAWithAdressSignTest() throws Exception {
        String message = "message";
        String mnemonic_code = "fd8cee9c1a3f3f57ab51b25740b24341ae093c8f697fde4df948050d3acd1700f6379d716104d2159e4912509c40ac81714d833e93b822e5ba0fadd68d5568a2";
        SecureRandom random = SecureRandom.getInstance(AdrestusConfiguration.ALGORITHM, AdrestusConfiguration.PROVIDER);
        random.setSeed(Hex.decode(mnemonic_code));

        ECKeyPair ecKeyPair = Keys.createEcKeyPair(random);
        String adddress = WalletAddress.generate_address((byte) version, ecKeyPair.getPublicKey());
        assertEquals("ADR-AB2C-ARNW-4BYP-7CGJ-K6AD-OSNM-NC6Q-ET2C-6DEW-AAWY", adddress);
        ECDSASign ecdsaSign = new ECDSASign();


        ECDSASignatureData signatureData = ecdsaSign.secp256SignMessage(message.getBytes(StandardCharsets.UTF_8), ecKeyPair);
        boolean verify = ecdsaSign.secp256Verify(message.getBytes(StandardCharsets.UTF_8), adddress, signatureData);

        assertEquals(verify, true);


        ECDSASignatureData signatureData2 = ecdsaSign.secp256SignMessage(HashUtil.sha256(adddress.getBytes(StandardCharsets.UTF_8)), ecKeyPair);
        boolean verify2 = ecdsaSign.secp256Verify(HashUtil.sha256(adddress.getBytes(StandardCharsets.UTF_8)), adddress, signatureData2);

        assertEquals(verify2, true);
    }

}
