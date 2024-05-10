package io.Adrestus.core.RingBuffer.handler.transactions;

import io.Adrestus.TreeFactory;
import io.Adrestus.Trie.StorageInfo;
import io.Adrestus.config.SocketConfigOptions;
import io.Adrestus.core.*;
import io.Adrestus.core.Resourses.CachedLatestBlocks;
import io.Adrestus.core.Resourses.CachedZoneIndex;
import io.Adrestus.core.Resourses.MemoryTransactionPool;
import io.Adrestus.core.RingBuffer.event.TransactionEvent;
import io.Adrestus.crypto.elliptic.mapper.BigIntegerSerializer;
import io.Adrestus.network.AsyncService;
import io.Adrestus.util.SerializationUtil;
import io.distributedLedger.DatabaseFactory;
import io.distributedLedger.DatabaseType;
import io.distributedLedger.IDatabase;
import io.distributedLedger.ZoneDatabaseFactory;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

public class ZoneEventHandler extends TransactionEventHandler implements TransactionUnitVisitor {
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

            transaction.accept(this);


        } catch (NullPointerException ex) {
            LOG.info("Transaction is empty");
        }
    }

    @SneakyThrows
    private void SendAsync(Transaction transaction) throws InterruptedException {
        LOG.info("Transaction: is not in the valid zone send async");
        MemoryTransactionPool.getInstance().delete(transaction);
        Transaction transactionToSend = (Transaction) transaction.clone();
        transactionToSend.setStatus(StatusType.PENDING);
        //make sure give enough time for block sync
        Thread.sleep(500);
        List<String> ips = CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(transactionToSend.getZoneFrom()).values().stream().collect(Collectors.toList());
        var executor = new AsyncService<Long>(ips, transaction_encode.encode(transactionToSend, 1024), SocketConfigOptions.TRANSACTION_PORT);

        var asyncResult1 = executor.startProcess(300L);
        final var result1 = executor.endProcess(asyncResult1);
    }

    @Override
    public void visit(RegularTransaction regularTransaction) {
        if (regularTransaction.getZoneFrom() != CachedZoneIndex.getInstance().getZoneIndex()) {
            ArrayList<StorageInfo> tosearch;
            try {
                tosearch = (ArrayList<StorageInfo>) TreeFactory.getMemoryTree(regularTransaction.getZoneFrom()).getByaddress(regularTransaction.getFrom()).get().retrieveTransactionInfoByHash(regularTransaction.getHash());
            } catch (NoSuchElementException e) {
                return;
            }
            for (int i = 0; i < tosearch.size(); i++) {
                IDatabase<String, TransactionBlock> transactionBlockIDatabase = new DatabaseFactory(String.class, TransactionBlock.class).getDatabase(DatabaseType.ROCKS_DB, ZoneDatabaseFactory.getZoneInstance(CachedZoneIndex.getInstance().getZoneIndex()));
                try {
                    Transaction trx = transactionBlockIDatabase.findByKey(String.valueOf(tosearch.get(i).getBlockHeight())).get().getTransactionList().get(tosearch.get(i).getPosition());
                    if (trx.equals(regularTransaction)) {
                        regularTransaction.setStatus(StatusType.BUFFERED);
                        return;
                    }
                } catch (NoSuchElementException e) {
                } catch (IndexOutOfBoundsException e) {
                }
            }

            regularTransaction.setStatus(StatusType.ABORT);
            try {
                SendAsync(regularTransaction);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void visit(RewardsTransaction rewardsTransaction) {
        if (CachedZoneIndex.getInstance().getZoneIndex() != rewardsTransaction.getZoneFrom() || CachedZoneIndex.getInstance().getZoneIndex() != 0) {
            LOG.info("Reward Transaction zone should be only in zone 0 abort");
            rewardsTransaction.setStatus(StatusType.ABORT);
            try {
                SendAsync(rewardsTransaction);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return;
    }

    @Override
    public void visit(StakingTransaction stakingTransaction) {

    }

    @Override
    public void visit(DelegateTransaction delegateTransaction) {

    }

    @Override
    public void visit(UnclaimedFeeRewardTransaction unclaimedFeeRewardTransaction) {

    }
}
