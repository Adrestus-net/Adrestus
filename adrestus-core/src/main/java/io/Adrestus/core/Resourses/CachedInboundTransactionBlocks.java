package io.Adrestus.core.Resourses;

import io.Adrestus.core.CommitteeBlock;
import io.Adrestus.core.Receipt;
import io.Adrestus.core.TransactionBlock;
import io.Adrestus.network.CachedEventLoop;
import io.Adrestus.network.IPFinder;
import io.Adrestus.rpc.RpcAdrestusClient;
import io.distributedLedger.*;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

public class CachedInboundTransactionBlocks {
    private static volatile CachedInboundTransactionBlocks instance;
    private static ConcurrentMap<Integer, HashMap<Integer, TransactionBlock>> transactionBlockHashMap;

    private static Map<Integer, HashMap<Integer, HashSet<String>>> block_retrieval;

    private CachedInboundTransactionBlocks() {
        if (instance != null) {
            throw new IllegalStateException("Already initialized.");
        }
        transactionBlockHashMap = new ConcurrentHashMap<Integer, HashMap<Integer, TransactionBlock>>();
        block_retrieval = new HashMap<Integer, HashMap<Integer, HashSet<String>>>();
    }

    public static CachedInboundTransactionBlocks getInstance() {
        var result = instance;
        if (result == null) {
            synchronized (CachedInboundTransactionBlocks.class) {
                result = instance;
                if (result == null) {
                    result = new CachedInboundTransactionBlocks();
                    instance = result;
                }
            }
        }
        return result;
    }


    private boolean contains(Integer key1, Integer key2) {
        if (transactionBlockHashMap.containsKey(key1))
            if (transactionBlockHashMap.get(key1).containsKey(key2))
                return true;
        return false;
    }

