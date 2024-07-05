package name.ekt.kafka.connect.transform

import org.apache.kafka.connect.data.Schema.INT32_SCHEMA
import org.apache.kafka.connect.data.Schema.STRING_SCHEMA
import org.apache.kafka.connect.data.SchemaBuilder
import org.apache.kafka.connect.data.Struct
import org.apache.kafka.connect.source.SourceRecord
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class UppercaseFieldNamesTest {
    @Test
    fun testReplaceRegexValueTransformationNull() {
        UppercaseFieldNames<SourceRecord>().use { transform ->
            val keySchema = SchemaBuilder.struct().name("EntityKey")
                .field("ID", INT32_SCHEMA)
                .build()
            val key = Struct(keySchema)
                .put("ID", 10)
            val valueSchema = SchemaBuilder.struct().name("Entity")
                .field("ID", INT32_SCHEMA)
                .field("Message", STRING_SCHEMA)
                .build()
            val value = Struct(valueSchema)
                .put("ID", 10)
                .put("Message", "foo")
            val record = SourceRecord(
                null,
                null,
                "my_topic",
                0,
                keySchema,
                key,
                valueSchema,
                value
            )
            val transformedRecord = transform.apply(record)
            val transformedValue = transformedRecord?.value() as Struct
            Assertions.assertEquals("foo", transformedValue["MESSAGE"])
        }
    }
}
