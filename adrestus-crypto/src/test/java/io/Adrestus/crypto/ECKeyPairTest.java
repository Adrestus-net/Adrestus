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
        byte[] otherPub = Hex.decode("04a359cbf789702dd4d6da661e90c0c1c3bc13bb3a7588b9c5c093cf2c9f65678f3afe11d2fb05dbed6553f63a6bb5d9b66c1a52abb7daade24a31fd870922d253");
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
        BigInteger x = new BigInteger("73885651435926854515264701221164520142160681037984229233067136520784684869519");
        BigInteger y = new BigInteger("26683047389995651185679566240952828910936171073908714048119596426948530852435");
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
        signatureData.setV((byte) 0);
        signatureData.setR(new BigInteger("30179190089666276834887403079562508974417649980904472865724382004973443579854").toByteArray());
        signatureData.setS(new BigInteger("14029798542497621816798343676332730497595770105064178818079147459382128035034").toByteArray());

        boolean verify = ecdsaSign.secp256Verify(HashUtil.sha256(message.getBytes(StandardCharsets.UTF_8)), publicKeyValue, signatureData);
        assertEquals(verify, true);

    }

    @Test
    public void verifySecp256ECDSAWithNoHashSignFromeNodJStest() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException, InvalidParameterSpecException {
        byte[] otherPub = Hex.decode("04a359cbf789702dd4d6da661e90c0c1c3bc13bb3a7588b9c5c093cf2c9f65678f3afe11d2fb05dbed6553f63a6bb5d9b66c1a52abb7daade24a31fd870922d253");
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
        BigInteger x = new BigInteger("73885651435926854515264701221164520142160681037984229233067136520784684869519");
        BigInteger y = new BigInteger("26683047389995651185679566240952828910936171073908714048119596426948530852435");
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
        String message = "e713ba6967eb576b9938386aa5b3e8fa2ed8db9f5f049a5eed98e5e047be547d";
        ECDSASignatureData signatureData = new ECDSASignatureData();
        signatureData.setV((byte) 0);
        signatureData.setR(new BigInteger("104104462189388496674295863654745954800812928602529478304174108943686078344309").toByteArray());
        signatureData.setS(new BigInteger("105281600949042327574926769196801460083914307623685880615063712348764126352282").toByteArray());

        boolean verify = ecdsaSign.secp256Verify(message.getBytes(StandardCharsets.UTF_8), publicKeyValue, signatureData);
        assertEquals(verify, false);

    }

    @Test
    public void verifySecp256ECDSASignFromeNodJStest2() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException, InvalidParameterSpecException {

        BigInteger x = new BigInteger("73885651435926854515264701221164520142160681037984229233067136520784684869519");
        BigInteger y = new BigInteger("26683047389995651185679566240952828910936171073908714048119596426948530852435");

        ECDSASign ecdsaSign = new ECDSASign();
        String message = "dd6d5849a507fc670db1c9ce77fea2166658e1c9b697b33ee9e1d07c03290da3";
        ECDSASignatureData signatureData = new ECDSASignatureData();
        signatureData.setV((byte) 0);
        signatureData.setR(new BigInteger("30179190089666276834887403079562508974417649980904472865724382004973443579854").toByteArray());
        signatureData.setS(new BigInteger("14029798542497621816798343676332730497595770105064178818079147459382128035034").toByteArray());

        BigInteger publicKeyValue=ecdsaSign.recoverPublicKeyValue(x,y);
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

    @Test
    public void verifySecp256ECDSAWithAdressSignFromNodeJSTest() throws Exception {
        String adddress ="ADR-GDQJ-JJTL-4IWX-4CUH-J73M-IRJP-BA44-UFXC-BNGK-HB6B";
        String OriginalKey="04a359cbf789702dd4d6da661e90c0c1c3bc13bb3a7588b9c5c093cf2c9f65678f3afe11d2fb05dbed6553f63a6bb5d9b66c1a52abb7daade24a31fd870922d253";
        BigInteger x = new BigInteger("73885651435926854515264701221164520142160681037984229233067136520784684869519");
        BigInteger y = new BigInteger("26683047389995651185679566240952828910936171073908714048119596426948530852435");

        ECDSASign ecdsaSign = new ECDSASign();
        String message = "dd6d5849a507fc670db1c9ce77fea2166658e1c9b697b33ee9e1d07c03290da3";
        ECDSASignatureData signatureData = new ECDSASignatureData();
        signatureData.setV((byte) 0);
        signatureData.setR(new BigInteger("30179190089666276834887403079562508974417649980904472865724382004973443579854").toByteArray());
        signatureData.setS(new BigInteger("14029798542497621816798343676332730497595770105064178818079147459382128035034").toByteArray());

        BigInteger publicKeyValue=ecdsaSign.recoverPublicKeyValue(x,y);
        boolean verify = ecdsaSign.secp256Verify(HashUtil.sha256(message.getBytes(StandardCharsets.UTF_8)),adddress,signatureData, publicKeyValue, OriginalKey);
        assertEquals(verify, true);
    }
}
