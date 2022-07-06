package io.Adrestus.crypto.vdf.model;

import io.Adrestus.crypto.pca.cs.jna.gmp.GMP;
import io.Adrestus.crypto.vdf.utils.BigIntUtils;


import java.math.BigInteger;

public class CongruenceContext {
    public BigInteger g, d, q, r;
    
    public CongruenceContext() {
        this.g = BigIntUtils.createBigInteger(0);
        this.d = BigIntUtils.createBigInteger(0);
        this.q = BigIntUtils.createBigInteger(0);
        this.r = BigIntUtils.createBigInteger(0);
    }
    
    public BigInteger[] solveLinearCongruence(BigInteger a, BigInteger b, BigInteger m) {
        BigInteger[] tmp = GMP.gcdExt(a, m);
        this.g = tmp[0];
        this.d = tmp[1];
        //BigInteger mu = tmp[2];
        
        //ffi::mpz_divexact(&mut self.q, b, &self.g)
        this.q = GMP.exactDivide(b, this.g);
        
        //ffi::mpz_mul(&mut self.r, &self.q, &self.d);
        this.r = this.q.multiply(this.d);
        
        //ffi::mpz_tdiv_r(mu, &self.r, m);
        BigInteger mu = GMP.remainder(this.r, m);
        
        //ffi::mpz_divexact(v, &m, &self.g)
        BigInteger v = GMP.exactDivide(m, this.g);
        
        return new BigInteger[] { mu, v};
    }
}