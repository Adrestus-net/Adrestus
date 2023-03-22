package io.Adrestus.protocol;

import io.distributedLedger.DatabaseInstance;
import io.distributedLedger.PatriciaTreeInstance;

import java.security.SecureRandom;

public interface IAdrestusFactory {

    AdrestusTask createBindServerTransactionTask();

    AdrestusTask createBindServerReceiptTask();

    AdrestusTask createSendReceiptTask();

    AdrestusTask createRepositoryTransactionTask(DatabaseInstance databaseInstance);

    AdrestusTask createRepositoryCommitteeTask();

    AdrestusTask createRepositoryPatriciaTreeTask(PatriciaTreeInstance patriciaTreeInstance);

    AdrestusTask createBindServerKademliaTask();

    AdrestusTask createBindServerKademliaTask(SecureRandom secureRandom, byte[] passphrase);

    AdrestusTask createBindServerCachedTask();
}
