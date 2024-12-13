package io.Adrestus.crypto;

import io.Adrestus.config.AdrestusConfiguration;
import io.Adrestus.crypto.elliptic.ECDSASign;
import io.Adrestus.crypto.elliptic.ECDSASignatureData;
import io.Adrestus.crypto.elliptic.ECKeyPair;
import io.Adrestus.crypto.elliptic.Keys;
import org.apache.hc.core5.util.ByteArrayBuffer;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.conscrypt.Conscrypt;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.*;
import java.util.Arrays;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

public class ECKeyPairTest {
    static SecureRandom random;
    private static int version = 0x00;

    @BeforeAll
    public static void Setup() throws NoSuchAlgorithmException, NoSuchProviderException {
        Security.addProvider(Conscrypt.newProvider());
        Security.insertProviderAt(Conscrypt.newProvider(), 0);

        String mnemonic_code = "fd8cee9c1a3f3f57ab51b25740b24341ae093c8f697fde4df948050d3acd1700f6379d716104d2159e4912509c40ac81714d833e93b822e5ba0fadd68d5568a2";
        random = SecureRandom.getInstance(AdrestusConfiguration.ALGORITHM, AdrestusConfiguration.PROVIDER);
        random.setSeed(Hex.decode(mnemonic_code));
    }

    @Test
    public void verifySecp256k1ECDSASignTest() throws Exception {
        ECKeyPair ecKeyPair = Keys.create256k1KeyPair(random);
        ECDSASign ecdsaSign = new ECDSASign();
        String message = "message";


        ECDSASignatureData signatureData = ecdsaSign.signSecp256k1Message(message.getBytes(), ecKeyPair);
        String r = Hex.toHexString(signatureData.getR());
        String s = Hex.toHexString(signatureData.getS());

        boolean verify = ecdsaSign.secp256k1Verify(message.getBytes(StandardCharsets.UTF_8), ecKeyPair.getPubKey(), signatureData);

        assertEquals(verify, true);

    }

    @Test
    public void verifySecp256k1ECDSASignFromeNodJStest() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException, InvalidParameterSpecException {
        byte[] otherPub = Hex.decode("04a359cbf789702dd4d6da661e90c0c1c3bc13bb3a7588b9c5c093cf2c9f65678f3afe11d2fb05dbed6553f63a6bb5d9b66c1a52abb7daade24a31fd870922d253");
        ByteArrayBuffer xBytes = new ByteArrayBuffer(33);
        ByteArrayBuffer yBytes = new ByteArrayBuffer(33);

        byte[] zero = {(byte) 0x00};
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
        BigInteger publicKeyValue = new BigInteger(1, Arrays.copyOfRange(publicKeyBytes, 1, publicKeyBytes.length));
        String gfd = Hex.toHexString(publicKeyValue.toByteArray());
        ECDSASign ecdsaSign = new ECDSASign();
        String message = "dd6d5849a507fc670db1c9ce77fea2166658e1c9b697b33ee9e1d07c03290da3";
        ECDSASignatureData signatureData = new ECDSASignatureData((byte) 0, new BigInteger("30179190089666276834887403079562508974417649980904472865724382004973443579854").toByteArray(), new BigInteger("14029798542497621816798343676332730497595770105064178818079147459382128035034").toByteArray());

        boolean verify = ecdsaSign.secp256k1Verify(HashUtil.sha256(message.getBytes(StandardCharsets.UTF_8)), publicKeyValue, signatureData);
        assertEquals(verify, true);

    }

    @Test
    public void verifySecp256k1ECDSAWithNoHashSignFromeNodJStest() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException, InvalidParameterSpecException {
        byte[] otherPub = Hex.decode("04a359cbf789702dd4d6da661e90c0c1c3bc13bb3a7588b9c5c093cf2c9f65678f3afe11d2fb05dbed6553f63a6bb5d9b66c1a52abb7daade24a31fd870922d253");
        ByteArrayBuffer xBytes = new ByteArrayBuffer(33);
        ByteArrayBuffer yBytes = new ByteArrayBuffer(33);

        byte[] zero = {(byte) 0x00};
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
        BigInteger publicKeyValue = new BigInteger(1, Arrays.copyOfRange(publicKeyBytes, 1, publicKeyBytes.length));
        String gfd = Hex.toHexString(publicKeyValue.toByteArray());
        ECDSASign ecdsaSign = new ECDSASign();
        String message = "e713ba6967eb576b9938386aa5b3e8fa2ed8db9f5f049a5eed98e5e047be547d";
        ECDSASignatureData signatureData = new ECDSASignatureData((byte) 0, new BigInteger("104104462189388496674295863654745954800812928602529478304174108943686078344309").toByteArray(), new BigInteger("105281600949042327574926769196801460083914307623685880615063712348764126352282").toByteArray());

        boolean verify = ecdsaSign.secp256k1Verify(message.getBytes(StandardCharsets.UTF_8), publicKeyValue, signatureData);
        assertEquals(verify, false);

    }

