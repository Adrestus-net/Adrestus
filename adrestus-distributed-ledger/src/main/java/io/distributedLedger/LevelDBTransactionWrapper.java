package io.distributedLedger;

import io.activej.serializer.annotations.Serialize;
import lombok.SneakyThrows;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.*;

// This class is useless and should be removed in the future
public class LevelDBTransactionWrapper<T> implements Serializable {
    private final LevelDBTransactionWrapper.TransactionHashComparator fromhashComparator;
    private final LevelDBTransactionWrapper.TransactionHashComparator tohashComparator;
    private ArrayList<T> from;
    private ArrayList<T> to;


    public LevelDBTransactionWrapper() {
        this.from = new ArrayList<>();
        this.to = new ArrayList<>();
        this.fromhashComparator = new LevelDBTransactionWrapper.TransactionHashComparator();
        this.tohashComparator = new LevelDBTransactionWrapper.TransactionHashComparator();
    }

    @Serialize
    public ArrayList<T> getFrom() {
        return from;
    }

    @Serialize
    public ArrayList<T> getTo() {
        return to;
    }

    public void setFrom(ArrayList<T> from) {
        this.from = from;
    }

    public void setTo(ArrayList<T> to) {
        this.to = to;
    }

    public boolean addFrom(T transaction) {
        int index = Collections.binarySearch(from, transaction, fromhashComparator);
        if (index >= 0)
            return true;
        else if (index < 0) index = ~index;
        from.add(index, transaction);
        return false;
    }

    public boolean addTo(T transaction) {
        int index = Collections.binarySearch(to, transaction, tohashComparator);
        if (index >= 0)
            return true;
        else if (index < 0) index = ~index;
        to.add(index, transaction);
        return false;
    }

    public Optional<T> getTransactionFrom(String hash) throws Exception {
        Optional<T> result = from.stream().filter(val -> {
            try {
                return val.getClass().getDeclaredMethod("getHash", val.getClass().getClasses()).invoke(val).equals(hash);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }).findFirst();
        return result;
    }

    public Optional<T> getTransactionTo(String hash) throws Exception {
        Optional<T> result = to.stream().filter(val -> {
            try {
                return val.getClass().getDeclaredMethod("getHash", val.getClass().getClasses()).invoke(val).equals(hash);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }).findFirst();
        return result;
    }

    public void deleteFrom(T transaction) throws Exception {
        int index = Collections.binarySearch(from, transaction, fromhashComparator);
        if (index >= 0)
            from.remove(index);

    }

    public void deleteTo(T transaction) throws Exception {
        int index = Collections.binarySearch(to, transaction, tohashComparator);
        if (index >= 0)
            to.remove(index);

    }

    public void deleteFrom(List<T> list_transaction) throws Exception {
        from.removeAll(list_transaction);
    }

    public void deleteTo(List<T> list_transaction) throws Exception {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LevelDBTransactionWrapper<?> that = (LevelDBTransactionWrapper<?>) o;
        return Objects.equals(from, that.from) && Objects.equals(to, that.to);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fromhashComparator, tohashComparator, from, to);
    }

    private final class TransactionHashComparator implements Comparator<T>, Serializable {
        @SneakyThrows
        @Override
        public int compare(T o1, T o2) {
            Method m1 = o1.getClass().getDeclaredMethod("getHash", o1.getClass().getClasses());
            Method m2 = o2.getClass().getDeclaredMethod("getHash", o1.getClass().getClasses());
            return ((String) m1.invoke(o1)).compareTo((String) m2.invoke(o2));
        }
    }
}
