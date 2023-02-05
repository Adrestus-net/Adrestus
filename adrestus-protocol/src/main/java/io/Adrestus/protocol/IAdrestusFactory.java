package io.Adrestus.protocol;

public interface IAdrestusFactory {

    AdrestusTask createTransactionTask();

    AdrestusTask createBindServerReceiptTask();

    AdrestusTask createSendReceiptTask();

    AdrestusTask createRepositoryTransactionTask();

    AdrestusTask createRepositoryCommitteeTask();
}