    @Test
    public void verifySecp256k1ECDSASignFromeNodJStest2() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException, InvalidParameterSpecException {

        BigInteger x = new BigInteger("73885651435926854515264701221164520142160681037984229233067136520784684869519");
        BigInteger y = new BigInteger("26683047389995651185679566240952828910936171073908714048119596426948530852435");

        ECDSASign ecdsaSign = new ECDSASign();
        String message = "dd6d5849a507fc670db1c9ce77fea2166658e1c9b697b33ee9e1d07c03290da3";
        ECDSASignatureData signatureData = new ECDSASignatureData((byte) 0, new BigInteger("30179190089666276834887403079562508974417649980904472865724382004973443579854").toByteArray(), new BigInteger("14029798542497621816798343676332730497595770105064178818079147459382128035034").toByteArray());

        BigInteger publicKeyValue = ecdsaSign.recoverSecp256k1PublicKeyValue(x, y);
        boolean verify = ecdsaSign.secp256k1Verify(HashUtil.sha256(message.getBytes(StandardCharsets.UTF_8)), publicKeyValue, signatureData);
        assertEquals(verify, true);

    }

    @Test
    public void verifySecp256k1ECDSAWithAdressSignTest() throws Exception {
        String message = "message";
        String mnemonic_code = "fd8cee9c1a3f3f57ab51b25740b24341ae093c8f697fde4df948050d3acd1700f6379d716104d2159e4912509c40ac81714d833e93b822e5ba0fadd68d5568a2";
        SecureRandom random = SecureRandom.getInstance(AdrestusConfiguration.ALGORITHM, AdrestusConfiguration.PROVIDER);
        random.setSeed(Hex.decode(mnemonic_code));

        ECKeyPair ecKeyPair = Keys.create256k1KeyPair(random);
        String adddress = WalletAddress.generate_address((byte) version, ecKeyPair.getPubKey());
        assertEquals("ADR-AB2C-ARNW-4BYP-7CGJ-K6AD-OSNM-NC6Q-ET2C-6DEW-AAWY", adddress);
        ECDSASign ecdsaSign = new ECDSASign();


        ECDSASignatureData signatureData = ecdsaSign.signSecp256k1Message(message.getBytes(StandardCharsets.UTF_8), ecKeyPair);
        boolean verify = ecdsaSign.secp256k1Verify(message.getBytes(StandardCharsets.UTF_8), adddress, signatureData);

        assertEquals(verify, true);


        ECDSASignatureData signatureData2 = ecdsaSign.signSecp256k1Message(HashUtil.sha256(adddress.getBytes(StandardCharsets.UTF_8)), ecKeyPair);
        boolean verify2 = ecdsaSign.secp256k1Verify(HashUtil.sha256(adddress.getBytes(StandardCharsets.UTF_8)), adddress, signatureData2);

        assertEquals(verify2, true);
    }

    @Test
    public void verifySecp256k1ECDSAWithAdressSignFromNodeJSTest() throws Exception {
        String adddress = "ADR-GBXZ-Z5UR-3UVR-6XUE-NP4N-VDVA-BE3S-TLOL-GEYS-VLBJ";
        BigInteger x = new BigInteger("73885651435926854515264701221164520142160681037984229233067136520784684869519");
        BigInteger y = new BigInteger("26683047389995651185679566240952828910936171073908714048119596426948530852435");

        ECDSASign ecdsaSign = new ECDSASign();
        String message = "dd6d5849a507fc670db1c9ce77fea2166658e1c9b697b33ee9e1d07c03290da3";
        ECDSASignatureData signatureData = new ECDSASignatureData((byte) 0, new BigInteger("30179190089666276834887403079562508974417649980904472865724382004973443579854").toByteArray(), new BigInteger("14029798542497621816798343676332730497595770105064178818079147459382128035034").toByteArray());

        BigInteger publicKeyValue = ecdsaSign.recoverSecp256k1PublicKeyValue(x, y);
        boolean verify = ecdsaSign.secp256k1Verify(HashUtil.sha256(message.getBytes(StandardCharsets.UTF_8)), adddress, publicKeyValue, signatureData);
        assertEquals(verify, true);
    }


