package name.ekt.kafka.connect.transform

import org.apache.kafka.connect.data.Schema
import org.apache.kafka.connect.data.SchemaBuilder
import org.apache.kafka.connect.data.Struct
import org.apache.kafka.connect.source.SourceRecord
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class IntShiftTest {
    @Test
    fun `should shift by 5 units`() {
        IntShift<SourceRecord>()
            .also {
                it.configure(
                    mapOf<String, Any>(
                        "field" to "myfield",
                        "shift-amount" to 5
                    )
                )
            }
            .use { transform ->
                val myInt: Long = 5
                val keySchema = SchemaBuilder.struct().name("EntityKey")
                    .field("ID", Schema.INT32_SCHEMA)
                    .build()
                val key = Struct(keySchema)
                    .put("ID", 10)
                val valueSchema = SchemaBuilder.struct().name("Entity")
                    .field("ID", Schema.INT32_SCHEMA)
                    .field("myfield", Schema.INT64_SCHEMA)
                    .build()
                val value = Struct(valueSchema)
                    .put("ID", 10)
                    .put("myfield", myInt)
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
                val transformedValue = transformedRecord.value() as Struct

                val newTime = transformedValue["myfield"] as Long
                val expectedValue = 5L

                assertEquals(expectedValue, newTime)
            }
    }
}
