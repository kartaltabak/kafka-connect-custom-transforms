package name.ekt.kafka.connect.transform

import org.apache.kafka.connect.data.Schema
import org.apache.kafka.connect.data.SchemaBuilder
import org.apache.kafka.connect.data.Struct
import org.apache.kafka.connect.source.SourceRecord
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class RenameFieldNamesRegExTest {
    @Test
    fun testRenameFieldNames() {
        RenameFieldNamesRegEx<SourceRecord>()
            .also {
                it.configure(
                    mapOf(
                        "regex" to "\\\\",
                        "replacement" to "_"
                    )
                )
            }
            .use { transform ->
                val keySchema = SchemaBuilder.struct().name("EntityKey")
                    .field("\\XYZ\\ID", Schema.INT32_SCHEMA)
                    .build()
                val key = Struct(keySchema)
                    .put("\\XYZ\\ID", 10)
                val valueSchema = SchemaBuilder.struct().name("Entity")
                    .field("\\XYZ\\ID", Schema.INT32_SCHEMA)
                    .field("\\XYZ\\Message", Schema.STRING_SCHEMA)
                    .build()
                val value = Struct(valueSchema)
                    .put("\\XYZ\\ID", 10)
                    .put("\\XYZ\\Message", "foo")
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
                Assertions.assertEquals("foo", transformedValue["_XYZ_Message"])
            }
    }

}