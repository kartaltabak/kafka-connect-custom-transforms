package name.ekt.kafka.connect.transform;

import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.transforms.Transformation;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.apache.kafka.connect.data.Schema.INT32_SCHEMA;
import static org.apache.kafka.connect.data.Schema.STRING_SCHEMA;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class UppercaseFieldNamesTest {

    @Test
    public void testReplaceRegexValueTransformationNull() {
        Map<String, String> props = new HashMap<>();
        Transformation<SourceRecord> transform = new UppercaseFieldNames<>();
        transform.configure(props);

        Schema keySchema = SchemaBuilder.struct().name("EntityKey")
                .field("ID", INT32_SCHEMA)
                .build();

        Struct key = new Struct(keySchema)
                .put("ID", 10);

        Schema valueSchema = SchemaBuilder.struct().name("Entity")
                .field("ID", INT32_SCHEMA)
                .field("Message", STRING_SCHEMA)
                .build();

        Struct value = new Struct(valueSchema)
                .put("ID", 10)
                .put("Message", "foo");

        SourceRecord record = new SourceRecord(
                null,
                null,
                "my_topic",
                0,
                keySchema,
                key,
                valueSchema,
                value
        );

        SourceRecord transformedRecord = transform.apply(record);
        Struct transformedValue = (Struct) transformedRecord.value();

        assertEquals("foo", transformedValue.get("MESSAGE"));
        transform.close();
    }
}
