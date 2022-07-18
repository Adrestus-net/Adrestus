package io.Adrestus.crypto;

import io.Adrestus.crypto.elliptic.ECKeyPair;
import io.Adrestus.crypto.elliptic.Keys;
import org.junit.jupiter.api.Test;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class KeysTest {


    @Test
    public void testCreateSecp256k1KeyPair() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
        KeyPair keyPair = Keys.createSecp256k1KeyPair(new SecureRandom("fd8cee9c1a3f3f57ab51b25740b24341ae093c8f697fde4df948050d3acd1700f6379d716104d2159e4912509c40ac81714d833e93b822e5ba0fadd68d5568a2".getBytes(StandardCharsets.UTF_8)));
        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();
        assertNotNull(privateKey);
        assertNotNull(publicKey);

        assertEquals(privateKey.getEncoded().length, 144);
        assertEquals(publicKey.getEncoded().length, 88);

        assertEquals("30818d020100301006072a8648ce3d020106052b8104000a047630740201010420abe4be4ffb24b6fa4e60d73d363226bb5491ea12860bc32bda6cc8aa4b353010a00706052b8104000aa144034200048bd1f6e01fdea256a9b076c6aea0655178fe137a9b3830a7c332708bc1b0408412554bc21a1c26280b553e9268f946675a2781d98f46b8cbb4d02e2eaeed1a4a", Hex.toHexString(privateKey.getEncoded()));
        assertEquals("3056301006072a8648ce3d020106052b8104000a034200048bd1f6e01fdea256a9b076c6aea0655178fe137a9b3830a7c332708bc1b0408412554bc21a1c26280b553e9268f946675a2781d98f46b8cbb4d02e2eaeed1a4a", Hex.toHexString(publicKey.getEncoded()));

        ECKeyPair ecKeyPair = ECKeyPair.create(keyPair);
        BigInteger privkey = ecKeyPair.getPrivateKey();
        BigInteger pubkey = ecKeyPair.getPublicKey();

    }

    @Test
    public void testCreateSecp256k1ECKeyPair() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
        ECKeyPair ecKeyPair = Keys.createEcKeyPair(new SecureRandom("fd8cee9c1a3f3f57ab51b25740b24341ae093c8f697fde4df948050d3acd1700f6379d716104d2159e4912509c40ac81714d833e93b822e5ba0fadd68d5568a2".getBytes(StandardCharsets.UTF_8)));
        BigInteger privkey = ecKeyPair.getPrivateKey();
        BigInteger pubkey = ecKeyPair.getPublicKey();

        assertEquals("30489271286773948315576322815247146897355215523116155573218828219209841422097", privkey.toString());
        assertEquals("705006124375164519399359298485541078144015988848578870590849882689690858916196764077370345376000353127571009618456922651069629791442505421063198506831197", pubkey.toString());

    }

}
