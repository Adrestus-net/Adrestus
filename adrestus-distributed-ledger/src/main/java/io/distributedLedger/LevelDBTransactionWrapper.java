package io.distributedLedger;

import io.Adrestus.core.Transaction;
import io.activej.serializer.annotations.Serialize;

import java.util.*;

public class LevelDBTransactionWrapper {
    private final LevelDBTransactionWrapper.TransactionHashComparator fromhashComparator;
    private final LevelDBTransactionWrapper.TransactionHashComparator tohashComparator;
    private ArrayList<Transaction> from;
    private ArrayList<Transaction> to;


    public LevelDBTransactionWrapper() {
        this.from = new ArrayList<>();
        this.to = new ArrayList<>();
        this.fromhashComparator = new LevelDBTransactionWrapper.TransactionHashComparator();
        this.tohashComparator = new LevelDBTransactionWrapper.TransactionHashComparator();
    }

    @Serialize
    public ArrayList<Transaction> getFrom() {
        return from;
    }

    @Serialize
    public ArrayList<Transaction> getTo() {
        return to;
    }

    public void setFrom(ArrayList<Transaction> from) {
        this.from = from;
    }

    public void setTo(ArrayList<Transaction> to) {
        this.to = to;
    }

    public boolean addFrom(Transaction transaction) {
        int index = Collections.binarySearch(from, transaction, fromhashComparator);
        if (index >= 0)
            return true;
        else if (index < 0) index = ~index;
        from.add(index, transaction);
        return false;
    }

    public boolean addTo(Transaction transaction) {
        int index = Collections.binarySearch(to, transaction, tohashComparator);
        if (index >= 0)
            return true;
        else if (index < 0) index = ~index;
        to.add(index, transaction);
        return false;
    }

    public Optional<Transaction> getTransactionFrom(String hash) throws Exception {
        Optional<Transaction> result = from.stream().filter(val -> val.getHash().equals(hash)).findFirst();
        return result;
    }

    public Optional<Transaction> getTransactionTo(String hash) throws Exception {
        Optional<Transaction> result = to.stream().filter(val -> val.getHash().equals(hash)).findFirst();
        return result;
    }

    public void deleteFrom(Transaction transaction) throws Exception {
        int index = Collections.binarySearch(from, transaction, fromhashComparator);
        if (index >= 0)
            from.remove(index);

    }

    public void deleteTo(Transaction transaction) throws Exception {
        int index = Collections.binarySearch(to, transaction, tohashComparator);
        if (index >= 0)
            from.remove(index);

    }

    public void deleteFrom(List<Transaction> list_transaction) throws Exception {
        from.removeAll(list_transaction);
    }

    public void deleteTo(List<Transaction> list_transaction) throws Exception {
        to.removeAll(list_transaction);
    }

    public void clearFrom() throws Exception {
        from.clear();
    }

    public void clearTo() throws Exception {
        to.clear();
    }

    @Override
    public String toString() {
        return "LevelDBTransactionWrapper{" +
                "from=" + from +
                ", to=" + to +
                '}';
    }

    private final class TransactionHashComparator implements Comparator<Transaction> {
        @Override
        public int compare(Transaction t1, Transaction t2) {
            return t1.getHash().compareTo(t2.getHash());
        }
    }
}
