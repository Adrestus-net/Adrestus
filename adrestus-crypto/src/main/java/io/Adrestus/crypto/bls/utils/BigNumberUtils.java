package io.Adrestus.crypto.bls.utils;

import io.Adrestus.crypto.bls.constants.Constants;
import org.apache.milagro.amcl.BLS381.*;

public class BigNumberUtils {

    private static int lbits(BIG n3, BIG n) {
        n.copy(new BIG(Constants.CURVE_BNX));
        n.norm();
        n3.copy(new BIG(n));
        n3.pmul(3);
        n3.norm();
        return n3.nbits();
    }

    private static FP12 linedbl(ECP2 A, FP qx, FP qy) {
        FP2 xx = new FP2(A.getx());
        FP2 yy = new FP2(A.gety());
        FP2 zz = new FP2(A.getz());
        FP2 yz = new FP2(yy);

        yz.mul(zz);
        xx.sqr();
        yy.sqr();
        zz.sqr();

        yz.imul(4);
        yz.neg();
        yz.norm();
        yz.pmul(qy);

        xx.imul(6);
        xx.pmul(qx);

        int sb = 3 * Constants.CURVE_B_I;
        zz.imul(sb);

        //mtype
        zz.mul_ip();
        zz.add(new FP2(zz)); //zz.dbl();
        yz.mul_ip();
        yz.norm();

        zz.norm();

        yy.add(new FP2(yy)); // yy.dbl()
        zz.sub(yy);
        zz.norm();

        FP4 a = new FP4(yz, zz);
        FP4 b = new FP4(0);
        //mtype
        FP4 c = new FP4(xx);
        c.times_i();

        A.add(new ECP2(A)); // A.dbl()
        return new FP12(a, b, c);
    }

    private static FP12 lineadd(ECP2 A, ECP2 B, FP qx, FP qy) {
        FP2 x1 = new FP2(A.getx());
        FP2 y1 = new FP2(A.gety());
        FP2 t1 = new FP2(A.getz());
        FP2 t2 = new FP2(A.getz());

        t1.mul(B.gety());
        t2.mul(B.getx());

        x1.sub(t2);
        x1.norm(); // X1=X1-Z1.X2
        y1.sub(t1);
        y1.norm(); // Y1=Y1-Z1.Y2

        t1.copy(x1); // T1=X1-Z1.X2
        x1.pmul(qy); // X1=(X1-Z1.X2).Ys

        //mtype
        x1.mul_ip();
        x1.norm();

        t1.mul(B.gety()); // T1=(X1-Z1.X2).Y2

        t2.copy(y1); // T2=Y1-Z1.Y2
        t2.mul(B.getx()); // T2=(Y1-Z1.Y2).X2
        t2.sub(t1);
        t2.norm(); // T2=(Y1-Z1.Y2).X2 - (X1-Z1.X2).Y2
        y1.pmul(qx);
        y1.neg();
        y1.norm(); // Y1=-(Y1-Z1.Y2).Xs

        //mtype
        FP4 a = new FP4(0);
        FP4 b = new FP4(0);
        FP4 c = new FP4(y1);
        c.times_i();

        A.add(B);
        return new FP12(a, b, c);
    }

    public static void another(FP12[] r, ECP2 P1, ECP Q1) {
        BIG n = new BIG(), n3 = new BIG();

        if (Q1.is_infinity()) {
            return;
        }

        ECP2 P = new ECP2(P1);
        P.affine();

        ECP Q = new ECP(Q1);
        Q.affine();

        FP qx = new FP(Q.getx());
        FP qy = new FP(Q.gety());

        ECP2 A = new ECP2(P);
        ECP2 NP = new ECP2(P);
        NP.neg();

        int nb = lbits(n3, n);
        for (int i = nb - 2; i >= 1; i--) {
            FP12 lv = linedbl(A, qx, qy);
            int bt = n3.bit(i) - n.bit(i);
            if (bt == 1) {
                FP12 lv2 = lineadd(A, P, qx, qy);
                lv.smul(lv2, ECP.M_TYPE);
            }
            if (bt == -1) {
                FP12 lv2 = lineadd(A, NP, qx, qy);
                lv.smul(lv2, ECP.M_TYPE);
            }
            r[i].smul(lv, ECP.M_TYPE);
        }
    }


