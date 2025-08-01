package name.ekt.kafka.connect.transform

import org.apache.kafka.connect.data.Schema
import org.apache.kafka.connect.data.SchemaBuilder
import org.apache.kafka.connect.data.Struct
import org.apache.kafka.connect.sink.SinkRecord
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.locationtech.jts.io.ParseException
import org.locationtech.jts.io.WKBReader

class WktToPostgresGeometryTest {

    private lateinit var transform: WktToPostgresGeometry<SinkRecord>
    private lateinit var schema: Schema
    private lateinit var struct: Struct

    @BeforeEach
    fun setUp() {
        transform = WktToPostgresGeometry()
        schema = SchemaBuilder.struct()
            .field("id", Schema.INT32_SCHEMA)
            .field("shape", Schema.OPTIONAL_STRING_SCHEMA)
            .build()
        struct = Struct(schema)
    }

    @Test
    fun `should convert point WKT to PostgreSQL geometry`() {
        // Given
        val wkt = "POINT(30 10)"
        struct.put("id", 1)
        struct.put("shape", wkt)
        
        val record = SinkRecord("test-topic", 0, null, null, schema, struct, 0L)
        
        val config = mapOf<String?, Any>(
            "field" to "shape",
            "srid" to 4326
        )
        transform.configure(config)

        // When
        val result = transform.apply(record)
        val resultStruct = result.value() as Struct
        val postgresGeometry = resultStruct.getBytes("shape")

        // Then
        assertNotNull(postgresGeometry)
        val geometry = WKBReader().read(postgresGeometry)
        assertEquals(4326, geometry.srid)
        assertEquals("POINT (30 10)", geometry.toText())
    }

    @Test
    fun `should convert linestring WKT to PostgreSQL geometry`() {
        // Given
        val wkt = "LINESTRING(30 10, 10 30, 40 40)"
        struct.put("id", 1)
        struct.put("shape", wkt)
        
        val record = SinkRecord("test-topic", 0, null, null, schema, struct, 0L)
        
        val config = mapOf<String?, Any>(
            "field" to "shape",
            "srid" to 4326
        )
        transform.configure(config)

        // When
        val result = transform.apply(record)
        val resultStruct = result.value() as Struct

        val postgresGeometry = resultStruct.getBytes("shape")

        // Then
        assertNotNull(postgresGeometry)
        val geometry = WKBReader().read(postgresGeometry)
        assertEquals(4326, geometry.srid)
        assertEquals("LINESTRING (30 10, 10 30, 40 40)", geometry.toText())
    }

    @Test
    fun `should convert polygon WKT to PostgreSQL geometry`() {
        // Given
        val wkt = "POLYGON((30 10, 40 40, 20 40, 10 20, 30 10))"
        struct.put("id", 1)
        struct.put("shape", wkt)
        
        val record = SinkRecord("test-topic", 0, null, null, schema, struct, 0L)
        
        val config = mapOf<String?, Any>(
            "field" to "shape",
            "srid" to 4326
        )
        transform.configure(config)

        // When
        val result = transform.apply(record)
        val resultStruct = result.value() as Struct
        val postgresGeometry = resultStruct.getBytes("shape")

        // Then
        assertNotNull(postgresGeometry)
        assertTrue(postgresGeometry.isNotEmpty())
        val geometry = WKBReader().read(postgresGeometry)
        assertEquals(4326, geometry.srid)
        assertEquals("POLYGON ((30 10, 40 40, 20 40, 10 20, 30 10))", geometry.toText())
    }

    @Test
    fun `should handle null WKT value gracefully`() {
        // Given
        struct.put("id", 1)
        struct.put("shape", null)
        
        val record = SinkRecord("test-topic", 0, null, null, schema, struct, 0L)
        
        val config = mapOf<String?, Any>(
            "field" to "shape",
            "srid" to 4326
        )
        transform.configure(config)

        // When
        val result = transform.apply(record)
        val resultStruct = result.value() as Struct

        // Then
        assertNull(resultStruct.getString("shape"))
    }

    @Test
    fun `should handle empty WKT value gracefully`() {
        // Given
        struct.put("id", 1)
        struct.put("shape", "")
        
        val record = SinkRecord("test-topic", 0, null, null, schema, struct, 0L)
        
        val config = mapOf<String?, Any>(
            "field" to "shape",
            "srid" to 4326
        )
        transform.configure(config)

        // When
        val result = transform.apply(record)
        val resultStruct = result.value() as Struct

        // Then
        assertEquals("", resultStruct.getString("shape"))
    }

    @Test
    fun `should handle invalid WKT properly`() {
        // Given
        struct.put("id", 1)
        struct.put("shape", "INVALID WKT")
        
        val record = SinkRecord("test-topic", 0, null, null, schema, struct, 0L)
        
        val config = mapOf<String?, Any>(
            "field" to "shape",
            "srid" to 4326
        )
        transform.configure(config)

        // When
        assertThrows<ParseException> {
            transform.apply(record)
        }
    }

    @Test
    fun `should use custom SRID when provided`() {
        // Given
        val wkt = "POINT(30 10)"
        struct.put("id", 1)
        struct.put("shape", wkt)
        
        val record = SinkRecord("test-topic", 0, null, null, schema, struct, 0L)
        
        val config = mapOf<String?, Any>(
            "field" to "shape",
            "srid" to 3857 // Web Mercator
        )
        transform.configure(config)

        // When
        val result = transform.apply(record)
        val resultStruct = result.value() as Struct
        val postgresGeometry = resultStruct.getBytes("shape")

        // Then
        assertNotNull(postgresGeometry)
        val geometry = WKBReader().read(postgresGeometry)
        assertEquals(3857, geometry.srid)
        assertEquals("POINT (30 10)", geometry.toText())
    }

    @Test
    fun `should preserve other fields in record`() {
        // Given
        val wkt = "POINT(30 10)"
        struct.put("id", 1)
        struct.put("shape", wkt)
        
        val record = SinkRecord("test-topic", 0, null, null, schema, struct, 0L)
        
        val config = mapOf<String?, Any>(
            "field" to "shape",
            "srid" to 4326
        )
        transform.configure(config)

        // When
        val result = transform.apply(record)
        val resultStruct = result.value() as Struct

        // Then
        assertEquals(1, resultStruct.getInt32("id"))
        val postgresGeometry = resultStruct.getBytes("shape")
        assertNotNull(postgresGeometry)
        assertTrue(postgresGeometry.isNotEmpty())
    }
} 