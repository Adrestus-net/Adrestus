package io.Adrestus.core.RingBuffer.handler.transactions;

import io.Adrestus.TreeFactory;
import io.Adrestus.Trie.StorageInfo;
import io.Adrestus.config.SocketConfigOptions;
import io.Adrestus.core.Resourses.CachedLatestBlocks;
import io.Adrestus.core.Resourses.CachedZoneIndex;
import io.Adrestus.core.Resourses.MemoryTransactionPool;
import io.Adrestus.core.RingBuffer.event.TransactionEvent;
import io.Adrestus.core.StatusType;
import io.Adrestus.core.Transaction;
import io.Adrestus.core.TransactionBlock;
import io.Adrestus.crypto.elliptic.mapper.BigIntegerSerializer;
import io.Adrestus.network.AsyncService;
import io.Adrestus.util.SerializationUtil;
import io.distributedLedger.DatabaseFactory;
import io.distributedLedger.DatabaseType;
import io.distributedLedger.IDatabase;
import io.distributedLedger.ZoneDatabaseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

public class ZoneEventHandler extends TransactionEventHandler {
    private static Logger LOG = LoggerFactory.getLogger(ZoneEventHandler.class);
    private final SerializationUtil<Transaction> transaction_encode;

    public ZoneEventHandler() {
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        transaction_encode = new SerializationUtil<Transaction>(Transaction.class, list);
    }

    @Override
    public void onEvent(TransactionEvent transactionEvent, long l, boolean b) throws Exception {
        try {
            Transaction transaction = transactionEvent.getTransaction();

            if (transaction.getStatus().equals(StatusType.BUFFERED) || transaction.getStatus().equals(StatusType.ABORT))
                return;

            if (transaction.getZoneFrom() != CachedZoneIndex.getInstance().getZoneIndex()) {
                ArrayList<StorageInfo> tosearch;
                try {
                    tosearch = (ArrayList<StorageInfo>) TreeFactory.getMemoryTree(transaction.getZoneFrom()).getByaddress(transaction.getFrom()).get().retrieveTransactionInfoByHash(transaction.getHash());
                } catch (NoSuchElementException e) {
                    return;
                }
                for (int i = 0; i < tosearch.size(); i++) {
                    IDatabase<String, TransactionBlock> transactionBlockIDatabase = new DatabaseFactory(String.class, TransactionBlock.class).getDatabase(DatabaseType.ROCKS_DB, ZoneDatabaseFactory.getZoneInstance(CachedZoneIndex.getInstance().getZoneIndex()));
                    try {
                        Transaction trx = transactionBlockIDatabase.findByKey(String.valueOf(tosearch.get(i).getBlockHeight())).get().getTransactionList().get(tosearch.get(i).getPosition());
                        if (trx.equals(transaction)) {
                            transaction.setStatus(StatusType.BUFFERED);
                            return;
                        }
                    } catch (NoSuchElementException e) {
                    } catch (IndexOutOfBoundsException e) {
                    }
                }

                SendAsync(transaction);
            }
        } catch (NullPointerException ex) {
            LOG.info("Transaction is empty");
        }
    }

    private void SendAsync(Transaction transaction) throws InterruptedException {
        LOG.info("Transaction abort: Transaction is not in the valid zone send async");

        //make sure give enough time for block sync
        Thread.sleep(500);
        List<String> ips = CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(transaction.getZoneFrom()).values().stream().collect(Collectors.toList());
        var executor = new AsyncService<Long>(ips, transaction_encode.encode(transaction, 1024), SocketConfigOptions.TRANSACTION_PORT);

        var asyncResult1 = executor.startProcess(300L);
        final var result1 = executor.endProcess(asyncResult1);
        transaction.setStatus(StatusType.ABORT);
        MemoryTransactionPool.getInstance().delete(transaction);
    }

}