    private void storeAll(Map<Integer, HashSet<String>> block_map) {
        for (Map.Entry<Integer, HashSet<String>> entry : block_map.entrySet()) {
            List<String> ips = CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(entry.getKey()).values().stream().collect(Collectors.toList());
            ips.remove(IPFinder.getLocalIP());

            int RPCTransactionZonePort = ZoneDatabaseFactory.getDatabaseRPCPort(entry.getKey());
            ArrayList<InetSocketAddress> toConnectTransaction = new ArrayList<>();
            ips.stream().forEach(ip -> {
                try {
                    toConnectTransaction.add(new InetSocketAddress(InetAddress.getByName(ip), RPCTransactionZonePort));
                } catch (UnknownHostException e) {
                    throw new RuntimeException(e);
                }
            });

            RpcAdrestusClient client = null;
            try {
                client = new RpcAdrestusClient(new TransactionBlock(), toConnectTransaction, CachedEventLoop.getInstance().getEventloop());
                client.connect();


                List<TransactionBlock> currentblock = client.getBlock(entry.getValue().stream().collect(Collectors.toList()));
                if (!currentblock.isEmpty()) {
                    HashMap<Integer, TransactionBlock> current = new HashMap<>();
                    currentblock.stream().forEach(val -> current.put(val.getHeight(), val));
                    transactionBlockHashMap.put(entry.getKey(), current);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (client != null) {
                    client.close();
                    client = null;
                }
            }
        }
    }

    public void StoreAll() {
        if (block_retrieval.isEmpty())
            return;


        IDatabase<String, CommitteeBlock> database = new DatabaseFactory(String.class, CommitteeBlock.class).getDatabase(DatabaseType.ROCKS_DB, DatabaseInstance.COMMITTEE_BLOCK);
        for (Map.Entry<Integer, HashMap<Integer, HashSet<String>>> entry : block_retrieval.entrySet()) {
            for (Map.Entry<Integer, HashSet<String>> entry2 : entry.getValue().entrySet()) {
                Optional<CommitteeBlock> committeeBlock=database.findByKey(String.valueOf(entry2.getKey()));
                if(!committeeBlock.isPresent()){
                    throw new IllegalArgumentException("Cannot find commit block for this generation");
                }
                List<String> ips = committeeBlock.get().getStructureMap().get(entry.getKey()).values().stream().collect(Collectors.toList());
                ips.remove(IPFinder.getLocalIP());

                int RPCTransactionZonePort = ZoneDatabaseFactory.getDatabaseRPCPort(entry.getKey());
                ArrayList<InetSocketAddress> toConnectTransaction = new ArrayList<>();
                ips.stream().forEach(ip -> {
                    try {
                        toConnectTransaction.add(new InetSocketAddress(InetAddress.getByName(ip), RPCTransactionZonePort));
                    } catch (UnknownHostException e) {
                        throw new RuntimeException(e);
                    }
                });

                RpcAdrestusClient client = null;
                try {
                    client = new RpcAdrestusClient(new TransactionBlock(), toConnectTransaction, CachedEventLoop.getInstance().getEventloop());
                    client.connect();


                    List<TransactionBlock> currentblock = client.getBlock(entry2.getValue().stream().collect(Collectors.toList()));
                    if (!currentblock.isEmpty()) {
                        HashMap<Integer, TransactionBlock> current = new HashMap<>();
                        currentblock.stream().forEach(val -> current.put(val.getHeight(), val));
                        transactionBlockHashMap.put(entry.getKey(), current);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (client != null) {
                        client.close();
                        client = null;
                    }
                }
            }
        }
    }

    public TransactionBlock retrieve(int zoneFrom, int height) {
        return transactionBlockHashMap.get(zoneFrom).get(height);
    }

    public void store(int zoneFrom, HashMap<Integer, TransactionBlock> map) {
        transactionBlockHashMap.put(zoneFrom, map);
    }

    public void clear() {
        block_retrieval.clear();
        transactionBlockHashMap.clear();
    }

    public void generate(LinkedHashMap<Integer, LinkedHashMap<Receipt.ReceiptBlock, List<Receipt>>> inboundmap) {
        if (inboundmap.isEmpty())
            return;

        Map<Integer, HashSet<String>> block_retrieval = new HashMap<Integer, HashSet<String>>();
        for (Map.Entry<Integer, LinkedHashMap<Receipt.ReceiptBlock, List<Receipt>>> entry : inboundmap.entrySet()) {
            HashSet<String> height_list = new HashSet<>();
            for (Map.Entry<Receipt.ReceiptBlock, List<Receipt>> entry2 : entry.getValue().entrySet()) {
                if (!contains(entry.getKey(), entry2.getKey().getHeight()))
                    height_list.add(String.valueOf(entry2.getKey().getHeight()));
            }
            block_retrieval.put(entry.getKey(), height_list);
        }
        CachedInboundTransactionBlocks.getInstance().storeAll(block_retrieval);

    }

    public void prepare(LinkedHashMap<Integer, LinkedHashMap<Receipt.ReceiptBlock, List<Receipt>>> inboundmap) {
        if (inboundmap.isEmpty())
            return;


        for (Map.Entry<Integer, LinkedHashMap<Receipt.ReceiptBlock, List<Receipt>>> entry : inboundmap.entrySet()) {
            if (block_retrieval.containsKey(entry.getKey())) {
                HashMap<Integer, HashSet<String>> gen_list = block_retrieval.get(entry.getKey());
                for (Map.Entry<Receipt.ReceiptBlock, List<Receipt>> entry2 : entry.getValue().entrySet()) {
                    HashSet<String> height_list = null;
                    if (!contains(entry.getKey(), entry2.getKey().getHeight())) {
                        if (gen_list.containsKey(entry2.getKey().getGeneration())) {
                            height_list = gen_list.get(entry2.getKey().getGeneration());
                        } else {
                            height_list = new HashSet<>();
                        }
                        if (!contains(entry.getKey(), entry2.getKey().getHeight()))
                            height_list.add(String.valueOf(entry2.getKey().getHeight()));
                    }
                    gen_list.put(entry2.getKey().getGeneration(), height_list);
                }
                block_retrieval.put(entry.getKey(), gen_list);
            } else {
                HashMap<Integer, HashSet<String>> gen_list = new HashMap<Integer, HashSet<String>>();
                for (Map.Entry<Receipt.ReceiptBlock, List<Receipt>> entry2 : entry.getValue().entrySet()) {
                    HashSet<String> height_list = null;
                    if (!contains(entry.getKey(), entry2.getKey().getHeight())) {
                        if (gen_list.containsKey(entry2.getKey().getGeneration())) {
                            height_list = gen_list.get(entry2.getKey().getGeneration());
                        } else {
                            height_list = new HashSet<>();
                        }
                        if (!contains(entry.getKey(), entry2.getKey().getHeight()))
                            height_list.add(String.valueOf(entry2.getKey().getHeight()));
                    }
                    gen_list.put(entry2.getKey().getGeneration(), height_list);
                }
                block_retrieval.put(entry.getKey(), gen_list);
            }
        }
    }

    public ConcurrentMap<Integer, HashMap<Integer, TransactionBlock>> getTransactionBlockHashMap() {
        return transactionBlockHashMap;
    }

    public static Map<Integer, HashMap<Integer, HashSet<String>>> getBlock_retrieval() {
        return block_retrieval;
    }
}
