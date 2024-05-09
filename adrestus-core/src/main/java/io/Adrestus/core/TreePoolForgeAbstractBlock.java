package io.Adrestus.core;

import io.Adrestus.IMemoryTreePool;

import java.util.*;
import java.util.stream.Collectors;

public class TreePoolForgeAbstractBlock implements ITreePoolBlacksmith {
    private static final Map<TransactionType, TransactionTreePoolEntries> map;
    private static volatile TreePoolForgeAbstractBlock instance;

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
                case UNCLAIMED_FEE_REWARD:
                    map.put(type, new UnclaimedFeeRewardTransactionTreePoolEntry());
                    break;
            }
        });
    }

    private TreePoolForgeAbstractBlock() {
        if (instance != null) {
            throw new IllegalStateException("Already initialized.");
        }
    }

    public static TreePoolForgeAbstractBlock getInstance() {
        var result = instance;
        if (result == null) {
            synchronized (TreePoolForgeAbstractBlock.class) {
                result = instance;
                if (result == null) {
                    result = new TreePoolForgeAbstractBlock();
                    instance = result;
                }
            }
        }
        return result;
    }

    @Override
    public void visitTreePool(TransactionBlock transactionBlock, IMemoryTreePool memoryTreePool) {
        Map<TransactionType, List<Transaction>> typedlistGrouped = transactionBlock.getTransactionList().stream().collect(Collectors.groupingBy(Transaction::getType));
        typedlistGrouped.keySet().forEach(type -> {
            map.get(type).SetArrayList((ArrayList) typedlistGrouped.get(type));
            map.get(type).ForgeEntriesBuilder(memoryTreePool);
        });
    }
}
