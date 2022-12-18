package io.distributedLedger;

import org.rocksdb.Options;
import org.rocksdb.RocksDB;

public class ZoneDatabaseFactory {

    public static RocksDB getDatabaseInstance(DatabaseInstance instance, Options options, String path) {
        switch (instance) {
            case COMMITTEE_BLOCK:
                return DatabaseZone.getInstance(options, path).getRocksDB();
            case ZONE_0_TRANSACTION_BLOCK:
                return DatabaseZone0.getInstance(options, path).getRocksDB();
            case ZONE_1_TRANSACTION_BLOCK:
                return DatabaseZone1.getInstance(options, path).getRocksDB();
            case ZONE_2_TRANSACTION_BLOCK:
                return DatabaseZone2.getInstance(options, path).getRocksDB();
            case ZONE_3_TRANSACTION_BLOCK:
                return DatabaseZone3.getInstance(options, path).getRocksDB();
            default:
                return DatabaseZone.getInstance(options, path).getRocksDB();
        }
    }


    public static void closeDatabaseInstance(DatabaseInstance instance, Options options, String path) {
        switch (instance) {
            case COMMITTEE_BLOCK:
                DatabaseZone.getInstance(options, path).close(options);
                break;
            case ZONE_0_TRANSACTION_BLOCK:
                DatabaseZone0.getInstance(options, path).close(options);
                break;
            case ZONE_1_TRANSACTION_BLOCK:
                DatabaseZone1.getInstance(options, path).close(options);
                break;
            case ZONE_2_TRANSACTION_BLOCK:
                DatabaseZone2.getInstance(options, path).close(options);
                break;
            case ZONE_3_TRANSACTION_BLOCK:
                DatabaseZone3.getInstance(options, path).close(options);
                break;
            default:
                DatabaseZone.getInstance(options, path).close(options);
                break;
        }
    }


    public static DatabaseInstance getZoneInstance(int zone) {
        switch (zone) {
            case 0:
                return DatabaseInstance.ZONE_0_TRANSACTION_BLOCK;
            case 1:
                return DatabaseInstance.ZONE_1_TRANSACTION_BLOCK;
            case 2:
                return DatabaseInstance.ZONE_2_TRANSACTION_BLOCK;
            case 3:
                return DatabaseInstance.ZONE_3_TRANSACTION_BLOCK;
            default:
                return DatabaseInstance.COMMITTEE_BLOCK;
        }
    }


}
