package io.Adrestus.core.BlockPipeline;

import io.Adrestus.core.AbstractBlock;

import java.util.Comparator;
import java.util.List;

public abstract class BlockFlowChain<T extends AbstractBlock> {
    private final List<BlockRequestHandler<T>> forgeHandlers;
    private final List<BlockRequestHandler<T>> inventorHandlers;

    public BlockFlowChain(List<BlockRequestHandler<T>> forgeHandlers, List<BlockRequestHandler<T>> inventorHandlers) {
        this.forgeHandlers = forgeHandlers;
        this.inventorHandlers = inventorHandlers;
    }


    public abstract void clear();

    public void makeInventorRequest(BlockRequest<T> req) {
        inventorHandlers
                .stream()
                .sorted(Comparator.comparing(BlockRequestHandler::getPriority))
                .filter(handler -> handler.canHandleRequest(req))
                .findFirst()
                .ifPresent(handler -> handler.process(req));
    }

    public void makeForgeHandlersRequest(BlockRequest<T> req) {
        forgeHandlers
                .stream()
                .sorted(Comparator.comparing(BlockRequestHandler::getPriority))
                .filter(handler -> handler.canHandleRequest(req))
                .findFirst()
                .ifPresent(handler -> handler.process(req));
    }

    public void cleanForgeHandlersRequest(BlockRequest<T> req) {
        forgeHandlers
                .stream()
                .sorted(Comparator.comparing(BlockRequestHandler::getPriority))
                .filter(handler -> handler.canHandleRequest(req))
                .findFirst()
                .ifPresent(handler -> handler.clear(req));
    }

    public void cleanInventHandlersRequest(BlockRequest<T> req) {
        inventorHandlers
                .stream()
                .sorted(Comparator.comparing(BlockRequestHandler::getPriority))
                .filter(handler -> handler.canHandleRequest(req))
                .findFirst()
                .ifPresent(handler -> handler.clear(req));
    }
}
