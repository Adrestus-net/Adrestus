/*
 * Copyright ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package io.Adrestus.Trie.optimize64_trie;

import io.Adrestus.util.bytes.Bytes;
import io.Adrestus.util.bytes.Bytes32;
import io.Adrestus.util.bytes.Bytes53;
import io.activej.serializer.annotations.Serialize;
import io.vavr.control.Option;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.Adrestus.Trie.optimize64_trie.CompactEncoding.bytesToPath;
import static java.util.stream.Collectors.toUnmodifiableSet;

/**
 * An in-memory {@link IMerklePatriciaTrie}.
 *
 * @param <V> The type of values stored by this trie.
 */
public class MerklePatriciaTrie<K extends Bytes, V> implements IMerklePatriciaTrie<K, V>, Cloneable {
    private final PathNodeVisitor<V> getVisitor = new GetVisitor<>();
    private final PathNodeVisitor<V> removeVisitor = new RemoveVisitor<>();
    private final DefaultNodeFactory<V> nodeFactory;

    private Node<V> root;

    /**
     * Create a trie.
     *
     * @param valueSerializer A function for serializing values to bytes.
     */
    public MerklePatriciaTrie(final Function<V, Bytes> valueSerializer) {
        this.nodeFactory = new DefaultNodeFactory<>(valueSerializer);
        this.root = NullNode.instance();
    }

    public MerklePatriciaTrie() {
        this.nodeFactory = null;
        this.root = NullNode.instance();
    }

    @Override
    public Option<V> get(final K key) {
        checkNotNull(key);
        return root.accept(getVisitor, bytesToPath(key)).getValue();
    }

    @Override
    public Option<V> getPath(final K path) {
        checkNotNull(path);
        return root.accept(getVisitor, path).getValue();
    }

    @Override
    public Proof<V> getValueWithProof(final K key) {
        checkNotNull(key);
        final ProofVisitor<V> proofVisitor = new ProofVisitor<>(root);
        final Option<V> value = root.accept(proofVisitor, bytesToPath(key)).getValue();
        final List<Bytes> proof =
                proofVisitor.getProof().stream().map(Node::getRlp).collect(Collectors.toList());
        return new Proof<>(value, proof);
    }

    @Override
    public void put(final K key, final V value) {
        checkNotNull(key);
        checkNotNull(value);
        this.root = root.accept(new PutVisitor<>(nodeFactory, value), bytesToPath(key));
    }

    @Override
    public void put(final K key, final PutVisitor<V> putVisitor) {
        checkNotNull(key);
        this.root = root.accept(putVisitor, bytesToPath(key));
    }

    @Override
    public void remove(final K key) {
        checkNotNull(key);
        this.root = root.accept(removeVisitor, bytesToPath(key));
    }

    @Override
    public void removePath(final K path, final RemoveVisitor<V> removeVisitor) {
        checkNotNull(path);
        this.root = root.accept(removeVisitor, path);
    }

    @Override
    public Bytes32 getRootHash() {
        return root.getHash();
    }

    @Override
    public PathNodeVisitor<V> getGetVisitor() {
        return getVisitor;
    }

    @Override

    public PathNodeVisitor<V> getRemoveVisitor() {
        return removeVisitor;
    }

    @Override
    public DefaultNodeFactory<V> getNodeFactory() {
        return nodeFactory;
    }

    @Override
    @Serialize
    public Node<V> getRoot() {
        return root;
    }

    @Override
    public void setRoot(Node<V> root) {
        this.root = root;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + getRootHash() + "]";
    }

    @Override
    public void commit(final NodeUpdater nodeUpdater) {
        // Nothing to do here
    }

    @Override
    public void commit(final NodeUpdater nodeUpdater, final CommitVisitor<V> commitVisitor) {
        // Nothing to do here
    }

    @Override
    public Map<Bytes53, V> entriesFrom(final Bytes53 startKeyHash, final int limit) {
        return StorageEntriesCollector.collectEntries(root, startKeyHash, limit);
    }

    @Override
    public Map<Bytes53, V> entriesFrom(final Function<Node<V>, Map<Bytes53, V>> handler) {
        return handler.apply(root);
    }

    @Override
    public void visitAll(final Consumer<Node<V>> nodeConsumer) {
        root.accept(new AllNodesVisitor<>(nodeConsumer));
    }

    @Override
    public CompletableFuture<Void> visitAll(
            final Consumer<Node<V>> nodeConsumer, final ExecutorService executorService) {
        return CompletableFuture.allOf(
                Stream.concat(
                                Stream.of(
                                        CompletableFuture.runAsync(() -> nodeConsumer.accept(root), executorService)),
                                root.getChildren().stream()
                                        .map(
                                                rootChild ->
                                                        CompletableFuture.runAsync(
                                                                () -> rootChild.accept(new AllNodesVisitor<>(nodeConsumer)),
                                                                executorService)))
                        .collect(toUnmodifiableSet())
                        .toArray(CompletableFuture[]::new));
    }

    @Override
    public void visitLeafs(final TrieIterator.LeafHandler<V> handler) {
        final TrieIterator<V> visitor = new TrieIterator<>(handler, true);
        root.accept(visitor, bytesToPath(Bytes32.ZERO));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MerklePatriciaTrie<?, ?> that = (MerklePatriciaTrie<?, ?>) o;
        return Objects.equals(root.getHash(), that.root.getHash());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getVisitor, removeVisitor, nodeFactory, root);
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
