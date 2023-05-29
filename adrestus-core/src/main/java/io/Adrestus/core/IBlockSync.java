package io.Adrestus.core;

public interface IBlockSync {


    void WaitPatientlyYourPosition();

    void SyncState();

    void checkIfNeedsSync();

    void syncCommitBlock();
}
