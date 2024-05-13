package io.Adrestus.Trie;

import io.activej.serializer.annotations.Serialize;
import io.activej.serializer.annotations.SerializeNullable;

import java.util.*;

public class PatriciaTreeUnDelegateTransaction implements PatriciaTreeTransactionMethods {
    private HashMap<Integer, @SerializeNullable TransactionPointerStorage> undelegateCapacities;

    public PatriciaTreeUnDelegateTransaction() {
        this.undelegateCapacities = new HashMap<Integer,TransactionPointerStorage>();
    }

    @Override
    public void addTransactionPosition(String hash, int origin_zone, int height, int position) {
        TransactionPointerStorage pointerStorage;
        if (this.undelegateCapacities.containsKey(origin_zone)) {
            pointerStorage = this.undelegateCapacities.get(origin_zone);
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
        this.undelegateCapacities.put(origin_zone, pointerStorage);
    }

    @Override
    public List<StorageInfo> retrieveTransactionInfoByHash(String hash) {
        LinkedHashSet<StorageInfo> result = new LinkedHashSet<>();
        if (this.undelegateCapacities.isEmpty())
            return new ArrayList<>(result);

        for (Map.Entry<Integer,TransactionPointerStorage> entry : this.undelegateCapacities.entrySet()) {
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
        if (undelegateCapacities.isEmpty())
            return result;

        if (!undelegateCapacities.containsKey(zone))
            return result;
        return undelegateCapacities.get(zone).getPositions();
    }

    @Override
    public Optional<StorageInfo> findLatestStorageInfo(int zone) {
        if (undelegateCapacities.isEmpty())
            return Optional.empty();
        int max = -1;
        for (Map.Entry<Integer,TransactionPointerStorage> entry : undelegateCapacities.entrySet()) {
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

        return Optional.of(new StorageInfo(max,undelegateCapacities.get(zone).getPositions().get(max)));
    }

    @Serialize
    @Override
    public HashMap<Integer, @SerializeNullable TransactionPointerStorage> getCapacities() {
        return undelegateCapacities;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PatriciaTreeUnDelegateTransaction that = (PatriciaTreeUnDelegateTransaction) o;
        return linkedEquals(undelegateCapacities, that.undelegateCapacities);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(undelegateCapacities);
    }

    @Override
    public String toString() {
        return "PatriciaTreeRewardsTransaction{" +
                "undelegateCapacities=" + undelegateCapacities +
                '}';
    }
}
