package name.ekt.kafka.connect.transforms.test1;

import com.github.dockerjava.api.command.CreateNetworkCmd;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
public class E2ETest {
    private static final Network network = Network.builder()
            .createNetworkCmdModifier(createNetworkCmd -> createNetworkCmd.withName("kafka-connect-transforms-test1"))
            .build();
    public static final String CONFLUENT_VERSION = "7.5.3";

    @Container
    public KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:" + CONFLUENT_VERSION))
            .withNetwork(network)
            .withNetworkAliases("kafka");

    @Container
    public GenericContainer<?> kafkaConnect = new GenericContainer<>(DockerImageName.parse("confluentinc/cp-kafka-connect:" + CONFLUENT_VERSION))
            .withExposedPorts(8083)
            .withNetwork(network)
            .withNetworkAliases("kafka-connect")
            .dependsOn(kafka)
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


    @Container
    public MSSQLServerContainer<?> sqlServer = new MSSQLServerContainer<>(DockerImageName.parse("mcr.microsoft.com/mssql/server:2022-latest"))
            .acceptLicense()
            .withInitScript("test1/sqlserver-init.sql")
            .withNetwork(network)
            .withNetworkAliases("sqlserver");


    @Container
    public PostgreSQLContainer<?> postgreSQL = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16.3"))
            .withDatabaseName("testdb")
            .withNetwork(network)
            .withNetworkAliases("postgredb")
            .withUsername("test")
            .withPassword("test")
            .withInitScript("test1/postgres-init.sql");

    private static final OkHttpClient client = new OkHttpClient();
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final MustacheFactory mf = new DefaultMustacheFactory();


    private void createConnector(String templatePath, Map<String, Object> context) throws IOException {
        Mustache mustache = mf.compile(templatePath);
        StringWriter writer = new StringWriter();
        mustache.execute(writer, context).flush();
        String renderedTemplate = writer.toString();

        System.out.println(renderedTemplate);

        RequestBody body = RequestBody.create(renderedTemplate, JSON);
        Request request = new Request.Builder()
                .url("http://" + kafkaConnect.getHost() + ":" + kafkaConnect.getFirstMappedPort() + "/connectors")
                .post(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new RuntimeException("Failed to create connector: " + response.body().string());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testE2E() throws Exception {
        // Prepare context for templates
        Map<String, Object> contextSqlServer = new HashMap<>();
        contextSqlServer.put("jdbcurl", sqlServer.getJdbcUrl());
        contextSqlServer.put("username", sqlServer.getUsername());
        contextSqlServer.put("password", sqlServer.getPassword());
        contextSqlServer.put("hostname", "sqlserver");
        contextSqlServer.put("port", MSSQLServerContainer.MS_SQL_SERVER_PORT);
        contextSqlServer.put("kafkaurl", "kafka:9092");

        createConnector("test1/sqlserver-source-connector.json.mustache", contextSqlServer);

        Map<String, Object> contextPostgre = new HashMap<>();
        contextPostgre.put("jdbcurl", postgreSQL.getJdbcUrl());
        contextPostgre.put("username", postgreSQL.getUsername());
        contextPostgre.put("password", postgreSQL.getPassword());
        createConnector("test1/postgres-sink-connector.json.mustache", contextPostgre);

        // Wait for connectors to process data (Replace with actual wait mechanism)
        Thread.sleep(5000);

        // Verify data in PostgreSQL
        try (Connection conn = DriverManager.getConnection(postgreSQL.getJdbcUrl(), postgreSQL.getUsername(), postgreSQL.getPassword());
             Statement stmt = conn.createStatement();
             var rs = stmt.executeQuery("SELECT \"Message\" FROM \"usrkafka\".\"TestTable\" WHERE \"ID\" = 1;")) {
            if (rs.next()) {
                String message = rs.getString("Message");
                assertEquals("foo", message);
            } else {
                throw new AssertionError("Data not found in PostgreSQL");
            }
        }
    }
}
