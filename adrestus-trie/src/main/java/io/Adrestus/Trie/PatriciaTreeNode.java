package io.Adrestus.Trie;

import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;
import io.activej.serializer.annotations.SerializeNullable;

import java.io.Serializable;
import java.util.*;

public class PatriciaTreeNode implements PatriciaTreeTransactionBlackSmith, PatriciaTreeReceiptBlackSmith, Serializable, Cloneable {

    private double amount;
    private double staking_amount;
    private double unclaimed_reward;
    private int nonce;
    private StakingInfo stakingInfo;
    private Map<PatriciaTreeTransactionType, @SerializeNullable PatriciaTreeTransactionMethods> transactionsMap;
    private PatriciaTreeReceiptMethods patriciaTreeReceiptMethods;

    public PatriciaTreeNode(double amount, int nonce) {
        this.amount = amount;
        this.nonce = nonce;
        this.staking_amount = 0;
        this.unclaimed_reward = 0;
        this.stakingInfo = new StakingInfo();
        this.patriciaTreeReceiptMethods = new PatriciaTreeReceipts();
        this.transactionsMap = new EnumMap<>(PatriciaTreeTransactionType.class);
        this.Init();
    }

    public PatriciaTreeNode(double amount, int nonce, double staking_amount) {
        this.amount = amount;
        this.nonce = nonce;
        this.staking_amount = staking_amount;
        this.unclaimed_reward = 0;
        this.stakingInfo = new StakingInfo();
        this.patriciaTreeReceiptMethods = new PatriciaTreeReceipts();
        this.transactionsMap = new EnumMap<>(PatriciaTreeTransactionType.class);
        this.Init();
    }

    public PatriciaTreeNode(@Deserialize("amount") double amount, @Deserialize("nonce") int nonce, @Deserialize("staking_amount") double staking_amount, @Deserialize("unclaimed_reward") double unclaimed_reward) {
        this.amount = amount;
        this.nonce = nonce;
        this.staking_amount = staking_amount;
        this.unclaimed_reward = unclaimed_reward;
        this.stakingInfo = new StakingInfo();
        this.patriciaTreeReceiptMethods = new PatriciaTreeReceipts();
        this.transactionsMap = new EnumMap<>(PatriciaTreeTransactionType.class);
        this.Init();
    }

    public PatriciaTreeNode(double amount) {
        this.amount = amount;
        this.nonce = 0;
        this.staking_amount = 0;
        this.unclaimed_reward = 0;
        this.stakingInfo = new StakingInfo();
        this.patriciaTreeReceiptMethods = new PatriciaTreeReceipts();
        this.transactionsMap = new EnumMap<>(PatriciaTreeTransactionType.class);
        this.Init();
    }

    public PatriciaTreeNode() {
        this.amount = 0;
        this.nonce = 0;
        this.staking_amount = 0;
        this.unclaimed_reward = 0;
        this.stakingInfo = new StakingInfo();
        this.patriciaTreeReceiptMethods = new PatriciaTreeReceipts();
        this.transactionsMap = new EnumMap<>(PatriciaTreeTransactionType.class);
        this.Init();
    }

    private void Init() {
        Arrays.stream(PatriciaTreeTransactionType.values()).forEach(type -> {
            switch (type) {
                case REGULAR:
                    this.transactionsMap.put(type, new PatriciaTreeRegularTransaction());
                    break;
                case STAKING:
                    this.transactionsMap.put(type, new PatriciaTreeStakingTransaction());
                    break;
                case DELEGATE:
                    this.transactionsMap.put(type, new PatriciaTreeDelegateTransaction());
                    break;
                case UNDELEGATE:
                    this.transactionsMap.put(type, new PatriciaTreeUnDelegateTransaction());
                    break;
                case UNSTAKING:
                    this.transactionsMap.put(type, new PatriciaTreeUnstakingTransaction());
                    break;
                case REWARDS:
                    this.transactionsMap.put(type, new PatriciaTreeRewardsTransaction());
                    break;
            }
        });
    }

    @Override
    public void addReceiptPosition(String hash, int origin_zone, int blockHeight, int zone, int receiptBlockHeight, int position) {
        this.patriciaTreeReceiptMethods.addReceiptPosition(hash, origin_zone, blockHeight, zone, receiptBlockHeight, position);
    }

    @Override
    public List<StorageInfo> retrieveReceiptInfoByHash(String hash) {
        return this.patriciaTreeReceiptMethods.retrieveReceiptInfoByHash(hash);
    }

    @Override
    public void addTransactionPosition(PatriciaTreeTransactionType type, String hash, int origin_zone, int height, int position) {
        this.transactionsMap.get(type).addTransactionPosition(hash, origin_zone, height, position);
    }

    @Override
    public List<StorageInfo> retrieveTransactionInfoByHash(PatriciaTreeTransactionType type, String hash) {
        return this.transactionsMap.get(type).retrieveTransactionInfoByHash(hash);
    }

