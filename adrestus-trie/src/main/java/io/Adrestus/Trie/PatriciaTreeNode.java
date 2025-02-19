package io.Adrestus.Trie;

import io.activej.serializer.annotations.Serialize;
import io.activej.serializer.annotations.SerializeNullable;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;

public class PatriciaTreeNode implements PatriciaTreeTransactionBlackSmith, PatriciaTreeReceiptBlackSmith, Serializable, Cloneable {

    private BigDecimal amount;
    private BigDecimal staking_amount;
    private BigDecimal private_staking_amount;
    private BigDecimal unclaimed_reward;
    private int nonce;
    private StakingInfo stakingInfo;
    private Map<PatriciaTreeTransactionType, @SerializeNullable PatriciaTreeTransactionMethods> transactionsMap;
    private PatriciaTreeReceiptMethods patriciaTreeReceiptMethods;
    private Map<String, BigDecimal> delegation;


    public PatriciaTreeNode(BigDecimal amount, int nonce) {
        this.amount = amount;
        this.nonce = nonce;
        this.staking_amount = BigDecimal.ZERO;
        this.private_staking_amount = BigDecimal.ZERO;
        this.unclaimed_reward = BigDecimal.ZERO;
        this.stakingInfo = new StakingInfo();
        this.patriciaTreeReceiptMethods = new PatriciaTreeReceipts();
        this.transactionsMap = new EnumMap<>(PatriciaTreeTransactionType.class);
        this.delegation = new HashMap<>();
        this.Init();
    }


    public PatriciaTreeNode(BigDecimal amount, int nonce, BigDecimal staking_amount) {
        this.amount = amount;
        this.nonce = nonce;
        this.staking_amount = staking_amount;
        this.unclaimed_reward = BigDecimal.ZERO;
        this.private_staking_amount = BigDecimal.ZERO;
        this.stakingInfo = new StakingInfo();
        this.patriciaTreeReceiptMethods = new PatriciaTreeReceipts();
        this.transactionsMap = new EnumMap<>(PatriciaTreeTransactionType.class);
        this.delegation = new HashMap<>();
        this.Init();
    }


    public PatriciaTreeNode(BigDecimal amount, int nonce, BigDecimal staking_amount, BigDecimal private_staking_amount, BigDecimal unclaimed_reward) {
        this.amount = amount;
        this.nonce = nonce;
        this.staking_amount = staking_amount;
        this.private_staking_amount = private_staking_amount;
        this.unclaimed_reward = unclaimed_reward;
        this.stakingInfo = new StakingInfo();
        this.patriciaTreeReceiptMethods = new PatriciaTreeReceipts();
        this.transactionsMap = new EnumMap<>(PatriciaTreeTransactionType.class);
        this.delegation = new HashMap<>();
        this.Init();
    }


    public PatriciaTreeNode(BigDecimal amount, int nonce, BigDecimal staking_amount, BigDecimal private_staking_amount) {
        this.amount = amount;
        this.nonce = nonce;
        this.staking_amount = staking_amount;
        this.private_staking_amount = private_staking_amount;
        this.unclaimed_reward = BigDecimal.ZERO;
        this.stakingInfo = new StakingInfo();
        this.patriciaTreeReceiptMethods = new PatriciaTreeReceipts();
        this.transactionsMap = new EnumMap<>(PatriciaTreeTransactionType.class);
        this.delegation = new HashMap<>();
        this.Init();
    }


    public PatriciaTreeNode(BigDecimal amount) {
        this.amount = amount;
        this.nonce = 0;
        this.staking_amount = BigDecimal.ZERO;
        this.unclaimed_reward = BigDecimal.ZERO;
        this.private_staking_amount = BigDecimal.ZERO;
        this.stakingInfo = new StakingInfo();
        this.patriciaTreeReceiptMethods = new PatriciaTreeReceipts();
        this.transactionsMap = new EnumMap<>(PatriciaTreeTransactionType.class);
        this.delegation = new HashMap<>();
        this.Init();
    }

