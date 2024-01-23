package io.distributedLedger;

import io.activej.serializer.annotations.Serialize;
import lombok.SneakyThrows;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.*;

public class LevelDBReceiptWrapper<T> implements Serializable {
    private final LevelDBReceiptWrapper.ReceiptHashComparator toreceiptComparator;
    private ArrayList<T> to;


    public LevelDBReceiptWrapper() {
        this.to = new ArrayList<>();
        this.toreceiptComparator = new LevelDBReceiptWrapper.ReceiptHashComparator();
    }


    public ReceiptHashComparator getToreceiptComparator() {
        return toreceiptComparator;
    }

    @Serialize
    public ArrayList<T> getTo() {
        return to;
    }

    public void setTo(ArrayList<T> to) {
        this.to = to;
    }

    public boolean addTo(T receipt) {
        int index = Collections.binarySearch(to, receipt, toreceiptComparator);
        if (index >= 0)
            return true;
        else if (index < 0) index = ~index;
        to.add(index, receipt);
        return false;
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


    public void deleteTo(T receipt) throws Exception {
        int index = Collections.binarySearch(to, receipt, toreceiptComparator);
        if (index >= 0)
            to.remove(index);

    }

    public void deleteTo(List<T> list_receipt) throws Exception {
        to.removeAll(list_receipt);
    }

    public void clearTo() throws Exception {
        to.clear();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LevelDBReceiptWrapper<?> that = (LevelDBReceiptWrapper<?>) o;
        return Objects.equals(toreceiptComparator, that.toreceiptComparator) && Objects.equals(to, that.to);
    }

    @Override
    public int hashCode() {
        return Objects.hash(toreceiptComparator, to);
    }

    @Override
    public String toString() {
        return "LevelDBReceiptWrapper{" +
                "toreceiptComparator=" + toreceiptComparator +
                ", to=" + to +
                '}';
    }

    private final class ReceiptHashComparator implements Comparator<T>, Serializable {

        @SneakyThrows
        @Override
        public int compare(T o1, T o2) {
            Method m1 = Arrays.stream(o1.getClass().getMethods()).filter(val -> val.getName().equals("getTransaction_hash")).findFirst().get();
            Method m2 = Arrays.stream(o2.getClass().getMethods()).filter(val -> val.getName().equals("getTransaction_hash")).findFirst().get();
            return ((String) m1.invoke(o1)).compareTo((String) m2.invoke(o2));
        }
    }
}
