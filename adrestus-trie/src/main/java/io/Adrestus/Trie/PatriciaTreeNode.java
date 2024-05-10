package io.Adrestus.Trie;

import io.Adrestus.bloom_filter.BloomFilter;
import io.Adrestus.bloom_filter.Util.UtilConstants;
import io.Adrestus.bloom_filter.impl.InMemoryBloomFilter;
import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;
import io.activej.serializer.annotations.SerializeNullable;

import java.io.Serializable;
import java.util.*;

public class PatriciaTreeNode implements PatriciaTreeMethods, Serializable, Cloneable {

    private double amount;
    private double staking_amount;
    private double unclaimed_reward;
    private int nonce;

    private HashMap<Integer, @SerializeNullable TransactionPointerStorage> transactionCapacities;
    private HashMap<Integer, @SerializeNullable ReceiptPointerStorage> receiptCapacities;

    public PatriciaTreeNode(double amount, int nonce) {
        this.amount = amount;
        this.nonce = nonce;
        this.unclaimed_reward = 0;
        this.transactionCapacities = new HashMap<Integer, TransactionPointerStorage>();
        this.receiptCapacities = new HashMap<Integer, ReceiptPointerStorage>();
    }

    public PatriciaTreeNode(@Deserialize("amount") double amount, @Deserialize("nonce") int nonce, @Deserialize("staking_amount") double staking_amount) {
        this.amount = amount;
        this.nonce = nonce;
        this.staking_amount = staking_amount;
        this.unclaimed_reward = 0;
        this.transactionCapacities = new HashMap<Integer, TransactionPointerStorage>();
        this.receiptCapacities = new HashMap<Integer, ReceiptPointerStorage>();
    }

    public PatriciaTreeNode(@Deserialize("amount") double amount, @Deserialize("nonce") int nonce, @Deserialize("staking_amount") double staking_amount, @Deserialize("unclaimed_reward") double unclaimed_reward) {
        this.amount = amount;
        this.nonce = nonce;
        this.staking_amount = staking_amount;
        this.unclaimed_reward = unclaimed_reward;
        this.transactionCapacities = new HashMap<Integer, TransactionPointerStorage>();
        this.receiptCapacities = new HashMap<Integer, ReceiptPointerStorage>();
    }

    public PatriciaTreeNode(double amount) {
        this.amount = amount;
    }

    public PatriciaTreeNode() {
    }

    @Serialize
    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    @Serialize
    public int getNonce() {
        return nonce;
    }

    public void setNonce(int nonce) {
        this.nonce = nonce;
    }

    @Serialize
    public double getStaking_amount() {
        return staking_amount;
    }

    public void setStaking_amount(double staking_amount) {
        this.staking_amount = staking_amount;
    }

    @Serialize
    public double getUnclaimed_reward() {
        return unclaimed_reward;
    }

    public void setUnclaimed_reward(double unclaimed_reward) {
        this.unclaimed_reward = unclaimed_reward;
    }

    @Serialize
    public HashMap<Integer, TransactionPointerStorage> getTransactionCapacities() {
        return transactionCapacities;
    }

    public void setTransactionCapacities(HashMap<Integer, TransactionPointerStorage> transactionCapacities) {
        this.transactionCapacities = transactionCapacities;
    }

    @Serialize
    public HashMap<Integer, ReceiptPointerStorage> getReceiptCapacities() {
        return receiptCapacities;
    }

    public void setReceiptCapacities(HashMap<Integer, ReceiptPointerStorage> receiptCapacities) {
        this.receiptCapacities = receiptCapacities;
    }


    public static boolean linkedEquals(HashMap<Integer, TransactionPointerStorage> left, HashMap<Integer, TransactionPointerStorage> right) {
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

    public static boolean linkedEquals2(HashMap<Integer, ReceiptPointerStorage> left, HashMap<Integer, ReceiptPointerStorage> right) {
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
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        PatriciaTreeNode that = (PatriciaTreeNode) object;
        return Double.compare(unclaimed_reward, that.unclaimed_reward) == 0 && Double.compare(amount, that.amount) == 0 && Double.compare(staking_amount, that.staking_amount) == 0 && nonce == that.nonce && linkedEquals(transactionCapacities, that.transactionCapacities) && linkedEquals2(receiptCapacities, that.receiptCapacities);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, unclaimed_reward, staking_amount, nonce, transactionCapacities, receiptCapacities);
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }


    @Override
    public String toString() {
        return "PatriciaTreeNode{" +
                "amount=" + amount +
                ", staking_amount=" + staking_amount +
                ", unclaimed_reward=" + unclaimed_reward +
                ", nonce=" + nonce +
                ", transactionCapacities=" + transactionCapacities +
                ", receiptCapacities=" + receiptCapacities +
                '}';
    }

