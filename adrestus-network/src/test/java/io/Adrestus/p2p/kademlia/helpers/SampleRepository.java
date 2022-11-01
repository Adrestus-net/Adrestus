package io.Adrestus.p2p.kademlia.helpers;

import io.Adrestus.p2p.kademlia.repository.KademliaRepository;

import java.util.HashMap;
import java.util.Map;

public class SampleRepository implements KademliaRepository<Long, String> {
    protected final Map<Long, String> data = new HashMap<>();

    @Override
    public void store(Long key, String value) {
        data.putIfAbsent(key, value);
    }

    @Override
    public String get(Long key) {
        return data.get(key);
    }

    @Override
    public void remove(Long key) {
        data.remove(key);
    }

    @Override
    public boolean contains(Long key) {
        return data.containsKey(key);
    }
}
