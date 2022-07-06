package io.Adrestus.crypto.bls.model;

import io.Adrestus.crypto.bls.constants.*;
import io.Adrestus.crypto.bls.utils.*;
import org.apache.milagro.amcl.BLS381.ECP;

import java.util.ArrayList;
import java.util.List;

public class G1 {

    public ECP value;
    
    public G1() {
        this.value = new ECP();
    }
    
    public G1(G1 g1) {
        this.value = new ECP(g1.value);
    }
    
    public G1(ECP value) {
        this.value = value;
    }
    
    public G1(byte[] msg) {
        this.value = ECP.mapit(msg);
    }
    
    public void neg() {
        this.value.neg();
    }
    
    public void add(G1 rhs) {
        this.value.add(rhs.value);
    }
    
    public void sub(G1 rhs) {
        this.value.sub(rhs.value);
    }
    
    public void dbl() {
        value.dbl();
    }
    
    public static G1 identity() {
        ECP value = new ECP();
        value.inf();
        return new G1(value);
    }
    
    public byte[] toBytes() {
        byte[] buf = new byte[Constants.GROUP_G1_SIZE];
        value.toBytes(buf, false);
        return buf;
    }
    
    public static G1 fromBytes(byte[] buf) {
        return new G1(ECP.fromBytes(buf));
    }
    
    public static G1 multiScalarMulVarTime(G1[] g1Arr, FieldElement[] fieldElems) {
        G1LookupTable[] lookups = new G1LookupTable[g1Arr.length];
        
        for(int i = 0; i < lookups.length; i++) {
            lookups[i] = new G1LookupTable(g1Arr[i]);
        }
        
        List<List<Integer>> nafs = new ArrayList<>();
        for(int i = 0; i < fieldElems.length; i++) {
            nafs.add(fieldElems[i].toWnaf(5));
        }
        
        int newLength = CommonUtils.padCollection(nafs, 0);
        G1 r = G1.identity();
        for(int i = newLength - 1; i >= 0; i--) {
            G1 t = new G1(r);
            t.dbl();
            for(int j = 0; j < nafs.size(); j++) {
                List<Integer> naf = nafs.get(j);
                G1LookupTable lookup = lookups[j];
                
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
}
