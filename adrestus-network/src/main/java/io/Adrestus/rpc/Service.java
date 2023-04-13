package io.Adrestus.rpc;

import io.distributedLedger.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Service<T> implements IService<T> {
    private final IDatabase<String, T> database;
    private final Class<T> typeParameterClass;
    private DatabaseInstance instance;
    public Service(Class<T> typeParameterClass) {
        this.typeParameterClass = typeParameterClass;
        DatabaseFactory factory = new DatabaseFactory(String.class, typeParameterClass);
        this.database = factory.getDatabase(DatabaseType.ROCKS_DB);
    }

    public Service(Class<T> typeParameterClass, DatabaseInstance instance) {
        this.instance=instance;
        this.typeParameterClass = typeParameterClass;
        DatabaseFactory factory = new DatabaseFactory(String.class, typeParameterClass);
        this.database = factory.getDatabase(DatabaseType.ROCKS_DB, instance);
    }

    public Service(Class<T> typeParameterClass, PatriciaTreeInstance instance) {
        this.typeParameterClass = typeParameterClass;
        DatabaseFactory factory = new DatabaseFactory(String.class, typeParameterClass);
        this.database = factory.getDatabase(DatabaseType.ROCKS_DB, instance);
    }

    @Override
    public List<T> download(String hash) throws Exception {
        Map<String, T> map;
        if (hash.equals(""))
            map = database.seekFromStart();
        else
            map = database.findBetweenRange(hash);

        List<T> result = new ArrayList<T>(map.values());
        return result;
    }

    @Override
    public List<T> downloadPatriciaTree(String hash) throws Exception {
        Map<String, T> map;
        if (hash.equals(""))
            map = database.seekFromStart();
        else
            map = database.findBetweenRange(hash);
        List<T> result = new ArrayList<T>(map.values());
        return result;
    }

    @Override
    public List<T> migrateBlock(ArrayList<String> list_hash) throws Exception {
        return (List<T>) database.findByListKey(list_hash);
    }

}
