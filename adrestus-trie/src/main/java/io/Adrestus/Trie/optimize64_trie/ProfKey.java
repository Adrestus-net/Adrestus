package io.Adrestus.Trie.optimize64_trie;

import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;

import java.io.Serializable;
import java.util.Objects;

public class ProfKey implements Serializable, Cloneable {
    private final int position;
    private final boolean left;

    public ProfKey(@Deserialize("position") int position, @Deserialize("left") boolean left) {
        this.position = position;
        this.left = left;
    }


    @Serialize
    public int getPosition() {
        return position;
    }

    @Serialize
    public boolean isLeft() {
        return left;
    }

    @Override
    public ProfKey clone() {
        try {
            return (ProfKey) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(); // Can't happen
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProfKey profKey = (ProfKey) o;
        return position == profKey.position && left == profKey.left;
    }

    @Override
    public int hashCode() {
        return Objects.hash(position, left);
    }
}
