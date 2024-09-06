package name.ekt.kafka.connect.transform

import org.apache.kafka.connect.data.Schema
import org.apache.kafka.connect.data.SchemaBuilder
import org.apache.kafka.connect.data.Struct
import org.apache.kafka.connect.data.Timestamp
import org.apache.kafka.connect.source.SourceRecord
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.Calendar
import java.util.Date

class TimeShiftFieldTest {
    @Test
    fun `should shift date by 5 hours`() {
        TimeShift<SourceRecord>()
            .also {
                it.configure(
                    mapOf<String, Any>(
                        "field" to "myDateField",
                        "hours" to 5
                    )
                )
            }
            .use { transform ->
                val time = Calendar.getInstance()
                    .also { it.set(2023, Calendar.JANUARY, 1, 10, 0) }
                    .time
                val keySchema = SchemaBuilder.struct().name("EntityKey")
                    .field("ID", Schema.INT32_SCHEMA)
                    .build()
                val key = Struct(keySchema)
                    .put("ID", 10)
                val valueSchema = SchemaBuilder.struct().name("Entity")
                    .field("ID", Schema.INT32_SCHEMA)
                    .field("myDateField", Timestamp.SCHEMA)
                    .build()
                val value = Struct(valueSchema)
                    .put("ID", 10)
                    .put("myDateField", time)
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

                val newTime = transformedValue["myDateField"] as Date
                val expectedDate = Calendar.getInstance()
                    .also{
                        it.time = time
                    }.also{
                        it.add(Calendar.HOUR, 5)
                    }.time

                assertEquals(expectedDate, newTime)
            }
    }
}
