package io.Adrestus.core;


import io.distributedLedger.DatabaseFactory;
import io.distributedLedger.DatabaseType;
import io.distributedLedger.IDatabase;
import io.distributedLedger.ZoneDatabaseFactory;
import lombok.SneakyThrows;

public class Genesis implements BlockForge {

    private final IDatabase<String, TransactionBlock> Zone0TransactionDatabase;
    private final IDatabase<String, TransactionBlock> Zone1TransactionDatabase;
    private final IDatabase<String, TransactionBlock> Zone2TransactionDatabase;
    private final IDatabase<String, TransactionBlock> Zone3TransactionDatabase;

    public Genesis() {
        this.Zone0TransactionDatabase = new DatabaseFactory(String.class, TransactionBlock.class).getDatabase(DatabaseType.ROCKS_DB, ZoneDatabaseFactory.getZoneInstance(0));
        this.Zone1TransactionDatabase = new DatabaseFactory(String.class, TransactionBlock.class).getDatabase(DatabaseType.ROCKS_DB, ZoneDatabaseFactory.getZoneInstance(1));
        this.Zone2TransactionDatabase = new DatabaseFactory(String.class, TransactionBlock.class).getDatabase(DatabaseType.ROCKS_DB, ZoneDatabaseFactory.getZoneInstance(2));
        this.Zone3TransactionDatabase = new DatabaseFactory(String.class, TransactionBlock.class).getDatabase(DatabaseType.ROCKS_DB, ZoneDatabaseFactory.getZoneInstance(3));
    }

    @SneakyThrows
    @Override
    public void forgeTransactionBlock(TransactionBlock transactionBlock) {
        System.out.println(transactionBlock.toString());
    }

    @Override
    public void forgeCommitteBlock(CommitteeBlock committeeBlock) {

    }

}
