package io.Adrestus.core;


import io.Adrestus.util.GetTime;
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
        TransactionBlock TransactionBlockZone2 = new TransactionBlock();
        TransactionBlockZone2.setHeight(1);
        TransactionBlockZone2.setHash("TransactionBlockZone0");
        TransactionBlockZone2.getHeaderData().setTimestamp(GetTime.GetTimeStampInString());
        Thread.sleep(200);
        TransactionBlock TransactionBlockZone3 = new TransactionBlock();
        TransactionBlockZone3.setHeight(1);
        TransactionBlockZone3.setHash("TransactionBlockZone0");
        TransactionBlockZone3.getHeaderData().setTimestamp(GetTime.GetTimeStampInString());

        this.Zone2TransactionDatabase.save("1", TransactionBlockZone2);
        this.Zone3TransactionDatabase.save("1", TransactionBlockZone3);
    }

    @Override
    public void forgeCommitteBlock(CommitteeBlock committeeBlock) {

    }

}