    //same values as the above
    @Test
    public void verifySecp256k1ECDSABase64EncodeWithAdressSignFromNodeJSTest() throws Exception {
        String adddress = "ADR-GBXZ-Z5UR-3UVR-6XUE-NP4N-VDVA-BE3S-TLOL-GEYS-VLBJ";
        BigInteger x = new BigInteger("73885651435926854515264701221164520142160681037984229233067136520784684869519");
        BigInteger y = new BigInteger("26683047389995651185679566240952828910936171073908714048119596426948530852435");

        ECDSASign ecdsaSign = new ECDSASign();
        String message = "dd6d5849a507fc670db1c9ce77fea2166658e1c9b697b33ee9e1d07c03290da3";
        ECDSASignatureData signatureData = new ECDSASignatureData((byte) 0, Base64.getDecoder().decode("QrjQ9wiWv/wkFXTZN20gdKU/cmjH1Dv1o8snhZH5f84="), Base64.getDecoder().decode("HwSVpnel8+Wg/o8u9Q99fQmSvmeLNDFEG/zDhQ+mRNo="));

        BigInteger publicKeyValue = ecdsaSign.recoverSecp256k1PublicKeyValue(x, y);
        boolean verify = ecdsaSign.secp256k1Verify(HashUtil.sha256(message.getBytes(StandardCharsets.UTF_8)), adddress, publicKeyValue, signatureData);
        assertEquals(verify, true);
    }

    @Test
    public void verifySecp256r1ECDSAGenKeysFromJava() throws Exception {
        String mnemonic_code = "fd8cee9c1a3f3f57ab51b25740b24341ae093c8f697fde4df948050d3acd1700f6379d716104d2159e4912509c40ac81714d833e93b822e5ba0fadd68d5568a2";
        SecureRandom random = SecureRandom.getInstance(AdrestusConfiguration.ALGORITHM, AdrestusConfiguration.PROVIDER);
        random.setSeed(Hex.decode(mnemonic_code));

        ECKeyPair ecKeyPair = Keys.create256r1KeyPair(random);
        PrivateKey privateKey = ecKeyPair.getPrivateKey();
        PublicKey publicKey = ecKeyPair.getPublicKey();
        assertNotNull(privateKey);
        assertNotNull(publicKey);

        assertEquals(privateKey.getEncoded().length, 150);
        assertEquals(publicKey.getEncoded().length, 91);

        assertEquals("308193020100301306072a8648ce3d020106082a8648ce3d03010704793077020101042056c015ffc097286d7a7df56ac009207b6c4dc1cc58d0f9f371f3b407cb3823f0a00a06082a8648ce3d030107a14403420004fc67db266ee61e5d49d72244912a4653cb7e02e58dbe2c9750c2f554ff1c8d12e2e469be8e9fef80001b26d907b57eae389fb58a19945fd5bc8e7acd45255f1a", Hex.toHexString(privateKey.getEncoded()));
        assertEquals("3059301306072a8648ce3d020106082a8648ce3d03010703420004fc67db266ee61e5d49d72244912a4653cb7e02e58dbe2c9750c2f554ff1c8d12e2e469be8e9fef80001b26d907b57eae389fb58a19945fd5bc8e7acd45255f1a", Hex.toHexString(publicKey.getEncoded()));

        KeyPair keyPair = new KeyPair(publicKey, privateKey);
        ECKeyPair ecKeyPair1 = new ECKeyPair(keyPair.getPrivate(), keyPair.getPublic());
        BigInteger privkey1 = ecKeyPair1.getPrivKey();
        BigInteger pubkey1 = ecKeyPair1.getPubKey();
        assertEquals("39238291446340184678769765295892952269353051716317326752595290395849166169072", privkey1.toString());
        assertEquals("13219558520765083084724661669251575557534740818689330502418938789975937142671164642064607206315682893215990855302393233429971338670635453148794084315717402", pubkey1.toString());


        String message = "This is a message to encrypt";
        // Sign a message
        Signature ecdsaSign = Signature.getInstance("SHA256withECDSA", "Conscrypt");
        ecdsaSign.initSign(ecKeyPair.getPrivateKey());
        ecdsaSign.update(message.getBytes());
        byte[] signature = ecdsaSign.sign();

        Signature ecdsaVerify = Signature.getInstance("SHA256withECDSA", "Conscrypt");
        ecdsaVerify.initVerify(ecKeyPair.getPublicKey());
        ecdsaVerify.update(message.getBytes());
        boolean isVerified = ecdsaVerify.verify(signature);
        assertTrue(isVerified);
    }

