package io.Adrestus.core.RingBuffer.handler.transactions;

import io.Adrestus.TreeFactory;
import io.Adrestus.Trie.PatriciaTreeTransactionType;
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
    private ArrayList<StorageInfo> tosearch;

    public DuplicateEventHandler() {
        this.tosearch = new ArrayList<>();
    }

    @Override
    public void onEvent(TransactionEvent transactionEvent, long l, boolean b) throws Exception {
        Transaction transaction = transactionEvent.getTransaction();
        transaction.accept(this);
        if (tosearch.isEmpty())
            return;

        try {
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
        } catch (NullPointerException ex) {
            LOG.info("Transaction is empty");
        } finally {
            this.tosearch.clear();
        }
    }

    @Override
    public void visit(RegularTransaction regularTransaction) {
        try {
            tosearch = (ArrayList<StorageInfo>) TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(regularTransaction.getFrom()).get().retrieveTransactionInfoByHash(PatriciaTreeTransactionType.REGULAR, regularTransaction.getHash());
        } catch (NoSuchElementException e) {
            return;
        }
    }

    @Override
    public void visit(RewardsTransaction rewardsTransaction) {
        try {
            tosearch = (ArrayList<StorageInfo>) TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(rewardsTransaction.getRecipientAddress()).get().retrieveTransactionInfoByHash(PatriciaTreeTransactionType.REWARDS, rewardsTransaction.getHash());
        } catch (NoSuchElementException e) {
            return;
        }

    }

    @Override
    public void visit(StakingTransaction stakingTransaction) {
        try {
            tosearch = (ArrayList<StorageInfo>) TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(stakingTransaction.getValidatorAddress()).get().retrieveTransactionInfoByHash(PatriciaTreeTransactionType.STAKING, stakingTransaction.getHash());
        } catch (NoSuchElementException e) {
            return;
        }

    }

    @Override
    public void visit(DelegateTransaction delegateTransaction) {
        try {
            tosearch = (ArrayList<StorageInfo>) TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(delegateTransaction.getDelegatorAddress()).get().retrieveTransactionInfoByHash(PatriciaTreeTransactionType.DELEGATE, delegateTransaction.getHash());
        } catch (NoSuchElementException e) {
            return;
        }
    }

    @Override
    public void visit(UnclaimedFeeRewardTransaction unclaimedFeeRewardTransaction) {

    }

    @Override
    public void visit(UnDelegateTransaction unDelegateTransaction) {
        try {
            tosearch = (ArrayList<StorageInfo>) TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(unDelegateTransaction.getDelegatorAddress()).get().retrieveTransactionInfoByHash(PatriciaTreeTransactionType.UNDELEGATE, unDelegateTransaction.getHash());
        } catch (NoSuchElementException e) {
            return;
        }
    }

    @Override
    public void visit(UnstakingTransaction unstakingTransaction) {
        try {
            tosearch = (ArrayList<StorageInfo>) TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(unstakingTransaction.getValidatorAddress()).get().retrieveTransactionInfoByHash(PatriciaTreeTransactionType.UNSTAKING, unstakingTransaction.getHash());
        } catch (NoSuchElementException e) {
            return;
        }
    }
}
