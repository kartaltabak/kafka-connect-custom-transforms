package name.ekt.kafka.connect.transform

import org.apache.kafka.connect.data.Schema.INT32_SCHEMA
import org.apache.kafka.connect.data.Schema.STRING_SCHEMA
import org.apache.kafka.connect.data.SchemaBuilder
import org.apache.kafka.connect.data.Struct
import org.apache.kafka.connect.source.SourceRecord
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ReplaceRegexValueTest {
    @Test
    fun testReplaceRegexValueTransformation() {
        ReplaceRegexValue<SourceRecord>()
            .also {
                it.configure(
                    mapOf(
                        "field" to "message",
                        "regex" to "foo",
                        "replacement" to "bar"
                    )
                )
            }
            .use { transform ->
                val schema = SchemaBuilder.struct().name("Entity")
                    .field("id", INT32_SCHEMA)
                    .field("message", STRING_SCHEMA)
                    .build()
                val value = Struct(schema)
                    .put("id", 10)
                    .put("message", "foo is here")
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
                assertEquals("bar is here", transformedValue["message"])
            }
    }

    @Test
    fun testReplaceRegexValueTransformationNull() {
        val props: MutableMap<String?, String?> = HashMap()
        props["field"] = "message"
        props["regex"] = "\\u0000"
        props["replacement"] = "/"
        val transform = ReplaceRegexValue<SourceRecord>()
        transform.configure(props)
        val schema = SchemaBuilder.struct().name("Entity")
            .field("id", INT32_SCHEMA)
            .field("message", STRING_SCHEMA)
            .build()
        val value = Struct(schema)
            .put("id", 10)
            .put("message", "foo\u0000is here")
        val record = SourceRecord(
            null,
            null,
            "my_topic2",
            0,
            STRING_SCHEMA,
            value
        )
        val transformedRecord = transform.apply(record)
        val transformedValue = transformedRecord.value() as Struct
        assertEquals("foo/is here", transformedValue["message"])
        transform.close()
    }
}
