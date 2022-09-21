package io.Adrestus.crypto.bls.BLS381;

public final class FP12 {
    private final FP4 a;
    private final FP4 b;
    private final FP4 c;

    /* reduce all components of this mod Modulus */
    public void reduce() {
        a.reduce();
        b.reduce();
        c.reduce();
    }

    /* normalise all components of this */
    public void norm() {
        a.norm();
        b.norm();
        c.norm();
    }

    /* test x==0 ? */
    public boolean iszilch() {
        //reduce();
        return (a.iszilch() && b.iszilch() && c.iszilch());
    }

    public void cmove(FP12 g, int d) {
        a.cmove(g.a, d);
        b.cmove(g.b, d);
        c.cmove(g.c, d);
    }


    /* return 1 if b==c, no branching */
    public static int teq(int b, int c) {
        int x = b ^ c;
        x -= 1;  // if x=0, x now -1
        return ((x >> 31) & 1);
    }

    /* Constant time select from pre-computed table */
    public void select(FP12 g[], int b) {
        int m = b >> 31;
        int babs = (b ^ m) - m;

        babs = (babs - 1) / 2;

        cmove(g[0], teq(babs, 0));  // conditional move
        cmove(g[1], teq(babs, 1));
        cmove(g[2], teq(babs, 2));
        cmove(g[3], teq(babs, 3));
        cmove(g[4], teq(babs, 4));
        cmove(g[5], teq(babs, 5));
        cmove(g[6], teq(babs, 6));
        cmove(g[7], teq(babs, 7));

        FP12 invf = new FP12(this);
        invf.conj();
        cmove(invf, (int) (m & 1));
    }


    /* test x==1 ? */
    public boolean isunity() {
        FP4 one = new FP4(1);
        return (a.equals(one) && b.iszilch() && c.iszilch());
    }

    /* return 1 if x==y, else 0 */
    public boolean equals(FP12 x) {
        return (a.equals(x.a) && b.equals(x.b) && c.equals(x.c));
    }

    /* extract a from this */
    public FP4 geta() {
        return a;
    }

    /* extract b */
    public FP4 getb() {
        return b;
    }

    /* extract c */
    public FP4 getc() {
        return c;
    }

    /* copy this=x */
    public void copy(FP12 x) {
        a.copy(x.a);
        b.copy(x.b);
        c.copy(x.c);
    }

    /* set this=1 */
    public void one() {
        a.one();
        b.zero();
        c.zero();
    }

    /* this=conj(this) */
    public void conj() {
        a.conj();
        b.nconj();
        c.conj();
    }

    /* Constructors */
    public FP12(FP4 d) {
        a = new FP4(d);
        b = new FP4(0);
        c = new FP4(0);
    }

    public FP12(int d) {
        a = new FP4(d);
        b = new FP4(0);
        c = new FP4(0);
    }

    public FP12(FP4 d, FP4 e, FP4 f) {
        a = new FP4(d);
        b = new FP4(e);
        c = new FP4(f);
    }

    public FP12(FP12 x) {
        a = new FP4(x.a);
        b = new FP4(x.b);
        c = new FP4(x.c);
    }

    /* Granger-Scott Unitary Squaring */
    public void usqr() {
//System.out.println("Into usqr");
        FP4 A = new FP4(a);
        FP4 B = new FP4(c);
        FP4 C = new FP4(b);
        FP4 D = new FP4(0);

        a.sqr();
        D.copy(a);
        D.add(a);
        a.add(D);

        a.norm();
        A.nconj();

        A.add(A);
        a.add(A);
        B.sqr();
        B.times_i();

        D.copy(B);
        D.add(B);
        B.add(D);
        B.norm();

        C.sqr();
        D.copy(C);
        D.add(C);
        C.add(D);
        C.norm();

        b.conj();
        b.add(b);
        c.nconj();

        c.add(c);
        b.add(B);
        c.add(C);
//System.out.println("Out of usqr 1");
        reduce();
//System.out.println("Out of usqr 2");
    }

