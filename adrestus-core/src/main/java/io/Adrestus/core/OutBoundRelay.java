package io.Adrestus.core;

import com.google.common.base.Objects;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;

import java.io.Serializable;
import java.util.*;

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



    //NEVER DELETE THIS FUNCTION ELSE BLOCK HASH EVENT HANDLER WILL HAVE PROBLEM IS EQUALS FUNCTIONALITY
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OutBoundRelay that = (OutBoundRelay) o;
        boolean keys=Objects.equal(map_receipts.keySet(), that.map_receipts.keySet() );
        boolean values=linkedEquals(map_receipts,that.map_receipts);
        return keys && values;
    }

    public static <K, V> boolean linkedEquals( LinkedHashMap<K, V> left, LinkedHashMap<K, V> right) {
        Iterator<Map.Entry<K, V>> leftItr = left.entrySet().iterator();
        Iterator<Map.Entry<K, V>> rightItr = right.entrySet().iterator();

        while ( leftItr.hasNext() && rightItr.hasNext()) {
            Map.Entry<K, V> leftEntry = leftItr.next();
            Map.Entry<K, V> rightEntry = rightItr.next();

            //AbstractList does null checks here but for maps we can assume you never get null entries
            if (! leftEntry.equals(rightEntry))
                return false;
        }
        return !(leftItr.hasNext() || rightItr.hasNext());
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
