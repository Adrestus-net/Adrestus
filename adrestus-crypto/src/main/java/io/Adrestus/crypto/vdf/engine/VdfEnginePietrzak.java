package io.Adrestus.crypto.vdf.engine;

import com.google.common.hash.Hashing;
import io.Adrestus.crypto.vdf.model.ClassGroup;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static io.Adrestus.crypto.vdf.utils.BigIntUtils.createBigInteger;


public class VdfEnginePietrzak extends VdfEngine {

    public VdfEnginePietrzak(int numBits) {
        super(numBits);
    }

    private long approximateI(long iterations) {
        double x = ((iterations >> 1) / 8.0) * Math.log(2.0);
        double w = Math.log(x) - Math.log(Math.log(x)) + 0.25;
        return Math.round(w / (2.0 * Math.log(2.0)));
    }

    private List<Long> sumCombinations(List<Long> numbers) {
        List<Long> combinations = new ArrayList<>();
        combinations.add(0l);
        for (Long i : numbers) {
            List<Long> newCombinations = new ArrayList<>(combinations);
            for (Long j : combinations) {
                newCombinations.add(i + j);
            }
            combinations = newCombinations;
        }
        combinations.remove(0);
        return combinations;
    }

    private List<Long> cacheIndicesForCount(long iterations) {
        long i = approximateI(iterations);
        List<Long> intermediateTs = new ArrayList<>();
        long currT = iterations;
        for (long j = 0; j < i; j++) {
            currT >>= 1;
            intermediateTs.add(currT);

            if ((currT & 1) != 0) {
                currT += 1;
            }
        }
        List<Long> cacheIndices = sumCombinations(intermediateTs);
        Collections.sort(cacheIndices);
        cacheIndices.add(iterations);
        return cacheIndices;
    }

    private long calculateFinalT(long t, int delta) {
        long currT = t;
        List<Long> ts = new ArrayList<>();

        while (currT != 2) {
            ts.add(currT);
            currT >>= 1;
            if ((currT & 1) == 1) {
                currT += 1;
            }
        }
        ts.add(2l);
        ts.add(1l);

        if (!(ts.size() >= delta)) {
            throw new RuntimeException("Error ts length");
        }
        return ts.get(ts.size() - delta);
    }

    private BigInteger generateRvalue(ClassGroup x, ClassGroup y, ClassGroup sqrtMu, int numBits) {
        int size = (numBits + 16) >> 4;
        byte[] v = new byte[size * 2];
        byte[] input = new byte[v.length * 3];

        x.serialize(v);
        System.arraycopy(v, 0, input, 0, v.length);

        y.serialize(v);
        System.arraycopy(v, 0, input, v.length, v.length);

        sqrtMu.serialize(v);
        System.arraycopy(v, 0, input, 2 * v.length, v.length);

        byte[] res = Hashing.sha256().hashBytes(input).asBytes();

        return createBigInteger(1, res, 0, 16);
    }

    private List<ClassGroup> generateProof(ClassGroup x, long iterations, int delta, Map<Long, ClassGroup> powers, int numBits) {
        ClassGroup identity = x.identity();
        long i = approximateI(iterations);

        List<BigInteger> rs = new ArrayList<>();

        List<ClassGroup> xp = new ArrayList<>();
        xp.add(x);

        long currT = iterations;
        List<ClassGroup> yp = new ArrayList<>();
        yp.add(powers.get(currT).clone());

        long finalT = calculateFinalT(iterations, delta);

        List<Long> ts = new ArrayList<>();
        List<ClassGroup> mus = new ArrayList<>();

        int roundIndex = 0;
        while (currT != finalT) {
            if ((currT & 1) != 0) {
                throw new RuntimeException("currT is odd!");
            }

            long halfT = currT >> 1;
            ts.add(halfT);

            if (!(roundIndex < 63)) {
                throw new RuntimeException("roundIndex larger than 63!");
            }

            long denominator = 1 << (roundIndex + 1);

            if (roundIndex < i) {
                ClassGroup mu = identity.clone();
                for (int numerator = 1; numerator < denominator; numerator += 2) {
                    long num_bits = 62 - Long.numberOfLeadingZeros(denominator);
                    BigInteger rProp = createBigInteger(1);

                    for (long b = num_bits - 1; b >= 0; b--) {
                        if ((numerator & (1 << (b + 1))) == 0) {
                            rProp = rProp.multiply(rs.get((int) (num_bits - b - 1)));
                        }
                    }

                    long tSum = halfT;
                    for (long b = 0; b < num_bits; b++) {
                        if ((numerator & (1 << (b + 1))) != 0) {
                            tSum += ts.get((int) (num_bits - b - 1));
                        }
                    }

                    ClassGroup power = powers.get(tSum).clone();
                    power.pow(rProp);
                    mu.multiplyWith(power);
                }
                mus.add(mu);
            } else {
                ClassGroup mu = xp.get(xp.size() - 1).clone();

                for (long t = 0; t < halfT; t++) {
                    mu.multiplyWith(mu.clone());
                }
                mus.add(mu);
            }

            ClassGroup mu = mus.get(mus.size() - 1).clone();
            BigInteger lastR = generateRvalue(xp.get(0), yp.get(0), mu, numBits);

            if (lastR.signum() < 0) {
                throw new RuntimeException("lastR is negative");
            }

            rs.add(lastR);

            ClassGroup lastX = xp.get(xp.size() - 1).clone();
            lastX.pow(lastR);
            lastX.multiplyWith(mu);
            xp.add(lastX);

            mu.pow(lastR);
            mu.multiplyWith(yp.get(yp.size() - 1));
            yp.add(mu);

            currT >>= 1;
            if ((currT & 1) != 0) {
                currT += 1;
                yp.get(yp.size() - 1).square();
            }
            roundIndex += 1;
        }

        return mus;
    }