    /* Chung-Hasan SQR2 method from http://cacr.uwaterloo.ca/techreports/2006/cacr2006-24.pdf */
    public void sqr() {
//System.out.println("Into sqr");
        FP4 A = new FP4(a);
        FP4 B = new FP4(b);
        FP4 C = new FP4(c);
        FP4 D = new FP4(a);

        A.sqr();
        B.mul(c);
        B.add(B);
        B.norm();
        C.sqr();
        D.mul(b);
        D.add(D);

        c.add(a);
        c.add(b);
        c.norm();
        c.sqr();

        a.copy(A);

        A.add(B);
        A.norm();
        A.add(C);
        A.add(D);
        A.norm();

        A.neg();
        B.times_i();
        C.times_i();

        a.add(B);

        b.copy(C);
        b.add(D);
        c.add(A);
//System.out.println("Out of sqr");
        norm();
    }

    /* FP12 full multiplication this=this*y */
    public void mul(FP12 y) {
//System.out.println("Into mul");
        FP4 z0 = new FP4(a);
        FP4 z1 = new FP4(0);
        FP4 z2 = new FP4(b);
        FP4 z3 = new FP4(0);
        FP4 t0 = new FP4(a);
        FP4 t1 = new FP4(y.a);

        z0.mul(y.a);
        z2.mul(y.b);

        t0.add(b);
        t1.add(y.b);

        t0.norm();
        t1.norm();

        z1.copy(t0);
        z1.mul(t1);
        t0.copy(b);
        t0.add(c);

        t1.copy(y.b);
        t1.add(y.c);

        t0.norm();
        t1.norm();

        z3.copy(t0);
        z3.mul(t1);

        t0.copy(z0);
        t0.neg();
        t1.copy(z2);
        t1.neg();

        z1.add(t0);
        //z1.norm();
        b.copy(z1);
        b.add(t1);

        z3.add(t1);
        z2.add(t0);

        t0.copy(a);
        t0.add(c);
        t1.copy(y.a);
        t1.add(y.c);

        t0.norm();
        t1.norm();

        t0.mul(t1);
        z2.add(t0);

        t0.copy(c);
        t0.mul(y.c);
        t1.copy(t0);
        t1.neg();

//		z2.norm();
//		z3.norm();
//		b.norm();

        c.copy(z2);
        c.add(t1);
        z3.add(t1);
        t0.times_i();
        b.add(t0);
        z3.norm();
        z3.times_i();
        a.copy(z0);
        a.add(z3);
        norm();
//System.out.println("Out of mul");
    }

    /* Special case of multiplication arises from special form of ATE pairing line function */
    public void smul(FP12 y, int type) {
//System.out.println("Into smul");

        if (type == ECP.D_TYPE) {
            FP4 z0 = new FP4(a);
            FP4 z2 = new FP4(b);
            FP4 z3 = new FP4(b);
            FP4 t0 = new FP4(0);
            FP4 t1 = new FP4(y.a);
            z0.mul(y.a);
            z2.pmul(y.b.real());
            b.add(a);
            t1.real().add(y.b.real());

            t1.norm();
            b.norm();
            b.mul(t1);
            z3.add(c);
            z3.norm();
            z3.pmul(y.b.real());

            t0.copy(z0);
            t0.neg();
            t1.copy(z2);
            t1.neg();

            b.add(t0);

            b.add(t1);
            z3.add(t1);
            z2.add(t0);

            t0.copy(a);
            t0.add(c);
            t0.norm();
            z3.norm();
            t0.mul(y.a);
            c.copy(z2);
            c.add(t0);

            z3.times_i();
            a.copy(z0);
            a.add(z3);
        }
        if (type == ECP.M_TYPE) {
            FP4 z0 = new FP4(a);
            FP4 z1 = new FP4(0);
            FP4 z2 = new FP4(0);
            FP4 z3 = new FP4(0);
            FP4 t0 = new FP4(a);
            FP4 t1 = new FP4(0);

            z0.mul(y.a);
            t0.add(b);
            t0.norm();

            z1.copy(t0);
            z1.mul(y.a);
            t0.copy(b);
            t0.add(c);
            t0.norm();

            z3.copy(t0); //z3.mul(y.c);
            z3.pmul(y.c.getb());
            z3.times_i();

            t0.copy(z0);
            t0.neg();

            z1.add(t0);
            b.copy(z1);
            z2.copy(t0);

            t0.copy(a);
            t0.add(c);
            t1.copy(y.a);
            t1.add(y.c);

            t0.norm();
            t1.norm();

            t0.mul(t1);
            z2.add(t0);

            t0.copy(c);

            t0.pmul(y.c.getb());
            t0.times_i();

            t1.copy(t0);
            t1.neg();

            c.copy(z2);
            c.add(t1);
            z3.add(t1);
            t0.times_i();
            b.add(t0);
            z3.norm();
            z3.times_i();
            a.copy(z0);
            a.add(z3);
        }
        norm();
//System.out.println("Out of smul");
    }

