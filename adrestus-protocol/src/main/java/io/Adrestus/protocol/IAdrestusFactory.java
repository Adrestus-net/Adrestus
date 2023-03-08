package io.Adrestus.protocol;

import io.distributedLedger.DatabaseInstance;

public interface IAdrestusFactory {

    AdrestusTask createTransactionTask();

    AdrestusTask createBindServerReceiptTask();

    AdrestusTask createSendReceiptTask();

    AdrestusTask createRepositoryTransactionTask(DatabaseInstance databaseInstance);

    AdrestusTask createRepositoryCommitteeTask();
}
