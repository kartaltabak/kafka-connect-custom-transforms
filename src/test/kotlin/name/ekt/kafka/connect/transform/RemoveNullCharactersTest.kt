package name.ekt.kafka.connect.transform

import org.apache.kafka.connect.data.Schema.INT32_SCHEMA
import org.apache.kafka.connect.data.Schema.STRING_SCHEMA
import org.apache.kafka.connect.data.SchemaBuilder
import org.apache.kafka.connect.data.Struct
import org.apache.kafka.connect.source.SourceRecord
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RemoveNullCharactersTest {
    @Test
    fun testReplaceRegexValueTransformationNull() {
        RemoveNullCharacters<SourceRecord>().use { transform ->
            val schema = SchemaBuilder.struct().name("Entity")
                .field("id", INT32_SCHEMA)
                .field("message", STRING_SCHEMA)
                .build()
            val value = Struct(schema)
                .put("id", 10)
                .put("message", "foo\u0000is\u0000here")
            val record = SourceRecord(
                null,
                null,
                "my_topic",
                0,
                schema,
                value
            )
            val transformedRecord = transform.apply(record)
            val transformedValue = transformedRecord.value() as Struct
            assertEquals("fooishere", transformedValue["message"])
        }
    }
}
