package name.ekt.kafka.connect.transform

import org.apache.kafka.common.config.ConfigDef
import org.apache.kafka.connect.connector.ConnectRecord
import org.apache.kafka.connect.data.Schema
import org.apache.kafka.connect.data.SchemaBuilder
import org.apache.kafka.connect.data.Struct
import org.apache.kafka.connect.transforms.Transformation

abstract class ProcessFieldNames<R : ConnectRecord<R>> : Transformation<R> {
    override fun apply(record: R?): R? =
        record?.let {
            val newKeySchema = makeLowercaseSchema(it.keySchema())
            val newValueSchema = makeLowercaseSchema(it.valueSchema())
            val newKey = transformStruct(it.key(), newKeySchema)
            val newValue = transformStruct(it.value(), newValueSchema)
            it.newRecord(
                it.topic(), it.kafkaPartition(),
                newKeySchema, newKey,
                newValueSchema, newValue,
                it.timestamp()
            )
        }

    private fun makeLowercaseSchema(schema: Schema?): Schema? =
        schema?.let {
            SchemaBuilder.struct()
                .also {
                    schema.fields().forEach { field ->
                        it.field(transformFieldName(field.name()), field.schema())
                    }
                }.build()
        }

    private fun transformStruct(data: Any, newSchema: Schema?): Any =
        when (data) {
            is Struct -> {
                Struct(newSchema)
                    .also {
                        data.schema().fields().forEach { field ->
                            it.put(transformFieldName(field.name()), data[field])
                        }
                    }
            }

            else -> data
        }

    override fun config(): ConfigDef = ConfigDef()

    override fun close() = Unit
    override fun configure(configs: Map<String, *>?) = Unit

    abstract fun transformFieldName(fieldName: String): String
}
