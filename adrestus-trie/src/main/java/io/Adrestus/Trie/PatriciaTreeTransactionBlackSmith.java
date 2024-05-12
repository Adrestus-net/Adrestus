package io.Adrestus.Trie;

import io.activej.serializer.annotations.SerializeNullable;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

public interface PatriciaTreeTransactionBlackSmith extends Serializable {
    void addTransactionPosition(PatriciaTreeTransactionType type,String hash, int origin_zone, int height, int position);

    List<StorageInfo> retrieveTransactionInfoByHash(PatriciaTreeTransactionType type,String hash);

    HashMap<Integer, HashSet<Integer>> retrieveAllTransactionsByOriginZone(PatriciaTreeTransactionType type,int zone);


    Optional<StorageInfo> findLatestStorageInfo(PatriciaTreeTransactionType type,int zone);

    HashMap<Integer, @SerializeNullable TransactionPointerStorage> getCapacities(PatriciaTreeTransactionType type);
}
