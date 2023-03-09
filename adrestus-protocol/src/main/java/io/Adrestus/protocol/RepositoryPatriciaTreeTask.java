package io.Adrestus.protocol;

import io.Adrestus.core.AbstractBlock;
import io.Adrestus.core.TransactionBlock;
import io.Adrestus.network.CachedEventLoop;
import io.Adrestus.network.IPFinder;
import io.Adrestus.rpc.RpcAdrestusServer;
import io.distributedLedger.DatabaseInstance;
import io.distributedLedger.PatriciaTreeInstance;
import io.distributedLedger.ZoneDatabaseFactory;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RepositoryPatriciaTreeTask extends AdrestusTask{
    private static Logger LOG = LoggerFactory.getLogger(RepositoryPatriciaTreeTask.class);
    private RpcAdrestusServer<byte[]> rpcAdrestusServer;
    private final ExecutorService executorService;

    private final PatriciaTreeInstance instance;

    public RepositoryPatriciaTreeTask(PatriciaTreeInstance instance) {
        this.instance = instance;
        this.executorService = Executors.newSingleThreadExecutor();
    }

    @SneakyThrows
    @Override
    public void execute() {
        this.rpcAdrestusServer = new RpcAdrestusServer<byte[]>(new byte[]{}, this.instance, IPFinder.getLocal_address(), ZoneDatabaseFactory.getDatabasePatriciaRPCPort(instance), CachedEventLoop.getInstance().getEventloop());
        this.executorService.execute(rpcAdrestusServer);
        LOG.info("execute");
    }

    public void close() {
        this.executorService.shutdownNow();
        this.rpcAdrestusServer.close();
        this.rpcAdrestusServer = null;
    }
}
