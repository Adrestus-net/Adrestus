package io.distributedLedger;

import java.util.Map;
import java.util.Optional;

public interface IDatabase<K, V> {

    void setupOptions();

    void load_connection();

    /**
     * Inserts key-value pair into RocksDB.
     *
     * @param key of value.
     * @param value that should be persisted.
     */
    void save(K key, V value);

    void saveAll(Map<K,V>map);
    /**
     * Try to find value for a given key.
     *
     * @param key of entity that should be retrieved.
     * @return Optional of entity.
     */
    Optional<V> findByKey(K key);

    /**
     * Delete entity for a given key.
     *
     * @param key of entity that should be deleted.
     */
    void deleteByKey(K key);

    /**
     * Deletes all entities from RocksDB.
     *
     */
    void deleteAll();

    boolean isDBexists();

    boolean delete_db();

    Map<K,V>findBetweenRange(K key);

}
