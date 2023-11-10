package io.distributedLedger;

import io.Adrestus.config.Directory;
import io.Adrestus.crypto.bls.BLS381.ECP;
import io.Adrestus.crypto.bls.BLS381.ECP2;
import io.Adrestus.crypto.bls.mapper.ECP2mapper;
import io.Adrestus.crypto.bls.mapper.ECPmapper;
import io.Adrestus.crypto.elliptic.mapper.*;
import io.Adrestus.util.SerializationUtil;
import io.distributedLedger.exception.*;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SerializationException;
import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.apache.tuweni.bytes.MutableBytes;
import org.rocksdb.*;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static io.distributedLedger.Constants.RocksDBConstants.*;
import static java.lang.Math.max;

public class RocksDBConnectionManager<K, V> implements IDatabase<K, V> {

    private static org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(RocksDBConnectionManager.class);

    private static final boolean enableDbCompression = false;


    private final DatabaseInstance databaseInstance;
    private final PatriciaTreeInstance patriciaTreeInstance;
    private final SerializationUtil valueMapper;
    private final SerializationUtil keyMapper;
    private final Class<K> keyClass;
    private final Class<V> valueClass;
    private final ReentrantReadWriteLock rwl;
    private final Lock r;
    private final Lock w;


    private String CONNECTION_NAME = "Blockchain_rocks-db";
    private File dbFile;
    private Options options;
    private RocksDB rocksDB;

