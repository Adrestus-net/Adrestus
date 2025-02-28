package io.Adrestus.core.BlockPipeline;

import io.Adrestus.core.AbstractBlock;
import lombok.Getter;

import java.io.Serializable;
import java.util.Objects;

@Getter
public class BlockRequest<T extends AbstractBlock & Serializable> implements Serializable {
    private BlockRequestType requestType;
    private T block;
    private boolean handled;
    private final int zoneIndex;

    public BlockRequest(final BlockRequestType requestType, final T abstractBlock) {
        this.requestType = Objects.requireNonNull(requestType);
        this.block = Objects.requireNonNull(abstractBlock);
        this.zoneIndex = 0;
    }

    public BlockRequest(final BlockRequestType requestType, final T abstractBlock, final int zoneIndex) {
        this.requestType = Objects.requireNonNull(requestType);
        this.block = Objects.requireNonNull(abstractBlock);
        this.zoneIndex = zoneIndex;
    }

    public void clear() {
        block = null;
        handled = false;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        BlockRequest<?> that = (BlockRequest<?>) o;
        return handled == that.handled && zoneIndex == that.zoneIndex && requestType == that.requestType && Objects.equals(block, that.block);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requestType, block, handled, zoneIndex);
    }

    @Override
    public String toString() {
        return "BlockRequest{" +
                "requestType=" + requestType +
                ", block=" + block +
                ", handled=" + handled +
                ", zoneIndex=" + zoneIndex +
                '}';
    }
}