    /* this=1/this */
    public void inverse() {
        FP4 f0 = new FP4(a);
        FP4 f1 = new FP4(b);
        FP4 f2 = new FP4(a);
        FP4 f3 = new FP4(0);

        norm();
        f0.sqr();
        f1.mul(c);
        f1.times_i();
        f0.sub(f1);
        f0.norm();

        f1.copy(c);
        f1.sqr();
        f1.times_i();
        f2.mul(b);
        f1.sub(f2);
        f1.norm();

        f2.copy(b);
        f2.sqr();
        f3.copy(a);
        f3.mul(c);
        f2.sub(f3);
        f2.norm();

        f3.copy(b);
        f3.mul(f2);
        f3.times_i();
        a.mul(f0);
        f3.add(a);
        c.mul(f1);
        c.times_i();

        f3.add(c);
        f3.norm();
        f3.inverse();
        a.copy(f0);
        a.mul(f3);
        b.copy(f1);
        b.mul(f3);
        c.copy(f2);
        c.mul(f3);
    }

    /* this=this^p using Frobenius */
    public void frob(FP2 f) {
        FP2 f2 = new FP2(f);
        FP2 f3 = new FP2(f);

        f2.sqr();
        f3.mul(f2);

        a.frob(f3);
        b.frob(f3);
        c.frob(f3);

        b.pmul(f);
        c.pmul(f2);
    }

    /* trace function */
    public FP4 trace() {
        FP4 t = new FP4(0);
        t.copy(a);
        t.imul(3);
        t.reduce();
        return t;
    }

    /* convert from byte array to FP12 */
    public static FP12 fromBytes(byte[] w) {
        BIG a, b;
        FP2 c, d;
        FP4 e, f, g;
        byte[] t = new byte[BIG.MODBYTES];

        for (int i = 0; i < BIG.MODBYTES; i++) t[i] = w[i];
        a = BIG.fromBytes(t);
        for (int i = 0; i < BIG.MODBYTES; i++) t[i] = w[i + BIG.MODBYTES];
        b = BIG.fromBytes(t);
        c = new FP2(a, b);

        for (int i = 0; i < BIG.MODBYTES; i++) t[i] = w[i + 2 * BIG.MODBYTES];
        a = BIG.fromBytes(t);
        for (int i = 0; i < BIG.MODBYTES; i++) t[i] = w[i + 3 * BIG.MODBYTES];
        b = BIG.fromBytes(t);
        d = new FP2(a, b);

        e = new FP4(c, d);


        for (int i = 0; i < BIG.MODBYTES; i++) t[i] = w[i + 4 * BIG.MODBYTES];
        a = BIG.fromBytes(t);
        for (int i = 0; i < BIG.MODBYTES; i++) t[i] = w[i + 5 * BIG.MODBYTES];
        b = BIG.fromBytes(t);
        c = new FP2(a, b);

        for (int i = 0; i < BIG.MODBYTES; i++) t[i] = w[i + 6 * BIG.MODBYTES];
        a = BIG.fromBytes(t);
        for (int i = 0; i < BIG.MODBYTES; i++) t[i] = w[i + 7 * BIG.MODBYTES];
        b = BIG.fromBytes(t);
        d = new FP2(a, b);

        f = new FP4(c, d);


        for (int i = 0; i < BIG.MODBYTES; i++) t[i] = w[i + 8 * BIG.MODBYTES];
        a = BIG.fromBytes(t);
        for (int i = 0; i < BIG.MODBYTES; i++) t[i] = w[i + 9 * BIG.MODBYTES];
        b = BIG.fromBytes(t);
        c = new FP2(a, b);

        for (int i = 0; i < BIG.MODBYTES; i++) t[i] = w[i + 10 * BIG.MODBYTES];
        a = BIG.fromBytes(t);
        for (int i = 0; i < BIG.MODBYTES; i++) t[i] = w[i + 11 * BIG.MODBYTES];
        b = BIG.fromBytes(t);
        d = new FP2(a, b);

        g = new FP4(c, d);

        return new FP12(e, f, g);
    }

