package io.Adrestus.core.BlockPipeline.FlowChain.Committee;

import io.Adrestus.core.BlockPipeline.BlockRequest;
import io.Adrestus.core.BlockPipeline.BlockRequestHandler;
import io.Adrestus.core.BlockPipeline.BlockRequestType;
import io.Adrestus.core.CommitteeBlock;
import io.Adrestus.core.StatusType;
import io.distributedLedger.DatabaseFactory;
import io.distributedLedger.DatabaseInstance;
import io.distributedLedger.DatabaseType;
import io.distributedLedger.IDatabase;
import lombok.SneakyThrows;

public class InventDatabaseStorageBuilder implements BlockRequestHandler<CommitteeBlock> {
    private final IDatabase<String, CommitteeBlock> database;

    public InventDatabaseStorageBuilder() {
        this.database = new DatabaseFactory(String.class, CommitteeBlock.class).getDatabase(DatabaseType.ROCKS_DB, DatabaseInstance.COMMITTEE_BLOCK);
    }

    @Override
    public boolean canHandleRequest(BlockRequest<CommitteeBlock> req) {
        return req.getRequestType() == BlockRequestType.INVENT_DATABASE_STORAGE_BUILDER;
    }

    @Override
    public int getPriority() {
        return 3;
    }

    @Override
    @SneakyThrows
    public void process(BlockRequest<CommitteeBlock> blockRequest) {
        blockRequest.getBlock().setStatustype(StatusType.SUCCES);
        database.save(String.valueOf(blockRequest.getBlock().getGeneration()), blockRequest.getBlock());
    }

    @Override
    public String name() {
        return "ForgeSerializerBlock";
    }

    @Override
    public void clear(BlockRequest<CommitteeBlock> blockRequest) {
        blockRequest.clear();
    }
}