    @Override
    public void addTransactionPosition(String hash, int origin_zone, int height, int position) {
        TransactionPointerStorage pointerStorage;
        if (this.transactionCapacities.containsKey(origin_zone)) {
            pointerStorage = this.transactionCapacities.get(origin_zone);
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
        this.transactionCapacities.put(origin_zone, pointerStorage);
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
    public List<StorageInfo> retrieveTransactionInfoByHash(String hash) {

        LinkedHashSet<StorageInfo> result = new LinkedHashSet<>();
        if (transactionCapacities.isEmpty())
            return new ArrayList<>(result);

        for (Map.Entry<Integer, TransactionPointerStorage> entry : transactionCapacities.entrySet()) {
            TransactionPointerStorage pointerStorage = entry.getValue();
            pointerStorage.getPositions().forEach((blockheight, value) -> value.forEach(pos -> {
                if (pointerStorage.filter.contains(String.join("", hash, String.valueOf(entry.getKey()), String.valueOf(blockheight), String.valueOf(pos)))) {
                    result.add(new StorageInfo(entry.getKey(), blockheight, pos));
                }
            }));

        }
        return new ArrayList<>(result);
    }

    @Override
    public HashMap<Integer, HashSet<Integer>> retrieveAllTransactionsByOriginZone(int zone) {
        HashMap<Integer, HashSet<Integer>> result = new HashMap<Integer, HashSet<Integer>>();
        if (transactionCapacities.isEmpty())
            return result;

        if (!transactionCapacities.containsKey(zone))
            return result;
        return transactionCapacities.get(zone).getPositions();

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
                        if (pointerStorage.filter.contains(String.join("", hash, String.valueOf(entry.getKey()), String.valueOf(blockHeight), String.valueOf(zonekey), String.valueOf(receiptheight), String.valueOf(pos)))) {
                            result.add(new StorageInfo(entry.getKey(), blockHeight, zonekey, receiptheight, pos));
                        }
                    }))));

        }
        return new ArrayList<>(result);
    }

    @Override
    public Optional<StorageInfo> findLatestStorageInfo(int zone) {
        if (transactionCapacities.isEmpty())
            return Optional.empty();
        int max = -1;
        for (Map.Entry<Integer, TransactionPointerStorage> entry : transactionCapacities.entrySet()) {
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

        return Optional.of(new StorageInfo(max, transactionCapacities.get(zone).getPositions().get(max)));
    }


    public static final class TransactionPointerStorage implements Serializable {

        private HashMap<Integer, HashSet<Integer>> positions;
        private BloomFilter<String> filter;

        public TransactionPointerStorage() {
            this.positions = new HashMap<Integer, HashSet<Integer>>();
            this.filter = new InMemoryBloomFilter<String>(10 * UtilConstants.MAX, UtilConstants.FPP);
        }

        public TransactionPointerStorage(HashMap<Integer, HashSet<Integer>> positions, BloomFilter<String> filter) {
            this.positions = positions;
            this.filter = filter;
        }


        @Serialize
        public HashMap<Integer, HashSet<Integer>> getPositions() {
            return positions;
        }

        public void setPositions(HashMap<Integer, HashSet<Integer>> positions) {
            this.positions = positions;
        }

        @Serialize
        public BloomFilter<String> getFilter() {
            return filter;
        }


        public void setFilter(BloomFilter<String> filter) {
            this.filter = filter;
        }


        @Override
        public boolean equals(Object object) {
            if (this == object) return true;
            if (object == null || getClass() != object.getClass()) return false;
            TransactionPointerStorage that = (TransactionPointerStorage) object;
            return Objects.equals(positions, that.positions) && Objects.equals(filter, that.filter);
        }

        @Override
        public int hashCode() {
            return Objects.hash(positions, filter);
        }
    }

    public static final class ReceiptPointerStorage implements Serializable {

        private HashMap<Integer, HashMap<Integer, HashMap<Integer, HashSet<Integer>>>> positions;
        private BloomFilter<String> filter;

        public ReceiptPointerStorage() {
            this.positions = new HashMap<Integer, HashMap<Integer, HashMap<Integer, HashSet<Integer>>>>();
            this.filter = new InMemoryBloomFilter<String>(10 * UtilConstants.MAX, UtilConstants.FPP);
        }

        public ReceiptPointerStorage(HashMap<Integer, HashMap<Integer, HashMap<Integer, HashSet<Integer>>>> positions, BloomFilter<String> filter) {
            this.positions = positions;
            this.filter = filter;
        }

        @Serialize
        public HashMap<Integer, HashMap<Integer, HashMap<Integer, HashSet<Integer>>>> getPositions() {
            return positions;
        }

        @Serialize
        public BloomFilter<String> getFilter() {
            return filter;
        }


        public void setPositions(HashMap<Integer, HashMap<Integer, HashMap<Integer, HashSet<Integer>>>> positions) {
            this.positions = positions;
        }

        public void setFilter(BloomFilter<String> filter) {
            this.filter = filter;
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) return true;
            if (object == null || getClass() != object.getClass()) return false;
            ReceiptPointerStorage that = (ReceiptPointerStorage) object;
            return Objects.equals(positions, that.positions) && Objects.equals(filter, that.filter);
        }

        @Override
        public int hashCode() {
            return Objects.hash(positions, filter);
        }

        @Override
        public String toString() {
            return "ReceiptPointerStorage{" +
                    "positions=" + positions +
                    ", filter=" + filter +
                    '}';
        }
    }
}
