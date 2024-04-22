package io.Adrestus.rpc;


import java.util.ArrayList;

//store in array 3 messages that organizer publishes to subscriber to help slow subscribers
public class CachedConsensusPublisherData {
    private static volatile CachedConsensusPublisherData instance;
    private final ArrayList<byte[]> consensus_retrieval;

    private CachedConsensusPublisherData() {
        if (instance != null) {
            throw new IllegalStateException("Already initialized.");
        }
        this.consensus_retrieval = new ArrayList<>(3);
        this.consensus_retrieval.add(0,null);
        this.consensus_retrieval.add(1,null);
        this.consensus_retrieval.add(2,null);
    }

    public static CachedConsensusPublisherData getInstance() {
        var result = instance;
        if (result == null) {
            synchronized (CachedConsensusPublisherData.class) {
                result = instance;
                if (result == null) {
                    result = new CachedConsensusPublisherData();
                    instance = result;
                }
            }
        }
        return result;
    }


    public void storeAtPosition(int pos, byte[] data) {
        this.consensus_retrieval.set(pos, data);
    }

    public byte[] getDataAtPosition(int pos) {
        if (pos > 2)
            return null;
        return this.consensus_retrieval.get(pos);
    }

    public void clear() {
        this.consensus_retrieval.set(0,null);
        this.consensus_retrieval.set(1,null);
        this.consensus_retrieval.set(2,null);
    }
}