    @Test
    public void verifySecp256r1ECDSAGenKeysFromJava2() throws Exception {
        String mnemonic_code = "fd8cee9c1a3f3f57ab51b25740b24341ae093c8f697fde4df948050d3acd1700f6379d716104d2159e4912509c40ac81714d833e93b822e5ba0fadd68d5568a2";
        SecureRandom random = SecureRandom.getInstance(AdrestusConfiguration.ALGORITHM, AdrestusConfiguration.PROVIDER);
        random.setSeed(Hex.decode(mnemonic_code));

        ECKeyPair ecKeyPair = Keys.create256r1KeyPair(random);
        PrivateKey privateKey = ecKeyPair.getPrivateKey();
        PublicKey publicKey = ecKeyPair.getPublicKey();
        assertNotNull(privateKey);
        assertNotNull(publicKey);

        assertEquals(privateKey.getEncoded().length, 150);
        assertEquals(publicKey.getEncoded().length, 91);

        assertEquals("308193020100301306072a8648ce3d020106082a8648ce3d03010704793077020101042056c015ffc097286d7a7df56ac009207b6c4dc1cc58d0f9f371f3b407cb3823f0a00a06082a8648ce3d030107a14403420004fc67db266ee61e5d49d72244912a4653cb7e02e58dbe2c9750c2f554ff1c8d12e2e469be8e9fef80001b26d907b57eae389fb58a19945fd5bc8e7acd45255f1a", Hex.toHexString(privateKey.getEncoded()));
        assertEquals("3059301306072a8648ce3d020106082a8648ce3d03010703420004fc67db266ee61e5d49d72244912a4653cb7e02e58dbe2c9750c2f554ff1c8d12e2e469be8e9fef80001b26d907b57eae389fb58a19945fd5bc8e7acd45255f1a", Hex.toHexString(publicKey.getEncoded()));

        ECDSASign ecdsaSign = new ECDSASign();
        String message = "This is a message to encrypt";
        ECDSASignatureData signatureData = ecdsaSign.signSecp256r1Message(message.getBytes(), ecKeyPair);
        boolean isVerify = ecdsaSign.secp256r1Verify(message.getBytes(), ecKeyPair.getXpubAxis(), ecKeyPair.getYpubAxis(), signatureData);
        assertTrue(isVerify);
    }

    @Test
    public void verifySecp256r1ECDSAGenKeysFromJava3() throws Exception {
        String mnemonic_code = "fd8cee9c1a3f3f57ab51b25740b24341ae093c8f697fde4df948050d3acd1700f6379d716104d2159e4912509c40ac81714d833e93b822e5ba0fadd68d5568a2";
        SecureRandom random = SecureRandom.getInstance(AdrestusConfiguration.ALGORITHM, AdrestusConfiguration.PROVIDER);
        random.setSeed(Hex.decode(mnemonic_code));

        ECKeyPair ecKeyPair = Keys.create256r1KeyPair(random);
        PrivateKey privateKey = ecKeyPair.getPrivateKey();
        PublicKey publicKey = ecKeyPair.getPublicKey();
        assertNotNull(privateKey);
        assertNotNull(publicKey);

        assertEquals(privateKey.getEncoded().length, 150);
        assertEquals(publicKey.getEncoded().length, 91);

        assertEquals("308193020100301306072a8648ce3d020106082a8648ce3d03010704793077020101042056c015ffc097286d7a7df56ac009207b6c4dc1cc58d0f9f371f3b407cb3823f0a00a06082a8648ce3d030107a14403420004fc67db266ee61e5d49d72244912a4653cb7e02e58dbe2c9750c2f554ff1c8d12e2e469be8e9fef80001b26d907b57eae389fb58a19945fd5bc8e7acd45255f1a", Hex.toHexString(privateKey.getEncoded()));
        assertEquals("3059301306072a8648ce3d020106082a8648ce3d03010703420004fc67db266ee61e5d49d72244912a4653cb7e02e58dbe2c9750c2f554ff1c8d12e2e469be8e9fef80001b26d907b57eae389fb58a19945fd5bc8e7acd45255f1a", Hex.toHexString(publicKey.getEncoded()));

        ECDSASign ecdsaSign = new ECDSASign();
        String message = "This is a message to encrypt";
        ECDSASignatureData signatureData = ecdsaSign.signSecp256r1Message(message.getBytes(), ecKeyPair);
        boolean isVerify = ecdsaSign.secp256r1Verify(message.getBytes(), publicKey, signatureData);
        assertTrue(isVerify);
    }

