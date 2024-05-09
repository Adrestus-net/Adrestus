package io.Adrestus.core;

import io.Adrestus.IMemoryTreePool;

public interface ITreePoolBlacksmith {
    void visitTreePool(TransactionBlock transactionBlock, IMemoryTreePool memoryTreePool);
}