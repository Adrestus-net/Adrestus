package io.Adrestus.protocol;

import io.Adrestus.config.NetworkConfiguration;
import io.Adrestus.core.AbstractBlock;
import io.Adrestus.core.CommitteeBlock;
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

public class RepositoryCommitteeTask extends AdrestusTask {
    private static Logger LOG = LoggerFactory.getLogger(RepositoryCommitteeTask.class);
    private final CommitteeBlock committeeBlock;
    private final DatabaseInstance databaseInstance;
    private final ExecutorService executorService;
    private RpcAdrestusServer<AbstractBlock> rpcAdrestusServer;

    public RepositoryCommitteeTask() {
        this.committeeBlock = new CommitteeBlock();
        this.databaseInstance = ZoneDatabaseFactory.getZoneInstance(4);
        this.executorService = Executors.newSingleThreadExecutor();
    }

    @SneakyThrows
    @Override
    public void execute() {
        rpcAdrestusServer = new RpcAdrestusServer<AbstractBlock>(this.committeeBlock, this.databaseInstance, IPFinder.getLocal_address(), NetworkConfiguration.RPC_PORT, CachedEventLoop.getInstance().getEventloop());
        this.executorService.execute(rpcAdrestusServer);
        LOG.info("execute");
    }

    public void close() {
        this.executorService.shutdownNow();
        if (this.rpcAdrestusServer != null) {
            this.rpcAdrestusServer.close();
            this.rpcAdrestusServer = null;
        }
    }
}