    public RocksDBConnectionManager(Class<K> keyClass, Class<V> valueClass) {
        this.databaseInstance = DatabaseInstance.COMMITTEE_BLOCK;
        this.patriciaTreeInstance = null;
        this.rwl = new ReentrantReadWriteLock();
        this.r = rwl.readLock();
        this.w = rwl.writeLock();
        String path= Paths.get(Directory.getConfigPath(), CONNECTION_NAME).toString();
        this.dbFile = new File(path);
        this.valueClass = valueClass;
        this.keyClass = keyClass;
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(ECP.class, ctx -> new ECPmapper()));
        list.add(new SerializationUtil.Mapping(ECP2.class, ctx -> new ECP2mapper()));
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        list.add(new SerializationUtil.Mapping(TreeMap.class, ctx -> new CustomSerializerTreeMap()));
        //list.add(new SerializationUtil.Mapping(MemoryTreePool.class, ctx->new MemoryTreePoolSerializer()));
        list.add(new SerializationUtil.Mapping(Bytes.class, ctx -> new BytesSerializer()));
        list.add(new SerializationUtil.Mapping(Bytes32.class, ctx -> new Bytes32Serializer()));
        list.add(new SerializationUtil.Mapping(MutableBytes.class, ctx -> new MutableBytesSerializer()));
        this.keyMapper = new SerializationUtil<>(this.keyClass);
        this.valueMapper = new SerializationUtil<>(this.valueClass, list);
        setupOptions();
        load_connection();
    }

    public RocksDBConnectionManager(Class<K> keyClass, Class<V> valueClass, DatabaseInstance databaseInstance) {
        this.databaseInstance = databaseInstance;
        this.patriciaTreeInstance = null;
        this.rwl = new ReentrantReadWriteLock();
        this.r = rwl.readLock();
        this.w = rwl.writeLock();
        this.CONNECTION_NAME = databaseInstance.getTitle();
        String path= Paths.get(Directory.getConfigPath(), CONNECTION_NAME).toString();
        this.dbFile = new File(path);
        this.valueClass = valueClass;
        this.keyClass = keyClass;
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(ECP.class, ctx -> new ECPmapper()));
        list.add(new SerializationUtil.Mapping(ECP2.class, ctx -> new ECP2mapper()));
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        list.add(new SerializationUtil.Mapping(TreeMap.class, ctx -> new CustomSerializerTreeMap()));
        list.add(new SerializationUtil.Mapping(Bytes.class, ctx -> new BytesSerializer()));
        list.add(new SerializationUtil.Mapping(Bytes32.class, ctx -> new Bytes32Serializer()));
        list.add(new SerializationUtil.Mapping(MutableBytes.class, ctx -> new MutableBytesSerializer()));
        this.keyMapper = new SerializationUtil<>(this.keyClass);
        this.valueMapper = new SerializationUtil<>(this.valueClass, list);
        setupOptions();
        load_connection();
    }

    public RocksDBConnectionManager(Class<K> keyClass, Class<V> valueClass, PatriciaTreeInstance patriciaTreeInstance) {
        this.databaseInstance = null;
        this.patriciaTreeInstance = patriciaTreeInstance;
        this.rwl = new ReentrantReadWriteLock();
        this.r = rwl.readLock();
        this.w = rwl.writeLock();
        this.CONNECTION_NAME = patriciaTreeInstance.getTitle();
        String path= Paths.get(Directory.getConfigPath(), CONNECTION_NAME).toString();
        this.dbFile = new File(path);
        this.valueClass = valueClass;
        this.keyClass = keyClass;
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(ECP.class, ctx -> new ECPmapper()));
        list.add(new SerializationUtil.Mapping(ECP2.class, ctx -> new ECP2mapper()));
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        list.add(new SerializationUtil.Mapping(TreeMap.class, ctx -> new CustomSerializerTreeMap()));
        list.add(new SerializationUtil.Mapping(Bytes.class, ctx -> new BytesSerializer()));
        list.add(new SerializationUtil.Mapping(Bytes32.class, ctx -> new Bytes32Serializer()));
        list.add(new SerializationUtil.Mapping(MutableBytes.class, ctx -> new MutableBytesSerializer()));
        this.keyMapper = new SerializationUtil<>(this.keyClass);
        this.valueMapper = new SerializationUtil<>(this.valueClass, list);
        setupOptions();
        load_connection();
    }


    public void chooseDB(File dbFile) {
        this.dbFile = dbFile;
    }

    @Override
    public void setupOptions() {
        options = new Options();

        options.setCreateIfMissing(true);
        options.setUseFsync(false);
        options.setCompressionType(
                enableDbCompression
                        ? CompressionType.LZ4_COMPRESSION
                        : CompressionType.NO_COMPRESSION);

        options.setBottommostCompressionType(CompressionType.ZLIB_COMPRESSION);
        options.setMinWriteBufferNumberToMerge(MIN_WRITE_BUFFER_NUMBER_TOMERGE);
        options.setLevel0StopWritesTrigger(LEVEL0_STOP_WRITES_TRIGGER);
        options.setLevel0SlowdownWritesTrigger(LEVEL0_SLOWDOWN_WRITES_TRIGGER);
        options.setAtomicFlush(true);
        options.setWriteBufferSize(WRITE_BUFFER_SIZE);
        options.setRandomAccessMaxBufferSize(READ_BUFFER_SIZE);
        options.setParanoidChecks(true);
        options.setMaxOpenFiles(MAX_OPEN_FILES);
        options.setTableFormatConfig(setupBlockBasedTableConfig());
        options.setDisableAutoCompactions(false);
        options.setIncreaseParallelism(max(1, Runtime.getRuntime().availableProcessors() / 2));

        options.setLevelCompactionDynamicLevelBytes(true);
        options.setMaxBackgroundCompactions(MAX_BACKGROUND_COMPACTIONS);
        options.setMaxBackgroundFlushes(MAX_BACKGROUND_FLUSHES);
        options.setBytesPerSync(BYTES_PER_SYNC);
        options.setCompactionPriority(CompactionPriority.MinOverlappingRatio);
        options.optimizeLevelStyleCompaction(OPTIMIZE_LEVEL_STYLE_COMPACTION);
    }

    private BlockBasedTableConfig setupBlockBasedTableConfig() {
        BlockBasedTableConfig bbtc = new BlockBasedTableConfig();
        bbtc.setBlockSize(BLOCK_SIZE);
        bbtc.setCacheIndexAndFilterBlocks(true);
        bbtc.setPinL0FilterAndIndexBlocksInCache(true);
        bbtc.setFilterPolicy(new BloomFilter(BLOOMFILTER_BITS_PER_KEY, false));
        return bbtc;
    }

    @SneakyThrows
    @Override
    public synchronized void load_connection() {
        w.lock();
        String path= Paths.get(Directory.getConfigPath(), CONNECTION_NAME).toString();
        this.dbFile = new File(path);
        try {
            Files.createDirectories(dbFile.getParentFile().toPath());
            //Files.createDirectories(dbFile.getAbsoluteFile().toPath());
            // rocksDB = RocksDB.open(options,dbFile.getAbsolutePath());
            if (this.databaseInstance != null)
                rocksDB = ZoneDatabaseFactory.getDatabaseInstance(this.databaseInstance, options, dbFile.getAbsolutePath());
            else
                rocksDB = ZoneDatabaseFactory.getDatabaseInstance(this.patriciaTreeInstance, options, dbFile.getAbsolutePath());
        } catch (IOException e) {
            LOGGER.error("Path to create file is incorrect. {}", e.getMessage());
        } catch (Exception e) {
            LOGGER.error("RocksDB exception caught. {}", e.getMessage());
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
            byte[] serializedkey = keyMapper.encode(key);
            byte[] serializedValue = valueMapper.encode(value);
            rocksDB.put(serializedkey, serializedValue);
        } catch (NullPointerException exception) {
            LOGGER.error("NullPointer exception occurred during save operation. {}", exception.getMessage());
            throw exception;
        } catch (final SerializationException exception) {
            LOGGER.error("Serialization exception occurred during save operation. {}", exception.getMessage());
            throw exception;
        } catch (final RocksDBException exception) {
            LOGGER.error("RocksDBException occurred during save operation. {}", exception.getMessage());
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
            if (!map.isEmpty()) {
                K[] keys = (K[]) map.keySet().toArray();
                V[] values = (V[]) map.values().toArray();

                for (int i = 0; i < keys.length; i++) {
                    byte[] serializedkey = keyMapper.encode(keys[i]);
                    byte[] serializedValue = valueMapper.encode(values[i]);
                    rocksDB.put(serializedkey, serializedValue);
                }
            }
        } catch (NullPointerException exception) {
            LOGGER.error("NullPointer exception occurred during save operation. {}", exception.getMessage());
            throw exception;
        } catch (final SerializationException exception) {
            LOGGER.error("Serialization exception occurred during save operation. {}", exception.getMessage());
            throw exception;
        } catch (final RocksDBException exception) {
            LOGGER.error("RocksDBException occurred during save operation. {}", exception.getMessage());
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
            final byte[] bytes = rocksDB.get(serializedKey);
            return (Optional<V>) Optional.ofNullable(valueMapper.decode(bytes));
        } catch (final NullPointerException exception) {
            LOGGER.info("Key value not exists in Database return empty");
            return Optional.empty();
        } catch (final SerializationException exception) {
            LOGGER.error("Serialization exception occurred during findByKey operation. {}", exception.getMessage());
        } catch (final RocksDBException exception) {
            LOGGER.error("RocksDBException occurred during findByKey operation. {}", exception.getMessage());
            throw new FindFailedException(exception.getMessage(), exception);
        } finally {
            r.unlock();
        }
        return Optional.empty();
    }

    @SneakyThrows
    @Override
    public List<V> findByListKey(List<K> key) {
        r.lock();
        try {
            List<V> list = new ArrayList<>();
            for (int i = 0; i < key.size(); i++) {
                final byte[] serializedKey = keyMapper.encode(key.get(i));
                final byte[] bytes = rocksDB.get(serializedKey);
                list.add((V) valueMapper.decode(bytes));
            }
            return list;
        } catch (final NullPointerException exception) {
            LOGGER.info("Key value not exists in Database return empty");
        } catch (final SerializationException exception) {
            LOGGER.error("Serialization exception occurred during findByKey operation. {}", exception.getMessage());
        } catch (final RocksDBException exception) {
            LOGGER.error("RocksDBException occurred during findByKey operation. {}", exception.getMessage());
            throw new FindFailedException(exception.getMessage(), exception);
        } finally {
            r.unlock();
        }
        return new ArrayList<V>();
    }

    @SneakyThrows
    @Override
    public void deleteByKey(K key) {
        w.lock();
        try {
            final byte[] serializedKey = keyMapper.encode(key);
            rocksDB.delete(serializedKey);
        } catch (final SerializationException exception) {
            LOGGER.error("Serialization exception occurred during findByKey operation. {}", exception.getMessage());
            throw exception;
        } catch (final RocksDBException exception) {
            LOGGER.error("RocksDBException occurred during deleteByKey operation. {}", exception.getMessage());
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
            final RocksIterator iterator = rocksDB.newIterator();

            iterator.seekToFirst();
            final byte[] firstKey = getKey(iterator);

            iterator.seekToLast();
            final byte[] lastKey = getKey(iterator);

            if (firstKey != null || lastKey != null) {
                rocksDB.deleteRange(firstKey, lastKey);
                rocksDB.delete(lastKey);
            }
            rocksDB.deleteFile(Directory.getConfigPath());
            RocksDB.destroyDB(Directory.getConfigPath(), options);
            rocksDB.close();
            rocksDB = null;
        } catch (NullPointerException exception) {
            LOGGER.error("RocksDBException occurred during delete_db operation. {}", exception.getMessage());
        } catch (final RocksDBException exception) {
            LOGGER.error("RocksDBException occurred during deleteAll operation. {}", exception.getMessage());
            throw new DeleteAllFailedException(exception.getMessage(), exception);
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

    @SneakyThrows
    @Override
    public synchronized boolean delete_db() {
        w.lock();
        try {
            final RocksIterator iterator = rocksDB.newIterator();

            iterator.seekToFirst();
            final byte[] firstKey = getKey(iterator);

            iterator.seekToLast();
            final byte[] lastKey = getKey(iterator);


            if (firstKey != null || lastKey != null) {
                rocksDB.deleteRange(firstKey, lastKey);
                rocksDB.delete(lastKey);
            }

            boolean del = dbFile.delete();
            //RocksDB.destroyDB(Directory.getConfigPath(), options);

            if (this.databaseInstance != null)
                ZoneDatabaseFactory.closeDatabaseInstance(databaseInstance, options, dbFile.getAbsolutePath());
            else
                ZoneDatabaseFactory.closeDatabaseInstance(patriciaTreeInstance, options, dbFile.getAbsolutePath());

            if (options != null)
                options.close();
            if (rocksDB != null)
                rocksDB.close();
            rocksDB = null;
            if (databaseInstance.equals(DatabaseInstance.ZONE_0_TRANSACTION_BLOCK) || databaseInstance.equals(DatabaseInstance.ZONE_1_TRANSACTION_BLOCK) || databaseInstance.equals(DatabaseInstance.ZONE_2_TRANSACTION_BLOCK) || databaseInstance.equals(DatabaseInstance.ZONE_3_TRANSACTION_BLOCK))
                FileUtils.deleteDirectory(dbFile.getParentFile());
            FileUtils.deleteDirectory(dbFile);
            return del;
        } catch (NullPointerException exception) {
            LOGGER.error("RocksDBException occurred during delete_db operation. {}", exception.getMessage());
        } catch (final RocksDBException exception) {
            LOGGER.error("RocksDBException occurred during deleteAll operation. {}", exception.getMessage());
            throw new DeleteAllFailedException(exception.getMessage(), exception);
        } finally {
            w.unlock();
        }
        return true;
    }

    @Override
    public boolean erase_db() {
        w.lock();
        try {
            final RocksIterator iterator = rocksDB.newIterator();

            iterator.seekToFirst();
            final byte[] firstKey = getKey(iterator);

            iterator.seekToLast();
            final byte[] lastKey = getKey(iterator);


            if (firstKey != null || lastKey != null) {
                rocksDB.deleteRange(firstKey, lastKey);
                rocksDB.delete(lastKey);
            }

        } catch (NullPointerException exception) {
            LOGGER.error("RocksDBException occurred during delete_db operation. {}", exception.getMessage());
        } catch (final RocksDBException exception) {
            LOGGER.error("RocksDBException occurred during deleteAll operation. {}", exception.getMessage());
        } finally {
            w.unlock();
            return true;
        }
    }

    @SneakyThrows
    @Override
    public void closeNoDelete() {
        w.lock();
        try {
            rocksDB.close();
            options.close();
            rocksDB = null;
        } catch (NullPointerException exception) {
            LOGGER.error("RocksDBException occurred during delete_db operation. {}", exception.getMessage());
        } finally {
            w.unlock();
        }
        return;
    }

    private byte[] getKey(final RocksIterator iterator) {
        if (!iterator.isValid()) {
            return null;
        }
        return iterator.key();
    }

    @SneakyThrows
    @Override
    public Map<K, V> findBetweenRange(K key) {
        r.lock();
        Map<Object, Object> hashmap = new LinkedHashMap<>();
        try {
            final RocksIterator iterator = rocksDB.newIterator();
            iterator.seek(keyMapper.encode(key));
            do {
                byte[] serializedKey = iterator.key();
                byte[] serializedValue = iterator.value();
                final byte[] res = rocksDB.get(serializedKey);
                if (res == null) {
                    return (Map<K, V>) hashmap;
                }
                hashmap.put(keyMapper.decode(serializedKey), valueMapper.decode(serializedValue));
                iterator.next();
            } while (iterator.isValid());
        } catch (NullPointerException exception) {
            LOGGER.error("NullPointerException exception occurred during findBetweenRange operation. {}", exception.getMessage());
        } catch (final SerializationException exception) {
            LOGGER.error("Serialization exception occurred during findBetweenRange operation. {}", exception.getMessage());
        } finally {
            r.unlock();
        }
        return (Map<K, V>) hashmap;
    }

    @Override
    public Map<K, V> seekFromStart() {
        r.lock();
        Map<Object, Object> hashmap = new LinkedHashMap<>();
        try {
            final RocksIterator iterator = rocksDB.newIterator();
            iterator.seekToFirst();
            do {
                byte[] serializedKey = iterator.key();
                byte[] serializedValue = iterator.value();
                hashmap.put(keyMapper.decode(serializedKey), valueMapper.decode(serializedValue));
                iterator.next();
            } while (iterator.isValid());
        } catch (final SerializationException exception) {
            LOGGER.error("Serialization exception occurred during findByKey operation. {}", exception.getMessage());
        } finally {
            r.unlock();
        }
        return (Map<K, V>) hashmap;
    }

    @Override
    public Map<K, V> seekBetweenRange(int start, int finish) {
        r.lock();
        Map<Object, Object> hashmap = new LinkedHashMap<>();
        try {
            final RocksIterator iterator = rocksDB.newIterator();
            iterator.seekToFirst();
            while (iterator.isValid() && start <= finish) {
                byte[] serializedKey = iterator.key();
                byte[] serializedValue = iterator.value();
                hashmap.put(keyMapper.decode(serializedKey), valueMapper.decode(serializedValue));
                iterator.next();
                start++;
            }
        } catch (final SerializationException exception) {
            LOGGER.error("Serialization exception occurred during findByKey operation. {}", exception.getMessage());
        } finally {
            r.unlock();
        }
        return (Map<K, V>) hashmap;
    }


    @Override
    public Optional<V> seekLast() {
        r.lock();
        try {
            final RocksIterator iterator = rocksDB.newIterator();
            iterator.seekToLast();
            byte[] serializedValue = iterator.value();
            return (Optional<V>) Optional.of(valueMapper.decode(serializedValue));
        } catch (final SerializationException exception) {
            LOGGER.error("Serialization exception occurred during findByKey operation. {}", exception.getMessage());
        } catch (ArrayIndexOutOfBoundsException exception) {
            LOGGER.error("ArrayIndexOutOfBoundsException exception occurred during seekLast operation. {}", exception.getMessage());
            return Optional.empty();
        } finally {
            r.unlock();
        }
        return Optional.empty();
    }

    @Override
    public Optional<V> seekFirst() {
        r.lock();
        try {
            final RocksIterator iterator = rocksDB.newIterator();
            iterator.seekToFirst();
            byte[] serializedValue = iterator.value();
            return (Optional<V>) Optional.of(valueMapper.decode(serializedValue));
        } catch (final SerializationException exception) {
            LOGGER.error("Serialization exception occurred during findByKey operation. {}", exception.getMessage());
        } catch (ArrayIndexOutOfBoundsException exception) {
            LOGGER.error("ArrayIndexOutOfBoundsException exception occurred during seekFirst operation. {}", exception.getMessage());
            return Optional.empty();
        } finally {
            r.unlock();
        }
        return Optional.empty();
    }

    @SneakyThrows
    @Override
    public int findDBsize() {
        r.lock();
        try {
            final RocksIterator start_iterator = rocksDB.newIterator();
            start_iterator.seekToFirst();

            int entries = 0;

            while (start_iterator.isValid()) {
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

            try (RocksIterator itr = rocksDB.newIterator()) {
                itr.seekToFirst();

                return !itr.isValid();
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
                rocksDB.compactRange(new byte[]{(byte) 0x00}, new byte[]{(byte) 0xff});
            } catch (RocksDBException e) {
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
            return rocksDB != null;
        } finally {
            r.unlock();
        }
    }

}
