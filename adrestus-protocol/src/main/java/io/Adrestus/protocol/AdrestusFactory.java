package io.Adrestus.protocol;

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
        return new SendReceiptTask();
    }

    @Override
    public AdrestusTask createRepositoryTransactionTask() {
        return new RepositoryTransactionTask();
    }

    @Override
    public AdrestusTask createRepositoryCommitteeTask() {
        return new RepositoryCommitteeTask();
    }
}
