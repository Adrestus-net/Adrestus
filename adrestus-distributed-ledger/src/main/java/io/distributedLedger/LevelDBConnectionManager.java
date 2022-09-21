package io.distributedLedger;

import com.linkedin.paldb.api.*;
import io.Adrestus.config.Directory;
import io.Adrestus.util.SerializationUtil;
import io.distributedLedger.Constants.LevelDBConstants;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.LogManager;


public class LevelDBConnectionManager<K, V> implements IDriver<LevelDBConnectionManager>, IDatabase<K, V> {

    private static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(LevelDBConnectionManager.class);
    private static final String CONNECTION_NAME = "\\Blockchain.paldb";
    private static volatile LevelDBConnectionManager instance;


    private final SerializationUtil valueMapper;
    private final ReentrantReadWriteLock rwl;
    private final Lock r;
    private final Lock w;
    private final Class<V> valueClass;

    private  File dbFile;
    private Configuration config;

    private LevelDBConnectionManager(Class<V> valueClass) {
        if (instance != null) {
            throw new IllegalStateException("Already initialized.");
        }
        LogManager.getLogManager().reset();
        this.rwl = new ReentrantReadWriteLock();
        this.r = rwl.readLock();
        this.w = rwl.writeLock();
        this.valueClass = valueClass;
        this.valueMapper = new SerializationUtil<>(valueClass);
        setupOptions();
        load_connection();
        this.dbFile=new File(Directory.getConfigPath() + CONNECTION_NAME);
    }


    public static synchronized LevelDBConnectionManager getInstance(Class valueClass) {
        if (instance == null) {
            synchronized (LevelDBConnectionManager.class) {
                if (instance == null) {
                    instance = new LevelDBConnectionManager<>(valueClass);
                }
            }
        }
        return instance;
    }

    @Override
    public LevelDBConnectionManager get() {
        return instance;
    }

    @Override
    public void setupOptions() {
        config = PalDB.newConfiguration();
        config.set(Configuration.CACHE_ENABLED, LevelDBConstants.CACHE_ENABLED);
        config.set(Configuration.CACHE_BYTES, LevelDBConstants.MAP_CACHE_BYTES);
        config.set(Configuration.CACHE_INITIAL_CAPACITY, LevelDBConstants.CACHE_INITIAL_CAPACITY);
        config.set(Configuration.LOAD_FACTOR, LevelDBConstants.LOAD_FACTOR);
        config.set(Configuration.CACHE_LOAD_FACTOR, LevelDBConstants.CACHE_LOAD_FACTOR);
        config.set(Configuration.COMPRESSION_ENABLED, LevelDBConstants.COMPRESSION_ENABLED);
        config.set(Configuration.MMAP_DATA_ENABLED, LevelDBConstants.MAP_DATA_ENABLED);
        config.set(Configuration.MMAP_SEGMENT_SIZE, LevelDBConstants.MAP_SEGMENT_SIZE);
    }

    @Override
    public void load_connection() {
        w.lock();
        dbFile = new File(Directory.getConfigPath() + CONNECTION_NAME);
        try {
            dbFile.createNewFile();
            StoreWriter writer = PalDB.createWriter(dbFile, config);
            writer.close();
            StoreReader reader = PalDB.createReader(dbFile, config);
            reader.close();
        } catch (IOException e) {
            LOG.error("Path to create file is incorrect. {}", e.getMessage());
        } catch (Exception e) {
            LOG.error("PalDB exception caught. {}", e.getMessage());
        } finally {
            w.unlock();
        }

    }

    @Override
    public void save(K key, V value) {
        w.lock();
        try {
            StoreWriter writer = PalDB.createWriter(dbFile, config);

            byte[] serializedValue = valueMapper.encode(value);

            writer.put(key, serializedValue);
            writer.close();

        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("PalDBException occurred during save operation. {}", e.getMessage());
        } finally {
            w.unlock();
        }
    }

    @Override
    public void saveAll(Map<K, V> map) {
        w.lock();
        try {
            StoreWriter writer = PalDB.createWriter(dbFile, config);

            K[] keys = (K[]) map.keySet().toArray();
            V[] values = (V[]) map.values().toArray();

            writer.putAll(keys, values);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("PalDBException occurred during save operation. {}", e.getMessage());
        } finally {
            w.unlock();
        }

    }

    @Override
    public Optional<V> findByKey(K key) {
        r.lock();
        try {
            StoreReader reader = PalDB.createReader(dbFile);
            final byte[] raw_value = reader.get(key);
            reader.close();
            return Optional.of((V) valueMapper.decode(raw_value));
        } catch (NullPointerException e) {

        } catch (Exception e) {
            LOG.error("PalDBException occurred during read operation. {}", e.getMessage());
        } finally {
            r.unlock();
        }
        return Optional.empty();
    }

    @Override
    public void deleteByKey(K key) {
    }

    @Override
    public void deleteAll() {
        w.lock();
        try {
            instance = null;
            dbFile.delete();
        } finally {
            w.unlock();
        }
    }

    @Override
    public boolean isDBexists() {
        r.lock();
        try {
            return dbFile.exists();
        } finally {
            r.unlock();
        }
    }

    @Override
    public boolean delete_db() {
        w.lock();
        try {
            instance = null;
            return dbFile.delete();
        } finally {
            w.unlock();
        }
    }

    @Override
    public Map<K, V> findBetweenRange(K key) {
        r.lock();
        Map<K, V> hashmap = new HashMap<>();
        try {
            StoreReader reader = PalDB.createReader(dbFile);
            Iterable<Map.Entry<K, V>> iterable = reader.iterable();
            for (Map.Entry<K, V> entry : iterable) {
                hashmap.put(entry.getKey(), entry.getValue());
            }
            reader.close();
        }finally {
            r.unlock();
        }
        return hashmap;
    }


}
