package name.ekt.kafka.connect.predicate

import org.apache.kafka.connect.data.Schema
import org.apache.kafka.connect.data.SchemaBuilder
import org.apache.kafka.connect.data.Struct
import org.apache.kafka.connect.source.SourceRecord
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ValueTypeIsNotInstanceTest {
    @Test
    fun `test struct type detection returns false when value is struct`() {
        val predicate = ValueTypeIsNotInstance<SourceRecord>()

        predicate.configure(
            mapOf(
                "class.name" to "org.apache.kafka.connect.data.Struct"
            )
        )

        val schema = SchemaBuilder.struct()
            .field("name", Schema.STRING_SCHEMA)
            .field("age", Schema.INT32_SCHEMA)
            .build()

        val struct = Struct(schema)
            .put("name", "John")
            .put("age", 30)

        val record = SourceRecord(
            mapOf("partition" to 1),
            mapOf("offset" to 1),
            "test-topic",
            0,
            Schema.STRING_SCHEMA,
            "key",
            schema,
            struct
        )

        // Should return false since value IS a Struct
        assertFalse(predicate.test(record))
    }

    @Test
    fun `test byte array type returns false when value is byte array`() {
        val predicate = ValueTypeIsNotInstance<SourceRecord>()

        predicate.configure(
            mapOf(
                "class.name" to "[B"
            )
        )

        val record = SourceRecord(
            mapOf("partition" to 1),
            mapOf("offset" to 1),
            "test-topic",
            0,
            Schema.STRING_SCHEMA,
            "key",
            Schema.BYTES_SCHEMA,
            "test data".toByteArray()
        )

        // Should return false since value IS a byte array
        assertFalse(predicate.test(record))
    }

    @Test
    fun `test string type returns false when value is string`() {
        val predicate = ValueTypeIsNotInstance<SourceRecord>()

        predicate.configure(
            mapOf(
                "class.name" to "java.lang.String"
            )
        )

        val record = SourceRecord(
            mapOf("partition" to 1),
            mapOf("offset" to 1),
            "test-topic",
            0,
            Schema.STRING_SCHEMA,
            "key",
            Schema.STRING_SCHEMA,
            "test value"
        )

        // Should return false since value IS a String
        assertFalse(predicate.test(record))
    }

    @Test
    fun `test integer type returns false when value is integer`() {
        val predicate = ValueTypeIsNotInstance<SourceRecord>()

        predicate.configure(
            mapOf(
                "class.name" to "java.lang.Integer"
            )
        )

        val record = SourceRecord(
            mapOf("partition" to 1),
            mapOf("offset" to 1),
            "test-topic",
            0,
            Schema.STRING_SCHEMA,
            "key",
            Schema.INT32_SCHEMA,
            42
        )

        // Should return false since value IS an Integer
        assertFalse(predicate.test(record))
    }

    @Test
    fun `test long type returns false when value is long`() {
        val predicate = ValueTypeIsNotInstance<SourceRecord>()

        predicate.configure(
            mapOf(
                "class.name" to "java.lang.Long"
            )
        )

        val record = SourceRecord(
            mapOf("partition" to 1),
            mapOf("offset" to 1),
            "test-topic",
            0,
            Schema.STRING_SCHEMA,
            "key",
            Schema.INT64_SCHEMA,
            42L
        )

        // Should return false since value IS a Long
        assertFalse(predicate.test(record))
    }

    @Test
    fun `test null value returns true`() {
        val predicate = ValueTypeIsNotInstance<SourceRecord>()

        predicate.configure(
            mapOf(
                "class.name" to "java.lang.String"
            )
        )

        val record = SourceRecord(
            mapOf("partition" to 1),
            mapOf("offset" to 1),
            "test-topic",
            0,
            Schema.STRING_SCHEMA,
            "key",
            null,
            null
        )

        // Should return true when value is null
        assertTrue(predicate.test(record))
    }

    @Test
    fun `test type mismatch returns true`() {
        val predicate = ValueTypeIsNotInstance<SourceRecord>()

        predicate.configure(
            mapOf(
                "class.name" to "org.apache.kafka.connect.data.Struct"
            )
        )

        val record = SourceRecord(
            mapOf("partition" to 1),
            mapOf("offset" to 1),
            "test-topic",
            0,
            Schema.STRING_SCHEMA,
            "key",
            Schema.STRING_SCHEMA,
            "test value"
        )

        // Should return true since value is NOT a Struct
        assertTrue(predicate.test(record))
    }

    @Test
    fun `test boolean type returns false when value is boolean`() {
        val predicate = ValueTypeIsNotInstance<SourceRecord>()

        predicate.configure(
            mapOf(
                "class.name" to "java.lang.Boolean"
            )
        )

        val record = SourceRecord(
            mapOf("partition" to 1),
            mapOf("offset" to 1),
            "test-topic",
            0,
            Schema.STRING_SCHEMA,
            "key",
            Schema.BOOLEAN_SCHEMA,
            true
        )

        // Should return false since value IS a Boolean
        assertFalse(predicate.test(record))
    }

    @Test
    fun `test float type returns false when value is float`() {
        val predicate = ValueTypeIsNotInstance<SourceRecord>()

        predicate.configure(
            mapOf(
                "class.name" to "java.lang.Float"
            )
        )

        val record = SourceRecord(
            mapOf("partition" to 1),
            mapOf("offset" to 1),
            "test-topic",
            0,
            Schema.STRING_SCHEMA,
            "key",
            Schema.FLOAT32_SCHEMA,
            3.14f
        )

        // Should return false since value IS a Float
        assertFalse(predicate.test(record))
    }

    @Test
    fun `test double type returns false when value is double`() {
        val predicate = ValueTypeIsNotInstance<SourceRecord>()

        predicate.configure(
            mapOf(
                "class.name" to "java.lang.Double"
            )
        )

        val record = SourceRecord(
            mapOf("partition" to 1),
            mapOf("offset" to 1),
            "test-topic",
            0,
            Schema.STRING_SCHEMA,
            "key",
            Schema.FLOAT64_SCHEMA,
            3.14159
        )

        // Should return false since value IS a Double
        assertFalse(predicate.test(record))
    }

    @Test
    fun `test string check returns true when value is struct`() {
        val predicate = ValueTypeIsNotInstance<SourceRecord>()

        predicate.configure(
            mapOf(
                "class.name" to "java.lang.String"
            )
        )

        val schema = SchemaBuilder.struct()
            .field("name", Schema.STRING_SCHEMA)
            .build()

        val struct = Struct(schema)
            .put("name", "John")

        val record = SourceRecord(
            mapOf("partition" to 1),
            mapOf("offset" to 1),
            "test-topic",
            0,
            Schema.STRING_SCHEMA,
            "key",
            schema,
            struct
        )

        // Should return true since value is NOT a String
        assertTrue(predicate.test(record))
    }
}

