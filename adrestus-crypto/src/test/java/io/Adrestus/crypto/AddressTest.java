package io.Adrestus.crypto;

import io.Adrestus.config.AdrestusConfiguration;
import io.Adrestus.crypto.elliptic.ECKeyPair;
import io.Adrestus.crypto.elliptic.Keys;
import org.junit.jupiter.api.Test;
import org.spongycastle.util.encoders.Hex;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AddressTest {
    private static int version = 0x00;

    @Test
    public void generate_address() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
        String mnemonic_code = "fd8cee9c1a3f3f57ab51b25740b24341ae093c8f697fde4df948050d3acd1700f6379d716104d2159e4912509c40ac81714d833e93b822e5ba0fadd68d5568a2";
        SecureRandom random = SecureRandom.getInstance(AdrestusConfiguration.ALGORITHM, AdrestusConfiguration.PROVIDER);
        random.setSeed(Hex.decode(mnemonic_code));

        ECKeyPair ecKeyPair = Keys.createEcKeyPair(random);
        String adddress = WalletAddress.generate_address((byte) version, ecKeyPair.getPublicKey());

        //System.out.println(adddress);
        assertEquals("ADR-AB2C-ARNW-4BYP-7CGJ-K6AD-OSNM-NC6Q-ET2C-6DEW-AAWY", adddress);
        assertEquals(53, adddress.length());
    }


    @Test
    public void get_addrees(){
        String mnemonic1 = "sample sail jungle learn general promote task puppy own conduct green affair";
        String passphrase = "p4ssphr4se";
        String address=WalletAddressExport.getAddressFromMnemonic(mnemonic1,passphrase);
        assertEquals("ADR-ABAZ-ZEZO-MKGN-MQKJ-N3IF-P7QT-3MJX-ZFSM-RHOW-DZ7J", address);
    }

    @Test
    public void Javascript_addrees(){
        String key="04104afaf5a596a835d6d2039059a6a864af698a1611b6667b6bbaa68887af6f362b20bbac2b35be4bdd72bb6c2bd6b9e9820ab6e3ec9288c72471f7a9c4b69a65";
        String address=JavascriptWalletAddress.generate_address((byte) version,key);
        assertEquals(address,"ADR-GBQW-FKOL-XRJH-2YZ7-UN5S-SGAG-AJJH-4U2Z-TBLY-OOV2");
    }

    @Test
    public void Javascript_addrees2(){
        String key="04a359cbf789702dd4d6da661e90c0c1c3bc13bb3a7588b9c5c093cf2c9f65678f3afe11d2fb05dbed6553f63a6bb5d9b66c1a52abb7daade24a31fd870922d253";
        String address=JavascriptWalletAddress.generate_address((byte) version,key);
        assertEquals(address,"ADR-GDQJ-JJTL-4IWX-4CUH-J73M-IRJP-BA44-UFXC-BNGK-HB6B");
    }
}
