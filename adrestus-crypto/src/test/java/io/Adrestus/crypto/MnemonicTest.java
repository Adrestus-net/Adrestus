package io.Adrestus.crypto;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.Adrestus.crypto.mnemonic.Mnemonic;
import io.Adrestus.crypto.mnemonic.MnemonicException;
import io.Adrestus.crypto.mnemonic.Security;
import io.Adrestus.crypto.mnemonic.WordList;
import org.junit.jupiter.api.Test;
import org.spongycastle.util.encoders.Hex;

public class MnemonicTest {
    private Mnemonic mnem;

    @Test
    public void generate12WordMnemonic() throws MnemonicException {

        mnem=new Mnemonic(Security.NORMAL, WordList.ENGLISH);
        char[] mnemonic_sequence =mnem.create();
        String[] words = String.valueOf(mnemonic_sequence).split(" ");
        assertEquals(12,words.length);
    }

    @Test
    public void generate24WordMnemonic() throws MnemonicException {

        mnem=new Mnemonic(Security.HIGH, WordList.ENGLISH);
        char[] mnemonic_sequence =mnem.create();
        String[] words = String.valueOf(mnemonic_sequence).split(" ");
        assertEquals(24,words.length);
    }

    @Test
    public void seedTest() throws MnemonicException {
        char[] mnemonic = "bench hurt jump file august wise shallow faculty impulse spring exact slush thunder author capable act festival slice deposit sauce coconut afford frown better".toCharArray();
        char[] passphrase = "p4ssphr4se".toCharArray();

        mnem=new Mnemonic(Security.HIGH, WordList.ENGLISH);
        assertEquals("fd8cee9c1a3f3f57ab51b25740b24341ae093c8f697fde4df948050d3acd1700f6379d716104d2159e4912509c40ac81714d833e93b822e5ba0fadd68d5568a2",Hex.toHexString(mnem.createSeed(mnemonic,passphrase)));
    }
}
