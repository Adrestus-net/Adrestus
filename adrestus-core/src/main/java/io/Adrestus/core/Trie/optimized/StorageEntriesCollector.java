package io.Adrestus.core.Trie.optimized;

import java.util.Map;
import java.util.TreeMap;

import org.apache.tuweni.bytes.Bytes32;

public class StorageEntriesCollector<V> implements TrieIterator.LeafHandler<V> {

    protected final Bytes32 startKeyHash;
    protected final int limit;
    protected final Map<Bytes32, V> values = new TreeMap<>();

    public StorageEntriesCollector(final Bytes32 startKeyHash, final int limit) {
        this.startKeyHash = startKeyHash;
        this.limit = limit;
    }

    public static <V> Map<Bytes32, V> collectEntries(
            final Node<V> root, final Bytes32 startKeyHash, final int limit) {
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
    public TrieIterator.State onLeaf(final Bytes32 keyHash, final Node<V> node) {
        if (keyHash.compareTo(startKeyHash) >= 0) {
            node.getValue().ifPresent(value -> values.put(keyHash, value));
        }
        return limitReached() ? TrieIterator.State.STOP : TrieIterator.State.CONTINUE;
    }

    public Map<Bytes32, V> getValues() {
        return values;
    }
}