    private byte[] createProofOfTime(byte[] challenge, long iterations, int numBits) {
        BigInteger discriminant = createDiscriminant(challenge, numBits);
        ClassGroup x = ClassGroup.fromABDiscriminant(createBigInteger(2), createBigInteger(1), discriminant);
        int delta = 8;
        List<Long> powersToCalculate = cacheIndicesForCount(iterations);

        Map<Long, ClassGroup> powers = ClassGroup.iterateSquarings(x.clone(), new ArrayList<>(powersToCalculate));
        List<ClassGroup> proof = generateProof(x, iterations, delta, powers, numBits);
        return serialize(proof, powers.get(iterations), numBits);
    }

    @Override
    public byte[] solve(byte[] challenge, long difficulty) {
        return createProofOfTime(challenge, difficulty, numBits);
    }

    private List<ClassGroup> deserializeProof(byte[] proofBytes, BigInteger discriminant, int origLength) {
        int length = discriminant.bitLength();

        length = (length + 16) >> 4;

        if (length == 0) {
            return null;
        }

        if (origLength != length) {
            return null;
        }

        length *= 2;
        int proofBlobLength = proofBytes.length;

        if (proofBlobLength % length != 0) {
            return null;
        }

        int proofLen = proofBlobLength / length;

        List<ClassGroup> v = new ArrayList<>();

        for (int i = 0; i < proofLen; i++) {
            int offset = i * length;
            byte[] tmp = new byte[length];
            System.arraycopy(proofBytes, offset, tmp, 0, length);
            v.add(ClassGroup.fromBytes(tmp, discriminant));
        }

        return v;
    }

    private boolean verifyProof(ClassGroup xInitial, ClassGroup yInitial, List<ClassGroup> proof, long iterations, int delta, int numBits) {
        BigInteger one = createBigInteger(1);
        ClassGroup x = xInitial.clone();
        ClassGroup y = yInitial.clone();

        long finalT = calculateFinalT(iterations, delta);
        long currT = iterations;

        for (ClassGroup mu : proof) {
            if ((currT & 1) != 0) {
                throw new RuntimeException("Cannot have an odd number of iterations remaining");
            }
            BigInteger r = generateRvalue(xInitial, yInitial, mu, numBits);

            x.pow(r);
            x.multiplyWith(mu);
            mu.pow(r);
            y.multiplyWith(mu);

            currT >>= 1;
            if ((currT & 1) != 0) {
                currT += 1;
                y.square();
            }
        }

        one = one.shiftLeft((int) finalT);
        x.pow(one);
        x.discriminant.compareTo(y.discriminant);
        return x.equals(y);
    }

    @Override
    public boolean verify(byte[] challenge, long iterations, byte[] proofBlob) {
        BigInteger discriminant = createDiscriminant(challenge, numBits);
        ClassGroup x = ClassGroup.fromABDiscriminant(createBigInteger(2), createBigInteger(1), discriminant);
        int length = (numBits + 16) >> 4;

        if (proofBlob.length < 2 * length) {
            return false;
        }

        byte[] resultBytes = new byte[length * 2];
        System.arraycopy(proofBlob, 0, resultBytes, 0, 2 * length);

        byte[] proofBytes = new byte[proofBlob.length - 2 * length];
        System.arraycopy(proofBlob, 2 * length, proofBytes, 0, proofBytes.length);

        discriminant = x.discriminant;
        List<ClassGroup> proof = deserializeProof(proofBytes, discriminant, length);

        if (proof == null) {
            return false;
        }

        ClassGroup y = ClassGroup.fromBytes(resultBytes, discriminant);
        return verifyProof(x, y, proof, iterations, 8, numBits);
    }
}
