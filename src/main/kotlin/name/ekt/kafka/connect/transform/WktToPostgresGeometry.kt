package name.ekt.kafka.connect.transform

import name.ekt.kafka.connect.transform.WktToPostgresGeometry.ConfigName.Companion.FIELD
import name.ekt.kafka.connect.transform.WktToPostgresGeometry.ConfigName.Companion.SRID
import org.apache.kafka.common.config.ConfigDef
import org.apache.kafka.connect.connector.ConnectRecord
import org.apache.kafka.connect.data.Schema
import org.apache.kafka.connect.data.Schema.BYTES_SCHEMA
import org.apache.kafka.connect.data.SchemaBuilder
import org.apache.kafka.connect.data.Struct
import org.apache.kafka.connect.transforms.Transformation
import org.apache.kafka.connect.transforms.util.SimpleConfig
import org.locationtech.jts.io.WKBWriter
import org.locationtech.jts.io.WKTReader
import org.slf4j.LoggerFactory

class WktToPostgresGeometry<R : ConnectRecord<R>?> : Transformation<R> {
    private interface ConfigName {
        companion object {
            const val FIELD = "field"
            const val SRID = "srid"
        }
    }

    private val logger = LoggerFactory.getLogger(WktToPostgresGeometry::class.java)
    private val wktReader = WKTReader()

    private lateinit var field: String
    private var srid: Int = 4326 // Default to WGS84
    private val wkbWriter = WKBWriter(2, true)


    override fun configure(props: Map<String?, *>?) {
        val config = SimpleConfig(CONFIG_DEF, props)
        field = config.getString(FIELD)
        srid = config.getInt(SRID)
        logger.info("Configured with field: {}, SRID: {}", field, srid)
    }

    private fun copySchemaWithFieldOverride(
        original: Schema,
        overrideFieldName: String,
        overrideSchema: Schema
    ): Schema {
        val builder = SchemaBuilder.struct().name(original.name()).version(original.version())
        for (field in original.fields()) {
            val schema = if (field.name() == overrideFieldName) overrideSchema else field.schema()
            builder.field(field.name(), schema)
        }
        return builder.build()
    }

    override fun apply(record: R): R {
        val value = record!!.value()
        val valueSchema = record.valueSchema()

        if (value !is Struct) return record

        val wktValue = value.getString(field)
        if (wktValue.isNullOrBlank()) {
            return record // nothing to change
        }

        val wkbValue = convertWktToPostgresGeometry(wktValue, srid)
        logger.debug("Converted WKT to WKB for field: {}", field)

        // Create new schema with only one field overridden
        val newSchema = copySchemaWithFieldOverride(
            valueSchema,
            field,
            BYTES_SCHEMA
        )

        val newStruct = copyValueWithOverwrite(newSchema, valueSchema, wkbValue, value)

        return record.newRecord(
            record.topic(), record.kafkaPartition(),
            record.keySchema(), record.key(),
            newSchema, newStruct,
            record.timestamp()
        )
    }

    private fun copyValueWithOverwrite(
        newSchema: Schema,
        valueSchema: Schema,
        overwriteValue: ByteArray,
        value: Struct
    ): Struct {
        val newStruct = Struct(newSchema)
        for (f in valueSchema.fields()) {
            val name = f.name()
            if (name == field) {
                newStruct.put(name, overwriteValue)
            } else {
                newStruct.put(name, value.get(name))
            }
        }
        return newStruct
    }


    private fun convertWktToPostgresGeometry(wkt: String, srid: Int): ByteArray {
        val geometry = wktReader.read(wkt)
        geometry.srid = srid
        return wkbWriter.write(geometry)
    }

    override fun config(): ConfigDef {
        return CONFIG_DEF
    }

    override fun close() {}

    companion object {
        const val OVERVIEW_DOC = "Convert WKT geometry to PostgreSQL geometry format with SRID."
        val CONFIG_DEF = ConfigDef()
            .define(
                FIELD,
                ConfigDef.Type.STRING,
                ConfigDef.NO_DEFAULT_VALUE,
                ConfigDef.Importance.HIGH,
                "Field containing WKT geometry to convert"
            )
            .define(
                SRID,
                ConfigDef.Type.INT,
                4326,
                ConfigDef.Importance.MEDIUM,
                "Spatial Reference System Identifier (default: 4326 for WGS84)"
            )
    }
}