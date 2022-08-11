package io.Adrestus.core.Resourses;

import com.google.common.base.Objects;
import io.Adrestus.core.CommitteeBlock;
import io.Adrestus.core.TransactionBlock;

public class CachedBlocks {
    private static volatile CachedBlocks instance;
    private CommitteeBlock committeeBlock;
    private TransactionBlock transactionBlock;

    private CachedBlocks() {
        if (instance != null) {
            throw new IllegalStateException("Already initialized.");
        } else {
            this.committeeBlock = new CommitteeBlock();
            this.transactionBlock = new TransactionBlock();
        }
    }

    public static CachedBlocks getInstance() {
        var result = instance;
        if (result == null) {
            synchronized (CachedBlocks.class) {
                result = instance;
                if (result == null) {
                    instance = result = new CachedBlocks();
                }
            }
        }
        return result;
    }

    public CommitteeBlock getCommitteeBlock() {
        return committeeBlock;
    }

    public void setCommitteeBlock(CommitteeBlock committeeBlock) {
        this.committeeBlock = committeeBlock;
    }

    public TransactionBlock getTransactionBlock() {
        return transactionBlock;
    }

    public void setTransactionBlock(TransactionBlock transactionBlock) {
        this.transactionBlock = transactionBlock;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CachedBlocks that = (CachedBlocks) o;
        return Objects.equal(committeeBlock, that.committeeBlock) && Objects.equal(transactionBlock, that.transactionBlock);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(committeeBlock, transactionBlock);
    }

    @Override
    public String toString() {
        return "CachedBlocks{" +
                "committeeBlock=" + committeeBlock +
                ", transactionBlock=" + transactionBlock +
                '}';
    }
}
