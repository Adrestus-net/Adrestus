package io.Adrestus.network;

import io.Adrestus.config.Directory;
import io.Adrestus.config.KafkaConfiguration;
import kafka.server.KafkaConfig;
import kafka.server.KafkaServer;
import lombok.SneakyThrows;
import org.apache.kafka.common.utils.Time;
import scala.Option;

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Properties;

public class KafkaBroker implements IKafkaComponent {
    private static final String NAME = "Kafka-broker";
    private final ArrayList<String> ipAddresses;
    private KafkaServer kafkaServer;
    private String zkDir;

    public KafkaBroker(ArrayList<String> ipAddresses) {
        this.ipAddresses = ipAddresses;
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
        kafkaProps.setProperty("delete.topic.enable", "true");
        kafkaProps.setProperty("auto.create.topics.enable", "false");
        kafkaProps.setProperty("authorizer.class.name", "kafka.security.authorizer.AclAuthorizer");
        kafkaProps.setProperty("listeners", "SASL_PLAINTEXT://" + KafkaConfiguration.KAFKA_HOST + ":" + KafkaConfiguration.KAFKA_PORT);
        kafkaProps.setProperty("advertised.listeners", "SASL_PLAINTEXT://" + KafkaConfiguration.KAFKA_HOST + ":" + KafkaConfiguration.KAFKA_PORT);
        kafkaProps.setProperty("security.inter.broker.protocol", "SASL_PLAINTEXT");
        kafkaProps.setProperty("sasl.mechanism.inter.broker.protocol", "PLAIN");
        kafkaProps.setProperty("sasl.enabled.mechanisms", "PLAIN");
        kafkaProps.setProperty("allow.everyone.if.no.acl.found", "false");
        kafkaProps.setProperty("listener.name.sasl_plaintext.plain.sasl.jaas.config", getJaasConfigString());
        kafkaProps.setProperty("super.users", getSuperUsers());

        KafkaConfig kafkaConfig = new KafkaConfig(kafkaProps);
        kafkaServer = new KafkaServer(kafkaConfig, Time.SYSTEM, Option.apply("kafka-server-startup"), false);
        kafkaServer.startup();
    }

    private String getSuperUsers() {
        StringBuilder superUsers = new StringBuilder();
        superUsers.append("User:admin;");
        superUsers.append("User:producer;");
        int count = 0;
        for (String ip : ipAddresses) {
            superUsers.append("User:consumer").append("-").append(count).append("-").append(ip).append(";");
            count++;
        }
        return superUsers.toString();
    }

    private String getJaasConfigString() {
        ArrayList<String> users = new ArrayList<>();
        for (String ip : ipAddresses) {
            users.add(ip + ":" + "consumer-secret");
        }
        String baseConfig = "org.apache.kafka.common.security.plain.PlainLoginModule required" + System.lineSeparator() +
                "username=\"admin\"" + System.lineSeparator() +
                "password=\"admin-secret\"" + System.lineSeparator() +
                "user_admin=\"admin-secret\"" + System.lineSeparator() +
                "user_producer=\"producer-secret\"" + System.lineSeparator();

        // Build the JAAS config string dynamically
        StringBuilder jaasConfig = new StringBuilder(baseConfig);

        int count = 0;
        for (String user : users) {
            String[] parts = user.split(":");
            String username = parts[0];
            String password = parts[1];
            jaasConfig.append("user_consumer").append("-").append(count).append("-").append(username).append("=\"").append(password).append("\"" + System.lineSeparator());
            count++;
        }

        // Final JAAS config string with control flag
        return jaasConfig.append(";").toString();
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
