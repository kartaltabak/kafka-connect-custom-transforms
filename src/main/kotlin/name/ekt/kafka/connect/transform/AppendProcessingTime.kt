package name.ekt.kafka.connect.transform

import org.apache.kafka.common.config.ConfigDef
import org.apache.kafka.common.config.ConfigDef.Importance.HIGH
import org.apache.kafka.common.config.ConfigDef.Type.BOOLEAN
import org.apache.kafka.common.config.ConfigDef.Type.STRING
import org.apache.kafka.connect.connector.ConnectRecord
import org.apache.kafka.connect.data.Schema
import org.apache.kafka.connect.data.SchemaBuilder
import org.apache.kafka.connect.data.Struct
import org.apache.kafka.connect.data.Timestamp
import org.apache.kafka.connect.transforms.Transformation
import org.apache.kafka.connect.transforms.util.SimpleConfig
import java.util.*


class AppendProcessingTime<R : ConnectRecord<R>>
    : Transformation<R> {

    private interface ConfigName {
        companion object {
            const val FIELD_NAME = "field"
            const val IS_OPTIONAL_NAME = "is_optional"
        }
    }

    val CONFIG_DEF = ConfigDef()
        .define(
            ConfigName.FIELD_NAME,
            STRING,
            "processing_time",
            HIGH,
            "Field name for the processing timestamp"
        )
        .define(
            ConfigName.IS_OPTIONAL_NAME,
            BOOLEAN,
            true,
            HIGH,
            "Optionality for the processing timestamp"
        )

    private var fieldName: String? = null
    private var isOptional: Boolean = true
    private var timestampSchema: Schema? = null
    override fun configure(configs: Map<String, *>?) {
        val config = SimpleConfig(CONFIG_DEF, configs)
        fieldName = config.getString(ConfigName.FIELD_NAME)
        isOptional = config.getBoolean(ConfigName.IS_OPTIONAL_NAME) ?: true
        timestampSchema = if (isOptional) Timestamp.builder().optional().schema() else Timestamp.SCHEMA
    }

    override fun close() = Unit

    override fun config(): ConfigDef = CONFIG_DEF

    override fun apply(record: R): R =
        when (val value = record.value()) {
            is Struct -> {
                val valueSchema: Schema = record.valueSchema()
                val updatedSchema: Schema = makeUpdatedSchema(valueSchema)
                val updatedValue = Struct(updatedSchema)
                    .also {
                        for (field in valueSchema.fields()) {
                            it.put(field.name(), value.get(field))
                        }
                    }
                    .also {
                        it.put(fieldName, Date())
                    }
                record.newRecord(
                    record.topic(), record.kafkaPartition(),
                    record.keySchema(), record.key(),
                    updatedSchema, updatedValue,
                    record.timestamp()
                )
            }

            else -> record
        }

    private fun makeUpdatedSchema(originalSchema: Schema): Schema =
        SchemaBuilder.struct()
            .also {
                for (field in originalSchema.fields()) {
                    it.field(field.name(), field.schema())
                }
            }
            .also {
                it.field(fieldName, timestampSchema)
            }
            .build()
}