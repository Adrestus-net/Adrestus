package io.Adrestus.core;

import com.google.common.base.Objects;
import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;

import java.io.Serializable;
import java.util.*;

public class InboundRelay implements Serializable, Cloneable {
    private LinkedHashMap<Integer, LinkedHashMap<Receipt.ReceiptBlock, List<Receipt>>> map_receipts;

    public InboundRelay(@Deserialize("map_receipts") Map<Integer, Map<Receipt.ReceiptBlock, List<Receipt>>> map_receipts) {
        this.map_receipts = new LinkedHashMap<>();
        for (Map.Entry<Integer, Map<Receipt.ReceiptBlock, List<Receipt>>> entry : map_receipts.entrySet()) {
            this.map_receipts.put(entry.getKey(), new LinkedHashMap<>(entry.getValue()));
        }
    }

    public InboundRelay() {
        this.map_receipts = new LinkedHashMap<>();
    }

    @Serialize
    public LinkedHashMap<Integer, LinkedHashMap<Receipt.ReceiptBlock, List<Receipt>>> getMap_receipts() {
        return map_receipts;
    }

    public void setMap_receipts(LinkedHashMap<Integer, LinkedHashMap<Receipt.ReceiptBlock, List<Receipt>>> map_receipts) {
        this.map_receipts = map_receipts;
    }

    //NEVER DELETE THIS FUNCTION ELSE BLOCK HASH EVENT HANDLER WILL HAVE PROBLEM with  EQUALS FUNCTIONALITY
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InboundRelay that = (InboundRelay) o;
        boolean keys = Objects.equal(map_receipts.keySet(), that.map_receipts.keySet());
        boolean values = linkedEquals(map_receipts.values(), that.map_receipts.values());
        return keys && values;
    }

    public static boolean linkedEquals(Collection<LinkedHashMap<Receipt.ReceiptBlock, List<Receipt>>> left, Collection<LinkedHashMap<Receipt.ReceiptBlock, List<Receipt>>> right) {
        if (left.size() != right.size())
            return false;

        List<LinkedHashMap<Receipt.ReceiptBlock, List<Receipt>>> left_list = new ArrayList(left);
        List<LinkedHashMap<Receipt.ReceiptBlock, List<Receipt>>> right_list = new ArrayList(right);
        for (int i = 0; i < left_list.size(); i++) {
            Iterator<Map.Entry<Receipt.ReceiptBlock, List<Receipt>>> leftItr = left_list.get(i).entrySet().iterator();
            Iterator<Map.Entry<Receipt.ReceiptBlock, List<Receipt>>> rightItr = right_list.get(i).entrySet().iterator();

            while (leftItr.hasNext() && rightItr.hasNext()) {
                Map.Entry<Receipt.ReceiptBlock, List<Receipt>> leftEntry = leftItr.next();
                Map.Entry<Receipt.ReceiptBlock, List<Receipt>> rightEntry = rightItr.next();
                if (!leftEntry.getKey().equals(rightEntry.getKey()))
                    return false;
                if (!new HashSet<>(leftEntry.getValue()).equals(new HashSet<>(rightEntry.getValue())))
                    return false;
            }
            return !(leftItr.hasNext() || rightItr.hasNext());
        }
        return true;
    }

    @Override
    public InboundRelay clone() throws CloneNotSupportedException {
        InboundRelay inboundRelay = (InboundRelay) super.clone();
        inboundRelay.setMap_receipts(new LinkedHashMap<>());

        if (this.getMap_receipts() == null || this.getMap_receipts().isEmpty())
            return inboundRelay;

        for (var outer : this.getMap_receipts().entrySet()) {
            for (var inner : outer.getValue().entrySet()) {
                inner.setValue(new ArrayList<>(inner.getValue()));
                outer.setValue(new LinkedHashMap<>(outer.getValue()));
            }
            inboundRelay.getMap_receipts().put(outer.getKey(), new LinkedHashMap<>(outer.getValue()));
        }
        return inboundRelay;
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
