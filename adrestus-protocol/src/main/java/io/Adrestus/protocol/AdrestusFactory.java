package io.Adrestus.protocol;

import io.distributedLedger.DatabaseInstance;

public class AdrestusFactory implements IAdrestusFactory {
    @Override
    public AdrestusTask createTransactionTask() {
        return new TransactionTask();
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
    public AdrestusTask createRepositoryCommitteeTask() {
        return new RepositoryCommitteeTask();
    }
}
