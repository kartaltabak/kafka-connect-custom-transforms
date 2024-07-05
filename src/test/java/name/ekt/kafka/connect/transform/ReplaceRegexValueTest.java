package name.ekt.kafka.connect.transform;

import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.apache.kafka.connect.data.Schema.INT32_SCHEMA;
import static org.apache.kafka.connect.data.Schema.STRING_SCHEMA;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReplaceRegexValueTest {

    @Test
    public void testReplaceRegexValueTransformation() {
        Map<String, String> props = new HashMap<>();
        props.put("field", "message");
        props.put("regex", "foo");
        props.put("replacement", "bar");

        ReplaceRegexValue<SourceRecord> transform = new ReplaceRegexValue<>();
        transform.configure(props);

        Schema schema = SchemaBuilder.struct().name("Entity")
                .field("id", INT32_SCHEMA)
                .field("message", STRING_SCHEMA)
                .build();
        Struct value = new Struct(schema)
                .put("id", 10)
                .put("message", "foo is here");

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

        assertEquals("bar is here", transformedValue.get("message"));
        transform.close();
    }

    @Test
    public void testReplaceRegexValueTransformationNull() {
        Map<String, String> props = new HashMap<>();
        props.put("field", "message");
        props.put("regex", "\\u0000");
        props.put("replacement", "/");

        ReplaceRegexValue<SourceRecord> transform = new ReplaceRegexValue<>();
        transform.configure(props);

        Schema schema = SchemaBuilder.struct().name("Entity")
                .field("id", INT32_SCHEMA)
                .field("message", STRING_SCHEMA)
                .build();
        Struct value = new Struct(schema)
                .put("id", 10)
                .put("message", "foo\u0000is here");

        SourceRecord record = new SourceRecord(
                null,
                null,
                "my_topic2",
                0,
                STRING_SCHEMA,
                value
        );

        SourceRecord transformedRecord = transform.apply(record);
        Struct transformedValue = (Struct) transformedRecord.value();

        assertEquals("foo/is here", transformedValue.get("message"));
        transform.close();
    }
}
