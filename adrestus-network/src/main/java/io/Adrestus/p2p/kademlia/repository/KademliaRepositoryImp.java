package io.Adrestus.p2p.kademlia.repository;

import java.util.HashMap;
import java.util.Map;

public class KademliaRepositoryImp implements KademliaRepository<String, KademliaData> {
    private final Map<String, KademliaData> stored_map;

    public KademliaRepositoryImp() {
        this.stored_map = new HashMap<>();
    }

    @Override
    public void store(String key, KademliaData value) {
        stored_map.put(key, value);
    }

    @Override
    public KademliaData get(String key) {
        return stored_map.get(key);
    }

    @Override
    public void remove(String key) {
        stored_map.remove(key);
    }

    @Override
    public boolean contains(String key) {
        if (stored_map.containsKey(key))
            return true;
        return false;
    }
}
