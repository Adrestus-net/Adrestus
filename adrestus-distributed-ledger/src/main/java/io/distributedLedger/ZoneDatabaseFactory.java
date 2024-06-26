package io.distributedLedger;

import io.Adrestus.config.NetworkConfiguration;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;

public class ZoneDatabaseFactory {

    public static RocksDB getDatabaseInstance(DatabaseInstance instance, Options options, String path) {
        switch (instance) {
            case COMMITTEE_BLOCK:
                return DatabaseCommitteeZone.getInstance(options, path).getDB();
            case ZONE_0_TRANSACTION_BLOCK:
                return DatabaseTransactionZone0.getInstance(options, path).getDB();
            case ZONE_1_TRANSACTION_BLOCK:
                return DatabaseTransactionZone1.getInstance(options, path).getDB();
            case ZONE_2_TRANSACTION_BLOCK:
                return DatabaseTransactionZone2.getInstance(options, path).getDB();
            case ZONE_3_TRANSACTION_BLOCK:
                return DatabaseTransactionZone3.getInstance(options, path).getDB();
            default:
                return DatabaseCommitteeZone.getInstance(options, path).getDB();
        }
    }

    public static RocksDB getDatabaseInstance(PatriciaTreeInstance instance, Options options, String path) {
        switch (instance) {
            case PATRICIA_TREE_INSTANCE_0:
                return DatabasePatriciaTreeZone0.getInstance(options, path).getDB();
            case PATRICIA_TREE_INSTANCE_1:
                return DatabasePatriciaTreeZone1.getInstance(options, path).getDB();
            case PATRICIA_TREE_INSTANCE_2:
                return DatabasePatriciaTreeZone2.getInstance(options, path).getDB();
            case PATRICIA_TREE_INSTANCE_3:
                return DatabasePatriciaTreeZone3.getInstance(options, path).getDB();
            default:
                return DatabasePatriciaTreeZone0.getInstance(options, path).getDB();
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

    public static boolean isDatabaseInstanceClosed(DatabaseInstance instance) {
        switch (instance) {
            case COMMITTEE_BLOCK:
                return DatabaseCommitteeZone.isNull();
            case ZONE_0_TRANSACTION_BLOCK:
                return DatabaseTransactionZone0.isNull();
            case ZONE_1_TRANSACTION_BLOCK:
                return DatabaseTransactionZone1.isNull();
            case ZONE_2_TRANSACTION_BLOCK:
                return DatabaseTransactionZone2.isNull();
            case ZONE_3_TRANSACTION_BLOCK:
                return DatabaseTransactionZone3.isNull();
            default:
                return DatabaseTransactionZone0.isNull();
        }
    }

    public static boolean isPatriciaTreeInstanceClosed(PatriciaTreeInstance instance) {
        switch (instance) {
            case PATRICIA_TREE_INSTANCE_0:
                return DatabasePatriciaTreeZone0.isNull();
            case PATRICIA_TREE_INSTANCE_1:
                return DatabasePatriciaTreeZone1.isNull();
            case PATRICIA_TREE_INSTANCE_2:
                return DatabasePatriciaTreeZone2.isNull();
            case PATRICIA_TREE_INSTANCE_3:
                return DatabasePatriciaTreeZone3.isNull();
            default:
                return DatabasePatriciaTreeZone0.isNull();
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

    public static int getDatabaseRPCPort(int zone) {
        switch (zone) {
            case 0:
                return NetworkConfiguration.ZONE0_RPC_PORT;
            case 1:
                return NetworkConfiguration.ZONE1_RPC_PORT;
            case 2:
                return NetworkConfiguration.ZONE2_RPC_PORT;
            case 3:
                return NetworkConfiguration.ZONE3_RPC_PORT;
            default:
                return NetworkConfiguration.ZONE0_RPC_PORT;
        }
    }

    public static int getDatabaseRPCPort(DatabaseInstance instance) {
        switch (instance) {
            case ZONE_0_TRANSACTION_BLOCK:
                return NetworkConfiguration.ZONE0_RPC_PORT;
            case ZONE_1_TRANSACTION_BLOCK:
                return NetworkConfiguration.ZONE1_RPC_PORT;
            case ZONE_2_TRANSACTION_BLOCK:
                return NetworkConfiguration.ZONE2_RPC_PORT;
            case ZONE_3_TRANSACTION_BLOCK:
                return NetworkConfiguration.ZONE3_RPC_PORT;
            default:
                return NetworkConfiguration.ZONE0_RPC_PORT;
        }
    }

    public static int getDatabasePatriciaRPCPort(PatriciaTreeInstance instance) {
        switch (instance) {
            case PATRICIA_TREE_INSTANCE_0:
                return NetworkConfiguration.PATRICIATREE_ZONE0_RPC_PORT;
            case PATRICIA_TREE_INSTANCE_1:
                return NetworkConfiguration.PATRICIATREE_ZONE1_RPC_PORT;
            case PATRICIA_TREE_INSTANCE_2:
                return NetworkConfiguration.PATRICIATREE_ZONE2_RPC_PORT;
            case PATRICIA_TREE_INSTANCE_3:
                return NetworkConfiguration.PATRICIATREE_ZONE3_RPC_PORT;
            default:
                return NetworkConfiguration.PATRICIATREE_ZONE0_RPC_PORT;
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

    public static int getPatriciaTreeZoneInstance(PatriciaTreeInstance patriciaTreeInstance) {
        switch (patriciaTreeInstance) {
            case PATRICIA_TREE_INSTANCE_0:
                return 0;
            case PATRICIA_TREE_INSTANCE_1:
                return 1;
            case PATRICIA_TREE_INSTANCE_2:
                return 2;
            case PATRICIA_TREE_INSTANCE_3:
                return 3;
            default:
                return 0;
        }
    }


}
