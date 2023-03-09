package io.distributedLedger;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IDatabase<K, V> {

    void setupOptions();

    void load_connection();

    /**
     * Inserts key-value pair into RocksDB.
     *
     * @param key   of value.
     * @param value that should be persisted.
     */
    void save(K key, Object value);

    void saveAll(Map<K, V> map);

    /**
     * Try to find value for a given key.
     *
     * @param key of entity that should be retrieved.
     * @return Optional of entity.
     */
    Optional<V> findByKey(K key);

    List<V> findByListKey(List<K> key);

    /**
     * Delete entity for a given key.
     *
     * @param key of entity that should be deleted.
     */
    void deleteByKey(K key);

    /**
     * Deletes all entities from RocksDB.
     */
    void deleteAll();

    boolean isDBexists();

    boolean delete_db();

    Map<K, V> findBetweenRange(K key);

    Map<K, V> seekBetweenRange(int start, int finish);

    Map<K, V> seekFromStart();
    Optional<V> seekLast();

    int findDBsize();

    boolean isEmpty();

    boolean isOpen();

    void compact();

    void chooseDB(File dbFile);

    void closeNoDelete();

}
