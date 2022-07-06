package io.Adrestus.crypto.bls.model;

import io.Adrestus.crypto.HashUtil;
import io.Adrestus.crypto.bls.constants.*;
import io.Adrestus.crypto.bls.utils.*;
import org.apache.milagro.amcl.BLS381.BIG;
import org.apache.milagro.amcl.BLS381.DBIG;
import org.apache.milagro.amcl.RAND;

import java.util.ArrayList;
import java.util.List;

public class FieldElement {

    public BIG value;
    
    public FieldElement() {
        this.value = new BIG();
    }
    
    public FieldElement(BIG value) {
        this.value = value;
    }
    
    public FieldElement(FieldElement fe) {
        this.value = new BIG(fe.value);
    }
    
    public FieldElement(RAND r) {
        BIG n;
        do {
            n = BIG.randomnum(Constants.curveOrder, r);
        }while(n.iszilch());
        this.value = n;
    }
    
    public FieldElement plus(FieldElement rhs) {
        BIG sum = new BIG(this.value);
        sum.add(rhs.value);        
        BigNumberUtils.rmod(sum, Constants.curveOrder);
        return new FieldElement(sum);        
    }
    
    public FieldElement minus(FieldElement rhs) {
        BIG sum = new BIG(this.value);
        BIG neg_b = BIG.modneg(rhs.value, Constants.curveOrder);
        sum.add(neg_b);
        BigNumberUtils.rmod(sum, Constants.curveOrder);
        return new FieldElement(sum);        
    }    
    
    public FieldElement neg() {
        FieldElement z = zero();
        return z.minus(this);
    }
    
    public static FieldElement fromBytes(byte[] x) {
        BIG value = BIG.fromBytes(x);
        BigNumberUtils.rmod(value, Constants.curveOrder);
        return new FieldElement(value);
    }
    
    public byte[] toBytes() {
        byte[] buf = new byte[Constants.FIELD_ELEMENT_SIZE];
        this.value.toBytes(buf);
        return buf;
    }
    
    public static FieldElement fromMsgHash(byte[] msg) {
        byte[] hash = HashUtil.sha256(msg);
        return fromBytes(hash);
    }
    
    public FieldElement mul(FieldElement rhs) {
        DBIG d = BIG.mul(this.value, rhs.value);
        return new FieldElement(BigNumberUtils.reduceDmodCurveOrder(d));
    }
    
    public List<Integer> toPowerOf2Base(int n) {
        List<Integer> baseRepr = new ArrayList<Integer>();
        
        if(this.isZero()) {
            return baseRepr;
        }
        BIG t = toBignum();
        t.norm();
        
        while(!t.iszilch()) {
            BIG d = new BIG(t);
            d.mod2m(n);
            baseRepr.add((int)d.get(0));
            t.fshr(n);
        }
        
        return baseRepr;        
    }
    
    public void inverse() {
        this.value.invmodp(Constants.curveOrder);
    }
    
    public boolean isZero() {
        return value.iszilch();
    }
    
    public boolean isOne() {
        return value.isunity();
    }
    
    public static FieldElement zero() {
        return new FieldElement(new BIG(0));
    }
    
    public static FieldElement one() {
        return new FieldElement(new BIG(1));
    }
    
    public BIG toBignum() {
        BIG v = new BIG(this.value);
        BigNumberUtils.rmod(v, Constants.curveOrder);
        return v;
    }
    
    public List<Integer> toWnaf(int w) {
        BIG k = toBignum();
        List<Integer> naf = new ArrayList<>();
        
        long twoW1 = 1 << (w - 1); // 2^(w-1)
        long twoW = 1 << w; // 2^w
        
        while(!k.iszilch()) {
            int t = 0;
            if(k.parity() == 1) {
                BIG b = new BIG(k);
                b.mod2m(w);
                long u = b.get(0);
                
                if(u >= twoW1) {
                    u = u - twoW;
                }
                
                k.set(0, k.get(0)-u);
                t = (int) u;
            }
            naf.add(t);
            k.fshr(1);
        }
        
        return naf;
    }
    
    public static List<FieldElement> newVandermondeVector(FieldElement elem, int size) {
        List<FieldElement> res = new ArrayList<>();
        
        if(size == 0) {
            return res;
        }else if(elem.isZero()) {
            for(int i = 0; i < size; i++) {
                res.add(zero());
            }
        }else if(elem.isOne()) {
            for(int i = 0; i < size; i++) {
                res.add(one());
            }
        }else {
            res.add(one());
            for(int i = 1; i < size; i++) {
                res.add(res.get(i-1).mul(elem));
            }
        }
        return res;        
    }
    
    public static FieldElement innerProduct(List<FieldElement> a, List<FieldElement> b) {
        FieldElement acc = new FieldElement();
        for(int i = 0; i < a.size(); i++) {
            acc = acc.plus(a.get(i).mul(b.get(i)));
        }
        return new FieldElement(acc);
    }
}
