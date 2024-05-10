package io.Adrestus.core;

import io.Adrestus.IMemoryTreePool;

public interface ITreePoolBlacksmith {
    void visitForgeTreePool(TransactionBlock transactionBlock, IMemoryTreePool memoryTreePool);

    void visitInventTreePool(TransactionBlock transactionBlock, IMemoryTreePool memoryTreePool);
}