    /* convert this to byte array */
    public void toBytes(byte[] w) {
        byte[] t = new byte[BIG.MODBYTES];
        a.geta().getA().toBytes(t);
        for (int i = 0; i < BIG.MODBYTES; i++) w[i] = t[i];
        a.geta().getB().toBytes(t);
        for (int i = 0; i < BIG.MODBYTES; i++) w[i + BIG.MODBYTES] = t[i];
        a.getb().getA().toBytes(t);
        for (int i = 0; i < BIG.MODBYTES; i++) w[i + 2 * BIG.MODBYTES] = t[i];
        a.getb().getB().toBytes(t);
        for (int i = 0; i < BIG.MODBYTES; i++) w[i + 3 * BIG.MODBYTES] = t[i];

        b.geta().getA().toBytes(t);
        for (int i = 0; i < BIG.MODBYTES; i++) w[i + 4 * BIG.MODBYTES] = t[i];
        b.geta().getB().toBytes(t);
        for (int i = 0; i < BIG.MODBYTES; i++) w[i + 5 * BIG.MODBYTES] = t[i];
        b.getb().getA().toBytes(t);
        for (int i = 0; i < BIG.MODBYTES; i++) w[i + 6 * BIG.MODBYTES] = t[i];
        b.getb().getB().toBytes(t);
        for (int i = 0; i < BIG.MODBYTES; i++) w[i + 7 * BIG.MODBYTES] = t[i];

        c.geta().getA().toBytes(t);
        for (int i = 0; i < BIG.MODBYTES; i++) w[i + 8 * BIG.MODBYTES] = t[i];
        c.geta().getB().toBytes(t);
        for (int i = 0; i < BIG.MODBYTES; i++) w[i + 9 * BIG.MODBYTES] = t[i];
        c.getb().getA().toBytes(t);
        for (int i = 0; i < BIG.MODBYTES; i++) w[i + 10 * BIG.MODBYTES] = t[i];
        c.getb().getB().toBytes(t);
        for (int i = 0; i < BIG.MODBYTES; i++) w[i + 11 * BIG.MODBYTES] = t[i];
    }

    /* convert to hex string */
    public String toString() {
        return ("[" + a.toString() + "," + b.toString() + "," + c.toString() + "]");
    }

    /* this=this^e */
    /* Note this is simple square and multiply, so not side-channel safe */
    public FP12 pow(BIG e) {
        norm();
        e.norm();
        BIG e3 = new BIG(e);
        e3.pmul(3);
        e3.norm();

        FP12 w = new FP12(this);

        int nb = e3.nbits();
        for (int i = nb - 2; i >= 1; i--) {
            w.usqr();
            int bt = e3.bit(i) - e.bit(i);
            if (bt == 1)
                w.mul(this);
            if (bt == -1) {
                conj();
                w.mul(this);
                conj();
            }
        }
        w.reduce();
        return w;


/*
		BIG z=new BIG(e);
		FP12 r=new FP12(1);
		while (true)
		{
			int bt=z.parity();
			z.fshr(1);
			if (bt==1) r.mul(w);
			if (z.iszilch()) break;
			w.usqr();
		}
		r.reduce();
		return r; */
    }

    /* constant time powering by small integer of max length bts */
    public void pinpow(int e, int bts) {
        int i, b;
        FP12[] R = new FP12[2];
        R[0] = new FP12(1);
        R[1] = new FP12(this);
        for (i = bts - 1; i >= 0; i--) {
            b = (e >> i) & 1;
            R[1 - b].mul(R[b]);
            R[b].usqr();
        }
        this.copy(R[0]);
    }

    public FP4 compow(BIG e, BIG r) {
        FP12 g1 = new FP12(0);
        FP12 g2 = new FP12(0);
        FP2 f = new FP2(new BIG(ROM.Fra), new BIG(ROM.Frb));
        BIG q = new BIG(ROM.Modulus);

        BIG m = new BIG(q);
        m.mod(r);

        BIG a = new BIG(e);
        a.mod(m);

        BIG b = new BIG(e);
        b.div(m);

        g1.copy(this);
        g2.copy(this);

        FP4 c = g1.trace();

        if (b.iszilch()) {
            c = c.xtr_pow(e);
            return c;
        }

        g2.frob(f);
        FP4 cp = g2.trace();
        g1.conj();
        g2.mul(g1);
        FP4 cpm1 = g2.trace();
        g2.mul(g1);
        FP4 cpm2 = g2.trace();

        c = c.xtr_pow2(cp, cpm1, cpm2, a, b);

        return c;
    }

