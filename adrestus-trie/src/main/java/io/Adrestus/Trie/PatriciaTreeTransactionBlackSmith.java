package io.Adrestus.Trie;

import io.activej.serializer.annotations.Serialize;
import io.activej.serializer.annotations.SerializeNullable;

import java.io.Serializable;
import java.util.*;

public interface PatriciaTreeTransactionBlackSmith extends Serializable {
    void addTransactionPosition(PatriciaTreeTransactionType type, String hash, int origin_zone, int height, int position);

    List<StorageInfo> retrieveTransactionInfoByHash(PatriciaTreeTransactionType type, String hash);

    HashMap<Integer, HashSet<Integer>> retrieveAllTransactionsByOriginZone(PatriciaTreeTransactionType type, int zone);


    Optional<StorageInfo> findLatestStorageInfo(PatriciaTreeTransactionType type, int zone);

    HashMap<Integer, @SerializeNullable TransactionPointerStorage> getTransactionCapacities(PatriciaTreeTransactionType type);

    void setTransactionCapacities(PatriciaTreeTransactionType type, HashMap<Integer, @SerializeNullable TransactionPointerStorage> capacities);

    @Serialize
    Map<PatriciaTreeTransactionType, PatriciaTreeTransactionMethods> getTransactionsMap();

    void setTransactionsMap(Map<PatriciaTreeTransactionType, PatriciaTreeTransactionMethods> transactionsMap);
}
