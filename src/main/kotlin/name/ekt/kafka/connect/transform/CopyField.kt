package name.ekt.kafka.connect.transform

import name.ekt.kafka.connect.transform.CopyField.ConfigName.Companion.SOURCE_FIELD
import name.ekt.kafka.connect.transform.CopyField.ConfigName.Companion.TARGET_FIELD
import org.apache.kafka.common.config.ConfigDef
import org.apache.kafka.connect.connector.ConnectRecord
import org.apache.kafka.connect.data.Schema
import org.apache.kafka.connect.data.SchemaBuilder
import org.apache.kafka.connect.data.Struct
import org.apache.kafka.connect.transforms.Transformation
import org.apache.kafka.connect.transforms.util.SimpleConfig
import org.slf4j.LoggerFactory

class CopyField<R : ConnectRecord<R>> : Transformation<R> {
    private interface ConfigName {
        companion object {
            const val SOURCE_FIELD = "source.field"
            const val TARGET_FIELD = "target.field"
        }
    }

    private val logger = LoggerFactory.getLogger(CopyField::class.java)

    private lateinit var sourceField: String
    private lateinit var targetField: String

    override fun configure(props: Map<String?, *>?) {
        val config = SimpleConfig(CONFIG_DEF, props)
        sourceField = config.getString(SOURCE_FIELD)
        targetField = config.getString(TARGET_FIELD)
        logger.info("Configured with sourceField: {}, targetField: {}", sourceField, targetField)
    }

    override fun apply(record: R): R {
        val value = record.value()
        if (value !is Struct) return record
        val valueSchema: Schema = record.valueSchema()
        val sourceFieldSchema = valueSchema.field(sourceField)?.schema()
            ?: return record

        val updatedSchema: Schema = makeUpdatedSchema(valueSchema, sourceFieldSchema)
        val sourceValue = value.get(sourceField)

        val updatedValue = Struct(updatedSchema)
            .also {
                for (field in valueSchema.fields()) {
                    it.put(field.name(), value.get(field))
                }
            }
            .also {
                it.put(targetField, sourceValue)
            }
        return record.newRecord(
            record.topic(),
            record.kafkaPartition(),
            record.keySchema(),
            record.key(),
            updatedSchema,
            updatedValue,
            record.timestamp()
        )
    }

    private fun makeUpdatedSchema(originalSchema: Schema, sourceFieldSchema: Schema): Schema =
        SchemaBuilder.struct()
            .name(originalSchema.name())
            .version(originalSchema.version())
            .also {
                // Copy all existing fields
                for (field in originalSchema.fields()) {
                    it.field(field.name(), field.schema())
                }
            }
            .also {
                // Add the target field with the same schema as source
                it.field(targetField, sourceFieldSchema)
            }
            .build()

    override fun config(): ConfigDef = CONFIG_DEF

    override fun close() = Unit

    companion object {
        const val OVERVIEW_DOC = "Copy a field value to a new field in Kafka Connect records."
        val CONFIG_DEF = ConfigDef()
            .define(
                SOURCE_FIELD,
                ConfigDef.Type.STRING,
                ConfigDef.NO_DEFAULT_VALUE,
                ConfigDef.Importance.HIGH,
                "Source field to copy from"
            )
            .define(
                TARGET_FIELD,
                ConfigDef.Type.STRING,
                ConfigDef.NO_DEFAULT_VALUE,
                ConfigDef.Importance.HIGH,
                "Target field name to copy to"
            )
    }
}

