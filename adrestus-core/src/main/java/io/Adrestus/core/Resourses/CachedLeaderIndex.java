package io.Adrestus.core.Resourses;

public class CachedLeaderIndex {
    private static volatile CachedLeaderIndex instance;
    private int CommitteePositionLeader;

    public CachedLeaderIndex(int committeePositionLeader) {
        CommitteePositionLeader = committeePositionLeader;
    }

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
                '}';
    }
}
