package name.ekt.kafka.connect.transforms.test1;

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

import java.io.IOException;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import static name.ekt.kafka.connect.transforms.TestUtils.CONFLUENT_VERSION;
import static name.ekt.kafka.connect.transforms.TestUtils.createKafkaConnectContainer;
import static name.ekt.kafka.connect.transforms.TestUtils.getDockerImageName;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
public class E2ETest {
    public static final String POSTGRE_IMAGE = "postgres:16.3";
    private static final Network network = Network.newNetwork();
    private static final String SQLSERVER_IMAGE = "mcr.microsoft.com/mssql/server:2022-latest";

    @Container
    public KafkaContainer kafka = new KafkaContainer(getDockerImageName("confluentinc/cp-kafka:" + CONFLUENT_VERSION))
            .withNetwork(network)
            .withNetworkAliases("kafka");

    @Container
    public GenericContainer<?> kafkaConnect = createKafkaConnectContainer()
            .withNetwork(network)
            .withNetworkAliases("kafka-connect")
            .dependsOn(kafka);

    @Container
    public MSSQLServerContainer<?> sqlServer = new MSSQLServerContainer<>(getDockerImageName(SQLSERVER_IMAGE))
            .acceptLicense()
            .withInitScript("test1/sqlserver-init.sql")
            .withNetwork(network)
            .withNetworkAliases("sqlserver");


    @Container
    public PostgreSQLContainer<?> postgreSQL = new PostgreSQLContainer<>(getDockerImageName(POSTGRE_IMAGE))
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