    /* p=q0^u0.q1^u1.q2^u2.q3^u3 */
// Bos & Costello https://eprint.iacr.org/2013/458.pdf
// Faz-Hernandez & Longa & Sanchez  https://eprint.iacr.org/2013/158.pdf
// Side channel attack secure

    public static FP12 pow4(FP12[] q, BIG[] u) {
        int i, j, nb, pb;
        FP12[] g = new FP12[8];
        FP12 r = new FP12(1);
        FP12 p = new FP12(0);
        BIG[] t = new BIG[4];
        BIG mt = new BIG(0);
        byte[] w = new byte[BIG.NLEN * BIG.BASEBITS + 1];
        byte[] s = new byte[BIG.NLEN * BIG.BASEBITS + 1];

        for (i = 0; i < 4; i++) {
            t[i] = new BIG(u[i]);
            t[i].norm();
        }
        g[0] = new FP12(q[0]);  // q[0]
        g[1] = new FP12(g[0]);
        g[1].mul(q[1]); // q[0].q[1]
        g[2] = new FP12(g[0]);
        g[2].mul(q[2]); // q[0].q[2]
        g[3] = new FP12(g[1]);
        g[3].mul(q[2]); // q[0].q[1].q[2]
        g[4] = new FP12(q[0]);
        g[4].mul(q[3]); // q[0].q[3]
        g[5] = new FP12(g[1]);
        g[5].mul(q[3]); // q[0].q[1].q[3]
        g[6] = new FP12(g[2]);
        g[6].mul(q[3]); // q[0].q[2].q[3]
        g[7] = new FP12(g[3]);
        g[7].mul(q[3]); // q[0].q[1].q[2].q[3]

        // Make it odd
        pb = 1 - t[0].parity();
        t[0].inc(pb);
        t[0].norm();

        // Number of bits
        mt.zero();
        for (i = 0; i < 4; i++) {
            mt.or(t[i]);
        }
        nb = 1 + mt.nbits();

        // Sign pivot
        s[nb - 1] = 1;
        for (i = 0; i < nb - 1; i++) {
            t[0].fshr(1);
            s[i] = (byte) (2 * t[0].parity() - 1);
        }

        // Recoded exponent
        for (i = 0; i < nb; i++) {
            w[i] = 0;
            int k = 1;
            for (j = 1; j < 4; j++) {
                byte bt = (byte) (s[i] * t[j].parity());
                t[j].fshr(1);
                t[j].dec((int) (bt) >> 1);
                t[j].norm();
                w[i] += bt * (byte) k;
                k *= 2;
            }
        }

        // Main loop
        p.select(g, (int) (2 * w[nb - 1] + 1));
        for (i = nb - 2; i >= 0; i--) {
            p.usqr();
            r.select(g, (int) (2 * w[i] + s[i]));
            p.mul(r);
        }

        // apply correction
        r.copy(q[0]);
        r.conj();
        r.mul(p);
        p.cmove(r, pb);

        p.reduce();
        return p;
    }

