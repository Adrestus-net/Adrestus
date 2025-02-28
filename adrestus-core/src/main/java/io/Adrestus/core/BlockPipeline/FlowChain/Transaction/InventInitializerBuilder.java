package io.Adrestus.core.BlockPipeline.FlowChain.Transaction;

import io.Adrestus.core.BlockPipeline.BlockRequest;
import io.Adrestus.core.BlockPipeline.BlockRequestHandler;
import io.Adrestus.core.BlockPipeline.BlockRequestType;
import io.Adrestus.core.StatusType;
import io.Adrestus.core.TransactionBlock;
import lombok.SneakyThrows;

public class InventInitializerBuilder implements BlockRequestHandler<TransactionBlock> {
    @Override
    public boolean canHandleRequest(BlockRequest<TransactionBlock> req) {
        return req.getRequestType() == BlockRequestType.INVENT_INITIALIZER_BUILDER;
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    @SneakyThrows
    public void process(BlockRequest<TransactionBlock> blockRequest) {
        blockRequest.getBlock().setStatustype(StatusType.SUCCES);
        blockRequest.getBlock().getTransactionList().forEach(val -> val.setStatus(StatusType.SUCCES));
    }

    @Override
    public void clear(BlockRequest<TransactionBlock> blockRequest) {
        blockRequest.clear();
    }

    @Override
    public String name() {
        return "InventInitializerBuilder";
    }
}
