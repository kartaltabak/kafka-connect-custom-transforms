package name.ekt.kafka.connect.transform

import org.apache.kafka.common.config.ConfigDef
import org.apache.kafka.connect.connector.ConnectRecord
import org.apache.kafka.connect.data.Schema
import org.apache.kafka.connect.data.Struct
import org.apache.kafka.connect.transforms.Transformation
import java.util.regex.Pattern

class RemoveNullCharacters<R : ConnectRecord<R>?> : Transformation<R> {
    override fun configure(props: Map<String?, *>?) = Unit

    private val regexPattern: Pattern = Pattern.compile("\u0000")

    override fun apply(record: R): R {
        val value = record!!.value()
        if (value is Struct) {
            for (field in value.schema().fields()) {
                if (field.schema().equals(Schema.STRING_SCHEMA)) {
                    val stringValue = value[field] as String
                    if (stringValue.contains('\u0000')) {
                        val newValue = regexPattern.matcher(stringValue).replaceAll("")
                        value.put(field, newValue)
                    }
                }
            }
        }
        return record.newRecord(
            record.topic(), record.kafkaPartition(),
            record.keySchema(), record.key(),
            record.valueSchema(), value,
            record.timestamp()
        )
    }

    override fun config(): ConfigDef = CONFIG_DEF

    override fun close() = Unit

    companion object {
        val CONFIG_DEF = ConfigDef()
    }
}