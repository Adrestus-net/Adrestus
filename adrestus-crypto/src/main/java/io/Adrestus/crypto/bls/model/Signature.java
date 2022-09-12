package io.Adrestus.crypto.bls.model;

import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;

import java.util.Objects;
import java.util.function.Supplier;

public class Signature {
    private G2Point point;
    private Supplier<G2Point> supplier_point;

    public Signature(@Deserialize("point") G2Point point) {
        this.point = point;
    }

    public Signature() {
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

    public Signature combine(Signature signature) {
        point.getValue().add(signature.point.getValue());
        return new Signature(point);
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
        return Objects.equals(point, signature.point) && Objects.equals(supplier_point, signature.supplier_point);
    }

    @Override
    public int hashCode() {
        return Objects.hash(point, supplier_point);
    }
}
