package io.Adrestus.Trie;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

public interface PatriciaTreeMethods {


    void addTransactionPosition(String hash, int origin_zone, int height, int position);

    void addReceiptPosition(String hash, int origin_zone, int height, int blockheight, int position, int zone);

    List<StorageInfo> retrieveTransactionInfoByHash(String hash);

    HashMap<Integer, HashSet<Integer>> retrieveAllTransactionsByOriginZone(int zone);

    List<StorageInfo> retrieveReceiptInfoByHash(String hash);

    Optional<StorageInfo> findLatestStorageInfo(int zone);


}
