package io.Adrestus.crypto;

import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class JavascriptWalletAddress {

    private static final int ADR_CHECKSUM_BYTES = 4;
    private static final String ADR_CHECKSUM = "ADR";
    private static final String SEPERATOR = "-";

    public static String generate_address(final byte version, String hexkey) {
        // step 1: sha3 hash of the public key
        final byte[] sha256PublicKeyHash = HashUtil.sha256(hexkey.getBytes(StandardCharsets.UTF_8));

        // step 2: ripemd160 hash of (1)
        final byte[] ripemd160StepOneHash = HashUtil.ripemd160(sha256PublicKeyHash);

        String re2= Hex.toHexString(ripemd160StepOneHash);
        // step 3: add version byte in front of (2)
        final byte[] versionPrefixedRipemd160Hash = PrimitiveUtil.concat(String.valueOf(version).getBytes(StandardCharsets.UTF_8), ripemd160StepOneHash);

        String re= Hex.toHexString(versionPrefixedRipemd160Hash);
        // step 4: get the checksum of (3)
        final byte[] stepThreeChecksum = generateChecksum(versionPrefixedRipemd160Hash);

        String re23= Hex.toHexString(stepThreeChecksum);
        // step 5: concatenate (3) and (4)
        final byte[] concatStepThreeAndStepSix = PrimitiveUtil.concat(versionPrefixedRipemd160Hash, stepThreeChecksum);

        String re234= Hex.toHexString(concatStepThreeAndStepSix);
        // step 6: base32 encode (5)
        String encoded = Base32Encoder.getString(concatStepThreeAndStepSix);
        //step7 pretty_print encoded
        return preety_print(encoded);
    }

    private static byte[] generateChecksum(final byte[] input) {
        // step 1: sha3 hash of (input
        final byte[] sha3StepThreeHash = HashUtil.sha256(input);
        // step 2: get the first X bytes of (1)
        return Arrays.copyOfRange(sha3StepThreeHash, 0, ADR_CHECKSUM_BYTES);
    }

    private static String preety_print(String encoded) {
        StringBuilder builder = new StringBuilder(encoded);
        builder.insert(0, ADR_CHECKSUM);
        for (int i = 3; i < builder.length(); i = i + 5) {
            builder.insert(i, SEPERATOR);
        }
        return builder.toString();
    }
}
