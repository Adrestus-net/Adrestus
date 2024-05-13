package io.Adrestus.core;

import io.Adrestus.IMemoryTreePool;

import java.util.*;
import java.util.stream.Collectors;

public class TreePoolConstructBlock implements ITreePoolBlacksmith {
    private static final Map<TransactionType, TransactionTreePoolEntries> map;
    private static volatile TreePoolConstructBlock instance;
    private Map<TransactionType, List<Transaction>> typedlistGrouped;

    static {
        map = new EnumMap<>(TransactionType.class);
        Arrays.stream(TransactionType.values()).forEach(type -> {
            switch (type) {
                case REGULAR:
                    map.put(type, new RegularTransactionTreePoolEntry());
                    break;
                case REWARDS:
                    map.put(type, new RewardTransactionTreePoolEntry());
                    break;
                case STAKING:
                    map.put(type, new StakingTransactionTreePoolEntry());
                    break;
                case UNSTAKING:
                    map.put(type, new UnStakingTransactionTreePoolEntry());
                    break;
                case DELEGATE:
                    map.put(type, new DelegateTransactionTreePoolEntry());
                    break;
                case UNDELEGATE:
                    map.put(type, new UnDelegateTransactionTreePoolEntry());
                    break;
                case UNCLAIMED_FEE_REWARD:
                    map.put(type, new UnclaimedFeeRewardTransactionTreePoolEntry());
                    break;
            }
        });
    }

    private TreePoolConstructBlock() {
        if (instance != null) {
            throw new IllegalStateException("Already initialized.");
        }
    }

    public static TreePoolConstructBlock getInstance() {
        var result = instance;
        if (result == null) {
            synchronized (TreePoolConstructBlock.class) {
                result = instance;
                if (result == null) {
                    result = new TreePoolConstructBlock();
                    instance = result;
                }
            }
        }
        return result;
    }

    @Override
    public synchronized void visitForgeTreePool(TransactionBlock transactionBlock, IMemoryTreePool memoryTreePool) {
        typedlistGrouped = transactionBlock.getTransactionList().stream().collect(Collectors.groupingBy(Transaction::getType));
        typedlistGrouped.keySet().forEach(type -> {
            map.get(type).SetArrayList((ArrayList) typedlistGrouped.get(type));
            map.get(type).ForgeEntriesBuilder(memoryTreePool);
        });
    }

    @Override
    public synchronized void visitInventTreePool(TransactionBlock transactionBlock, IMemoryTreePool memoryTreePool) {
        typedlistGrouped.keySet().forEach(type -> {
            map.get(type).InventEntriesBuilder(memoryTreePool, transactionBlock.getHeight());
            map.get(type).Clear();
        });
        typedlistGrouped.clear();
    }
}
