package io.Adrestus.Trie;

import io.activej.serializer.annotations.SerializeNullable;

import java.util.HashMap;
import java.util.List;

public interface PatriciaTreeReceiptBlackSmith {

    void addReceiptPosition(String hash, int origin_zone, int height, int blockheight, int position, int zone);

    List<StorageInfo> retrieveReceiptInfoByHash(String hash);

    HashMap<Integer, @SerializeNullable ReceiptPointerStorage> getCapacities();
}
