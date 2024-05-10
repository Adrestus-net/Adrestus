package io.Adrestus.core;

import io.Adrestus.IMemoryTreePool;

import java.util.ArrayList;

public interface TransactionTreePoolEntries<T> {

    void ForgeEntriesBuilder(IMemoryTreePool memoryTreePool);

    void InventEntriesBuilder(IMemoryTreePool memoryTreePool, int blockHeight);

    void SetArrayList(ArrayList<T> transactionList);

    void Clear();

}
