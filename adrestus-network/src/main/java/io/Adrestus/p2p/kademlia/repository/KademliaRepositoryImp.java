package io.Adrestus.p2p.kademlia.repository;

import java.util.HashMap;
import java.util.Map;

public class KademliaRepositoryImp implements KademliaRepository<String,KademliaData>{
    private final Map<String, String> stored_map;

    public KademliaRepositoryImp() {
        this.stored_map = new HashMap<>();
    }

    @Override
    public void store(String key, KademliaData value) {

    }

    @Override
    public KademliaData get(String key) {
        return null;
    }

    @Override
    public void remove(String key) {

    }

    @Override
    public boolean contains(String key) {
        return false;
    }
}
