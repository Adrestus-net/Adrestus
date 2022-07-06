package io.Adrestus.crypto.vdf.engine;

import com.google.common.hash.Hashing;
import io.Adrestus.crypto.pca.cs.jna.gmp.GMP;
import io.Adrestus.crypto.vdf.model.ClassGroup;
import io.Adrestus.crypto.vdf.utils.BigIntUtils;

import java.math.BigInteger;
import java.util.*;

public class VdfEngineWesolowski extends VdfEngine {

    public VdfEngineWesolowski(int numBits) {
        super(numBits);
    }

    private long[] approximateParameters(long t) {
        double logMemory = Math.log(10_000_000.0)/Math.log(2);
        double logT = Math.log(t)/Math.log(2);
        double l = (logT - logMemory > 0)? Math.ceil(Math.pow(2.0, logMemory - 20.0)): 1.0;
        double intermediate = t * Math.log(2.0) / (2.0*l);
        double k = Math.round(Math.log(intermediate) - Math.log(Math.log(intermediate)) + 0.25);
        if(k < 1.0) k = 1.0;
        double w = Math.floor(t/(t/k + l*Math.pow(2.0, k+1.0) -2.0));
        return new long[] {(long)l, (long)k, (long)w};
    }
    
    private byte[] u64ToBytes(long q) {
        return new byte[] {
                (byte) (q >> 56),
                (byte) (q >> 48),
                (byte) (q >> 40),
                (byte) (q >> 32),
                (byte) (q >> 24),
                (byte) (q >> 16),
                (byte) (q >> 8),
                (byte) q
        };
    }
    
    private BigInteger hashPrime(byte[] xBuf, byte[] yBuf) {
        long j = 0;
        while(true) {            
            byte[] token = "prime".getBytes();
            byte[] jbytes = u64ToBytes(j); 
            int len = token.length + jbytes.length + xBuf.length + yBuf.length;
            byte[] input = new byte[len];
            
            int curLen = 0;
            
            System.arraycopy(token, 0, input, curLen, token.length);
            curLen += token.length;
            
            System.arraycopy(jbytes, 0, input, curLen, jbytes.length);
            curLen += jbytes.length;
            
            System.arraycopy(xBuf, 0, input, curLen, xBuf.length);
            curLen += xBuf.length;
            
            System.arraycopy(yBuf, 0, input, curLen, yBuf.length);
            
            byte[] hash = Hashing.sha256().hashBytes(input).asBytes();
            BigInteger n = BigIntUtils.createBigInteger(1, hash, 0, 16);
            if(n.isProbablePrime(2)) {
                return n;
            }
            j++;            
        }
    }
    
    private BigInteger getBlock(long i, long k, long t, BigInteger b) {
        BigInteger two = BigIntUtils.createBigInteger(2);
        BigInteger res = GMP.modPowInsecure(two, BigIntUtils.createBigInteger(t-k*(i+1)), b);
        res = res.multiply(two.shiftRight(1).shiftLeft((int)k));
        return GMP.divide(res, b);
    }
    
    private ClassGroup evalOptimized(ClassGroup h, BigInteger b, long t, long k, long l, Map<Long, ClassGroup> powers) {
        long kl = k*l;
        long k1 = k >> 1;
        long k0 = k - k1;
        ClassGroup x = h.identity();
        ClassGroup identity = h.identity();
        long kExp = 1l << k;
        long k0Exp = 1l << k0;
        long k1Exp = 1l << k1;
        
        for(long j = l-1; j>=0; j--) {
            x.pow(BigIntUtils.createBigInteger(kExp));
            Map<String, ClassGroup> ys = new HashMap<>();
            for(long _b = 0; _b < (1l<<k); _b++) {
                ys.put(BigIntUtils.createBigInteger(_b).toString(), identity.clone());
            }
            long endOfLoop = (long) Math.ceil((double)t / kl);
            
            for(int i = 0; i < endOfLoop; i++) {
                if(t < k * (i * l + j + 1)) {
                    continue;
                }
                BigInteger _b = getBlock(i*l, k, t, b);
                ys.get(_b.toString()).multiplyWith(powers.get(i*kl));
            }
            
            for(int b1 = 0; b1 < k1Exp; b1++) {
                ClassGroup z = identity.clone();
                for(int b0 = 0; b0 < k0Exp; b0++) {
                    BigInteger key = BigIntUtils.createBigInteger(b1 * k0Exp + b0);
                    z.multiplyWith(ys.get(key.toString()));
                }
                z.pow(BigIntUtils.createBigInteger(b1*k0Exp));
                x.multiplyWith(z);
            }
            
            for(int b0 = 0; b0 < k0Exp; b0++) {
                ClassGroup z = identity.clone();
                for(int b1 = 0; b1 < k1Exp; b1++) {
                    BigInteger key = BigIntUtils.createBigInteger(b1 * k0Exp + b0);
                    z.multiplyWith(ys.get(key.toString()));
                }
                z.pow(BigIntUtils.createBigInteger(b0));
                x.multiplyWith(z);
            }
        }
        return x;
    }
    
