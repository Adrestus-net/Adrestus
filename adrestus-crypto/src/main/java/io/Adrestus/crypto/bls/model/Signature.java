package io.Adrestus.crypto.bls.model;

import io.Adrestus.crypto.bls.BLS381.ECP2;
import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;
import org.spongycastle.util.encoders.Hex;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class Signature implements Cloneable, Serializable {
    private G2Point point;
    private Supplier<G2Point> supplier_point;

    public Signature(@Deserialize("point") G2Point point) {
        this.point = point;
    }

    public Signature() {
        this.point = new G2Point();
    }

    public Signature(Supplier<G2Point> supplier_point) {
        this.supplier_point = supplier_point;
    }

    @Serialize
    public G2Point getPoint() {
        return point;
    }

    public void setPoint(G2Point point) {
        this.point = point;
    }

    public static Signature aggregate(List<Signature> signatures) {
        ArrayList<Signature> cloned_keys = new ArrayList<Signature>();
        signatures.forEach(x -> cloned_keys.add((Signature) x.clone()));
        return cloned_keys.isEmpty() ? new Signature(new G2Point()) : cloned_keys.stream().reduce(Signature::combine).get();
    }

    public Signature combine(Signature signature) {
        point.getValue().add(signature.point.getValue());
        return new Signature(point);
    }

    public byte[] toBytes() {
        return point.tobytes();
    }

    public String toRaw() {
        return Hex.toHexString(toBytes());
    }

    public static Signature fromByte(byte[] buf) {
        return new Signature(new G2Point(ECP2.fromBytes(buf)));
    }

    @Serialize
    public Supplier<G2Point> getSupplier_point() {
        return supplier_point;
    }

    public void setSupplier_point(Supplier<G2Point> supplier_point) {
        this.supplier_point = supplier_point;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Signature signature = (Signature) o;
        return Arrays.equals(point.tobytes(), signature.point.tobytes());
    }

    @Override
    public Signature clone() {
        return new Signature(new G2Point(point));
    }

    @Override
    public int hashCode() {
        return Objects.hash(point, supplier_point);
    }
}
