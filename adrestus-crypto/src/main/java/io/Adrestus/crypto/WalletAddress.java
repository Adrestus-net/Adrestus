package io.Adrestus.crypto;

import java.math.BigInteger;
import java.util.Arrays;

public class WalletAddress {
    private static final int ADR_CHECKSUM_BYTES = 4;
    private static final String ADR_CHECKSUM = "ADR";
    private static final String SEPERATOR = "-";

    public static String generate_address(final byte version, BigInteger publicKey) {
        // step 1: sha3 hash of the public key
        final byte[] sha3PublicKeyHash = HashUtil.sha3(publicKey.toByteArray());

        // step 2: ripemd160 hash of (1)
        final byte[] ripemd160StepOneHash = HashUtil.ripemd160(sha3PublicKeyHash);

        // step 3: add version byte in front of (2)
        final byte[] versionPrefixedRipemd160Hash = PrimitiveUtil.concat(new byte[]{version}, ripemd160StepOneHash);

        // step 4: get the checksum of (3)
        final byte[] stepThreeChecksum = generateChecksum(versionPrefixedRipemd160Hash);

        // step 5: concatenate (3) and (4)
        final byte[] concatStepThreeAndStepSix = PrimitiveUtil.concat(versionPrefixedRipemd160Hash, stepThreeChecksum);

        // step 6: base32 encode (5)
        String encoded = Base32Encoder.getString(concatStepThreeAndStepSix);
        //step7 pretty_print encoded
        return preety_print(encoded);
    }

    private static byte[] generateChecksum(final byte[] input) {
        // step 1: sha3 hash of (input
        final byte[] sha3StepThreeHash = HashUtil.sha3(input);
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
