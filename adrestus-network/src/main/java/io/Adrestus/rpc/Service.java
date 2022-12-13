package io.Adrestus.rpc;

import io.distributedLedger.DatabaseFactory;
import io.distributedLedger.DatabaseType;
import io.distributedLedger.IDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Service<T> implements IService {
    private final IDatabase<String, T> database;
    private Class<T> typeParameterClass;

    public Service(Class<T> typeParameterClass) {
        this.typeParameterClass = typeParameterClass;
        this.database = new DatabaseFactory(String.class, this.typeParameterClass.getClass()).getDatabase(DatabaseType.ROCKS_DB);
    }

    @Override
    public List<T> download(String hash) throws Exception {
        Map<String, T> map = database.findBetweenRange(hash);
        List<T> result = new ArrayList<T>(map.values());
        return result;
    }

    @Override
    public Optional<T> getBlock(String hash) throws Exception {
        return database.findByKey(hash);
    }


}
