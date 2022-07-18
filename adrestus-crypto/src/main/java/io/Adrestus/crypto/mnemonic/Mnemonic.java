package io.Adrestus.crypto.mnemonic;

import io.Adrestus.crypto.AESKeyFactory;
import io.Adrestus.crypto.HashUtil;
import io.Adrestus.crypto.PrimitiveUtil;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.SecureRandom;
import java.util.List;

public class Mnemonic {


    private static final char[] SALT_CHARS = new char[]{'m', 'n', 'e', 'm', 'o', 'n', 'i', 'c'};
    private Security strength;
    private WordList wordList;

    public Mnemonic(Security strength, WordList wordList) {
        this.strength = strength;
        this.wordList = wordList;
    }

    public char[] create() throws MnemonicException {
        int byteCount = strength.getRawValue() / 8;
        byte[] bytes = SecureRandom.getSeed(byteCount);
        return create(bytes, wordList);
    }

    private char[] create(byte[] entropy, WordList wordList) throws MnemonicException {
        try {
            byte[] hashBits = HashUtil.sha256(entropy, new BouncyCastleProvider());
            char[] binaryHash = PrimitiveUtil.bytesToBinaryAsChars(hashBits);
            char[] checkSum = PrimitiveUtil.charSubArray(binaryHash, 0, entropy.length * 8 / 32);
            char[] entropyBits = PrimitiveUtil.bytesToBinaryAsChars(entropy);
            StringBuilder concatenatedBits = new StringBuilder().append(entropyBits).append(checkSum);

            List<char[]> words = wordList.getWordsAsCharArray();
            StringBuilder mnemonicBuilder = new StringBuilder();
            for (int index = 0; index < concatenatedBits.length() / 11; ++index) {

                int startIndex = index * 11;
                int endIndex = startIndex + 11;
                char[] wordIndexAsChars = new char[endIndex - startIndex];
                concatenatedBits.getChars(startIndex, endIndex, wordIndexAsChars, 0);
                int wordIndex = PrimitiveUtil.binaryCharsToInt(wordIndexAsChars);
                mnemonicBuilder.append(words.get(wordIndex)).append(' ');
            }

            char[] mnemonic = new char[mnemonicBuilder.length() - 1];
            mnemonicBuilder.getChars(0, mnemonicBuilder.length() - 1, mnemonic, 0);
            return mnemonic;
        } catch (Exception e) {
            throw new MnemonicException("Fatal error! SHA-256 algorithm does not exist!");
        }

    }


    public byte[] createSeed(char[] mnemonic, char[] passphrase) throws MnemonicException {
        char[] merged;
        if (passphrase == null) {
            throw new MnemonicException("Passphrase should not be null! Please enter a valid password");
        }
        merged = PrimitiveUtil.concatCharArrays(SALT_CHARS, passphrase);
        byte[] salt = PrimitiveUtil.toBytes(merged);
        return AESKeyFactory.PBEKeySpec(mnemonic, salt);
    }
}
