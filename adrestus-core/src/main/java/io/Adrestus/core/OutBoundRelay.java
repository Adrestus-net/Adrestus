package io.Adrestus.core;

import com.google.common.base.Objects;
import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OutBoundRelay implements Serializable {

    private LinkedHashMap<Integer, LinkedHashMap<Receipt.ReceiptBlock, List<Receipt>>> map_receipts;


    public OutBoundRelay(@Deserialize("map_receipts") Map<Integer, Map<Receipt.ReceiptBlock, List<Receipt>>> map_receipts) {
        this.map_receipts = new LinkedHashMap<>();
        for (Map.Entry<Integer, Map<Receipt.ReceiptBlock, List<Receipt>>> entry : map_receipts.entrySet()) {
            this.map_receipts.put(entry.getKey(), new LinkedHashMap<>(entry.getValue()));
        }
    }

    public OutBoundRelay() {
        this.map_receipts = new LinkedHashMap<>();
    }

    @Serialize
    public LinkedHashMap<Integer, LinkedHashMap<Receipt.ReceiptBlock, List<Receipt>>> getMap_receipts() {
        return map_receipts;
    }

    public void setMap_receipts(LinkedHashMap<Integer, LinkedHashMap<Receipt.ReceiptBlock, List<Receipt>>> map_receipts) {
        this.map_receipts = map_receipts;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OutBoundRelay that = (OutBoundRelay) o;
        return Objects.equal(map_receipts, that.map_receipts);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(map_receipts);
    }

    @Override
    public String toString() {
        return "OutBoundRelay{" +
                "map_receipts=" + map_receipts +
                '}';
    }
}