    @Override
    public HashMap<Integer, HashSet<Integer>> retrieveAllTransactionsByOriginZone(PatriciaTreeTransactionType type, int zone) {
        return this.transactionsMap.get(type).retrieveAllTransactionsByOriginZone(zone);
    }

    @Override
    public Optional<StorageInfo> findLatestStorageInfo(PatriciaTreeTransactionType type, int zone) {
        return this.transactionsMap.get(type).findLatestStorageInfo(zone);
    }

    @Override
    public HashMap<Integer, @SerializeNullable TransactionPointerStorage> getTransactionCapacities(PatriciaTreeTransactionType type) {
        return this.transactionsMap.get(type).getCapacities();
    }

    @Override
    public void setTransactionCapacities(PatriciaTreeTransactionType type, HashMap<Integer, @SerializeNullable TransactionPointerStorage> capacities) {
        this.transactionsMap.get(type).setCapacities(capacities);
    }


    @Override
    @Serialize
    public HashMap<Integer, @SerializeNullable ReceiptPointerStorage> getReceiptCapacities() {
        return this.patriciaTreeReceiptMethods.getReceiptCapacities();
    }

    @Override
    public void setReceiptCapacities(HashMap<Integer, @SerializeNullable ReceiptPointerStorage> capacities) {
        this.patriciaTreeReceiptMethods.SetReceiptCapacities(capacities);
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

    @Override
    @Serialize
    public Map<PatriciaTreeTransactionType, PatriciaTreeTransactionMethods> getTransactionsMap() {
        return transactionsMap;
    }


    @Override
    public void setTransactionsMap(Map<PatriciaTreeTransactionType, PatriciaTreeTransactionMethods> transactionsMap) {
        this.transactionsMap = transactionsMap;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }


    @Serialize
    public StakingInfo getStakingInfo() {
        return stakingInfo;
    }

    public void setStakingInfo(StakingInfo stakingInfo) {
        this.stakingInfo = stakingInfo;
    }

    private static boolean linkedEquals(Map<PatriciaTreeTransactionType, PatriciaTreeTransactionMethods> left, Map<PatriciaTreeTransactionType, PatriciaTreeTransactionMethods> right) {
        if (left.size() != right.size())
            return false;

        for (int i = 0; i < left.size(); i++) {
            Iterator<Map.Entry<PatriciaTreeTransactionType, PatriciaTreeTransactionMethods>> leftItr = left.entrySet().iterator();
            Iterator<Map.Entry<PatriciaTreeTransactionType, PatriciaTreeTransactionMethods>> rightItr = right.entrySet().iterator();

            while (leftItr.hasNext() && rightItr.hasNext()) {
                Map.Entry<PatriciaTreeTransactionType, PatriciaTreeTransactionMethods> leftEntry = leftItr.next();
                Map.Entry<PatriciaTreeTransactionType, PatriciaTreeTransactionMethods> rightEntry = rightItr.next();
                if (!leftEntry.getKey().equals(rightEntry.getKey()))
                    return false;
                Iterator<Map.Entry<Integer, @SerializeNullable TransactionPointerStorage>> leftmap = leftEntry.getValue().getCapacities().entrySet().iterator();
                Iterator<Map.Entry<Integer, @SerializeNullable TransactionPointerStorage>> rightmap = rightEntry.getValue().getCapacities().entrySet().iterator();

                while (rightmap.hasNext() && leftmap.hasNext()) {
                    Map.Entry<Integer, @SerializeNullable TransactionPointerStorage> leftside = leftmap.next();
                    Map.Entry<Integer, @SerializeNullable TransactionPointerStorage> rightside = rightmap.next();
                    if (!leftside.getKey().equals(rightside.getKey()))
                        return false;
                    if (!leftside.getValue().equals(rightside.getValue()))
                        return false;
                }
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
        PatriciaTreeNode that = (PatriciaTreeNode) o;
//        Stream.of(that.transactionsMap.keySet()).map()
        return Double.compare(amount, that.amount) == 0 && Double.compare(staking_amount, that.staking_amount) == 0 && Double.compare(unclaimed_reward, that.unclaimed_reward) == 0 && nonce == that.nonce && Objects.equals(stakingInfo, that.stakingInfo) && linkedEquals(transactionsMap, that.transactionsMap) && Objects.equals(patriciaTreeReceiptMethods, that.patriciaTreeReceiptMethods);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, staking_amount, unclaimed_reward, nonce, stakingInfo, transactionsMap, patriciaTreeReceiptMethods);
    }

    @Override
    public String toString() {
        return "PatriciaTreeNode{" +
                "amount=" + amount +
                ", staking_amount=" + staking_amount +
                ", unclaimed_reward=" + unclaimed_reward +
                ", nonce=" + nonce +
                ", stakingInfo=" + stakingInfo +
                ", transactionsMap=" + transactionsMap +
                ", patriciaTreeReceiptMethods=" + patriciaTreeReceiptMethods +
                '}';
    }
}
