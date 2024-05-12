package io.Adrestus.Trie;

import io.activej.serializer.annotations.SerializeNullable;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

public interface PatriciaTreeTransactionMethods extends Serializable {
    void addTransactionPosition(String hash, int origin_zone, int height, int position);

    List<StorageInfo> retrieveTransactionInfoByHash(String hash);

    HashMap<Integer, HashSet<Integer>> retrieveAllTransactionsByOriginZone(int zone);


    Optional<StorageInfo> findLatestStorageInfo(int zone);

    HashMap<Integer, @SerializeNullable TransactionPointerStorage> getCapacities();
}
