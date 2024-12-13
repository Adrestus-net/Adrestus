package io.Adrestus.crypto;

import io.Adrestus.config.AdrestusConfiguration;
import io.Adrestus.crypto.elliptic.ECKeyPair;
import io.Adrestus.crypto.elliptic.Keys;
import io.Adrestus.crypto.mnemonic.Mnemonic;
import io.Adrestus.crypto.mnemonic.Security;
import io.Adrestus.crypto.mnemonic.WordList;
import lombok.SneakyThrows;

import java.security.SecureRandom;

public class WalletAddressExport {
    private static final int version = 0x00;


    @SneakyThrows
    public static String getAddressFromMnemonic(String mnemonic, String passphrase) {
        Mnemonic mnem = new Mnemonic(Security.NORMAL, WordList.ENGLISH);
        char[] mnemonic1 = mnemonic.toCharArray();
        char[] passphrase_char = passphrase.toCharArray();
        byte[] seed = mnem.createSeed(mnemonic1, passphrase_char);
        SecureRandom random = SecureRandom.getInstance(AdrestusConfiguration.ALGORITHM, AdrestusConfiguration.PROVIDER);
        random.setSeed(seed);
        ECKeyPair ecKeyPair = Keys.create256k1KeyPair(random);
        String adddress = WalletAddress.generate_address((byte) version, ecKeyPair.getXpubAxis());
        return adddress;
    }
}
