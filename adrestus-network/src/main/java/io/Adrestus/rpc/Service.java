package io.Adrestus.rpc;

import io.Adrestus.core.AbstractBlock;
import io.distributedLedger.DatabaseFactory;
import io.distributedLedger.DatabaseType;
import io.distributedLedger.IDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Service implements IService {
    private final IDatabase<String, AbstractBlock> database;

    public Service() {
        this.database = new DatabaseFactory(String.class, AbstractBlock.class).getDatabase(DatabaseType.ROCKS_DB);
    }

    @Override
    public List<AbstractBlock> download(String hash) throws Exception {
        Map<String, AbstractBlock> map = database.findBetweenRange(hash);
        List<AbstractBlock> result = new ArrayList<AbstractBlock>(map.values());
        return result;
    }
}
