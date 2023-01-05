package io.distributedLedger;

import org.rocksdb.Options;
import org.rocksdb.RocksDB;

public class ZoneDatabaseFactory {

    public static RocksDB getDatabaseInstance(DatabaseInstance instance, Options options, String path) {
        switch (instance) {
            case COMMITTEE_BLOCK:
                return DatabaseCommitteeZone.getInstance(options, path).getRocksDB();
            case ZONE_0_TRANSACTION_BLOCK:
                return DatabaseTransactionZone0.getInstance(options, path).getRocksDB();
            case ZONE_1_TRANSACTION_BLOCK:
                return DatabaseTransactionZone1.getInstance(options, path).getRocksDB();
            case ZONE_2_TRANSACTION_BLOCK:
                return DatabaseTransactionZone2.getInstance(options, path).getRocksDB();
            case ZONE_3_TRANSACTION_BLOCK:
                return DatabaseTransactionZone3.getInstance(options, path).getRocksDB();
            default:
                return DatabaseCommitteeZone.getInstance(options, path).getRocksDB();
        }
    }

    public static RocksDB getDatabaseInstance(PatriciaTreeInstance instance, Options options, String path) {
        switch (instance) {
            case PATRICIA_TREE_INSTANCE_0:
                return DatabasePatriciaTreeZone0.getInstance(options, path).getRocksDB();
            case PATRICIA_TREE_INSTANCE_1:
                return DatabasePatriciaTreeZone1.getInstance(options, path).getRocksDB();
            case PATRICIA_TREE_INSTANCE_2:
                return DatabasePatriciaTreeZone2.getInstance(options, path).getRocksDB();
            case PATRICIA_TREE_INSTANCE_3:
                return DatabasePatriciaTreeZone3.getInstance(options, path).getRocksDB();
            default:
                return DatabasePatriciaTreeZone0.getInstance(options, path).getRocksDB();
        }
    }


    public static void closeDatabaseInstance(DatabaseInstance instance, Options options, String path) {
        switch (instance) {
            case COMMITTEE_BLOCK:
                DatabaseCommitteeZone.getInstance(options, path).close(options);
                break;
            case ZONE_0_TRANSACTION_BLOCK:
                DatabaseTransactionZone0.getInstance(options, path).close(options);
                break;
            case ZONE_1_TRANSACTION_BLOCK:
                DatabaseTransactionZone1.getInstance(options, path).close(options);
                break;
            case ZONE_2_TRANSACTION_BLOCK:
                DatabaseTransactionZone2.getInstance(options, path).close(options);
                break;
            case ZONE_3_TRANSACTION_BLOCK:
                DatabaseTransactionZone3.getInstance(options, path).close(options);
                break;
            default:
                DatabaseCommitteeZone.getInstance(options, path).close(options);
                break;
        }
    }

    public static void closeDatabaseInstance(PatriciaTreeInstance instance, Options options, String path) {
        switch (instance) {
            case PATRICIA_TREE_INSTANCE_0:
                DatabasePatriciaTreeZone0.getInstance(options, path).close(options);
                break;
            case PATRICIA_TREE_INSTANCE_1:
                DatabasePatriciaTreeZone1.getInstance(options, path).close(options);
                break;
            case PATRICIA_TREE_INSTANCE_2:
                DatabasePatriciaTreeZone2.getInstance(options, path).close(options);
                break;
            case PATRICIA_TREE_INSTANCE_3:
                DatabasePatriciaTreeZone3.getInstance(options, path).close(options);
                break;
            default:
                DatabasePatriciaTreeZone0.getInstance(options, path).close(options);
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

    public static PatriciaTreeInstance getPatriciaTreeZoneInstance(int zone) {
        switch (zone) {
            case 0:
                return PatriciaTreeInstance.PATRICIA_TREE_INSTANCE_0;
            case 1:
                return PatriciaTreeInstance.PATRICIA_TREE_INSTANCE_1;
            case 2:
                return PatriciaTreeInstance.PATRICIA_TREE_INSTANCE_2;
            case 3:
                return PatriciaTreeInstance.PATRICIA_TREE_INSTANCE_3;
            default:
                return PatriciaTreeInstance.PATRICIA_TREE_INSTANCE_0;
        }
    }


}