    public static FP12 miller(FP12[] r) {
        FP12 res = new FP12(1);
        for (int i = Constants.ATE_BITS - 1; i >= 1; i--) {
            res.sqr();
            res.smul(r[i], ECP.M_TYPE);
        }
        //ecp::SIGN_OF_X == SignOfX::NEGATIVEX
        res.conj();
        res.smul(r[0], ECP.M_TYPE);
        return res;
    }

    public static void rmod(BIG self, BIG n) {
        BIG m = new BIG(n);
        BIG r = new BIG();
        self.norm();

        if (BIG.comp(self, m) < 0) {
            return;
        }

        int k = 0;
        while (true) {
            m.fshl(1);
            k += 1;
            if (BIG.comp(self, m) < 0) {
                break;
            }
        }
        while (k > 0) {
            m.fshr(1);
            r.copy(self);
            r.sub(m);
            r.norm();
            long d = 1 - (r.get(Constants.NLEN - 1) >> (Constants.CHUNK - 1) & 1);
            self.cmove(r, (int) d);
            k -= 1;
        }
    }

    private static int BarrettRedcK() {
        return Constants.curveOrder.nbits();
    }

    private static BIG BarrettRedcU() {
        int k = Constants.curveOrder.nbits();
        DBIG u = new DBIG(1);
        u.shl(k);
        u.shl(k);
        return u.div(Constants.curveOrder);
    }

    private static BIG BarrettRedcV() {
        int k = BarrettRedcK();
        BIG v = new BIG(1);
        v.shl(k + 1);
        return v;
    }

    static class _DBIG extends DBIG {

        public _DBIG(BIG x) {
            super(x);
        }

        public _DBIG(DBIG x) {
            super(x);
        }

        public void mod2m(int m) {
            int wd = m / Constants.BASEBITS;
            int bt = m % Constants.BASEBITS;
            long msk = (1l << bt) - 1;
            this.w[wd] &= msk;
            for (int i = wd + 1; i < Constants.DNLEN; i++) {
                this.w[i] = 0;
            }
        }

        public BIG toBIG() {
            return new BIG(this.w);
        }
    }

    private static BIG barrettReduction(DBIG x, BIG modulus, int k, BIG u, BIG v) {
        DBIG dq1 = new DBIG(x);
        dq1.shr(k - 1);
        BIG q1 = new BIG(dq1);

        DBIG dq2 = BIG.mul(q1, u);
        DBIG dq3 = new DBIG(dq2);
        dq3.shr(k + 1);
        BIG q3 = new BIG(dq3);

        _DBIG dr1 = new _DBIG(x);
        dr1.mod2m(k + 1);
        BIG r1 = new BIG(dr1);

        _DBIG dr2 = new _DBIG(BIG.mul(q3, modulus));
        dr2.mod2m(k + 1);
        BIG r2 = new BIG(dr2);

        int diff = BIG.comp(r1, r2);
        BIG r = r1.minus(r2);

        if (diff < 0) {
            BIG m = r2.minus(r1);
            r = v.minus(m);
        }

        r.norm();

        while (BIG.comp(r, modulus) >= 0) {
            r.sub(modulus);
            r.norm();
        }
        return r;
    }

    public static BIG reduceDmodCurveOrder(DBIG x) {
        int k = BarrettRedcK();
        BIG u = BarrettRedcU();
        BIG v = BarrettRedcV();

        return barrettReduction(x, Constants.curveOrder, k, u, v);
    }
}
