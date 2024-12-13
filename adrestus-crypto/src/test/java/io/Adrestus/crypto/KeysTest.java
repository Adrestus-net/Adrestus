package io.Adrestus.crypto;

import io.Adrestus.config.AdrestusConfiguration;
import io.Adrestus.crypto.elliptic.ECKeyPair;
import io.Adrestus.crypto.elliptic.Keys;
import org.junit.jupiter.api.Test;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.security.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class KeysTest {


    @Test
    public void testCreateSecp256k1KeyPair() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
        String mnemonic_code = "fd8cee9c1a3f3f57ab51b25740b24341ae093c8f697fde4df948050d3acd1700f6379d716104d2159e4912509c40ac81714d833e93b822e5ba0fadd68d5568a2";
        SecureRandom random = SecureRandom.getInstance(AdrestusConfiguration.ALGORITHM, AdrestusConfiguration.PROVIDER);
        random.setSeed(Hex.decode(mnemonic_code));

        ECKeyPair ecKeyPair = Keys.create256k1KeyPair(random);
        PrivateKey privateKey = ecKeyPair.getPrivateKey();
        PublicKey publicKey = ecKeyPair.getPublicKey();
        assertNotNull(privateKey);
        assertNotNull(publicKey);

        assertEquals(privateKey.getEncoded().length, 144);
        assertEquals(publicKey.getEncoded().length, 88);

        assertEquals("30818d020100301006072a8648ce3d020106052b8104000a04763074020101042056c015ffc097286d7a7df56ac009207b6c4dc1cc58d0f9f371f3b407cb3823f0a00706052b8104000aa144034200042fc511f60742347dfdf32ba0857ebcc17f5406c4916ca14b5c4dfd1824dd7a679cb3d488b4847b8bd9850a03264b2ff2ed743bdfa104537f781f3004636c256c", Hex.toHexString(privateKey.getEncoded()));
        assertEquals("3056301006072a8648ce3d020106052b8104000a034200042fc511f60742347dfdf32ba0857ebcc17f5406c4916ca14b5c4dfd1824dd7a679cb3d488b4847b8bd9850a03264b2ff2ed743bdfa104537f781f3004636c256c", Hex.toHexString(publicKey.getEncoded()));


    }

    @Test
    public void testCreateSecp256r1KeyPair() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
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

        assertEquals("39238291446340184678769765295892952269353051716317326752595290395849166169072", privateKey.toString());
        assertEquals("13219558520765083084724661669251575557534740818689330502418938789975937142671164642064607206315682893215990855302393233429971338670635453148794084315717402", publicKey.toString());

    }

    @Test
    public void testCreateSecp256k1ECKeyPair() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
        String mnemonic_code = "fd8cee9c1a3f3f57ab51b25740b24341ae093c8f697fde4df948050d3acd1700f6379d716104d2159e4912509c40ac81714d833e93b822e5ba0fadd68d5568a2";
        SecureRandom random = SecureRandom.getInstance(AdrestusConfiguration.ALGORITHM, AdrestusConfiguration.PROVIDER);
        random.setSeed(Hex.decode(mnemonic_code));

        byte[] buffer = new byte[100];
        random.nextBytes(buffer);
        System.out.println(Hex.toHexString(buffer));
        assertEquals("56c015ffc097286d7a7df56ac009207b6c4dc1cc58d0f9f371f3b407cb3823f0a2e432f51e324accd2f3ade1b54c2a98d73faa2142545ee7013a2ee074db61d9034067d4cb75db7b3a13590da8f13d07aaf38c6930d7407020f1f06fbb9925ee83e0f609", Hex.toHexString(buffer));
        ECKeyPair ecKeyPair = Keys.create256k1KeyPair(random);
        BigInteger privkey = ecKeyPair.getPrivKey();
        BigInteger pubkey = ecKeyPair.getPubKey();

        assertEquals("99454431803388781435962397227963000152026134228328055028340563038084520124659", privkey.toString());
        assertEquals("13298854597259276942021680370136498911976670581556864990377071162998335940117506231969544158167789042632013409932975885259309830343819302384576809417303250", pubkey.toString());

    }

}
