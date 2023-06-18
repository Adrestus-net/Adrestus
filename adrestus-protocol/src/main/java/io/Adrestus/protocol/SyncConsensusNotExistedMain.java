package io.Adrestus.protocol;

import io.Adrestus.network.CachedEventLoop;
import io.distributedLedger.DatabaseInstance;
import io.distributedLedger.PatriciaTreeInstance;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SyncConsensusNotExistedMain {
    public static void main(String[] args) {
        String mnemonic="danger fluid abstract raise smart duty scare year add include ensure senior";
        String passphrase="p4ssphr4se";
        IAdrestusFactory factory = new AdrestusFactory();
        List<AdrestusTask> tasks = new java.util.ArrayList<>(List.of(factory.createBindServerKademliaTask(mnemonic,passphrase)));
        ExecutorService executor = Executors.newFixedThreadPool(tasks.size());
        tasks.stream().map(Worker::new).forEach(executor::execute);
        CachedEventLoop.getInstance().start();
    }
}
