package io.Adrestus.core.Resourses;

import gnu.trove.list.array.TLongArrayList;
import io.Adrestus.core.Transaction;

import java.util.Optional;
import java.util.stream.Stream;

public class InMemoryDao implements MemoryPool{
    private static TLongArrayList trove;


    public InMemoryDao() {
        trove = new TLongArrayList();
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
    public boolean add(Transaction customer) throws Exception {
        return false;
    }

    @Override
    public boolean update(Transaction customer) throws Exception {
        return false;
    }

    @Override
    public boolean delete(Transaction customer) throws Exception {
        return false;
    }
}
