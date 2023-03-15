package io.Adrestus.protocol;

import io.distributedLedger.DatabaseInstance;
import io.distributedLedger.PatriciaTreeInstance;

public interface IAdrestusFactory {

    AdrestusTask createBindServerTransactionTask();

    AdrestusTask createBindServerReceiptTask();

    AdrestusTask createSendReceiptTask();

    AdrestusTask createRepositoryTransactionTask(DatabaseInstance databaseInstance);

    AdrestusTask createRepositoryCommitteeTask();

    AdrestusTask createRepositoryPatriciaTreeTask(PatriciaTreeInstance patriciaTreeInstance);
}
