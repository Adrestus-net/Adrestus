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
import io.Adrestus.util.bytes.MutableBytes;
import io.vavr.control.Option;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;


class BranchNode<V> implements Node<V> {
    public static final byte RADIX = CompactEncoding.LEAF_TERMINATOR;

    @SuppressWarnings("rawtypes")
    private static final Node NULL_NODE = NullNode.instance();

    private final Option<Bytes> location;
    private final ArrayList<Node<V>> children;
    private final Option<V> value;
    private final NodeFactory<V> nodeFactory;
    private final Function<V, Bytes> valueSerializer;
    private Bytes rlp;
    private Bytes32 hash;
    private boolean dirty = false;
    private boolean needHeal = false;

    BranchNode(
            final Bytes location,
            final ArrayList<Node<V>> children,
            final Option<V> value,
            final NodeFactory<V> nodeFactory,
            final Function<V, Bytes> valueSerializer) {
        assert (children.size() == RADIX);
        this.location = Option.of(location);
        this.children = children;
        this.value = value;
        this.nodeFactory = nodeFactory;
        this.valueSerializer = valueSerializer;
    }

    BranchNode(
            final ArrayList<Node<V>> children,
            final Option<V> value,
            final NodeFactory<V> nodeFactory,
            final Function<V, Bytes> valueSerializer) {
        assert (children.size() == RADIX);
        this.location = Option.none();
        this.children = children;
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
    public Option<Bytes> getLocation() {
        return location;
    }

    @Override
    public Bytes getPath() {
        return Bytes.EMPTY;
    }

    @Override
    public Option<V> getValue() {
        return value;
    }

    @Override
    public List<Node<V>> getChildren() {
        return Collections.unmodifiableList(children);
    }

    public Node<V> child(final byte index) {
        return children.get(index);
    }

    @Override
    public Bytes getRlp() {
        if (rlp != null) {
            final Bytes encoded = rlp;
            if (encoded != null) {
                return encoded;
            }
        }
        final BytesValueRLPOutput out = new BytesValueRLPOutput();
        out.startList();
        for (int i = 0; i < RADIX; ++i) {
            out.writeRaw(children.get(i).getRlpRef());
        }
        if (value.isDefined()) {
            out.writeBytes(valueSerializer.apply(value.get()));
        } else {
            out.writeNull();
        }
        out.endList();
        final Bytes encoded = out.encoded();
        rlp = encoded;
        return encoded;
    }

    @Override
    public Bytes getRlpRef() {
        if (isReferencedByHash()) {
            return RLP.encodeOne(getHash());
        } else {
            return getRlp();
        }
    }

    @Override
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
    public Node<V> replacePath(final Bytes newPath) {
        return nodeFactory.createExtension(newPath, this);
    }

    public Node<V> replaceChild(final byte index, final Node<V> updatedChild) {
        return replaceChild(index, updatedChild, true);
    }

    public Node<V> replaceChild(
            final byte index, final Node<V> updatedChild, final boolean allowFlatten) {
        final ArrayList<Node<V>> newChildren = new ArrayList<>(children);
        newChildren.set(index, updatedChild);

        if (updatedChild == NULL_NODE) {
            if (value.isDefined() && !hasChildren()) {
                return nodeFactory.createLeaf(Bytes.of(index), value.get());
            } else if (value.isEmpty() && allowFlatten) {
                final Option<Node<V>> flattened = maybeFlatten(newChildren);
                if (flattened.isDefined()) {
                    return flattened.get();
                }
            }
        }

        return nodeFactory.createBranch(newChildren, value);
    }

    public Node<V> replaceValue(final V value) {
        return nodeFactory.createBranch(children, Option.of(value));
    }

    public Node<V> removeValue() {
        return (Node<V>) maybeFlatten(children).orElse(Option.of(nodeFactory.createBranch(children, Option.none())));
    }

    private boolean hasChildren() {
        for (final Node<V> child : children) {
            if (child != NULL_NODE) {
                return true;
            }
        }
        return false;
    }

    private static <V> Option<Node<V>> maybeFlatten(final ArrayList<Node<V>> children) {
        final int onlyChildIndex = findOnlyChild(children);
        if (onlyChildIndex >= 0) {
            // replace the path of the only child and return it
            final Node<V> onlyChild = children.get(onlyChildIndex);
            final Bytes onlyChildPath = onlyChild.getPath();
            final MutableBytes completePath = MutableBytes.create(1 + onlyChildPath.size());
            completePath.set(0, (byte) onlyChildIndex);
            onlyChildPath.copyTo(completePath, 1);
            return Option.of(onlyChild.replacePath(completePath));
        }
        return Option.none();
    }

    private static <V> int findOnlyChild(final ArrayList<Node<V>> children) {
        int onlyChildIndex = -1;
        assert (children.size() == RADIX);
        for (int i = 0; i < RADIX; ++i) {
            if (children.get(i) != NULL_NODE) {
                if (onlyChildIndex >= 0) {
                    return -1;
                }
                onlyChildIndex = i;
            }
        }
        return onlyChildIndex;
    }

    @Override
    public String print() {
        final StringBuilder builder = new StringBuilder();
        builder.append("Branch:");
        builder.append("\n\tRef: ").append(getRlpRef());
        for (int i = 0; i < RADIX; i++) {
            final Node<V> child = child((byte) i);
            if (!Objects.equals(child, NullNode.instance())) {
                final String branchLabel = "[" + Integer.toHexString(i) + "] ";
                final String childRep = child.print().replaceAll("\n\t", "\n\t\t");
                builder.append("\n\t").append(branchLabel).append(childRep);
            }
        }
        builder.append("\n\tValue: ").append(getValue().map(Object::toString).orElse(Option.of("empty")));
        return builder.toString();
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
        return needHeal;
    }

    @Override
    public void markHealNeeded() {
        this.needHeal = true;
    }
}
