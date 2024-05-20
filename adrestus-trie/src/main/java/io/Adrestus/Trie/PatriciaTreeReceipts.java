package io.Adrestus.Trie;

import io.activej.serializer.annotations.Serialize;
import io.activej.serializer.annotations.SerializeNullable;

import java.io.Serializable;
import java.util.*;

public class PatriciaTreeReceipts implements PatriciaTreeReceiptMethods, Serializable {
    private HashMap<Integer, @SerializeNullable ReceiptPointerStorage> receiptCapacities;

    public PatriciaTreeReceipts() {
        this.receiptCapacities = new HashMap<Integer, ReceiptPointerStorage>();
    }

    @Override
    public void addReceiptPosition(String hash, int origin_zone, int blockHeight, int zone, int receiptBlockHeight, int position) {
        ReceiptPointerStorage pointerStorage;
        if (this.receiptCapacities.containsKey(origin_zone)) {
            pointerStorage = this.receiptCapacities.get(origin_zone);
        } else {
            pointerStorage = new ReceiptPointerStorage();
        }
        HashMap<Integer, HashMap<Integer, HashSet<Integer>>> zoneMap;
        if (pointerStorage.getPositions().containsKey(blockHeight)) {
            zoneMap = pointerStorage.getPositions().get(blockHeight);
        } else {
            zoneMap = new HashMap<Integer, HashMap<Integer, HashSet<Integer>>>();
        }

        HashMap<Integer, HashSet<Integer>> receiptblockheightMap;
        if (zoneMap.containsKey(zone)) {
            receiptblockheightMap = zoneMap.get(zone);
        } else {
            receiptblockheightMap = new HashMap<Integer, HashSet<Integer>>();
        }
        HashSet<Integer> hashSet;
        if (receiptblockheightMap.containsKey(receiptBlockHeight)) {
            hashSet = receiptblockheightMap.get(receiptBlockHeight);
        } else {
            hashSet = new HashSet<>();
        }
        hashSet.add(position);
        receiptblockheightMap.put(receiptBlockHeight, hashSet);
        zoneMap.put(zone, receiptblockheightMap);
        pointerStorage.getPositions().put(blockHeight, zoneMap);
        pointerStorage.getFilter().add(String.join("", hash, String.valueOf(origin_zone), String.valueOf(blockHeight), String.valueOf(zone), String.valueOf(receiptBlockHeight), String.valueOf(position)));
        this.receiptCapacities.put(origin_zone, pointerStorage);
    }

    @Override
    public List<StorageInfo> retrieveReceiptInfoByHash(String hash) {
        LinkedHashSet<StorageInfo> result = new LinkedHashSet<>();
        if (receiptCapacities.isEmpty())
            return new ArrayList<>(result);


        for (Map.Entry<Integer, ReceiptPointerStorage> entry : receiptCapacities.entrySet()) {
            ReceiptPointerStorage pointerStorage = entry.getValue();
            pointerStorage.getPositions().forEach((blockHeight, blockHeightvalue) -> blockHeightvalue.forEach((zonekey, zonevalue) -> zonevalue.forEach((receiptheight, receiptvalue) ->
                    receiptvalue.forEach(pos -> {
                        if (pointerStorage.getFilter().contains(String.join("", hash, String.valueOf(entry.getKey()), String.valueOf(blockHeight), String.valueOf(zonekey), String.valueOf(receiptheight), String.valueOf(pos)))) {
                            result.add(new StorageInfo(entry.getKey(), blockHeight, zonekey, receiptheight, pos));
                        }
                    }))));

        }
        return new ArrayList<>(result);
    }

    @Serialize
    @Override
    public HashMap<Integer, @SerializeNullable ReceiptPointerStorage> getReceiptCapacities() {
        return receiptCapacities;
    }

    @Override
    public void SetReceiptCapacities(HashMap<Integer, @SerializeNullable ReceiptPointerStorage> receiptCapacities) {
        this.receiptCapacities = receiptCapacities;
    }

    public static boolean linkedEquals(HashMap<Integer, ReceiptPointerStorage> left, HashMap<Integer, ReceiptPointerStorage> right) {
        if (left.size() != right.size())
            return false;

        for (int i = 0; i < left.size(); i++) {
            Iterator<Map.Entry<Integer, ReceiptPointerStorage>> leftItr = left.entrySet().iterator();
            Iterator<Map.Entry<Integer, ReceiptPointerStorage>> rightItr = right.entrySet().iterator();

            while (leftItr.hasNext() && rightItr.hasNext()) {
                Map.Entry<Integer, ReceiptPointerStorage> leftEntry = leftItr.next();
                Map.Entry<Integer, ReceiptPointerStorage> rightEntry = rightItr.next();
                if (!leftEntry.getKey().equals(rightEntry.getKey()))
                    return false;
                if (!leftEntry.getValue().equals(rightEntry.getValue()))
                    return false;
            }
            return !(leftItr.hasNext() || rightItr.hasNext());
        }
        return true;
    }

    //NEVER DELETE THIS FUNCTION ELSE BLOCK HASH EVENT HANDLER WILL HAVE PROBLEM with  EQUALS FUNCTIONALITY
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PatriciaTreeReceipts that = (PatriciaTreeReceipts) o;
        return linkedEquals(receiptCapacities, that.receiptCapacities);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(receiptCapacities);
    }

    @Override
    public String toString() {
        return "PatriciaTreeReceipts{" +
                "receiptCapacities=" + receiptCapacities +
                '}';
    }
}
