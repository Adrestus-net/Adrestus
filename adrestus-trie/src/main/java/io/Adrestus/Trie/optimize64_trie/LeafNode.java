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

import io.Adrestus.util.BytesValueRLPOutput;
import io.Adrestus.util.RLP;
import io.Adrestus.util.bytes.Bytes;
import io.Adrestus.util.bytes.Bytes32;
import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;
import io.vavr.control.Option;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;


class LeafNode<V> implements Node<V>, Serializable {
    private final Option<Bytes> location;
    private final Bytes path;
    private final V value;
    private final NodeFactory<V> nodeFactory;
    private final Function<V, Bytes> valueSerializer;
    private Bytes rlp;
    private Bytes32 hash;
    private boolean dirty = false;

    LeafNode(
            @Deserialize("location") final Bytes location,
            @Deserialize("path") final Bytes path,
            @Deserialize("value") final V value,
            @Deserialize("nodeFactory") final NodeFactory<V> nodeFactory,
            @Deserialize("valueSerializer") final Function<V, Bytes> valueSerializer) {
        this.location = Option.of(location);
        this.path = path;
        this.value = value;
        this.nodeFactory = nodeFactory;
        this.valueSerializer = valueSerializer;
    }

    LeafNode(
            @Deserialize("path") final Bytes path,
            @Deserialize("value") final V value,
            @Deserialize("nodeFactory") final NodeFactory<V> nodeFactory,
            @Deserialize("valueSerializer") final Function<V, Bytes> valueSerializer) {
        this.location = Option.none();
        this.path = path;
        this.value = value;
        this.nodeFactory = nodeFactory;
        this.valueSerializer = valueSerializer;
    }

    @Override
    public Node<V> accept(final PathNodeVisitor<V> visitor, final Bytes path) {
        return visitor.visit(this, path);
    }

    @Override
    public void accept(final NodeVisitor<V> visitor) {
        visitor.visit(this);
    }

    @Override
    public void accept(final Bytes location, final LocationNodeVisitor<V> visitor) {
        visitor.visit(location, this);
    }

    @Override
    @Serialize
    public Option<Bytes> getLocation() {
        return location;
    }

    @Override
    @Serialize
    public Bytes getPath() {
        return path;
    }

    @Override
    @Serialize
    public Option<V> getValue() {
        return Option.of(value);
    }

    @Override
    @Serialize
    public List<Node<V>> getChildren() {
        return Collections.emptyList();
    }

    @Override
    @Serialize
    public Bytes getRlp() {
        if (rlp != null) {
            final Bytes encoded = rlp;
            if (encoded != null) {
                return encoded;
            }
        }

        final BytesValueRLPOutput out = new BytesValueRLPOutput();
        out.startList();
        out.writeBytes(CompactEncoding.encode(path));
        out.writeBytes(valueSerializer.apply(value));
        out.endList();
        final Bytes encoded = out.encoded();
        rlp = encoded;
        return encoded;
    }

    @Override
    @Serialize
    public Bytes getRlpRef() {
        if (isReferencedByHash()) {
            return RLP.encodeOne(getHash());
        } else {
            return getRlp();
        }
    }

    @Override
    @Serialize
    public Bytes32 getHash() {
        if (hash != null) {
            final Bytes32 hashed = hash;
            if (hashed != null) {
                return hashed;
            }
        }
        final Bytes32 hashed = Util.keccak256(getRlp());
        hash = hashed;
        return hashed;
    }

    @Override
    public Node<V> replacePath(final Bytes path) {
        return nodeFactory.createLeaf(path, value);
    }

    @Override
    public String print() {
        return "Leaf:"
                + "\n\tRef: "
                + getRlpRef()
                + "\n\tPath: "
                + CompactEncoding.encode(path)
                + "\n\tValue: "
                + getValue().map(Object::toString).orElse(Option.of("empty"));
    }

    @Override
    public boolean isDirty() {
        return dirty;
    }

    @Override
    public void markDirty() {
        dirty = true;
    }

    @Override
    public boolean isHealNeeded() {
        return false;
    }

    @Override
    public void markHealNeeded() {
        // nothing to do a leaf don't have child
    }
}