    @Test
    public void verifySecp256r1ECDSASignFromeNodJStest() throws Exception {
        // this values must be the same from signature test Secp256r1 javascript
        BigInteger x = new BigInteger("40514664234503751466016427554848457032751439455031112288062843717205542817298");
        BigInteger y = new BigInteger("14527189120191333519754157065023554017764004589942514072254600755095301164659");

        // Create ECParameterSpec for secp256r1
        AlgorithmParameters parameters = AlgorithmParameters.getInstance("EC");
        parameters.init(new ECGenParameterSpec("secp256r1"));
        ECParameterSpec ecParameterSpec = parameters.getParameterSpec(ECParameterSpec.class);

        ECPoint ecPoint = new ECPoint(x, y);
        ECPublicKeySpec publicKeySpec = new ECPublicKeySpec(ecPoint, ecParameterSpec);

        // Generate private and public keys
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);

        String message = "This is a message";
        ECDSASignatureData signatureData = new ECDSASignatureData((byte) 0, new BigInteger("48549064968240863147030116803103256473861424912475096964636056245877951323437").toByteArray(), new BigInteger("57334573088845009007921191622939254076444319597529167282060938590776629642203").toByteArray());


//        // Combine r and s into DER format
//        byte[] rBytes = signatureData.getR();
//        byte[] sBytes = signatureData.getS();
//        byte[] derSignature = new byte[rBytes.length + sBytes.length];
//        System.arraycopy(rBytes, 0, derSignature, 0, rBytes.length);
//        System.arraycopy(sBytes, 0, derSignature, rBytes.length, sBytes.length);
//
//        byte[] completeSignature = new byte[derSignature.length + 1];
//        System.arraycopy(derSignature, 0, completeSignature, 0, derSignature.length);
//        completeSignature[completeSignature.length - 1] = signatureData.getV();

        //Add der signature to the Transaction object
        // Verify the signature this is from the javascript test signature test line: const derSignature = signature.toDER('hex');
        String hexSignature = "304402206b55cc07e73993962f921f6f20d5ddc89aa1dc308c78c95340a16744fe4e5d2d02207ec237e77b582c26223e96c720e1a92507e42b9114e4cc2270ef60c5cd27c3db";
        Signature ecdsaVerify = Signature.getInstance("SHA256withECDSA", "Conscrypt");
        ecdsaVerify.initVerify(publicKey);
        ecdsaVerify.update(message.getBytes());
        boolean isVerified = ecdsaVerify.verify(Hex.decode(hexSignature));
        assertTrue(isVerified);

    }

    @Test
    public void verifySecp256r1ECDSASignFromeNodJStest2() throws Exception {
        // this values must be the same from signature test Secp256r1 javascript
        BigInteger x = new BigInteger("40514664234503751466016427554848457032751439455031112288062843717205542817298");
        BigInteger y = new BigInteger("14527189120191333519754157065023554017764004589942514072254600755095301164659");

        // Create ECParameterSpec for secp256r1
        AlgorithmParameters parameters = AlgorithmParameters.getInstance("EC");
        parameters.init(new ECGenParameterSpec("secp256r1"));
        ECParameterSpec ecParameterSpec = parameters.getParameterSpec(ECParameterSpec.class);

        ECPoint ecPoint = new ECPoint(x, y);
        ECPublicKeySpec publicKeySpec = new ECPublicKeySpec(ecPoint, ecParameterSpec);

        // Generate private and public keys
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);

        String message = "This is a message";
        ECDSASignatureData signatureData = new ECDSASignatureData((byte) 0, new BigInteger("48549064968240863147030116803103256473861424912475096964636056245877951323437").toByteArray(), new BigInteger("57334573088845009007921191622939254076444319597529167282060938590776629642203").toByteArray(), publicKey.getEncoded(), Hex.decode("304402206b55cc07e73993962f921f6f20d5ddc89aa1dc308c78c95340a16744fe4e5d2d02207ec237e77b582c26223e96c720e1a92507e42b9114e4cc2270ef60c5cd27c3db"));
        ECDSASign ecdsaSign = new ECDSASign();
        boolean isVerified = ecdsaSign.secp256r1Verify(message.getBytes(), x, y, signatureData);
        assertTrue(isVerified);

    }
}
