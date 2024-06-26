package io.Adrestus.Trie;

import io.activej.serializer.annotations.Serialize;
import io.activej.serializer.annotations.SerializeNullable;

import java.util.*;

public class PatriciaTreeRegularTransaction implements PatriciaTreeTransactionMethods {
    private HashMap<Integer, @SerializeNullable TransactionPointerStorage> regularCapacities;

    public PatriciaTreeRegularTransaction() {
        this.regularCapacities = new HashMap<Integer, TransactionPointerStorage>();
    }

    @Override
    public void addTransactionPosition(String hash, int origin_zone, int height, int position) {
        TransactionPointerStorage pointerStorage;
        if (this.regularCapacities.containsKey(origin_zone)) {
            pointerStorage = this.regularCapacities.get(origin_zone);
        } else {
            pointerStorage = new TransactionPointerStorage();
        }
        HashSet<Integer> hashSet;
        if (pointerStorage.getPositions().containsKey(height)) {
            hashSet = pointerStorage.getPositions().get(height);
        } else {
            hashSet = new HashSet<>();
        }
        hashSet.add(position);
        pointerStorage.getPositions().put(height, hashSet);
        pointerStorage.getFilter().add(String.join("", hash, String.valueOf(origin_zone), String.valueOf(height), String.valueOf(position)));
        this.regularCapacities.put(origin_zone, pointerStorage);
    }

    @Override
    public List<StorageInfo> retrieveTransactionInfoByHash(String hash) {
        LinkedHashSet<StorageInfo> result = new LinkedHashSet<>();
        if (this.regularCapacities.isEmpty())
            return new ArrayList<>(result);

        for (Map.Entry<Integer, TransactionPointerStorage> entry : this.regularCapacities.entrySet()) {
            TransactionPointerStorage pointerStorage = entry.getValue();
            pointerStorage.getPositions().forEach((blockheight, value) -> value.forEach(pos -> {
                if (pointerStorage.getFilter().contains(String.join("", hash, String.valueOf(entry.getKey()), String.valueOf(blockheight), String.valueOf(pos)))) {
                    result.add(new StorageInfo(entry.getKey(), blockheight, pos));
                }
            }));

        }
        return new ArrayList<>(result);
    }

    @Override
    public HashMap<Integer, HashSet<Integer>> retrieveAllTransactionsByOriginZone(int zone) {
        HashMap<Integer, HashSet<Integer>> result = new HashMap<Integer, HashSet<Integer>>();
        if (regularCapacities.isEmpty())
            return result;

        if (!regularCapacities.containsKey(zone))
            return result;
        return regularCapacities.get(zone).getPositions();
    }

    @Override
    public Optional<StorageInfo> findLatestStorageInfo(int zone) {
        if (regularCapacities.isEmpty())
            return Optional.empty();
        int max = -1;
        for (Map.Entry<Integer, TransactionPointerStorage> entry : regularCapacities.entrySet()) {
            if (entry.getKey() == zone) {
                for (Map.Entry<Integer, HashSet<Integer>> entry2 : entry.getValue().getPositions().entrySet()) {
                    if (entry2.getKey() > max) {
                        max = entry2.getKey();
                    }
                }
            }
        }
        if (max == -1)
            return Optional.empty();

        return Optional.of(new StorageInfo(max, regularCapacities.get(zone).getPositions().get(max)));
    }

    @Serialize
    @Override
    public HashMap<Integer, @SerializeNullable TransactionPointerStorage> getCapacities() {
        return regularCapacities;
    }

    @Override
    public void setCapacities(HashMap<Integer, @SerializeNullable TransactionPointerStorage> capacities) {
        this.regularCapacities = capacities;
    }

    private static boolean linkedEquals(HashMap<Integer, TransactionPointerStorage> left, HashMap<Integer, TransactionPointerStorage> right) {
        if (left.size() != right.size())
            return false;

        for (int i = 0; i < left.size(); i++) {
            Iterator<Map.Entry<Integer, TransactionPointerStorage>> leftItr = left.entrySet().iterator();
            Iterator<Map.Entry<Integer, TransactionPointerStorage>> rightItr = right.entrySet().iterator();

            while (leftItr.hasNext() && rightItr.hasNext()) {
                Map.Entry<Integer, TransactionPointerStorage> leftEntry = leftItr.next();
                Map.Entry<Integer, TransactionPointerStorage> rightEntry = rightItr.next();
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
        PatriciaTreeRegularTransaction that = (PatriciaTreeRegularTransaction) o;
        return linkedEquals(regularCapacities, that.regularCapacities);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(regularCapacities);
    }

    @Override
    public String toString() {
        return "PatriciaTreeRegularTransaction{" +
                "regularCapacities=" + regularCapacities +
                '}';
    }
}
