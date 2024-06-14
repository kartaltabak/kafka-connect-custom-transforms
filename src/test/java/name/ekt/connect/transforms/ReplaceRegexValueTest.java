package name.ekt.connect.transforms;

import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
public class ReplaceRegexValueTest {

    @Container
    public KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:5.5.0"));

    @Test
    public void testReplaceRegexValueTransformation() {
        Map<String, String> props = new HashMap<>();
        props.put("field", "message");
        props.put("regex", "foo");
        props.put("replacement", "bar");

        ReplaceRegexValue<SourceRecord> transform = new ReplaceRegexValue<>();
        transform.configure(props);

        Map<String, Object> value = new HashMap<>();
        value.put("message", "foo is here");

        SourceRecord record = new SourceRecord(
                null, null, "my_topic", 0, Schema.STRING_SCHEMA, value);

        SourceRecord transformedRecord = transform.apply(record);
        Map<String, Object> transformedValue = (Map<String, Object>) transformedRecord.value();

        assertEquals("bar is here", transformedValue.get("message"));
        transform.close();
    }
}
