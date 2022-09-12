package io.Adrestus.crypto.bls.model;

import io.Adrestus.crypto.bls.BLS381.ECP2;
import io.Adrestus.crypto.bls.constants.FP2Immutable;

import java.util.Objects;


public final class JacobianPoint {

    public static final JacobianPoint INFINITY = new JacobianPoint();

    private final FP2Immutable x;
    private final FP2Immutable y;
    private final FP2Immutable z;


    public JacobianPoint() {
        this(new FP2Immutable(0), new FP2Immutable(1), new FP2Immutable(0));
    }

    public JacobianPoint(JacobianPoint p) {
        this(p.x, p.y, p.z);
    }

    public JacobianPoint(FP2Immutable x, FP2Immutable y, FP2Immutable z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public JacobianPoint(ECP2 p) {
        ECP2 q = new ECP2(p);
        if (!p.getz().isunity()) {
            // This is quicker than converting directly between homogeneous and Jacobian coordinates
            q.affine();
        }
        this.x = new FP2Immutable(q.getX());
        this.y = new FP2Immutable(q.getY());
        this.z = FP2Immutable.ONE;
    }

    public boolean isInfinity() {
        return x.iszilch() && !y.iszilch() && z.iszilch();
    }

    public JacobianPoint dbl() {
        FP2Immutable a = x.sqr();
        FP2Immutable b = y.sqr();
        FP2Immutable c = b.sqr();
        FP2Immutable d = x.add(b).sqr().sub(a).sub(c).dbl();
        FP2Immutable e = a.mul(3);
        FP2Immutable f = e.sqr();

        FP2Immutable xOut = f.sub(d.dbl());
        FP2Immutable yOut = e.mul(d.sub(xOut)).sub(c.mul(8));
        FP2Immutable zOut = y.mul(z).dbl();

        return zOut.iszilch() ? INFINITY : new JacobianPoint(xOut, yOut, zOut);
    }


    public JacobianPoint add(JacobianPoint q) {

        FP2Immutable x1 = x;
        FP2Immutable y1 = y;
        FP2Immutable z1 = z;
        FP2Immutable x2 = q.x;
        FP2Immutable y2 = q.y;
        FP2Immutable z2 = q.z;

        boolean pInf = (z1.iszilch());
        boolean qInf = (z2.iszilch());
        if (pInf && qInf) {
            return INFINITY;
        } else if (qInf) {
            return new JacobianPoint(this);
        } else if (pInf) {
            return q;
        }

        FP2Immutable z1z1 = z1.sqr();
        FP2Immutable z2z2 = z2.sqr();
        FP2Immutable u1 = x1.mul(z2z2);
        FP2Immutable u2 = x2.mul(z1z1);
        FP2Immutable s1 = y1.mul(z2).mul(z2z2);
        FP2Immutable s2 = y2.mul(z1).mul(z1z1);

        // Shortcut for equal X coordinates case. Either P == Q or P == -Q.
        if (u1.equals(u2)) {
            return s1.equals(s2) ? dbl() : INFINITY;
        }

        FP2Immutable h = u2.sub(u1);
        FP2Immutable i = h.dbl().sqr();
        FP2Immutable j = h.mul(i);
        FP2Immutable rr = s2.sub(s1).dbl();
        FP2Immutable v = u1.mul(i);
        FP2Immutable x3 = rr.sqr().sub(j).sub(v.dbl());
        FP2Immutable y3 = rr.mul(v.sub(x3)).sub(s1.mul(j).dbl());
        FP2Immutable z3 = z1.mul(z2).mul(h).dbl();

        return new JacobianPoint(x3, y3, z3);
    }

    public JacobianPoint neg() {
        return new JacobianPoint(x, y.neg(), z);
    }

    public JacobianPoint dbls(int n) {
        JacobianPoint result = new JacobianPoint(this);
        while (n-- > 0) {
            result = result.dbl();
        }
        return result;
    }

    public JacobianPoint toAffine() {
        if (isInfinity()) {
            return INFINITY;
        }
        FP2Immutable zInv = z.inverse();
        FP2Immutable z2Inv = zInv.sqr();
        FP2Immutable z3Inv = zInv.mul(z2Inv);
        return new JacobianPoint(x.mul(z2Inv), y.mul(z3Inv), FP2Immutable.ONE);
    }

    public ECP2 toECP2() {
        JacobianPoint q = this.toAffine();
        return new ECP2(q.getX().getFp2(), q.getY().getFp2());
    }

    public FP2Immutable getX() {
        return new FP2Immutable(x);
    }

    public FP2Immutable getY() {
        return new FP2Immutable(y);
    }

    public FP2Immutable getZ() {
        return new FP2Immutable(z);
    }

    @Override
    public String toString() {
        return "JacobianPoint[" + x + ", " + y + ", " + z + "]";
    }

    @Override
    // Consider two Jacobian points to be equal iff their affine representations are equal.
    public boolean equals(Object obj) {
        if (Objects.isNull(obj)) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof JacobianPoint)) {
            return false;
        }
        JacobianPoint other = (JacobianPoint) obj;
        JacobianPoint p1 = this.toAffine();
        JacobianPoint p2 = other.toAffine();
        return (p1.getX().equals(p2.getX()) && p1.getY().equals(p2.getY()));
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }
}
