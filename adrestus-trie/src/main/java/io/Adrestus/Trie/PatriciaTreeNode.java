package io.Adrestus.Trie;

import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;
import io.activej.serializer.annotations.SerializeNullable;

import java.io.PipedReader;
import java.io.Serializable;
import java.util.*;

public class PatriciaTreeNode implements PatriciaTreeTransactionBlackSmith, PatriciaTreeReceiptBlackSmith, Serializable, Cloneable {

    private double amount;
    private double staking_amount;
    private double unclaimed_reward;
    private int nonce;
    private StakingInfo stakingInfo;
    private final Map<PatriciaTreeTransactionType, PatriciaTreeTransactionMethods> transactionsMap;
    private final PatriciaTreeReceiptMethods patriciaTreeReceiptMethods;

    public PatriciaTreeNode(double amount, int nonce) {
        this.amount = amount;
        this.nonce = nonce;
        this.staking_amount=0;
        this.unclaimed_reward = 0;
        this.stakingInfo=new StakingInfo();
        this.patriciaTreeReceiptMethods=new PatriciaTreeReceipts();
        this.transactionsMap = new EnumMap<>(PatriciaTreeTransactionType.class);
        this.Init();
    }

    public PatriciaTreeNode(@Deserialize("amount") double amount, @Deserialize("nonce") int nonce, @Deserialize("staking_amount") double staking_amount) {
        this.amount = amount;
        this.nonce = nonce;
        this.staking_amount = staking_amount;
        this.unclaimed_reward = 0;
        this.stakingInfo=new StakingInfo();
        this.patriciaTreeReceiptMethods=new PatriciaTreeReceipts();
        this.transactionsMap = new EnumMap<>(PatriciaTreeTransactionType.class);
        this.Init();
    }

    public PatriciaTreeNode(@Deserialize("amount") double amount, @Deserialize("nonce") int nonce, @Deserialize("staking_amount") double staking_amount, @Deserialize("unclaimed_reward") double unclaimed_reward) {
        this.amount = amount;
        this.nonce = nonce;
        this.staking_amount = staking_amount;
        this.unclaimed_reward = unclaimed_reward;
        this.stakingInfo=new StakingInfo();
        this.patriciaTreeReceiptMethods=new PatriciaTreeReceipts();
        this.transactionsMap = new EnumMap<>(PatriciaTreeTransactionType.class);
        this.Init();
    }

    public PatriciaTreeNode(double amount) {
        this.amount = amount;
        this.nonce = 0;
        this.staking_amount=0;
        this.unclaimed_reward = 0;
        this.stakingInfo=new StakingInfo();
        this.patriciaTreeReceiptMethods=new PatriciaTreeReceipts();
        this.transactionsMap = new EnumMap<>(PatriciaTreeTransactionType.class);
        this.Init();
    }

    public PatriciaTreeNode() {
        this.amount =0;
        this.nonce = 0;
        this.staking_amount=0;
        this.unclaimed_reward = 0;
        this.stakingInfo=new StakingInfo();
        this.patriciaTreeReceiptMethods=new PatriciaTreeReceipts();
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
                case REWARDS:
                    this.transactionsMap.put(type, new PatriciaTreeRewardsTransaction());
                    break;
            }
        });
    }

    @Override
    public void addReceiptPosition(String hash, int origin_zone, int height, int blockheight, int position, int zone) {
       this.patriciaTreeReceiptMethods.addReceiptPosition(hash, origin_zone, height, blockheight, position, zone);
    }

    @Override
    public List<StorageInfo> retrieveReceiptInfoByHash(String hash) {
        return  this.patriciaTreeReceiptMethods.retrieveReceiptInfoByHash(hash);
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
    public HashMap<Integer, @SerializeNullable TransactionPointerStorage> getCapacities(PatriciaTreeTransactionType type){
        return this.transactionsMap.get(type).getCapacities();
    }
    @Override
    public HashMap<Integer, @SerializeNullable ReceiptPointerStorage> getCapacities() {
        return this.patriciaTreeReceiptMethods.getCapacities();
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
    public Map<PatriciaTreeTransactionType, PatriciaTreeTransactionMethods> getTransactionsMap() {
        return transactionsMap;
    }

    @Serialize
    public PatriciaTreeReceiptMethods getPatriciaTreeReceiptMethods() {
        return patriciaTreeReceiptMethods;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PatriciaTreeNode that = (PatriciaTreeNode) o;
        return Double.compare(amount, that.amount) == 0 && Double.compare(staking_amount, that.staking_amount) == 0 && Double.compare(unclaimed_reward, that.unclaimed_reward) == 0 && nonce == that.nonce && Objects.equals(stakingInfo, that.stakingInfo) && Objects.equals(transactionsMap, that.transactionsMap) && Objects.equals(patriciaTreeReceiptMethods, that.patriciaTreeReceiptMethods);
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
