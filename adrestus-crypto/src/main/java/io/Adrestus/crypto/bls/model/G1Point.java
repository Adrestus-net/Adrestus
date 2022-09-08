package io.Adrestus.crypto.bls.model;

import io.Adrestus.crypto.bls.constants.Constants;
import io.Adrestus.crypto.bls.utils.CommonUtils;
import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;
import org.apache.milagro.amcl.BLS381.ECP;

import java.util.ArrayList;
import java.util.List;

public class G1Point {

    private ECP value;

    public G1Point() {
        this.value = new ECP();
    }

    public G1Point(G1Point g1) {
        this.value = new ECP(g1.value);
    }

    public G1Point(ECP value) {
        this.value = value;
    }

    public G1Point(byte[] msg) {
        this.value = ECP.mapit(msg);
    }

    public void neg() {
        this.value.neg();
    }

    public void add(G1Point rhs) {
        this.value.add(rhs.value);
    }

    public void sub(G1Point rhs) {
        this.value.sub(rhs.value);
    }

    public void dbl() {
        value.dbl();
    }

    public static G1Point identity() {
        ECP value = new ECP();
        value.inf();
        return new G1Point(value);
    }

    public byte[] toBytes() {
        byte[] buf = new byte[Constants.GROUP_G1_SIZE];
        value.toBytes(buf, false);
        return buf;
    }

    public static G1Point fromBytes(byte[] buf) {
        return new G1Point(ECP.fromBytes(buf));
    }

    public static G1Point multiScalarMulVarTime(G1Point[] g1Arr, FieldElement[] fieldElems) {
        G1LookupTable[] lookups = new G1LookupTable[g1Arr.length];

        for (int i = 0; i < lookups.length; i++) {
            lookups[i] = new G1LookupTable(g1Arr[i]);
        }

        List<List<Integer>> nafs = new ArrayList<>();
        for (int i = 0; i < fieldElems.length; i++) {
            nafs.add(fieldElems[i].toWnaf(5));
        }

        int newLength = CommonUtils.padCollection(nafs, 0);
        G1Point r = G1Point.identity();
        for (int i = newLength - 1; i >= 0; i--) {
            G1Point t = new G1Point(r);
            t.dbl();
            for (int j = 0; j < nafs.size(); j++) {
                List<Integer> naf = nafs.get(j);
                G1LookupTable lookup = lookups[j];

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

    @Serialize
    public ECP getValue() {
        return value;
    }

    public void setValue(ECP value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "G1Point{" +
                "value=" + value +
                '}';
    }
}
