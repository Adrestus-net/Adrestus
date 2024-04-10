package io.Adrestus.core.Resourses;

import io.Adrestus.core.Receipt;
import io.Adrestus.core.TransactionBlock;
import io.Adrestus.network.CachedEventLoop;
import io.Adrestus.network.IPFinder;
import io.Adrestus.rpc.RpcAdrestusClient;
import io.distributedLedger.ZoneDatabaseFactory;

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

    private CachedInboundTransactionBlocks() {
        if (instance != null) {
            throw new IllegalStateException("Already initialized.");
        }
        transactionBlockHashMap = new ConcurrentHashMap<Integer, HashMap<Integer, TransactionBlock>>();
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

    private void storeAll(Map<Integer, ArrayList<String>> block_map) {
        for (Map.Entry<Integer, ArrayList<String>> entry : block_map.entrySet()) {
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


                List<TransactionBlock> currentblock = client.getBlock(entry.getValue());
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

    public TransactionBlock retrieve(int zoneFrom, int height) {
        return transactionBlockHashMap.get(zoneFrom).get(height);
    }

    public void store(int zoneFrom, HashMap<Integer, TransactionBlock> map){
        transactionBlockHashMap.put(zoneFrom,map);
    }

    public void clear(){
        transactionBlockHashMap.clear();
    }
    public void generate(LinkedHashMap<Integer, LinkedHashMap<Receipt.ReceiptBlock, List<Receipt>>> inboundmap) {
        if (inboundmap.isEmpty())
            return;

        Map<Integer, ArrayList<String>> block_retrieval = new HashMap<Integer, ArrayList<String>>();
        for (Map.Entry<Integer, LinkedHashMap<Receipt.ReceiptBlock, List<Receipt>>> entry : inboundmap.entrySet()) {
            ArrayList<String> height_list = new ArrayList<>();
            for (Map.Entry<Receipt.ReceiptBlock, List<Receipt>> entry2 : entry.getValue().entrySet()) {
                if (!contains(entry.getKey(), entry2.getKey().getHeight()))
                    height_list.add(String.valueOf(entry2.getKey().getHeight()));
            }
            block_retrieval.put(entry.getKey(), height_list);
        }
        CachedInboundTransactionBlocks.getInstance().storeAll(block_retrieval);
    }


}
