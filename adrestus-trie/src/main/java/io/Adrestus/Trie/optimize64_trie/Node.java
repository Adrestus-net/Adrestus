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
import io.activej.serializer.annotations.Serialize;
import io.activej.serializer.annotations.SerializeClass;
import io.vavr.control.Option;

import java.util.List;


@SerializeClass(subclasses = {StoredNode.class, RestoreVisitor.PersistedNode.class, PersistVisitor.class, NullNode.class, LeafNode.class, ExtensionNode.class, DefaultNodeFactory.class, BranchNode.class, AllNodesVisitor.class})
public interface Node<V> {

    Node<V> accept(PathNodeVisitor<V> visitor, Bytes path);

    void accept(NodeVisitor<V> visitor);

    void accept(Bytes location, LocationNodeVisitor<V> visitor);

    @Serialize
    Bytes getPath();

    @Serialize
    default Option<Bytes> getLocation() {
        return Option.none();
    }

    @Serialize
    Option<V> getValue();

    @Serialize
    List<Node<V>> getChildren();

    @Serialize
    Bytes getRlp();

    @Serialize
    Bytes getRlpRef();

    /**
     * Whether a reference to this node should be represented as a hash of the rlp, or the node rlp
     * itself should be inlined (the rlp stored directly in the parent node). If true, the node is
     * referenced by hash. If false, the node is referenced by its rlp-encoded value.
     *
     * @return true if this node should be referenced by hash
     */
    default boolean isReferencedByHash() {
        return getRlp().size() >= 32;
    }

    @Serialize
    Bytes32 getHash();

    Node<V> replacePath(Bytes path);

    /**
     * Marks the node as needing to be persisted
     */
    void markDirty();

    /**
     * Is this node not persisted and needs to be?
     *
     * @return True if the node needs to be persisted.
     */
    boolean isDirty();

    String print();

    /**
     * Unloads the node if it is, for example, a StoredNode.
     */
    default void unload() {
    }

    /**
     * Return if a node needs heal. If one of its children missing in the storage
     *
     * @return true if the node need heal
     */
    boolean isHealNeeded();

    /**
     * Marking a node as need heal means that one of its children is not yet present in the storage
     */
    void markHealNeeded();
}
