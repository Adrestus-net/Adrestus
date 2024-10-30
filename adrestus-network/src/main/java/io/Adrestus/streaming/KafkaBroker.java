package io.Adrestus.streaming;

import io.Adrestus.config.Directory;
import io.Adrestus.config.KafkaConfiguration;
import kafka.server.KafkaConfig;
import kafka.server.KafkaServer;
import lombok.SneakyThrows;
import org.apache.kafka.common.utils.Time;
import scala.Option;

import java.io.File;
import java.time.Duration;
import java.util.Objects;
import java.util.Properties;

public class KafkaBroker implements IKafkaComponent {
    private static final String NAME = "Kafka-broker";
    private KafkaServer kafkaServer;
    private String zkDir;

    public KafkaBroker() {
    }

    @SneakyThrows
    @Override
    public void constructKafkaComponentType() {
        zkDir = Directory.CreateFolderPath(NAME);
        Properties kafkaProps = new Properties();
        kafkaProps.setProperty("broker.id", "0");
        kafkaProps.setProperty("log.dirs", zkDir);
        kafkaProps.setProperty("zookeeper.connect", KafkaConfiguration.ZOOKEEPER_HOST + ":" + KafkaConfiguration.ZOOKEEPER_PORT);
        kafkaProps.setProperty("offsets.topic.replication.factor", "1");
        kafkaProps.setProperty("listeners", "PLAINTEXT://" + KafkaConfiguration.KAFKA_HOST + ":" + KafkaConfiguration.KAFKA_PORT);

        KafkaConfig kafkaConfig = new KafkaConfig(kafkaProps);
        kafkaServer = new KafkaServer(kafkaConfig, Time.SYSTEM, Option.apply("kafka-server-startup"), false);
        kafkaServer.startup();
    }

    @Override
    public KafkaBroker getKafkaKingdomType() {
        return this;
    }

    @Override
    public void Shutdown() {
        if (kafkaServer == null) {
            return;
        }
        kafkaServer.shutdown();
        kafkaServer.shutdown(Duration.ofMillis(1000));
        kafkaServer = null;
        Directory.deleteKafkaLogFiles(new File(zkDir));
    }

    public KafkaServer getKafkaServer() {
        return kafkaServer;
    }

    public void setKafkaServer(KafkaServer kafkaServer) {
        this.kafkaServer = kafkaServer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KafkaBroker that = (KafkaBroker) o;
        return Objects.equals(kafkaServer, that.kafkaServer);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(kafkaServer);
    }

    @Override
    public String toString() {
        return "KafkaBroker{" +
                "kafkaServer=" + kafkaServer +
                '}';
    }
}
