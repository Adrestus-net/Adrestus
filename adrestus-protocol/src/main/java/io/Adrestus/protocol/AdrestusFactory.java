package io.Adrestus.protocol;

import io.Adrestus.crypto.bls.model.BLSPublicKey;
import io.Adrestus.crypto.elliptic.ECKeyPair;
import io.distributedLedger.DatabaseInstance;
import io.distributedLedger.PatriciaTreeInstance;

import java.security.SecureRandom;

public class AdrestusFactory implements IAdrestusFactory {
    @Override
    public AdrestusTask createBindServerTransactionTask() {
        return new BindServerTransactionTask();
    }

    @Override
    public AdrestusTask createBindServerReceiptTask() {
        return new BindServerReceiptTask();
    }

    @Override
    public AdrestusTask createSendReceiptTask() {
        return new ReceiptTask();
    }

    @Override
    public AdrestusTask createRepositoryTransactionTask(DatabaseInstance databaseInstance) {
        return new RepositoryTransactionTask(databaseInstance);
    }

    @Override
    public AdrestusTask createRepositoryPatriciaTreeTask(PatriciaTreeInstance patriciaTreeInstance) {
        return new RepositoryPatriciaTreeTask(patriciaTreeInstance);
    }

    @Override
    public AdrestusTask createBindServerKademliaTask() {
        return new BindServerKademliaTask();
    }

    @Override
    public AdrestusTask createBindServerKademliaTask(SecureRandom secureRandom,byte[] passphrase) {
        return new BindServerKademliaTask(secureRandom,passphrase);
    }

    @Override
    public AdrestusTask createBindServerKademliaTask(ECKeyPair keypair, BLSPublicKey blsPublicKey) {
        return new BindServerKademliaTask(keypair,blsPublicKey);
    }

    @Override
    public AdrestusTask createRepositoryCommitteeTask() {
        return new RepositoryCommitteeTask();
    }

    @Override
    public AdrestusTask createBindServerCachedTask() {
        return new BindServerCachedTask();
    }
}
