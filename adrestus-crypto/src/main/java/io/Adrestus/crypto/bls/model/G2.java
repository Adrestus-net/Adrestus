package io.Adrestus.crypto.bls.model;

import io.Adrestus.crypto.bls.utils.CommonUtils;
import org.apache.milagro.amcl.BLS381.ECP2;

import java.util.ArrayList;
import java.util.List;

public class G2 {

    public ECP2 value;
    
    public G2() {
        this.value = new ECP2();
    }
    
    public G2(G2 g2) {
        this.value = new ECP2(g2.value);
    }
    
    public G2(ECP2 value) {
        this.value = value;
    }
    
    public G2(byte[] msg) {
        this.value = ECP2.mapit(msg);
    }
    
    public void add(G2 rhs) {
        this.value.add(rhs.value);
    }
    
    public void sub(G2 rhs) {
        this.value.sub(rhs.value);
    }
    
    public void dbl() {
        this.value.dbl();
    }
    
    public G2[] getMultiples(int n) {
        G2[] res = new G2[n];
        res[0] = new G2(this);
        for(int i = 1; i < n; i++) {
            res[i] = new G2(this);
            res[i].add(res[i-1]);
        }
        return res;
    }
    
    public static G2 identity() {
        ECP2 value = new ECP2();
        value.inf();
        return new G2(value);
    }
    
    public static G2 multiScalarMulVarTime(G2[] g2Arr, FieldElement[] fieldElems) {
        G2LookupTable[] lookups = new G2LookupTable[g2Arr.length];
        
        for(int i = 0; i < lookups.length; i++) {
            lookups[i] = new G2LookupTable(g2Arr[i]);
        }
        
        List<List<Integer>> nafs = new ArrayList<>();
        for(int i = 0; i < fieldElems.length; i++) {
            nafs.add(fieldElems[i].toWnaf(5));
        }
        
        int newLength = CommonUtils.padCollection(nafs, 0);
        G2 r = G2.identity();
        for(int i = newLength - 1; i >= 0; i--) {
            G2 t = new G2(r);
            t.dbl();
            for(int j = 0; j < nafs.size(); j++) {
                List<Integer> naf = nafs.get(j);
                G2LookupTable lookup = lookups[j];
                
                if(naf.get(i) > 0) {
                    t.add(lookup.select(naf.get(i)));
                }else if(naf.get(i) < 0) {
                    t.sub(lookup.select(-naf.get(i)));
                }
            }
            r = t;
        }
        return r;        
    }
    
    public static G2 multiScalarMulConstTime(G2[] g2Arr, FieldElement[] fieldElems) {
        List<G2[]> g2ArrMultiple = new ArrayList<>();
        for(int i = 0; i < g2Arr.length; i++) {
            g2ArrMultiple.add(g2Arr[i].getMultiples(7));
        }
        
        List<List<Integer>> fieldElemsBaseRepr = new ArrayList<>();
        for(int i = 0; i < fieldElems.length; i++) {
            fieldElemsBaseRepr.add(fieldElems[i].toPowerOf2Base(3));
        }
        
        int newLength = CommonUtils.padCollection(fieldElemsBaseRepr, 0);
        G2 r = new G2();
        
        for(int i = newLength - 1; i >= 0; i--) {
            r.dbl();
            r.dbl();
            r.dbl();
            
            for(int k = 0 ; k < fieldElemsBaseRepr.size(); k++) {
                List<Integer> b = fieldElemsBaseRepr.get(k);
                G2[] m = g2ArrMultiple.get(k);
                if(b.get(i) != 0) {
                    r.add(m[b.get(i)-1]);
                }
            }
        }
        return r;
    }
}
