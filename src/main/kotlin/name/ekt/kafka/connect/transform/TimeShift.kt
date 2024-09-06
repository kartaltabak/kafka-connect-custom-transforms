package name.ekt.kafka.connect.transform

import org.apache.kafka.common.config.ConfigDef
import org.apache.kafka.connect.connector.ConnectRecord
import org.apache.kafka.connect.data.Struct
import org.apache.kafka.connect.transforms.Transformation
import org.apache.kafka.connect.transforms.util.SimpleConfig
import java.time.ZoneOffset
import java.util.Date

class TimeShift<R : ConnectRecord<R>> : Transformation<R> {

    private lateinit var fieldName: String
    private var hoursShift: Long = 0

    companion object {
        const val FIELD_CONFIG = "field"
        const val HOURS_CONFIG = "hours"

        val CONFIG_DEF: ConfigDef =
            ConfigDef().define(FIELD_CONFIG, ConfigDef.Type.STRING, ConfigDef.Importance.HIGH, "The field to shift.")
                .define(HOURS_CONFIG, ConfigDef.Type.LONG, ConfigDef.Importance.HIGH, "Number of hours to shift.")
    }

    override fun configure(configs: Map<String, *>?) {
        val config = SimpleConfig(CONFIG_DEF, configs)
        fieldName = config.getString(FIELD_CONFIG)
        hoursShift = config.getLong(HOURS_CONFIG)
    }

    override fun apply(record: R): R {
        if (record.value() is Struct) {
            val valueStruct = record.value() as Struct
            val schema = record.valueSchema()

            schema.field(fieldName)?.let {field ->
                val dateField = valueStruct.get(field) as? Date

                dateField?.let {date ->
                    date.toInstant()
                        .atZone(ZoneOffset.UTC)
                        .plusHours(hoursShift)
                        .toInstant()
                        .let { Date.from(it) }
                        .also {
                            valueStruct.put(fieldName, it)
                        }
                }
            }

            return record.newRecord(
                record.topic(),
                record.kafkaPartition(),
                record.keySchema(),
                record.key(),
                schema,
                valueStruct,
                record.timestamp()
            )
        }

        return record
    }

    override fun close() {
        // Clean up resources if needed
    }

    override fun config(): ConfigDef = CONFIG_DEF
}
