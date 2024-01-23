package io.Adrestus.Trie.optimize64_trie;

import io.Adrestus.util.bytes.Bytes53;

import java.util.Map;
import java.util.TreeMap;

public class StorageEntriesCollector<V> implements TrieIterator.LeafHandler<V> {

    protected final Bytes53 startKeyHash;
    protected final int limit;
    protected final Map<Bytes53, V> values = new TreeMap<>();

    public StorageEntriesCollector(final Bytes53 startKeyHash, final int limit) {
        this.startKeyHash = startKeyHash;
        this.limit = limit;
    }

    public static <V> Map<Bytes53, V> collectEntries(
            final Node<V> root, final Bytes53 startKeyHash, final int limit) {
        final StorageEntriesCollector<V> entriesCollector =
                new StorageEntriesCollector<>(startKeyHash, limit);
        final TrieIterator<V> visitor = new TrieIterator<>(entriesCollector, false);
        root.accept(visitor, CompactEncoding.bytesToPath(startKeyHash));
        return entriesCollector.getValues();
    }

    protected boolean limitReached() {
        return limit <= values.size();
    }

    @Override
    public TrieIterator.State onLeaf(final Bytes53 keyHash, final Node<V> node) {
        if (keyHash.compareTo(startKeyHash) >= 0) {
            node.getValue().peek(value -> values.put(keyHash, value));
        }
        return limitReached() ? TrieIterator.State.STOP : TrieIterator.State.CONTINUE;
    }

    public Map<Bytes53, V> getValues() {
        return values;
    }
}
