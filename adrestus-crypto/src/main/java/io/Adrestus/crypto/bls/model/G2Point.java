package io.Adrestus.crypto.bls.model;

import io.Adrestus.crypto.bls.BLS381.ECP2;
import io.Adrestus.crypto.bls.utils.CommonUtils;
import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;


import java.util.ArrayList;
import java.util.List;

public class G2Point {

    private ECP2 value;

    public G2Point() {
        this.value = new ECP2();
    }

    public G2Point(G2Point g2) {
        this.value = new ECP2(g2.value);
    }

    public G2Point(@Deserialize("value") ECP2 value) {
        this.value = value;
    }

    public G2Point(byte[] msg) {
        this.value = ECP2.mapit(msg);
    }

    public void add(G2Point rhs) {
        this.value.add(rhs.value);
    }

    public G2Point add2(G2Point other) {
        ECP2 newPoint = new ECP2(value);
        newPoint.add(other.value);
        return new G2Point(newPoint);
    }

    public void sub(G2Point rhs) {
        this.value.sub(rhs.value);
    }

    public void dbl() {
        this.value.dbl();
    }

    public G2Point[] getMultiples(int n) {
        G2Point[] res = new G2Point[n];
        res[0] = new G2Point(this);
        for (int i = 1; i < n; i++) {
            res[i] = new G2Point(this);
            res[i].add(res[i - 1]);
        }
        return res;
    }

    public static G2Point identity() {
        ECP2 value = new ECP2();
        value.inf();
        return new G2Point(value);
    }

    public G2Point mul(FieldElement scalar) {
        return new G2Point(value.mul(scalar.value));
    }

    public static boolean isInGroup(ECP2 point) {
        return HashToCurve.isInGroupG2(point);
    }

    public static G2Point multiScalarMulVarTime(G2Point[] g2Arr, FieldElement[] fieldElems) {
        G2LookupTable[] lookups = new G2LookupTable[g2Arr.length];

        for (int i = 0; i < lookups.length; i++) {
            lookups[i] = new G2LookupTable(g2Arr[i]);
        }

        List<List<Integer>> nafs = new ArrayList<>();
        for (int i = 0; i < fieldElems.length; i++) {
            nafs.add(fieldElems[i].toWnaf(5));
        }

        int newLength = CommonUtils.padCollection(nafs, 0);
        G2Point r = G2Point.identity();
        for (int i = newLength - 1; i >= 0; i--) {
            G2Point t = new G2Point(r);
            t.dbl();
            for (int j = 0; j < nafs.size(); j++) {
                List<Integer> naf = nafs.get(j);
                G2LookupTable lookup = lookups[j];

                if (naf.get(i) > 0) {
                    t.add(lookup.select(naf.get(i)));
                } else if (naf.get(i) < 0) {
                    t.sub(lookup.select(-naf.get(i)));
                }
            }
            r = t;
        }
        return r;
    }

    public static G2Point multiScalarMulConstTime(G2Point[] g2Arr, FieldElement[] fieldElems) {
        List<G2Point[]> g2ArrMultiple = new ArrayList<>();
        for (int i = 0; i < g2Arr.length; i++) {
            g2ArrMultiple.add(g2Arr[i].getMultiples(7));
        }

        List<List<Integer>> fieldElemsBaseRepr = new ArrayList<>();
        for (int i = 0; i < fieldElems.length; i++) {
            fieldElemsBaseRepr.add(fieldElems[i].toPowerOf2Base(3));
        }

        int newLength = CommonUtils.padCollection(fieldElemsBaseRepr, 0);
        G2Point r = new G2Point();

        for (int i = newLength - 1; i >= 0; i--) {
            r.dbl();
            r.dbl();
            r.dbl();

            for (int k = 0; k < fieldElemsBaseRepr.size(); k++) {
                List<Integer> b = fieldElemsBaseRepr.get(k);
                G2Point[] m = g2ArrMultiple.get(k);
                if (b.get(i) != 0) {
                    r.add(m[b.get(i) - 1]);
                }
            }
        }
        return r;
    }

    @Serialize
    public ECP2 getValue() {
        return value;
    }

    public void setValue(ECP2 value) {
        this.value = value;
    }
}