    private ClassGroup generateProof(ClassGroup x, long iterations, long k, long l, Map<Long,ClassGroup> powers, int numBits) {
        int elementLen = 2 * ((numBits + 16) >> 4);
        byte[] xBuf = new byte[elementLen];
        x.serialize(xBuf);
        byte[] yBuf = new byte[elementLen];
        powers.get(iterations).serialize(yBuf);
        BigInteger b = hashPrime(xBuf, yBuf);
        return evalOptimized(x, b, iterations, k, l, powers);
    }
    
    public byte[] createProofOfTime(byte[] challenge, long iterations, int numBits) {
        BigInteger discriminant = createDiscriminant(challenge, numBits);
        ClassGroup x = ClassGroup.fromABDiscriminant(BigIntUtils.createBigInteger(2), BigIntUtils.createBigInteger(1), discriminant);
        long[] tmp = approximateParameters(iterations);
        long l = tmp[0], k = tmp[1];
        long q = l*k;
        
        List<Long> powersToCalculate = new ArrayList<>();
        for(int i = 0; i <= iterations / q + 1; i++) {
            powersToCalculate.add((long) i*q);
        }
        powersToCalculate.add(iterations);
        
        Map<Long,ClassGroup> powers = ClassGroup.iterateSquarings(x.clone(), powersToCalculate);powers.get(200l);        
        ClassGroup proof = generateProof(x, iterations, k, l, powers, numBits);
        return serialize(Arrays.asList(proof), powers.get(iterations), numBits);
    }
    
    @Override
    public byte[] solve(byte[] challenge, long difficulty) {
        return createProofOfTime(challenge, difficulty, numBits);
    }
    
    private boolean verifyProof(ClassGroup x, ClassGroup y, ClassGroup proof, long iterations) {
        int elementLen = 2 * ((numBits + 16) >> 4);
        byte[] xBuf = new byte[elementLen];
        x.serialize(xBuf);
        byte[] yBuf = new byte[elementLen];
        y.serialize(yBuf);
        BigInteger b = hashPrime(xBuf, yBuf);
        BigInteger r = GMP.modPowInsecure(BigIntUtils.createBigInteger(2), BigIntUtils.createBigInteger(iterations), b);
        proof.pow(b);
        x.pow(r);
        proof.multiplyWith(x);
        return proof.equals(y);
    }
    
    @Override
    public boolean verify(byte[] challenge, long iterations, byte[] proofBlob) {
        BigInteger discriminant = createDiscriminant(challenge, numBits);
        ClassGroup x = ClassGroup.fromABDiscriminant(BigIntUtils.createBigInteger(2), BigIntUtils.createBigInteger(1), discriminant);
        int length = (numBits + 16) >> 4;
        
        if(length* 4 != proofBlob.length) {
            return false;
        }
        
        byte[] resultBytes = new byte[length*2];
        System.arraycopy(proofBlob, 0, resultBytes, 0, 2*length);
        
        byte[] proofBytes = new byte[proofBlob.length - 2*length];
        System.arraycopy(proofBlob, 2*length, proofBytes, 0, proofBytes.length);
        
        ClassGroup proof = ClassGroup.fromBytes(proofBytes, discriminant);
        ClassGroup y = ClassGroup.fromBytes(resultBytes, discriminant);
        
        return verifyProof(x, y, proof, iterations);
    }
}
