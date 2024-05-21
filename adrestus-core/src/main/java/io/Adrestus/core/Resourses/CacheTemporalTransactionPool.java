package io.Adrestus.core.Resourses;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.Adrestus.TreeFactory;
import io.Adrestus.Trie.PatriciaTreeNode;
import io.Adrestus.config.CacheConfigurationTest;
import io.Adrestus.core.StatusType;
import io.Adrestus.core.Transaction;
import lombok.SneakyThrows;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

public class CacheTemporalTransactionPool implements ICacheTemporalTransactionPool {

    private Cache<String, List<Transaction>> loadingCache;

    private static volatile CacheTemporalTransactionPool instance;
    private final ScheduledExecutorService scheduler;

    private CacheTemporalTransactionPool() {
        if (instance != null) {
            throw new IllegalStateException("Already initialized.");
        }
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.loadingCache = Caffeine.newBuilder()
                .initialCapacity(CacheConfigurationTest.INITIAL_CAPACITY)
                .maximumSize(CacheConfigurationTest.MAXIMUM_SIZE)
                .expireAfterWrite(CacheConfigurationTest.EXPIRATION_MINUTES, TimeUnit.MINUTES)
                .expireAfterAccess(CacheConfigurationTest.EXPIRATION_MINUTES, TimeUnit.MINUTES)
                .build();
    }

    public void setup(boolean debug) {
        final Runnable beeper = new Runnable() {
            @SneakyThrows
            public void run() {
                if (loadingCache.estimatedSize() == 0)
                    return;

                for (ConcurrentHashMap.Entry<String, List<Transaction>> entry : loadingCache.asMap().entrySet()) {
                    Optional<Transaction> trx = get(entry.getKey());
                    if (trx.isPresent()) {
                        Transaction transaction = trx.get();
                        if (!MemoryTransactionPool.getInstance().checkAdressExists(transaction)) {
                            if (debug) {
                                PatriciaTreeNode patriciaTreeNode = TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(trx.get().getFrom()).get();

                                if (transaction.getNonce() == patriciaTreeNode.getNonce() + 1) {
                                    transaction.setStatus(StatusType.PENDING);
                                    MemoryRingBuffer.getInstance().publish(transaction);
                                    remove(entry.getKey(), transaction);
                                }
                            } else {
                                remove(entry.getKey(), transaction);
                            }
                        }
                        entry.getKey();
                    }
                }
            }
        };
        scheduler.scheduleAtFixedRate(beeper, 0, 500, TimeUnit.MILLISECONDS);
    }

    public static CacheTemporalTransactionPool getInstance() {
        var result = instance;
        if (result == null) {
            synchronized (CacheTemporalTransactionPool.class) {
                result = instance;
                if (result == null) {
                    instance = result = new CacheTemporalTransactionPool();
                }
            }
        }
        return result;
    }


    public void add(Transaction transaction) {
        ArrayList<Transaction> current = (ArrayList<Transaction>) loadingCache.getIfPresent(transaction.getFrom());
        if (current == null)
            current = new ArrayList<>();

        if (current.stream().anyMatch(transaction::equals))
            return;

        current.add(transaction);
        loadingCache.put(transaction.getFrom(), current);
    }

    public Optional<Transaction> get(String key) {
        ArrayList<Transaction> current = (ArrayList<Transaction>) loadingCache.getIfPresent(key);
        if (current == null) {
            loadingCache.invalidate(key);
            return Optional.empty();
        }
        Optional<Transaction> transaction = current.stream().min(Comparator.comparing(Transaction::getNonce));
        return transaction;
    }

    public boolean isExist(String key) {
        ArrayList<Transaction> current = (ArrayList<Transaction>) loadingCache.getIfPresent(key);
        return (current == null) ? false : true;
    }

    public void remove(String key, Transaction transaction) {
        ArrayList<Transaction> current = (ArrayList<Transaction>) loadingCache.getIfPresent(key);
        if (current == null) {
            loadingCache.invalidate(key);
            return;
        }

        current.removeIf(x -> transaction.getHash().equals(x.getHash()));

        if (current.isEmpty()) {
            loadingCache.invalidate(key);
            return;
        }

        loadingCache.put(key, current);
    }

    public void clear() {
        loadingCache.invalidateAll();
        loadingCache.cleanUp();
    }

    public long size() {
        return loadingCache.estimatedSize();
    }

    public ConcurrentMap<String, List<Transaction>> getAsMap() {
        return loadingCache.asMap();
    }
}

