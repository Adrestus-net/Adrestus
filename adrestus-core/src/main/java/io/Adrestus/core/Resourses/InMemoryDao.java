package io.Adrestus.core.Resourses;

import gnu.trove.list.array.TLongArrayList;
import io.Adrestus.core.Transaction;

import java.util.Optional;
import java.util.stream.Stream;

public class InMemoryDao implements MemoryPool {
    private static TLongArrayList memorypool;


    public InMemoryDao() {
        memorypool = new TLongArrayList();
    }

    @Override
    public Stream<Transaction> getAll() throws Exception {
        //trove.iterator()
        return null;
    }

    @Override
    public Optional<Transaction> getById(int id) throws Exception {
        return Optional.empty();
    }

    @Override
    public boolean add(Transaction transaction) throws Exception {
        return false;
    }

    @Override
    public boolean update(Transaction transaction) throws Exception {
        return false;
    }

    @Override
    public boolean delete(Transaction transaction) throws Exception {
        return false;
    }

    @Override
    public boolean checkHashExists(Transaction transaction) throws Exception {
        return false;
    }

    @Override
    public boolean checkTimestamp(Transaction transaction) throws Exception {
        return false;
    }
}
