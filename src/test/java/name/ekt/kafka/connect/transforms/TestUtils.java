package name.ekt.kafka.connect.transforms;

import org.jetbrains.annotations.NotNull;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

public class TestUtils {
    public static final String CONFLUENT_VERSION = "7.5.3";

    @NotNull
    public static DockerImageName getDockerImageName(String imageName) {
        return DockerImageName.parse(imageName);
    }

    public static GenericContainer<?> createKafkaConnectContainer() {
        return new GenericContainer<>(getDockerImageName("confluentinc/cp-kafka-connect:" + CONFLUENT_VERSION))
                .withExposedPorts(8083)
                .withEnv("CONNECT_BOOTSTRAP_SERVERS", "PLAINTEXT://kafka:9092")
                .withEnv("CONNECT_REST_PORT", "8083")
                .withEnv("CONNECT_GROUP_ID", "kafka-connect")
                .withEnv("CONNECT_CONFIG_STORAGE_TOPIC", "connect-configs")
                .withEnv("CONNECT_OFFSET_STORAGE_TOPIC", "connect-offsets")
                .withEnv("CONNECT_STATUS_STORAGE_TOPIC", "connect-statuses")
                .withEnv("CONNECT_CONFIG_STORAGE_REPLICATION_FACTOR", "1")
                .withEnv("CONNECT_OFFSET_STORAGE_REPLICATION_FACTOR", "1")
                .withEnv("CONNECT_STATUS_STORAGE_REPLICATION_FACTOR", "1")
                .withEnv("CONNECT_KEY_CONVERTER", "org.apache.kafka.connect.json.JsonConverter")
                .withEnv("CONNECT_VALUE_CONVERTER", "org.apache.kafka.connect.json.JsonConverter")
                .withEnv("CONNECT_INTERNAL_KEY_CONVERTER", "org.apache.kafka.connect.json.JsonConverter")
                .withEnv("CONNECT_INTERNAL_VALUE_CONVERTER", "org.apache.kafka.connect.json.JsonConverter")
                .withEnv("CONNECT_PLUGIN_PATH", "/usr/share/java,/usr/share/confluent-hub-components")
                .withEnv("CONNECT_REST_ADVERTISED_HOST_NAME", "localhost")
                .withCommand("sh", "-c",
                        "confluent-hub install --no-prompt confluentinc/kafka-connect-jdbc:latest && " +
                                "confluent-hub install --no-prompt debezium/debezium-connector-sqlserver:latest && " +
                                "/etc/confluent/docker/run");
    }
}
