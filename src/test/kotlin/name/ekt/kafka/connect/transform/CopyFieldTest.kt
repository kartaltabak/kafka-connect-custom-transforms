package name.ekt.kafka.connect.transform

import org.apache.kafka.connect.data.Schema
import org.apache.kafka.connect.data.Schema.INT32_SCHEMA
import org.apache.kafka.connect.data.Schema.STRING_SCHEMA
import org.apache.kafka.connect.data.SchemaBuilder
import org.apache.kafka.connect.data.Struct
import org.apache.kafka.connect.source.SourceRecord
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class CopyFieldTest {
    @Test
    fun `should copy field value to new field`() {
        CopyField<SourceRecord>()
            .also {
                it.configure(
                    mapOf(
                        "source.field" to "original",
                        "target.field" to "copy"
                    )
                )
            }
            .use { transform ->
                val schema = SchemaBuilder.struct().name("Entity")
                    .field("id", INT32_SCHEMA)
                    .field("original", STRING_SCHEMA)
                    .build()
                val value = Struct(schema)
                    .put("id", 10)
                    .put("original", "test value")
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
                
                // Verify original field is unchanged
                assertEquals("test value", transformedValue["original"])
                // Verify new field has copied value
                assertEquals("test value", transformedValue["copy"])
            }
    }

    @Test
    fun `should copy null value`() {
        CopyField<SourceRecord>()
            .also {
                it.configure(
                    mapOf(
                        "source.field" to "original",
                        "target.field" to "copy"
                    )
                )
            }
            .use { transform ->
                val schema = SchemaBuilder.struct().name("Entity")
                    .field("id", INT32_SCHEMA)
                    .field("original", Schema.OPTIONAL_STRING_SCHEMA)
                    .build()
                val value = Struct(schema)
                    .put("id", 10)
                    .put("original", null)
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
                
                assertNull(transformedValue["original"])
                assertNull(transformedValue["copy"])
            }
    }

    @Test
    fun `should copy integer field`() {
        CopyField<SourceRecord>()
            .also {
                it.configure(
                    mapOf(
                        "source.field" to "count",
                        "target.field" to "count_backup"
                    )
                )
            }
            .use { transform ->
                val schema = SchemaBuilder.struct().name("Stats")
                    .field("id", INT32_SCHEMA)
                    .field("count", INT32_SCHEMA)
                    .build()
                val value = Struct(schema)
                    .put("id", 1)
                    .put("count", 42)
                val record = SourceRecord(
                    null,
                    null,
                    "stats",
                    0,
                    schema,
                    value
                )
                val transformedRecord = transform.apply(record)
                val transformedValue = transformedRecord.value() as Struct
                
                assertEquals(42, transformedValue["count"])
                assertEquals(42, transformedValue["count_backup"])
            }
    }

    @Test
    fun `should preserve all original fields`() {
        CopyField<SourceRecord>()
            .also {
                it.configure(
                    mapOf(
                        "source.field" to "name",
                        "target.field" to "name_copy"
                    )
                )
            }
            .use { transform ->
                val schema = SchemaBuilder.struct().name("Entity")
                    .field("id", INT32_SCHEMA)
                    .field("name", STRING_SCHEMA)
                    .field("status", STRING_SCHEMA)
                    .build()
                val value = Struct(schema)
                    .put("id", 100)
                    .put("name", "John Doe")
                    .put("status", "active")
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
                
                // Verify all original fields are preserved
                assertEquals(100, transformedValue["id"])
                assertEquals("John Doe", transformedValue["name"])
                assertEquals("active", transformedValue["status"])
                // Verify new field
                assertEquals("John Doe", transformedValue["name_copy"])
            }
    }

    @Test
    fun `should preserve schema name and version`() {
        CopyField<SourceRecord>()
            .also {
                it.configure(
                    mapOf(
                        "source.field" to "original",
                        "target.field" to "copy"
                    )
                )
            }
            .use { transform ->
                val schema = SchemaBuilder.struct()
                    .name("com.example.Entity")
                    .version(3)
                    .field("id", INT32_SCHEMA)
                    .field("original", STRING_SCHEMA)
                    .build()
                val value = Struct(schema)
                    .put("id", 10)
                    .put("original", "test")
                val record = SourceRecord(
                    null,
                    null,
                    "my_topic",
                    0,
                    schema,
                    value
                )
                val transformedRecord = transform.apply(record)
                
                // Verify schema name and version are preserved
                assertEquals("com.example.Entity", transformedRecord.valueSchema().name())
                assertEquals(3, transformedRecord.valueSchema().version())
            }
    }

    @Test
    fun `should handle non-existent source field gracefully`() {
        CopyField<SourceRecord>()
            .also {
                it.configure(
                    mapOf(
                        "source.field" to "nonexistent",
                        "target.field" to "copy"
                    )
                )
            }
            .use { transform ->
                val schema = SchemaBuilder.struct().name("Entity")
                    .field("id", INT32_SCHEMA)
                    .field("name", STRING_SCHEMA)
                    .build()
                val value = Struct(schema)
                    .put("id", 10)
                    .put("name", "test")
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
                
                // Record should be unchanged
                assertEquals(10, transformedValue["id"])
                assertEquals("test", transformedValue["name"])
                // New field should not exist
                assertEquals(2, transformedRecord.valueSchema().fields().size)
            }
    }

    @Test
    fun `target field should inherit source field schema type`() {
        CopyField<SourceRecord>()
            .also {
                it.configure(
                    mapOf(
                        "source.field" to "original",
                        "target.field" to "copy"
                    )
                )
            }
            .use { transform ->
                val schema = SchemaBuilder.struct().name("Entity")
                    .field("id", INT32_SCHEMA)
                    .field("original", STRING_SCHEMA)
                    .build()
                val value = Struct(schema)
                    .put("id", 10)
                    .put("original", "test")
                val record = SourceRecord(
                    null,
                    null,
                    "my_topic",
                    0,
                    schema,
                    value
                )
                val transformedRecord = transform.apply(record)
                
                // Verify target field has same schema as source
                val sourceSchema = transformedRecord.valueSchema().field("original").schema()
                val targetSchema = transformedRecord.valueSchema().field("copy").schema()
                assertEquals(sourceSchema.type(), targetSchema.type())
            }
    }
}

