package io.Adrestus.core.RingBuffer.handler.transactions;

import io.Adrestus.TreeFactory;
import io.Adrestus.Trie.StorageInfo;
import io.Adrestus.core.*;
import io.Adrestus.core.Resourses.CachedZoneIndex;
import io.Adrestus.core.RingBuffer.event.TransactionEvent;
import io.distributedLedger.DatabaseFactory;
import io.distributedLedger.DatabaseType;
import io.distributedLedger.IDatabase;
import io.distributedLedger.ZoneDatabaseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.NoSuchElementException;

public class DuplicateEventHandler extends TransactionEventHandler implements TransactionUnitVisitor {
    private static Logger LOG = LoggerFactory.getLogger(DuplicateEventHandler.class);


    public DuplicateEventHandler() {
    }

    @Override
    public void onEvent(TransactionEvent transactionEvent, long l, boolean b) throws Exception {
        Transaction transaction = transactionEvent.getTransaction();
        transaction.accept(this);
    }

    @Override
    public void visit(RegularTransaction regularTransaction) {
        try {
            ArrayList<StorageInfo> tosearch;
            try {
                tosearch = (ArrayList<StorageInfo>) TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(regularTransaction.getFrom()).get().retrieveTransactionInfoByHash(regularTransaction.getHash());
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

        } catch (NullPointerException ex) {
            LOG.info("Transaction is empty");
        }
    }

    @Override
    public void visit(RewardsTransaction rewardsTransaction) {
        try {
            ArrayList<StorageInfo> tosearch;
            try {
                tosearch = (ArrayList<StorageInfo>) TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(rewardsTransaction.getRecipientAddress()).get().retrieveTransactionInfoByHash(rewardsTransaction.getHash());
            } catch (NoSuchElementException e) {
                return;
            }
            for (int i = 0; i < tosearch.size(); i++) {
                IDatabase<String, TransactionBlock> transactionBlockIDatabase = new DatabaseFactory(String.class, TransactionBlock.class).getDatabase(DatabaseType.ROCKS_DB, ZoneDatabaseFactory.getZoneInstance(CachedZoneIndex.getInstance().getZoneIndex()));
                try {
                    Transaction trx = transactionBlockIDatabase.findByKey(String.valueOf(tosearch.get(i).getBlockHeight())).get().getTransactionList().get(tosearch.get(i).getPosition());
                    if (trx.equals(rewardsTransaction)) {
                        rewardsTransaction.setStatus(StatusType.BUFFERED);
                        return;
                    }
                } catch (NoSuchElementException e) {
                } catch (IndexOutOfBoundsException e) {

                }
            }

        } catch (NullPointerException ex) {
            LOG.info("Transaction is empty");
        }
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
