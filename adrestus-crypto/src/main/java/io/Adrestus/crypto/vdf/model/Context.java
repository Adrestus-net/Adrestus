package io.Adrestus.crypto.vdf.model;

import io.Adrestus.crypto.vdf.utils.BigIntUtils;

import java.math.BigInteger;


public class Context {
    public BigInteger negativeA, r, denom, oldA, oldB, ra, s, x, h, w, m, u, a, l, j, b, k, t, mu, v, sigma, lambda;
    public CongruenceContext congruenceContext = new CongruenceContext();

    public Context() {
        this.negativeA = BigIntUtils.createBigInteger(0);
        this.r = BigIntUtils.createBigInteger(0);
        this.denom = BigIntUtils.createBigInteger(0);
        this.oldA = BigIntUtils.createBigInteger(0);
        this.oldB = BigIntUtils.createBigInteger(0);
        this.ra = BigIntUtils.createBigInteger(0);
        this.s = BigIntUtils.createBigInteger(0);
        this.x = BigIntUtils.createBigInteger(0);
        this.h = BigIntUtils.createBigInteger(0);
        this.w = BigIntUtils.createBigInteger(0);
        this.m = BigIntUtils.createBigInteger(0);
        this.u = BigIntUtils.createBigInteger(0);
        this.a = BigIntUtils.createBigInteger(0);
        this.l = BigIntUtils.createBigInteger(0);
        this.j = BigIntUtils.createBigInteger(0);
        this.b = BigIntUtils.createBigInteger(0);
        this.k = BigIntUtils.createBigInteger(0);
        this.t = BigIntUtils.createBigInteger(0);
        this.mu = BigIntUtils.createBigInteger(0);
        this.v = BigIntUtils.createBigInteger(0);
        this.sigma = BigIntUtils.createBigInteger(0);
        this.lambda = BigIntUtils.createBigInteger(0);
    }
}