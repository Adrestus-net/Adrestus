package io.Adrestus.crypto;

import io.Adrestus.crypto.elliptic.ECKeyPair;
import io.Adrestus.crypto.elliptic.Keys;
import org.junit.jupiter.api.Test;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.*;

import static java.security.DrbgParameters.Capability.RESEED_ONLY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class KeysTest {


    @Test
    public void testCreateSecp256k1KeyPair() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
        String mnemonic_code="fd8cee9c1a3f3f57ab51b25740b24341ae093c8f697fde4df948050d3acd1700f6379d716104d2159e4912509c40ac81714d833e93b822e5ba0fadd68d5568a2";
        SecureRandom random = SecureRandom.getInstance("DRBG", DrbgParameters.instantiation(256, RESEED_ONLY, null));
        random.setSeed(Hex.decode(mnemonic_code));

        KeyPair keyPair = Keys.createSecp256k1KeyPair(random);
        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();
        assertNotNull(privateKey);
        assertNotNull(publicKey);

        assertEquals(privateKey.getEncoded().length, 144);
        assertEquals(publicKey.getEncoded().length, 88);

        //assertEquals("30818d020100301006072a8648ce3d020106052b8104000a047630740201010420c6f42ca1841591364360322a0ad036b1ce733108a4be0c417d103d4a257dfbf9a00706052b8104000aa144034200047dd30d3be14c2ee516250dbe13985b5ba513332468af7bc6f4e0315727b88574d322e3c909d10c9ed9ac771e7838e356d27296e16ef53fbd5d5ce5a962f99275", Hex.toHexString(privateKey.getEncoded()));
       // assertEquals("3056301006072a8648ce3d020106052b8104000a034200047dd30d3be14c2ee516250dbe13985b5ba513332468af7bc6f4e0315727b88574d322e3c909d10c9ed9ac771e7838e356d27296e16ef53fbd5d5ce5a962f99275", Hex.toHexString(publicKey.getEncoded()));

        ECKeyPair ecKeyPair = ECKeyPair.create(keyPair);
        BigInteger privkey = ecKeyPair.getPrivateKey();
        BigInteger pubkey = ecKeyPair.getPublicKey();

    }

    @Test
    public void testCreateSecp256k1ECKeyPair() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
        String mnemonic_code="fd8cee9c1a3f3f57ab51b25740b24341ae093c8f697fde4df948050d3acd1700f6379d716104d2159e4912509c40ac81714d833e93b822e5ba0fadd68d5568a2";
        SecureRandom random = SecureRandom.getInstance("DRBG", DrbgParameters.instantiation(256, RESEED_ONLY, null));
        random.setSeed(Hex.decode(mnemonic_code));

        ECKeyPair ecKeyPair = Keys.createEcKeyPair(random);
        BigInteger privkey = ecKeyPair.getPrivateKey();
        BigInteger pubkey = ecKeyPair.getPublicKey();

        assertEquals("26903329154642796832523293264795347878662684790193895193701921431089113363280", privkey.toString());
        assertEquals("11092764253300999286035676338784689255765239530865629740083516154369746518866752874188360339810766145570695426332984678123626401143532802369325886429810326", pubkey.toString());

    }

}
