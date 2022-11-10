package io.distributedLedger;

import io.Adrestus.config.Directory;
import io.Adrestus.util.SerializationUtil;
import io.distributedLedger.exception.*;
import lombok.SneakyThrows;
import org.apache.commons.lang3.SerializationException;
import org.iq80.leveldb.CompressionType;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBIterator;
import org.iq80.leveldb.Options;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static io.distributedLedger.Constants.LevelDBConstants.*;
import static org.iq80.leveldb.impl.Iq80DBFactory.factory;


public class LevelDBConnectionManager<K, V> implements IDriver<LevelDBConnectionManager>, IDatabase<K, V> {

    private static org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(LevelDBConnectionManager.class);
    private static final String CONNECTION_NAME = "\\TransactionDatabase";
    private static volatile LevelDBConnectionManager instance;


    private final SerializationUtil valueMapper;
    private final SerializationUtil keyMapper;
    private final Class<V> keyClass;
    private final ReentrantReadWriteLock rwl;
    private final Lock r;
    private final Lock w;


    private Class<V> valueClass;
    private File dbFile;
    private Options options;
    private DB level_db;

    private LevelDBConnectionManager(Class<V> keyClass, Class<V> valueClass) {
        if (instance != null) {
            throw new IllegalStateException("Already initialized.");
        }
        this.rwl = new ReentrantReadWriteLock();
        this.r = rwl.readLock();
        this.w = rwl.writeLock();
        this.dbFile = new File(Directory.getConfigPath() + CONNECTION_NAME);
        this.valueClass = valueClass;
        this.keyClass = keyClass;
        this.keyMapper = new SerializationUtil<>(this.keyClass);
        this.valueMapper = new SerializationUtil<>(this.valueClass);
        setupOptions();
        load_connection();
    }

    private LevelDBConnectionManager(Class<V> keyClass, Type fluentType) {
        if (instance != null) {
            throw new IllegalStateException("Already initialized.");
        }
        this.rwl = new ReentrantReadWriteLock();
        this.r = rwl.readLock();
        this.w = rwl.writeLock();
        this.dbFile = new File(Directory.getConfigPath() + CONNECTION_NAME);
        this.keyClass = keyClass;
        this.keyMapper = new SerializationUtil<>(this.keyClass);
        this.valueMapper = new SerializationUtil<>(fluentType);
        setupOptions();
        load_connection();
    }


    public static synchronized LevelDBConnectionManager getInstance(Class keyClass, Class valueClass) {
        if (instance == null) {
            synchronized (LevelDBConnectionManager.class) {
                if (instance == null) {
                    instance = new LevelDBConnectionManager<>(keyClass, valueClass);
                }
            }
        }
        return instance;
    }

