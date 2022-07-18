package io.Adrestus.crypto.vdf.model;


import io.Adrestus.crypto.pca.cs.jna.gmp.GMP;

import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.Adrestus.crypto.vdf.utils.BigIntUtils.createBigInteger;


public class ClassGroup {
    public BigInteger a, b, c, discriminant;

    public ClassGroup(BigInteger a, BigInteger b, BigInteger c, BigInteger discriminant) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.discriminant = discriminant;
    }

    public ClassGroup clone() {
        return new ClassGroup(
                a,
                b,
                c,
                discriminant
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof ClassGroup) {
            ClassGroup other = (ClassGroup) obj;
            return other.a.compareTo(this.a) == 0
                    && other.b.compareTo(this.b) == 0
                    && other.c.compareTo(this.c) == 0
                    && other.discriminant.compareTo(this.discriminant) == 0;
        }
        return false;
    }

    public static ClassGroup fromABDiscriminant(BigInteger a, BigInteger b, BigInteger discriminant) {
        BigInteger fourA = a.multiply(createBigInteger(4));
        BigInteger c = (b.multiply(b).subtract(discriminant)).divide(fourA);

        return new ClassGroup(a, b, c, discriminant);
    }

    public static ClassGroup fromBytes(byte[] bytearray, BigInteger discriminant) {
        int len = (discriminant.bitLength() + 16) >> 4;
        BigInteger a = createBigInteger(bytearray, 0, len);
        BigInteger b = createBigInteger(bytearray, len, bytearray.length - len);
        return fromABDiscriminant(a, b, discriminant);
    }

    public ClassGroup identityForDiscriminant(BigInteger discriminant) {
        return fromABDiscriminant(createBigInteger(1), createBigInteger(1), discriminant);
    }

    public ClassGroup identity() {
        return identityForDiscriminant(this.discriminant);
    }

    private static int rawExport(byte[] v, int offset, BigInteger obj) {
        byte[] tmp = GMP.mpzExport(obj, 1);
        System.arraycopy(tmp, 0, v, offset, tmp.length);
        return tmp.length;
    }

    private static void exportObj(byte[] v, int offset, int len, BigInteger obj) {
        int size = obj.bitLength();
        int byteLenNeeded = (size + 8) >> 3;

        if (len < byteLenNeeded) {
            if (len == 0 && obj.signum() == 0) {
                return; // Ok
            } else {
                throw new RuntimeException("Buffer needs at least " + byteLenNeeded + " bytes");
            }
        }

        boolean isNegative = obj.signum() < 0;
        if (isNegative) {
            //let obj = !obj;
            byte[] tmp = obj.toByteArray();
            for (int i = 0; i < tmp.length; i++) {
                tmp[i] = (byte) ~tmp[i];
            }
            obj = new BigInteger(tmp);

            int newByteSize = (obj.bitLength() + 7) >> 3;
            int rawOffset = len - newByteSize;

            for (int i = 0; i < rawOffset; i++) {
                v[offset + i] = -1;
            }

            if (rawExport(v, offset + rawOffset, obj) != newByteSize) {
                throw new RuntimeException("rawExport error");
            }

            for (int i = rawOffset; i < len; i++) {
                v[i + offset] ^= -1;
            }
        } else {
            int byteLen = (size + 7) >> 3;
            int rawOffset = len - byteLen;
            for (int i = 0; i < rawOffset; i++) {
                v[i + offset] = 0;
            }

            if (rawExport(v, offset + rawOffset, obj) != byteLen) {
                throw new RuntimeException("rawExport error");
            }
        }
    }

    public void serialize(byte[] buf) {
        if ((buf.length & 1) == 1) {
            // odd lengths do not make sense
            throw new RuntimeException("Odd length is not allowed");
        }

        int len = buf.length >> 1;
        exportObj(buf, 0, len, this.a);
        exportObj(buf, len, len, this.b);
    }

    private void innerMultiply(ClassGroup rhs, Context ctx) {
        //ffi::mpz_add(&mut ctx.congruence_context.g, &self.b, &rhs.b);
        ctx.congruenceContext.g = this.b.add(rhs.b);

        //ffi::mpz_fdiv_q_ui_self(&mut ctx.congruence_context.g, 2);
        ctx.congruenceContext.g = GMP.fdivQUI(ctx.congruenceContext.g, 2);

        //ffi::mpz_sub(&mut ctx.h, &rhs.b, &self.b);            
        ctx.h = rhs.b.subtract(this.b);

        //ffi::mpz_fdiv_q_ui_self(&mut ctx.h, 2);
        ctx.h = GMP.fdivQUI(ctx.h, 2);

        //ffi::three_gcd(&mut ctx.w, &self.a, &rhs.a, &ctx.congruence_context.g);
        ctx.w = this.a.gcd(rhs.a).gcd(ctx.congruenceContext.g);

        //ctx.j.set(&ctx.w);
        ctx.j = ctx.w;

        //ffi::mpz_fdiv_q(&mut ctx.s, &self.a, &ctx.w);
        ctx.s = GMP.fdiv(this.a, ctx.w);

        //ffi::mpz_fdiv_q(&mut ctx.t, &rhs.a, &ctx.w);
        ctx.t = GMP.fdiv(rhs.a, ctx.w);

        //ffi::mpz_fdiv_q(&mut ctx.u, &ctx.congruence_context.g, &ctx.w);
        ctx.u = GMP.fdiv(ctx.congruenceContext.g, ctx.w);

        //ffi::mpz_mul(&mut ctx.a, &ctx.t, &ctx.u);
        ctx.a = ctx.t.multiply(ctx.u);

        //ffi::mpz_mul(&mut ctx.b, &ctx.h, &ctx.u);
        ctx.b = ctx.h.multiply(ctx.u);

        //ffi::mpz_mul(&mut ctx.m, &ctx.s, &self.c);
        ctx.m = ctx.s.multiply(this.c);

        //ctx.b += &ctx.m;            
        ctx.b = ctx.b.add(ctx.m);

        //ffi::mpz_mul(&mut ctx.m, &ctx.s, &ctx.t);
        ctx.m = ctx.s.multiply(ctx.t);
        
        /*
        ctx.congruence_context.solve_linear_congruence(
                &mut ctx.mu,
                Some(&mut ctx.v),
                &ctx.a,
                &ctx.b,
                &ctx.m,
            );*/
        BigInteger[] tmp = ctx.congruenceContext.solveLinearCongruence(ctx.a, ctx.b, ctx.m);
        ctx.mu = tmp[0];
        ctx.v = tmp[1];

        //ffi::mpz_mul(&mut ctx.a, &ctx.t, &ctx.v);
        ctx.a = ctx.t.multiply(ctx.v);

        //ffi::mpz_mul(&mut ctx.m, &ctx.t, &ctx.mu);
        ctx.m = ctx.t.multiply(ctx.mu);

        //ffi::mpz_sub(&mut ctx.b, &ctx.h, &ctx.m);
        ctx.b = ctx.h.subtract(ctx.m);

        //ctx.m.set(&ctx.s);
        ctx.m = ctx.s;
        
        /*
         ctx.congruence_context.solve_linear_congruence(
                &mut ctx.lambda,
                Some(&mut ctx.sigma),
                &ctx.a,
                &ctx.b,
                &ctx.m,
            );
         */
        tmp = ctx.congruenceContext.solveLinearCongruence(ctx.a, ctx.b, ctx.m);
        ctx.lambda = tmp[0];
        ctx.sigma = tmp[1];

        //ffi::mpz_mul(&mut ctx.a, &ctx.v, &ctx.lambda);
        ctx.a = ctx.v.multiply(ctx.lambda);

        //ffi::mpz_add(&mut ctx.k, &ctx.mu, &ctx.a);
        ctx.k = ctx.mu.add(ctx.a);

        //ffi::mpz_mul(&mut ctx.l, &ctx.k, &ctx.t);
        ctx.l = ctx.k.multiply(ctx.t);

        //ffi::mpz_sub(&mut ctx.v, &ctx.l, &ctx.h);
        ctx.v = ctx.l.subtract(ctx.h);

        //ffi::mpz_fdiv_q(&mut ctx.l, &ctx.v, &ctx.s);
        ctx.l = GMP.fdiv(ctx.v, ctx.s);


        //ffi::mpz_mul(&mut ctx.m, &ctx.t, &ctx.u);
        ctx.m = ctx.t.multiply(ctx.u);

        //ctx.m *= &ctx.k;
        ctx.m = ctx.m.multiply(ctx.k);

        //ffi::mpz_mul(&mut ctx.a, &ctx.h, &ctx.u);
        ctx.a = ctx.h.multiply(ctx.u);

        //ctx.m -= &ctx.a;
        ctx.m = ctx.m.subtract(ctx.a);

        //ffi::mpz_mul(&mut ctx.a, &self.c, &ctx.s);
        ctx.a = this.c.multiply(ctx.s);

        //ctx.m -= &ctx.a;
        ctx.m = ctx.m.subtract(ctx.a);

        //ffi::mpz_mul(&mut ctx.a, &ctx.s, &ctx.t);
        ctx.a = ctx.s.multiply(ctx.t);

        //ffi::mpz_fdiv_q(&mut ctx.lambda, &ctx.m, &ctx.a);
        ctx.lambda = GMP.fdiv(ctx.m, ctx.a);

        //ffi::mpz_mul(&mut self.a, &ctx.s, &ctx.t);
        this.a = ctx.s.multiply(ctx.t);

        //ffi::mpz_mul(&mut self.b, &ctx.j, &ctx.u);
        this.b = ctx.j.multiply(ctx.u);

        //ffi::mpz_mul(&mut ctx.a, &ctx.k, &ctx.t);
        ctx.a = ctx.k.multiply(ctx.t);

        //self.b -= &ctx.a;
        this.b = this.b.subtract(ctx.a);

        //ffi::mpz_mul(&mut ctx.a, &ctx.l, &ctx.s);
        ctx.a = ctx.l.multiply(ctx.s);

        //self.b -= &ctx.a;
        this.b = this.b.subtract(ctx.a);

        //ffi::mpz_mul(&mut self.c, &ctx.k, &ctx.l);
        this.c = ctx.k.multiply(ctx.l);

        //ffi::mpz_mul(&mut ctx.a, &ctx.j, &ctx.lambda);
        ctx.a = ctx.j.multiply(ctx.lambda);

        //self.c -= &ctx.a;
        this.c = this.c.subtract(ctx.a);

        innerReduce(ctx);
    }

    private void innerNormalize(Context ctx) {
        //ctx.negative_a = -&self.a;
        ctx.negativeA = this.a.negate();

        //if self.b > ctx.negative_a && self.b <= self.a
        if (this.b.compareTo(ctx.negativeA) > 0 && this.b.compareTo(this.a) <= 0) {
            return;
        }

        //System.out.println(String.format(" Start: a=%s, b=%s", this.a.toString(), this.b.toString()));

        //ffi::mpz_sub(&mut ctx.r, &self.a, &self.b);            
        ctx.r = this.a.subtract(this.b);

        //ffi::mpz_mul_2exp(&mut ctx.denom, &self.a, 1);
        ctx.denom = this.a.multiply(createBigInteger(2));

        //ffi::mpz_fdiv_q(&mut ctx.negative_a, &ctx.r, &ctx.denom);
        ctx.negativeA = GMP.fdiv(ctx.r, ctx.denom);

        //swap(&mut ctx.negative_a, &mut ctx.r);
        BigInteger tmp = ctx.r;
        ctx.r = ctx.negativeA;
        ctx.negativeA = tmp;

        //swap(&mut ctx.old_b, &mut self.b);
        tmp = ctx.oldB;
        ctx.oldB = this.b;
        this.b = tmp;

        //ffi::mpz_mul(&mut ctx.ra, &ctx.r, &self.a);
        ctx.ra = ctx.r.multiply(this.a);

        //ffi::mpz_mul_2exp(&mut ctx.negative_a, &ctx.ra, 1);
        ctx.negativeA = ctx.ra.multiply(createBigInteger(2));

        //ffi::mpz_add(&mut self.b, &ctx.old_b, &ctx.negative_a);
        this.b = ctx.oldB.add(ctx.negativeA);

        //ffi::mpz_mul(&mut ctx.negative_a, &ctx.ra, &ctx.r);
        ctx.negativeA = ctx.ra.multiply(ctx.r);

        //ffi::mpz_add(&mut ctx.old_a, &self.c, &ctx.negative_a);
        ctx.oldA = this.c.add(ctx.negativeA);

        //ffi::mpz_mul(&mut ctx.ra, &ctx.r, &ctx.old_b);
        ctx.ra = ctx.r.multiply(ctx.oldB);

        //ffi::mpz_add(&mut self.c, &ctx.old_a, &ctx.ra);
        this.c = ctx.oldA.add(ctx.ra);

        //System.out.println(String.format(" End: a=%s, b=%s", this.a.toString(), this.b.toString()));
    }

    private void innerReduce(Context ctx) {
        innerNormalize(ctx);

        while (true) {
            boolean cont1 = this.b.signum() < 0 && this.a.compareTo(this.c) >= 0;
            boolean cont2 = this.b.signum() >= 0 && this.a.compareTo(this.c) > 0;
            boolean cont = cont1 || cont2;
            if (!cont) break;

            //ffi::mpz_add(&mut ctx.s, &self.c, &self.b);
            ctx.s = this.c.add(this.b);

            //ffi::mpz_add(&mut ctx.x, &self.c, &self.c);
            ctx.x = this.c.add(this.c);

            //swap(&mut self.b, &mut ctx.old_b);
            BigInteger tmp = this.b;
            this.b = ctx.oldB;
            ctx.oldB = tmp;

            //ffi::mpz_fdiv_q(&mut self.b, &ctx.s, &ctx.x);
            this.b = GMP.fdiv(ctx.s, ctx.x);

            //swap(&mut self.b, &mut ctx.s);
            tmp = this.b;
            this.b = ctx.s;
            ctx.s = tmp;

            //swap(&mut self.a, &mut self.c);
            tmp = this.a;
            this.a = this.c;
            this.c = tmp;

            //ffi::mpz_mul(&mut self.b, &ctx.s, &self.a);
            this.b = ctx.s.multiply(this.a);

            //ffi::mpz_mul_2exp(&mut ctx.x, &self.b, 1);
            ctx.x = this.b.multiply(createBigInteger(2));

            //ffi::mpz_sub(&mut self.b, &ctx.x, &ctx.old_b);
            this.b = ctx.x.subtract(ctx.oldB);

            //ffi::mpz_mul(&mut ctx.x, &ctx.old_b, &ctx.s);
            ctx.x = ctx.oldB.multiply(ctx.s);

            //ffi::mpz_mul(&mut ctx.old_b, &ctx.s, &ctx.s);
            ctx.oldB = ctx.s.multiply(ctx.s);

            //ffi::mpz_mul(&mut ctx.s, &self.a, &ctx.old_b);
            ctx.s = this.a.multiply(ctx.oldB);

            //ffi::mpz_sub(&mut ctx.old_a, &ctx.s, &ctx.x);
            ctx.oldA = ctx.s.subtract(ctx.x);

            // c += a
            this.c = this.c.add(ctx.oldA);
        }
        innerNormalize(ctx);
    }

    private void innerSquareImpl(Context ctx) {
        /*ctx.congruence_context.solve_linear_congruence(
                &mut ctx.mu,
                None,
                &self.b,
                &self.c,
                &self.a,
            );*/

        BigInteger[] tmp = ctx.congruenceContext.solveLinearCongruence(this.b, this.c, this.a);
        ctx.mu = tmp[0];

        //System.out.println(String.format(" mu=%s, a=%s, b=%s", ctx.mu.toString(), this.a.toString(), this.b.toString()));
        //ffi::mpz_mul(&mut ctx.m, &self.b, &ctx.mu);
        ctx.m = this.b.multiply(ctx.mu);

        //ctx.m -= &self.c;
        ctx.m = ctx.m.subtract(this.c);

        //ctx.m = ctx.m.div_floor(&self.a);
        ctx.m = ctx.m.divide(this.a);

        // New a
        //ctx.old_a.set(&self.a);
        ctx.oldA = this.a;

        //ffi::mpz_mul(&mut self.a, &ctx.old_a, &ctx.old_a);
        this.a = ctx.oldA.multiply(ctx.oldA);

        // New b
        //ffi::mpz_mul(&mut ctx.a, &ctx.mu, &ctx.old_a);
        ctx.a = ctx.mu.multiply(ctx.oldA);

        //ffi::mpz_double(&mut ctx.a);
        ctx.a = ctx.a.shiftLeft(1);

        //self.b -= &ctx.a;
        this.b = this.b.subtract(ctx.a);

        // New c
        //ffi::mpz_mul(&mut self.c, &ctx.mu, &ctx.mu);
        this.c = ctx.mu.multiply(ctx.mu);

        //self.c -= &ctx.m;
        this.c = this.c.subtract(ctx.m);

        innerReduce(ctx);
    }

    private void innerSquare(Context ctx) {
        innerSquareImpl(ctx);
    }

    private void repeatedSquare(long iterations) {
        Context ctx = new Context();
        for (long i = 0; i < iterations; i++) {
            innerSquare(ctx);
        }
    }

    public static Map<Long, ClassGroup> iterateSquarings(ClassGroup x, List<Long> powersToCalculate) {
        Map<Long, ClassGroup> powersCalculated = new HashMap<>();
        Collections.sort(powersToCalculate);
        long previousPower = 0;
        for (long currentPower : powersToCalculate) {
            x.repeatedSquare(currentPower - previousPower);
            powersCalculated.put(currentPower, x.clone());
            previousPower = currentPower;
        }
        return powersCalculated;
    }

    public void multiplyWith(ClassGroup rhs) {
        Context ctx = new Context();
        if (rhs.discriminant.compareTo(this.discriminant) != 0) {
            throw new RuntimeException("Discriminants need to be equal");
        }
        this.innerMultiply(rhs, ctx);
    }

    public void square() {
        this.multiplyWith(this.clone());
    }

    private void assignFrom(ClassGroup rhs) {
        this.a = rhs.a;
        this.b = rhs.b;
        this.c = rhs.c;
        this.discriminant = rhs.discriminant;
    }

    public void pow(BigInteger exponent) {
        if (exponent.signum() < 0) {
            throw new RuntimeException("Negative exponent !");
        }

        ClassGroup state = this.identity();

        while (true) {
            boolean isOdd = exponent.testBit(0);
            exponent = exponent.shiftRight(1);

            if (isOdd) {
                state.multiplyWith(this);
            }
            if (exponent.signum() == 0) {
                this.assignFrom(state);
                break;
            }
            this.square();
        }
    }
}


