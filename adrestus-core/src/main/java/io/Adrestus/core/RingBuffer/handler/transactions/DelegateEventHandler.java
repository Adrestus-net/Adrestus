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

import java.util.*;

public class DelegateEventHandler extends TransactionEventHandler implements TransactionUnitVisitor {
    private static Logger LOG = LoggerFactory.getLogger(DelegateEventHandler.class);

    public DelegateEventHandler() {
    }

    @Override
    public void onEvent(TransactionEvent transactionEvent, long l, boolean b) throws Exception {
        Transaction transaction = transactionEvent.getTransaction();
        if (transaction.getStatus().equals(StatusType.BUFFERED) || transaction.getStatus().equals(StatusType.ABORT))
            return;
        transaction.accept(this);

    }

    @Override
    public void visit(RegularTransaction regularTransaction) {

    }

    @Override
    public void visit(RewardsTransaction rewardsTransaction) {

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

    @Override
    public void visit(UnDelegateTransaction unDelegateTransaction) {
        IDatabase<String, TransactionBlock> transactionBlockIDatabase = new DatabaseFactory(String.class, TransactionBlock.class).getDatabase(DatabaseType.ROCKS_DB, ZoneDatabaseFactory.getZoneInstance(CachedZoneIndex.getInstance().getZoneIndex()));
        String res = this.findFingerprint(unDelegateTransaction);
        if (res.isEmpty()) {
            Optional.of("UnDelegateTransaction fails because delegator has not delegate to any validator").ifPresent(val -> {
                LOG.info(val);
                unDelegateTransaction.infos(val);
            });
            unDelegateTransaction.setStatus(StatusType.ABORT);
            return;
        }
        ArrayList<StorageInfo> tosearch = (ArrayList<StorageInfo>) TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(unDelegateTransaction.getValidatorAddress()).get().retrieveTransactionInfoByHash(PatriciaTreeTransactionType.DELEGATE, res);
        for (int i = 0; i < tosearch.size(); i++) {
            try {
                DelegateTransaction trx = (DelegateTransaction) transactionBlockIDatabase.findByKey(String.valueOf(tosearch.get(i).getBlockHeight())).get().getTransactionList().get(tosearch.get(i).getPosition());
                if (trx.getHash().equals(res)) {
                    return;
                }
            } catch (NoSuchElementException e) {
            } catch (IndexOutOfBoundsException e) {
            }
        }
        Optional.of("UnDelegate Transaction failed because delegating transaction has not been found on the validator list").ifPresent(val -> {
            LOG.info(val);
            unDelegateTransaction.infos(val);
        });
        unDelegateTransaction.setStatus(StatusType.ABORT);
        return;
    }

    @Override
    public void visit(UnstakingTransaction unstakingTransaction) {

    }

    private String findFingerprint(UnDelegateTransaction unDelegateTransaction) {
        IDatabase<String, TransactionBlock> transactionBlockIDatabase = new DatabaseFactory(String.class, TransactionBlock.class).getDatabase(DatabaseType.ROCKS_DB, ZoneDatabaseFactory.getZoneInstance(CachedZoneIndex.getInstance().getZoneIndex()));
        HashMap<Integer, HashSet<Integer>> mapsearch;
        try {
            mapsearch = TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(unDelegateTransaction.getDelegatorAddress()).get().retrieveAllTransactionsByOriginZone(PatriciaTreeTransactionType.DELEGATE, CachedZoneIndex.getInstance().getZoneIndex());
        } catch (NoSuchElementException e) {
            return "";
        }
        if (mapsearch.isEmpty())
            return "";

        for (Map.Entry<Integer, HashSet<Integer>> entry : mapsearch.entrySet()) {
            try {
                for (int position : entry.getValue()) {
                    DelegateTransaction trx = (DelegateTransaction) transactionBlockIDatabase.findByKey(String.valueOf(entry.getKey())).get().getTransactionList().get(position);
                    if (trx.getDelegatorAddress().equals(unDelegateTransaction.getDelegatorAddress()) && trx.getValidatorAddress().equals(unDelegateTransaction.getValidatorAddress())) {
                        return trx.getHash();
                    }
                }
            } catch (NoSuchElementException e) {
            } catch (IndexOutOfBoundsException e) {
            }
        }
        return "";
    }
}
