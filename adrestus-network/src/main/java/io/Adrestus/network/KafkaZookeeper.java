package io.Adrestus.network;

import io.Adrestus.config.Directory;
import io.Adrestus.config.KafkaConfiguration;
import lombok.SneakyThrows;
import org.apache.zookeeper.server.*;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Objects;

public class KafkaZookeeper implements IKafkaComponent {
    private static final String ZOOKEEPER_SNAPSHOT = "embeeded-zk-snapshot";
    private static final String ZOOKEEPER_LOG = "embeeded-zk-log";
    private static final int tickTime = 100;
    private ServerCnxnFactory factory;
    private ZooKeeperServer zkServer;

    private File snapshotDir;
    private File logDir;

    public KafkaZookeeper() {
    }

    @SneakyThrows
    @Override
    public void constructKafkaComponentType() {
        this.factory = NIOServerCnxnFactory.createFactory(new InetSocketAddress(KafkaConfiguration.ZOOKEEPER_HOST, Integer.parseInt(KafkaConfiguration.ZOOKEEPER_PORT)), 1024);
        this.snapshotDir = Directory.CreateFileFromPathName(ZOOKEEPER_SNAPSHOT);
        this.logDir = Directory.CreateFileFromPathName(ZOOKEEPER_LOG);
        this.zkServer = new ZooKeeperServer(this.snapshotDir, logDir, tickTime);
        this.zkServer.setTickTime(tickTime);
        this.zkServer.setMinSessionTimeout(4000);
        this.zkServer.setMaxSessionTimeout(30000);
        this.zkServer.setLargeRequestMaxBytes(1024 * 1024 * 1024);
        this.zkServer.setResponseCachingEnabled(true);

        System.setProperty("zookeeper.globalOutstandingLimit", Integer.toString(1000));
        System.setProperty("zookeeper.preAllocSize", Integer.toString(400*1024*1024));
        System.setProperty("maxClientCnxns", Integer.toString(5));
        System.setProperty("forceSync", "no");
        System.setProperty("zookeeper.jute.maxbuffer", "53687091");


        try {
            factory.startup(zkServer);
        } catch (InterruptedException e) {
            throw new IllegalStateException("Interrupted during test startup: ");
        }
    }


    @Override
    public IKafkaComponent getKafkaKingdomType() {
        return this;
    }

    @Override
    public void Shutdown() {
        if (factory != null) {
            try {
                if (zkServer != null) {
                    ZKDatabase zkDb = zkServer.getZKDatabase();
                    zkDb.close();
                    zkServer.shutdown(true);
                }
                factory.closeAll(ServerCnxn.DisconnectReason.CLEAN_UP);
                factory.shutdown();
                factory.join();
            } catch (InterruptedException e) {
                throw new IllegalStateException("Interrupted during test shutdown: ");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            factory = null;
            zkServer = null;
        }
        Directory.deleteKafkaLogFiles(logDir);
        Directory.deleteKafkaLogFiles(snapshotDir);
    }


    public ZooKeeperServer getZkServer() {
        return zkServer;
    }

    public void setZkServer(ZooKeeperServer zkServer) {
        this.zkServer = zkServer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KafkaZookeeper that = (KafkaZookeeper) o;
        return Objects.equals(zkServer, that.zkServer);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(zkServer);
    }

    @Override
    public String toString() {
        return "KafkaZookeeper{" +
                "zkServer=" + zkServer +
                '}';
    }
}
