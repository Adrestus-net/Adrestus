package io.Adrestus.crypto.vdf.engine;

import com.google.common.hash.Hashing;
import io.Adrestus.crypto.vdf.constants.Const;
import io.Adrestus.crypto.vdf.model.ClassGroup;

import java.math.BigInteger;
import java.util.List;

import static io.Adrestus.crypto.vdf.utils.BigIntUtils.createBigInteger;


abstract public class VdfEngine {

    abstract public byte[] solve(byte[] challenge, long difficulty);

    abstract public boolean verify(byte[] challenge, long difficulty, byte[] proofBlob);

    private final static BigInteger M = new BigInteger(String.valueOf(8 * 3 * 5 * 7 * 11 * 13));

    protected int numBits;

    public VdfEngine(int numBits) {
        this.numBits = numBits;
    }

    private byte[] randomBytesFromSeed(byte[] seed, int byteCount) {
        if (!(byteCount <= 32 * ((1 << 16) - 1))) {
            throw new RuntimeException("byteCount too large");
        }

        byte[] blob = new byte[byteCount];
        short extra = 0;

        int len = 0;

        while (len < byteCount) {
            byte[] tmp = new byte[seed.length + 2];
            System.arraycopy(seed, 0, tmp, 0, seed.length);
            tmp[seed.length] = (byte) ((extra & 0xFF00) >> 8);
            tmp[seed.length + 1] = (byte) ((extra & 0xFF));
            byte[] hash = Hashing.sha256().hashBytes(tmp).asBytes();
            int appendLen = hash.length < blob.length - len ? hash.length : blob.length - len;
            System.arraycopy(hash, 0, blob, len, appendLen);
            len += appendLen;
            extra += 1;
        }

        return blob;
    }

    protected BigInteger createDiscriminant(byte[] seed, int length) {
        byte extra = (byte) (length & 7);
        int randomBytesLen = (((int) (length) + 7) >> 3) + 2;
        byte[] randomBytes = randomBytesFromSeed(seed, randomBytesLen);
        int b1 = (randomBytes[randomBytes.length - 2] & 0x00FF);
        int b0 = (randomBytes[randomBytes.length - 1] & 0x00FF);
        int numerator = (b1 << 8) + b0;

        BigInteger n = createBigInteger(1, randomBytes, 0, randomBytes.length - 2);
        n = n.shiftRight((8 - extra) & 7);
        n = n.setBit(length - 1);

        BigInteger residue = createBigInteger(Const.RESIDUES[numerator % Const.RESIDUES.length]);
        BigInteger rem = n.remainder(M);

        if (residue.compareTo(rem) > 0) {
            n = n.add(residue.subtract(rem));
        } else {
            n = n.subtract(rem.subtract(residue));
        }

        while (true) {
            BigInteger sieve = new BigInteger(new byte[65536 / 8]);
            int[][] sieveInfo = Const.getSieveInfo();
            for (int i = 0; i < sieveInfo.length; i++) {
                int p = sieveInfo[i][0], q = sieveInfo[i][1];
                int crem = p - n.remainder(createBigInteger(p)).intValue();
                int ival = (int) ((crem * (long) q) % p);

                while (ival < 65536) {
                    sieve.setBit(ival);
                    ival += p;
                }

            }
            for (int i = 0; i < 65536; i++) {
                if (!sieve.testBit(i)) {
                    int q = M.intValue() * i;
                    BigInteger Q = createBigInteger(q);
                    if (n.add(Q).isProbablePrime(2)) {
                        return n.add(Q).negate();
                    }
                }
            }
            n = n.add(M.shiftLeft(16));
        }

    }

    protected byte[] serialize(List<ClassGroup> proof, ClassGroup y, int numBits) {
        int proofLen = proof.size();
        int elementLength = 2 * ((numBits + 16) >> 4);
        int proofLenInBytes = (proofLen + 1) * elementLength;
        byte[] v = new byte[proofLenInBytes];

        byte[] tmp = new byte[elementLength];
        y.serialize(tmp);
        System.arraycopy(tmp, 0, v, 0, tmp.length);

        for (int index = 0; index < proof.size(); index++) {
            ClassGroup group = proof.get(index);
            int offset = (index + 1) * elementLength;

            tmp = new byte[elementLength];
            group.serialize(tmp);

            System.arraycopy(tmp, 0, v, offset, tmp.length);
        }
        return v;

    }
}
