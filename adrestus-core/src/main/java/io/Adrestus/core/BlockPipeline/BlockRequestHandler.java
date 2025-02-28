package io.Adrestus.core.BlockPipeline;

import io.Adrestus.core.AbstractBlock;

import java.io.Serializable;

public interface BlockRequestHandler<T extends AbstractBlock & Serializable> {
    boolean canHandleRequest(BlockRequest<T> req);

    int getPriority();

    void process(BlockRequest<T> blockRequest);

    String name();

    void clear(BlockRequest<T> blockRequest);
}
