package io.Adrestus.protocol;

import io.Adrestus.config.NetworkConfiguration;
import io.Adrestus.core.AbstractBlock;
import io.Adrestus.core.Resourses.CachedZoneIndex;
import io.Adrestus.core.TransactionBlock;
import io.Adrestus.network.CachedEventLoop;
import io.Adrestus.network.IPFinder;
import io.Adrestus.rpc.RpcAdrestusServer;
import io.distributedLedger.DatabaseInstance;
import io.distributedLedger.ZoneDatabaseFactory;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RepositoryTransactionTask extends AdrestusTask {
    private static Logger LOG = LoggerFactory.getLogger(RepositoryTransactionTask.class);
    private final TransactionBlock transactionBlock;
    private final DatabaseInstance databaseInstance;
    private RpcAdrestusServer<AbstractBlock> rpcAdrestusServer;
    private final ExecutorService executorService;

    public RepositoryTransactionTask() {
        this.transactionBlock = new TransactionBlock();
        this.databaseInstance = ZoneDatabaseFactory.getZoneInstance(CachedZoneIndex.getInstance().getZoneIndex());
        this.executorService = Executors.newSingleThreadExecutor();
    }

    @SneakyThrows
    @Override
    public void execute() {
        rpcAdrestusServer = new RpcAdrestusServer<AbstractBlock>(this.transactionBlock, this.databaseInstance, IPFinder.getLocal_address(), ZoneDatabaseFactory.getDatabaseRPCPort(databaseInstance), CachedEventLoop.getInstance().getEventloop());
        this.executorService.execute(rpcAdrestusServer);
        LOG.info("execute");
    }

    public void close() {
        this.executorService.shutdownNow();
        this.rpcAdrestusServer.close();
        this.rpcAdrestusServer = null;
    }
}
