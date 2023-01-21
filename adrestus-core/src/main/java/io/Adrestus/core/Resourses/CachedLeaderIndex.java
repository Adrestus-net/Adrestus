package io.Adrestus.core.Resourses;

public class CachedLeaderIndex {
    private static volatile CachedLeaderIndex instance;
    private int CommitteePositionLeader;
    private int TransactionPositionLeader;

    private CachedLeaderIndex() {
        if (instance != null) {
            throw new IllegalStateException("Already initialized.");
        }
    }

    public int getCommitteePositionLeader() {
        return CommitteePositionLeader;
    }

    public void setCommitteePositionLeader(int committeePositionLeader) {
        CommitteePositionLeader = committeePositionLeader;
    }


    public static void setInstance(CachedLeaderIndex instance) {
        CachedLeaderIndex.instance = instance;
    }

    public int getTransactionPositionLeader() {
        return TransactionPositionLeader;
    }

    public void setTransactionPositionLeader(int transactionPositionLeader) {
        TransactionPositionLeader = transactionPositionLeader;
    }

    public static CachedLeaderIndex getInstance() {
        var result = instance;
        if (result == null) {
            synchronized (CachedLeaderIndex.class) {
                result = instance;
                if (result == null) {
                    instance = result = new CachedLeaderIndex();
                }
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return "CachedLeaderIndex{" +
                "CommitteePositionLeader=" + CommitteePositionLeader +
                ", TransactionPositionLeader=" + TransactionPositionLeader +
                '}';
    }
}
