package name.ekt.kafka.connect.transform

import org.apache.kafka.common.config.ConfigDef
import org.apache.kafka.common.config.ConfigDef.Importance.HIGH
import org.apache.kafka.common.config.ConfigDef.NO_DEFAULT_VALUE
import org.apache.kafka.common.config.ConfigDef.Type.STRING
import org.apache.kafka.connect.connector.ConnectRecord
import org.apache.kafka.connect.data.Schema
import org.apache.kafka.connect.data.Schema.STRING_SCHEMA
import org.apache.kafka.connect.data.SchemaBuilder
import org.apache.kafka.connect.data.Struct
import org.apache.kafka.connect.transforms.Transformation
import org.apache.kafka.connect.transforms.util.SimpleConfig
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class SqlVariantToString<R : ConnectRecord<R>> : Transformation<R> {

    companion object {
        const val FIELD_CONFIG = "field"
        val CONFIG_DEF: ConfigDef = ConfigDef()
            .define(
                FIELD_CONFIG,
                STRING,
                NO_DEFAULT_VALUE,
                HIGH,
                "The name of the field to be converted"
            )
    }

    private lateinit var fieldName: String

    private val logger: Logger = LoggerFactory.getLogger(SqlVariantToString::class.java)

    override fun configure(configs: Map<String, *>) {
        val config = SimpleConfig(CONFIG_DEF, configs)
        fieldName = config.getString(FIELD_CONFIG)
        logger.info("Configured to convert field: $fieldName")
    }

    override fun apply(record: R): R {
        val value = record.value() as? Struct ?: return record

        logger.info("Processing record: {}", record)

        val updatedSchema = createUpdatedSchema(value.schema())
        val updatedValue = createUpdatedValue(value, updatedSchema)

        logger.info("Updated schema: {}", updatedSchema)
        logger.info("Updated value: {}", updatedValue)

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

    private fun createUpdatedSchema(schema: Schema): Schema {
        logger.info("Creating updated schema for: {}", schema)
        return SchemaBuilder.struct()
            .also { builder ->
                schema.fields().forEach { field ->
                    builder.field(field.name(), if (fieldName == field.name()) STRING_SCHEMA else field.schema())
                }
            }
            .build()
    }

    private fun createUpdatedValue(value: Struct, updatedSchema: Schema): Struct {
        logger.info("Creating updated value for: {}", value)
        return Struct(updatedSchema)
            .also {
                for (field in value.schema().fields()) {
                    it.put(
                        field.name(),
                        if (fieldName == field.name()) value.get(field)?.toString() else value.get(field.name())
                    )
                }
            }
    }

    override fun config(): ConfigDef = CONFIG_DEF

    override fun close() {
        logger.info("Closing SqlVariantToString SMT")
    }
}
