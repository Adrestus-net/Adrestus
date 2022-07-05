package io.Adrestus.crypto;

import io.Adrestus.crypto.mnemonic.MnemonicException;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.spec.KeySpec;

public class AESKeyFactory {

    private static final int ITERATION_COUNT = 2048;
    private static final int KEY_LENGTH = 512;

    public static byte[] PBEKeySpec(char[] mnemonic, byte[] salt) throws MnemonicException {
        try {
            KeySpec ks = new PBEKeySpec(mnemonic, salt, 2048, 512);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
            return skf.generateSecret(ks).getEncoded();
        } catch (Exception e) {
            throw new MnemonicException("Fatal error when generating seed from io.Adrestus.crypto.mnemonic!");
        }
    }

}
