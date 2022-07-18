package io.Adrestus.core.Trie;

public interface PatriciaTree<K, V> {

    byte[] getRootHash();

    void setRoot(byte[] root);

    void clear();

    void put(K key, V val);

    V get(K key);

    void delete(K key);

    boolean flush();
}
