package name.ekt.kafka.connect.transform

import org.apache.kafka.connect.data.Schema.INT32_SCHEMA
import org.apache.kafka.connect.data.Schema.STRING_SCHEMA
import org.apache.kafka.connect.data.SchemaBuilder
import org.apache.kafka.connect.data.Struct
import org.apache.kafka.connect.source.SourceRecord
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.lang.System.currentTimeMillis
import java.util.Date

class AppendProcessingTimeTest {
    @Test
    fun testReplaceRegexValueTransformationNull() {
        AppendProcessingTime<SourceRecord>()
            .also {
                it.configure(mapOf("field" to "processed_time"))
            }
            .use { transform ->
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
                val ms1 = currentTimeMillis()
                Thread.sleep(2)
                val transformedRecord = transform.apply(record)
                val transformedValue = transformedRecord.value() as Struct
                Thread.sleep(2)
                val ms2 = currentTimeMillis()

                val processedTime = (transformedValue["processed_time"] as Date).time
                assertTrue(ms1 < processedTime)
                assertTrue(ms2 > processedTime)
            }
    }

    @Test
    fun `new field should be optional`() {
        AppendProcessingTime<SourceRecord>()
            .also {
                it.configure(mapOf("field" to "processed_time"))
            }
            .use { transform ->
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

                assertTrue(transformedRecord.valueSchema().field("processed_time").schema().isOptional)
            }
    }
}
