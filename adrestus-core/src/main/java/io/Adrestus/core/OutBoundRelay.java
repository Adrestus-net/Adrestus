package io.Adrestus.core;

import com.google.common.base.Objects;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;

import java.io.Serializable;
import java.util.HashSet;
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



    //NEVER DELETE THIS FUNCTION ELSE BLOCK HASH EVENT HANDLER WILL HAVE PROBLEM IS EQUALS FUNCTIONALITY
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OutBoundRelay that = (OutBoundRelay) o;
        boolean keys=Objects.equal(map_receipts.keySet(), that.map_receipts.keySet() );
        boolean values=new HashSet<>( map_receipts.values()).equals(new HashSet<>( that.map_receipts.values() ));
        return keys && values;
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