    public static synchronized LevelDBConnectionManager getInstance(Class keyClass, Type fluentType) {
        if (instance == null) {
            synchronized (LevelDBConnectionManager.class) {
                if (instance == null) {
                    instance = new LevelDBConnectionManager<>(keyClass, fluentType);
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
        options = new Options();

        options.createIfMissing(true);
        options.compressionType(CompressionType.NONE);
        options.blockSize(BLOCK_SIZE);
        options.writeBufferSize(WRITE_BUFFER_SIZE); // (levelDb default: 8mb)
        options.cacheSize(CACHE_SIZE);
        options.paranoidChecks(true);
        options.verifyChecksums(true);
        options.maxOpenFiles(MAX_OPEN_FILES);
    }

    @SneakyThrows
    @Override
    public void load_connection() {
        w.lock();
        dbFile = new File(Directory.getConfigPath(), CONNECTION_NAME);
        try {
            dbFile.createNewFile();
            level_db = factory.open(dbFile.getParentFile(), options);
        } catch (IOException e) {
            LOGGER.error("Path to create file is incorrect. {}", e.getMessage());
        } catch (Exception e) {
            LOGGER.error("LevelDB exception caught. {}", e.getMessage());
            throw new EmptyFailedException(e.getMessage());
        } finally {
            w.unlock();
        }
    }

    @SneakyThrows
    @Override
    public void save(K key, Object value) {
        w.lock();
        try {
            if (value instanceof String) {
                byte[] serializedkey = keyMapper.encode(key);
                byte[] serializedValue = valueMapper.encode(value);
                level_db.put(serializedkey, serializedValue);
            } else {
                final Optional<V> obj = findByKey(key);
                final String str_key = (String) key;
                final LevelDBTransactionWrapper<Object> wrapper;
                Method m1 = value.getClass().getDeclaredMethod("getFrom", value.getClass().getClasses());
                if (((String) m1.invoke(value)).equals(str_key)) {
                    if (obj.isEmpty()) {
                        wrapper = new LevelDBTransactionWrapper<Object>();
                        wrapper.addFrom(value);
                    } else {
                        wrapper = (LevelDBTransactionWrapper) obj.get();
                        wrapper.addFrom(value);
                    }
                } else {
                    if (obj.isEmpty()) {
                        wrapper = new LevelDBTransactionWrapper();
                        wrapper.addTo(value);
                    } else {
                        wrapper = (LevelDBTransactionWrapper) obj.get();
                        wrapper.addTo(value);
                    }
                }
                byte[] serializedkey = keyMapper.encode(key);
                byte[] serializedValue = valueMapper.encode(wrapper);
                level_db.put(serializedkey, serializedValue);
                return;
            }

        } catch (final SerializationException exception) {
            LOGGER.error("Serialization exception occurred during save operation. {}", exception.getMessage());
            throw exception;
        } catch (final Exception exception) {
            LOGGER.error("Exception occurred during save operation. {}", exception.getMessage());
            throw new SaveFailedException(exception.getMessage(), exception);
        } finally {
            w.unlock();
        }
    }

    @SneakyThrows
    @Override
    public void saveAll(Map<K, V> map) {
        w.lock();
        try {

            K[] keys = (K[]) map.keySet().toArray();
            V[] values = (V[]) map.values().toArray();

            for (int i = 0; i < keys.length; i++) {
                byte[] serializedkey = keyMapper.encode(keys[i]);
                byte[] serializedValue = valueMapper.encode(values[i]);
                level_db.put(serializedkey, serializedValue);
            }
        } catch (final SerializationException exception) {
            LOGGER.error("Serialization exception occurred during save operation. {}", exception.getMessage());
            throw exception;
        } catch (final Exception exception) {
            LOGGER.error("Exception occurred during save operation. {}", exception.getMessage());
            throw new SaveFailedException(exception.getMessage(), exception);
        } finally {
            w.unlock();
        }

    }


    @SneakyThrows
    @Override
    public Optional<V> findByKey(K key) {
        r.lock();
        try {
            final byte[] serializedKey = keyMapper.encode(key);
            final byte[] bytes = level_db.get(serializedKey);
            return (Optional<V>) Optional.ofNullable(valueMapper.decode(bytes));
        } catch (final NullPointerException exception) {
            LOGGER.info("Key value not exists in Database return empty");
            return Optional.empty();
        } catch (final SerializationException exception) {
            LOGGER.error("Serialization exception occurred during findByKey operation. {}", exception.getMessage());
        } catch (final Exception exception) {
            LOGGER.error("Exception occurred during findByKey operation. {}", exception.getMessage());
            throw new FindFailedException(exception.getMessage(), exception);
        } finally {
            r.unlock();
        }
        return Optional.empty();
    }

    @SneakyThrows
    @Override
    public void deleteByKey(K key) {
        w.lock();
        try {
            final byte[] serializedKey = keyMapper.encode(key);
            level_db.delete(serializedKey);
        } catch (final SerializationException exception) {
            LOGGER.error("Serialization exception occurred during findByKey operation. {}", exception.getMessage());
            throw exception;
        } catch (final Exception exception) {
            LOGGER.error("Exception occurred during deleteByKey operation. {}", exception.getMessage());
            throw new DeleteFailedException(exception.getMessage(), exception);
        } finally {
            w.unlock();
        }
    }

    @SneakyThrows
    @Override
    public void deleteAll() {
        w.lock();
        try {
            final DBIterator iterator = level_db.iterator();

            for (iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
                level_db.delete(iterator.peekNext().getKey());
            }
            level_db.close();

        } catch (NullPointerException exception) {
            LOGGER.error("Exception occurred during delete_db operation. {}", exception.getMessage());
        } catch (final Exception exception) {
            LOGGER.error("Exception occurred during deleteAll operation. {}", exception.getMessage());
            throw new DeleteAllFailedException(exception.getMessage(), exception);
        } finally {
            w.unlock();
            instance = null;
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

    @SneakyThrows
    @Override
    public boolean delete_db() {
        w.lock();
        try {
            final DBIterator iterator = level_db.iterator();

            for (iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
                level_db.delete(iterator.peekNext().getKey());
            }
            level_db.close();
            factory.destroy(dbFile.getParentFile(), options);
        } catch (NullPointerException exception) {
            LOGGER.error("Exception occurred during delete_db operation. {}", exception.getMessage());
        } catch (final Exception exception) {
            LOGGER.error("Exception occurred during deleteAll operation. {}", exception.getMessage());
            throw new DeleteAllFailedException(exception.getMessage(), exception);
        } finally {
            w.unlock();
            instance = null;
        }
        dbFile.delete();
        dbFile.getParentFile().delete();
        return dbFile.delete();
    }


    @SneakyThrows
    @Override
    public Map<K, V> findBetweenRange(K key) {
        r.lock();
        Map<Object, Object> hashmap = new HashMap<>();
        try {
            final DBIterator iterator = level_db.iterator();
            iterator.seek(keyMapper.encode(key));

            while (iterator.hasNext()) {
                byte[] serializedKey = iterator.peekNext().getKey();
                byte[] serializedValue = iterator.peekNext().getValue();
                hashmap.put(keyMapper.decode(serializedKey), valueMapper.decode(serializedValue));
                iterator.next();
            }
        } catch (final SerializationException exception) {
            LOGGER.error("Serialization exception occurred during findByKey operation. {}", exception.getMessage());
        } finally {
            r.unlock();
        }
        return (Map<K, V>) hashmap;
    }

    @SneakyThrows
    @Override
    public int findDBsize() {
        r.lock();
        try {
            final DBIterator start_iterator = level_db.iterator();

            start_iterator.seekToFirst();
            int entries = 0;

            while (start_iterator.hasNext()) {
                entries++;
                start_iterator.next();
            }
            return entries;
        } finally {
            r.unlock();
        }
    }

    @Override
    public boolean isEmpty() {
        r.lock();
        try {

            try (DBIterator itr = level_db.iterator()) {
                itr.seekToFirst();

                // check if there is at least one valid item
                return !itr.hasNext();
            } catch (Exception e) {
                LOGGER.error("Unable to extract information from database " + this.toString() + ".", e);
            }
        } finally {
            r.unlock();
        }

        return true;
    }

    @Override
    public void compact() {
        w.lock();
        try {
            try {
                level_db.compactRange(new byte[]{(byte) 0x00}, new byte[]{(byte) 0xff});
            } catch (Exception e) {
                LOGGER.error("Cannot compact data.");
                e.printStackTrace();
            }
        } finally {
            w.unlock();
        }
    }

    @Override
    public boolean isOpen() {
        r.lock();
        try {
            return level_db != null;
        } finally {
            r.unlock();
        }
    }


}
