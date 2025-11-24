package name.ekt.kafka.connect.predicate

import org.apache.kafka.connect.data.Schema
import org.apache.kafka.connect.data.SchemaBuilder
import org.apache.kafka.connect.data.Struct
import org.apache.kafka.connect.source.SourceRecord
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ValueTypeIsInstanceTest {
    @Test
    fun `test struct type detection on value`() {
        val predicate = ValueTypeIsInstance<SourceRecord>()

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

        assertTrue(predicate.test(record))
    }

    @Test
    fun `test byte array type detection on value`() {
        val predicate = ValueTypeIsInstance<SourceRecord>()

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

        assertTrue(predicate.test(record))
    }

    @Test
    fun `test string type detection on value`() {
        val predicate = ValueTypeIsInstance<SourceRecord>()

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

        assertTrue(predicate.test(record))
    }

    @Test
    fun `test integer type detection on value`() {
        val predicate = ValueTypeIsInstance<SourceRecord>()

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

        assertTrue(predicate.test(record))
    }

    @Test
    fun `test long type detection on value`() {
        val predicate = ValueTypeIsInstance<SourceRecord>()

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

        assertTrue(predicate.test(record))
    }

    @Test
    fun `test null value returns false`() {
        val predicate = ValueTypeIsInstance<SourceRecord>()

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

        // Should return false when value is null
        assertFalse(predicate.test(record))
    }

    @Test
    fun `test type mismatch on value`() {
        val predicate = ValueTypeIsInstance<SourceRecord>()

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

        assertFalse(predicate.test(record))
    }

    @Test
    fun `test float type detection on value`() {
        val predicate = ValueTypeIsInstance<SourceRecord>()

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

        assertTrue(predicate.test(record))
    }

    @Test
    fun `test double type detection on value`() {
        val predicate = ValueTypeIsInstance<SourceRecord>()

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

        assertTrue(predicate.test(record))
    }
}

