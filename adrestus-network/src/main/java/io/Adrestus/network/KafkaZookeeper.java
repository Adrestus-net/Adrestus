package io.Adrestus.network;

import io.Adrestus.config.Directory;
import io.Adrestus.config.KafkaConfiguration;
import lombok.SneakyThrows;
import org.apache.zookeeper.server.ServerConfig;
import org.apache.zookeeper.server.ZooKeeperServerMain;
import org.apache.zookeeper.server.admin.AdminServer;
import org.apache.zookeeper.server.quorum.QuorumPeerConfig;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.Properties;

public class KafkaZookeeper implements IKafkaComponent {
    private static final String NAME = "Zookeeper";
    private ZooKeeperServerMain zkServer;
    private String zkDir;

    public KafkaZookeeper() {
    }

    @SneakyThrows
    @Override
    public void constructKafkaComponentType() {
        zkDir = Directory.CreateFolderPath(NAME);
        Properties zkProps = new Properties();
        zkProps.setProperty("dataDir", zkDir);
        zkProps.setProperty("clientPort", KafkaConfiguration.ZOOKEEPER_PORT);

        QuorumPeerConfig quorumConfig = new QuorumPeerConfig();
        try {
            quorumConfig.parseProperties(zkProps);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        ServerConfig serverConfig = new ServerConfig();
        serverConfig.readFrom(quorumConfig);


        zkServer = new ZooKeeperServerMain();
        new Thread(() -> {
            try {
                zkServer.runFromConfig(serverConfig);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (AdminServer.AdminServerException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }


    @Override
    public IKafkaComponent getKafkaKingdomType() {
        return this;
    }

    @Override
    public void Shutdown() {
        if (zkServer != null) {
            zkServer.close();
            zkServer = null;
            if (zkDir != null)
                Directory.deleteKafkaLogFiles(new File(zkDir));
        }
    }

    public ZooKeeperServerMain getZkServer() {
        return zkServer;
    }

    public void setZkServer(ZooKeeperServerMain zkServer) {
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
