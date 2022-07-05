package io.Adrestus.crypto;
import io.Adrestus.crypto.elliptic.ECKeyPair;
import io.Adrestus.crypto.elliptic.Keys;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;
public class AddressTest {
    private static int version=0x00;
    @Test
    public void geneate_address() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
        ECKeyPair ecKeyPair = Keys.createEcKeyPair(new SecureRandom("fd8cee9c1a3f3f57ab51b25740b24341ae093c8f697fde4df948050d3acd1700f6379d716104d2159e4912509c40ac81714d833e93b822e5ba0fadd68d5568a2".getBytes(StandardCharsets.UTF_8)));
        String adddress=WalletAddress.generate_address((byte)version,ecKeyPair.getPublicKey());

        //System.out.println(adddress);
        assertEquals("ADR-ADL3-VDZK-ZU7H-2BX5-M2H4-S7LF-5SR4-ECQA-EIUJ-CBFK",adddress);
        assertEquals(53,adddress.length());
    }
}
