package name.ekt.kafka.connect.transform;

import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.HashMap;
import java.util.Map;

import static org.apache.kafka.connect.data.Schema.INT32_SCHEMA;
import static org.apache.kafka.connect.data.Schema.STRING_SCHEMA;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
public class RemoveNullCharactersTest {

    @Test
    public void testReplaceRegexValueTransformationNull() {
        Map<String, String> props = new HashMap<>();
        RemoveNullCharacters<SourceRecord> transform = new RemoveNullCharacters<>();
        transform.configure(props);

        Schema schema = SchemaBuilder.struct().name("Entity")
                .field("id", INT32_SCHEMA)
                .field("message", STRING_SCHEMA)
                .build();
        Struct value = new Struct(schema)
                .put("id", 10)
                .put("message", "foo\u0000is\u0000here");

        SourceRecord record = new SourceRecord(
                null,
                null,
                "my_topic",
                0,
                STRING_SCHEMA,
                value
        );

        SourceRecord transformedRecord = transform.apply(record);
        Struct transformedValue = (Struct) transformedRecord.value();

        assertEquals("fooishere", transformedValue.get("message"));
        transform.close();
    }
}
