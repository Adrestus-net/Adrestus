package io.Adrestus.protocol;

import io.distributedLedger.DatabaseInstance;
import io.distributedLedger.PatriciaTreeInstance;

public class AdrestusFactory implements IAdrestusFactory {
    @Override
    public AdrestusTask createTransactionTask() {
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
    public AdrestusTask createRepositoryCommitteeTask() {
        return new RepositoryCommitteeTask();
    }
}
