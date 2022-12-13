package io.Adrestus.core;

import com.google.common.base.Objects;
import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class InboundRelay {
    private Map<Integer, Map<Receipt.ReceiptBlock, List<Receipt>>> map_receipts;

    public InboundRelay(@Deserialize("map_receipts") Map<Integer, Map<Receipt.ReceiptBlock, List<Receipt>>> map_receipts) {
        this.map_receipts = map_receipts;
    }

    public InboundRelay() {
        this.map_receipts = new LinkedHashMap<>();
    }

    @Serialize
    public Map<Integer, Map<Receipt.ReceiptBlock, List<Receipt>>> getMap_receipts() {
        return map_receipts;
    }

    public void setMap_receipts(Map<Integer, Map<Receipt.ReceiptBlock, List<Receipt>>> map_receipts) {
        this.map_receipts = map_receipts;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InboundRelay that = (InboundRelay) o;
        return Objects.equal(map_receipts, that.map_receipts);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(map_receipts);
    }

    @Override
    public String toString() {
        return "InboundRelay{" +
                "map_receipts=" + map_receipts +
                '}';
    }
}
