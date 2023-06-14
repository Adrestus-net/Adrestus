package io.Adrestus.rpc;

import io.distributedLedger.LevelDBTransactionWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface IService<T> {
    List<T> download(String hash) throws Exception;

    List<T> downloadPatriciaTree(String hash) throws Exception;

    List<T> migrateBlock(ArrayList<String> list_hash) throws Exception;

    Map<String, LevelDBTransactionWrapper<T>> downloadTransactionDatabase(String hash) throws Exception;
}
