package io.Adrestus.core;

import com.google.common.base.Objects;
import io.Adrestus.MemoryTreePool;
import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;

public class RepositoryData {
    private AbstractBlock block;
    private MemoryTreePool tree;

    public RepositoryData(@Deserialize("block") AbstractBlock block, @Deserialize("tree") MemoryTreePool tree) {
        this.block = block;
        this.tree = tree;
    }


    @Serialize
    public AbstractBlock getBlock() {
        return block;
    }

    public void setBlock(AbstractBlock block) {
        this.block = block;
    }

    @Serialize
    public MemoryTreePool getTree() {
        return tree;
    }

    public void setTree(MemoryTreePool tree) {
        this.tree = tree;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RepositoryData that = (RepositoryData) o;
        return Objects.equal(block, that.block) && Objects.equal(tree, that.tree);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(block, tree);
    }


}
