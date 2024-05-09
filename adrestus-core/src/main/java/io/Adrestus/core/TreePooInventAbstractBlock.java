package io.Adrestus.core;

import io.Adrestus.IMemoryTreePool;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TreePooInventAbstractBlock implements ITreePoolBlacksmith {
    private static final Map<TransactionType, TransactionTreePoolEntries> map;
    private static volatile TreePooInventAbstractBlock instance;

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

    private TreePooInventAbstractBlock() {
        if (instance != null) {
            throw new IllegalStateException("Already initialized.");
        }
    }

    public static TreePooInventAbstractBlock getInstance() {
        var result = instance;
        if (result == null) {
            synchronized (TreePooInventAbstractBlock.class) {
                result = instance;
                if (result == null) {
                    result = new TreePooInventAbstractBlock();
                    instance = result;
                }
            }
        }
        return result;
    }

    @Override
    public void visitTreePool(TransactionBlock transactionBlock, IMemoryTreePool memoryTreePool) {
        map.keySet().forEach(type -> {
            map.get(type).InventEntriesBuilder(memoryTreePool,transactionBlock.getHeight());
            map.get(type).Clear();
        });
    }
}