    /* p=q0^u0.q1^u1.q2^u2.q3^u3 */
    /* Timing attack secure, but not cache attack secure */
/*
	public static FP12 pow4(FP12[] q,BIG[] u)
	{
		int i,j,nb,m;
		int[] a=new int[4];
		FP12 [] g=new FP12[8];
		FP12 [] s=new FP12[2];
		FP12 c=new FP12(1);
		FP12 p=new FP12(0);
		BIG [] t=new BIG[4];
		BIG mt=new BIG(0);
		byte[] w=new byte[BIG.NLEN*BIG.BASEBITS+1];
		for (i=0;i<4;i++)
			t[i]=new BIG(u[i]);
		s[0]=new FP12(0);
		s[1]=new FP12(0);
		g[0]=new FP12(q[0]); s[0].copy(q[1]); s[0].conj(); g[0].mul(s[0]);
		g[1]=new FP12(g[0]);
		g[2]=new FP12(g[0]);
		g[3]=new FP12(g[0]);
		g[4]=new FP12(q[0]); g[4].mul(q[1]);
		g[5]=new FP12(g[4]);
		g[6]=new FP12(g[4]);
		g[7]=new FP12(g[4]);
		s[1].copy(q[2]); s[0].copy(q[3]); s[0].conj(); s[1].mul(s[0]);
		s[0].copy(s[1]); s[0].conj(); g[1].mul(s[0]);
		g[2].mul(s[1]);
		g[5].mul(s[0]);
		g[6].mul(s[1]);
		s[1].copy(q[2]); s[1].mul(q[3]);
		s[0].copy(s[1]); s[0].conj(); g[0].mul(s[0]);
		g[3].mul(s[1]);
		g[4].mul(s[0]);
		g[7].mul(s[1]);
// if power is even add 1 to power, and add q to correction
		for (i=0;i<4;i++)
		{
			if (t[i].parity()==0)
			{
				t[i].inc(1); t[i].norm();
				c.mul(q[i]);
			}
			mt.add(t[i]); mt.norm();
		}
		c.conj();
		nb=1+mt.nbits();
// convert exponent to signed 1-bit window
		for (j=0;j<nb;j++)
		{
			for (i=0;i<4;i++)
			{
				a[i]=(t[i].lastbits(2)-2);
				t[i].dec(a[i]); t[i].norm();
				t[i].fshr(1);
			}
			w[j]=(byte)(8*a[0]+4*a[1]+2*a[2]+a[3]);
		}
		w[nb]=(byte)(8*t[0].lastbits(2)+4*t[1].lastbits(2)+2*t[2].lastbits(2)+t[3].lastbits(2));
		p.copy(g[(w[nb]-1)/2]);
		for (i=nb-1;i>=0;i--)
		{
			m=w[i]>>7;
			j=(w[i]^m)-m;  // j=abs(w[i])
			j=(j-1)/2;
			s[0].copy(g[j]); s[1].copy(g[j]); s[1].conj();
			p.usqr();
			p.mul(s[m&1]);
		}
		p.mul(c);  // apply correction
		p.reduce();
		return p;
	}
*/
/*
	public static void main(String[] args) {
		BIG p=new BIG(ROM.Modulus);
		FP2 w0,w1;
		BIG a=new BIG(0);
		BIG b=new BIG(0);

		a.zero(); b.zero(); a.inc(1); b.inc(2);
		w0=new FP2(a,b);
		a.zero(); b.zero(); a.inc(3); b.inc(4);
		w1=new FP2(a,b);
		FP4 t0=new FP4(w0,w1);
		a.zero(); b.zero(); a.inc(5); b.inc(6);
		w0=new FP2(a,b);
		a.zero(); b.zero(); a.inc(7); b.inc(8);
		w1=new FP2(a,b);
		FP4 t1=new FP4(w0,w1);
		a.zero(); b.zero(); a.inc(9); b.inc(10);
		w0=new FP2(a,b);
		a.zero(); b.zero(); a.inc(11); b.inc(12);
		w1=new FP2(a,b);
		FP4 t2=new FP4(w0,w1);
		FP12 w=new FP12(t0,t1,t2);
		FP12 t=new FP12(w);
		System.out.println("w= "+w.toString());
		a=new BIG(ROM_ZZZ.CURVE_Fra);
		b=new BIG(ROM_ZZZ.CURVE_Frb);
		FP2 f=new FP2(a,b);
		w.frob(f);
		System.out.println("w= "+w.toString());
		w=t.pow(p);
		System.out.println("w= "+w.toString());
		w.inverse();
		System.out.println("1/w= "+w.toString());
		w.inverse();
		System.out.println("w= "+w.toString());
		t.copy(w);
		w.conj();
		t.inverse();
		w.mul(t);
		System.out.println("w^(p^6-1)= "+w.toString());
		t.copy(w);
		w.frob(f);
		w.frob(f);
		w.mul(t);
		System.out.println("w^(p^6-1)(p^2+1)= "+w.toString());
		t.copy(w);
		t.inverse();
		w.conj();
		System.out.println("w= "+w.toString());
		System.out.println("t= "+t.toString());
	} */
}
