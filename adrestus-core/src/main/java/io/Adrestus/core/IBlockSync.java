package io.Adrestus.core;

public interface IBlockSync {


    void WaitPatientlyYourPosition();

    void SyncState();

    void SyncBeaconChainState();

    void checkIfNeedsSync();

    void syncCommitBlock();
}
