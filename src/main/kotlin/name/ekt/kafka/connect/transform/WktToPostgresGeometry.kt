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

class WktToPostgresGeometry<R : ConnectRecord<R>?> : Transformation<R> {
    private interface ConfigName {
        companion object {
            const val FIELD = "field"
            const val SRID = "srid"
        }
    }

    private lateinit var field: String
    private var srid: Int = 0

    private val wktReader = WKTReader()
    private val wkbWriter = WKBWriter(2, true)

    override fun configure(props: Map<String?, *>?) {
        val config = SimpleConfig(CONFIG_DEF, props)
        field = config.getString(FIELD)
        srid = config.getInt(SRID)
    }

    private fun copySchemaWithFieldByteArrayOverride(original: Schema): Schema =
        SchemaBuilder.struct()
            .name(original.name())
            .version(original.version())
            .also {
                original.fields().forEach { schemaField ->
                    val schema = if (schemaField.name() == field) BYTES_SCHEMA else schemaField.schema()
                    it.field(schemaField.name(), schema)
                }
            }
            .build()

    override fun apply(record: R): R {
        val value = record!!.value()
        val valueSchema = record.valueSchema()

        if (value !is Struct) return record

        val wktValue = value.getString(field)
        if (wktValue.isNullOrBlank()) {
            return record // nothing to change
        }

        val newValueSchema = copySchemaWithFieldByteArrayOverride(valueSchema)

        val wkbValue = convertWktToPostgresGeometry(wktValue, srid)
        val newValue = copyValueWithOverwrite(newValueSchema, valueSchema, wkbValue, value)

        return record.newRecord(
            record.topic(), record.kafkaPartition(),
            record.keySchema(), record.key(),
            newValueSchema, newValue,
            record.timestamp()
        )
    }

    private fun copyValueWithOverwrite(
        newSchema: Schema,
        valueSchema: Schema,
        overwriteValue: ByteArray,
        value: Struct
    ): Struct =
        Struct(newSchema)
            .also {
                valueSchema.fields().forEach { f ->
                    val name = f.name()
                    val v = if (name == field) overwriteValue else value.get(name)
                    it.put(name, v)
                }
            }

    private fun convertWktToPostgresGeometry(wkt: String, srid: Int): ByteArray =
        wktReader.read(wkt)
            .also { it.srid = srid }
            .let { wkbWriter.write(it) }

    override fun config(): ConfigDef = CONFIG_DEF

    override fun close() = Unit

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
                0,
                ConfigDef.Importance.MEDIUM,
                "Spatial Reference System Identifier (default: 0)"
            )
    }
}