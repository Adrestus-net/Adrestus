package io.Adrestus.protocol;

import io.Adrestus.crypto.bls.model.BLSPublicKey;
import io.Adrestus.crypto.elliptic.ECKeyPair;
import io.activej.eventloop.Eventloop;
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

    AdrestusTask createBindServerKademliaTask(ECKeyPair keypair, BLSPublicKey blsPublicKey);

    AdrestusTask createBindServerCachedTask();
}