    public PatriciaTreeNode() {
        this.amount = BigDecimal.ZERO;
        this.nonce = 0;
        this.staking_amount = BigDecimal.ZERO;
        this.unclaimed_reward = BigDecimal.ZERO;
        this.private_staking_amount = BigDecimal.ZERO;
        this.stakingInfo = new StakingInfo();
        this.patriciaTreeReceiptMethods = new PatriciaTreeReceipts();
        this.transactionsMap = new EnumMap<>(PatriciaTreeTransactionType.class);
        this.delegation = new HashMap<>();
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
    public int getNonce() {
        return nonce;
    }

    public void setNonce(int nonce) {
        this.nonce = nonce;
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

    public PatriciaTreeReceiptMethods getPatriciaTreeReceiptMethods() {
        return patriciaTreeReceiptMethods;
    }

    public void setPatriciaTreeReceiptMethods(PatriciaTreeReceiptMethods patriciaTreeReceiptMethods) {
        this.patriciaTreeReceiptMethods = patriciaTreeReceiptMethods;
    }


    @Serialize
    public Map<String, BigDecimal> getDelegation() {
        return delegation;
    }

    public void setDelegation(Map<String, BigDecimal> delegation) {
        this.delegation = delegation;
    }

    @Serialize
    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    @Serialize
    public BigDecimal getUnclaimed_reward() {
        return unclaimed_reward;
    }

    public void setUnclaimed_reward(BigDecimal unclaimed_reward) {
        this.unclaimed_reward = unclaimed_reward;
    }

    @Serialize
    public BigDecimal getPrivate_staking_amount() {
        return private_staking_amount;
    }

    public void setPrivate_staking_amount(BigDecimal private_staking_amount) {
        this.private_staking_amount = private_staking_amount;
    }

    @Serialize
    public BigDecimal getStaking_amount() {
        return staking_amount;
    }

    public void setStaking_amount(BigDecimal staking_amount) {
        this.staking_amount = staking_amount;
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            // This should never happen because we are Cloneable
            throw new AssertionError(e);
        }
    }


    @Serialize
    public StakingInfo getStakingInfo() {
        return stakingInfo;
    }

    public void setStakingInfo(StakingInfo stakingInfo) {
        this.stakingInfo = stakingInfo;
    }


    private static boolean MapAreEqual(Map<String, BigDecimal> first, Map<String, BigDecimal> second) {
        if (first.size() != second.size()) {
            return false;
        }

        return first.entrySet().stream()
                .allMatch(e -> e.getValue().equals(second.get(e.getKey())));
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
        return nonce == that.nonce && Objects.equals(amount, that.amount) && Objects.equals(staking_amount, that.staking_amount) && Objects.equals(private_staking_amount, that.private_staking_amount) && Objects.equals(unclaimed_reward, that.unclaimed_reward) && Objects.equals(stakingInfo, that.stakingInfo) && linkedEquals(transactionsMap, that.transactionsMap) && Objects.equals(patriciaTreeReceiptMethods, that.patriciaTreeReceiptMethods) && MapAreEqual(delegation, that.delegation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, staking_amount, private_staking_amount, unclaimed_reward, nonce, stakingInfo, transactionsMap, patriciaTreeReceiptMethods, delegation);
    }

    @Override
    public String toString() {
        return "PatriciaTreeNode{" +
                "amount=" + amount +
                ", staking_amount=" + staking_amount +
                ", private_staking_amount=" + private_staking_amount +
                ", unclaimed_reward=" + unclaimed_reward +
                ", nonce=" + nonce +
                ", stakingInfo=" + stakingInfo +
                ", transactionsMap=" + transactionsMap +
                ", patriciaTreeReceiptMethods=" + patriciaTreeReceiptMethods +
                ", delegation=" + delegation +
                '}';
    }
}

