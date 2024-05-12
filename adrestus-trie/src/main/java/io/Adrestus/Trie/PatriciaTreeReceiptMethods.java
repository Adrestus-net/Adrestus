package io.Adrestus.Trie;

import io.activej.serializer.annotations.SerializeNullable;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

public interface PatriciaTreeReceiptMethods extends Serializable {

    void addReceiptPosition(String hash, int origin_zone, int blockHeight, int zone, int receiptBlockHeight, int position);

    List<StorageInfo> retrieveReceiptInfoByHash(String hash);

    HashMap<Integer, @SerializeNullable ReceiptPointerStorage> getCapacities();
}
