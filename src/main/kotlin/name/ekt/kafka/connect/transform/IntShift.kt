package name.ekt.kafka.connect.transform

import org.apache.kafka.common.config.ConfigDef
import org.apache.kafka.connect.connector.ConnectRecord
import org.apache.kafka.connect.data.Struct
import org.apache.kafka.connect.transforms.Transformation
import org.apache.kafka.connect.transforms.util.SimpleConfig
import java.time.ZoneOffset
import java.util.Date

class IntShift<R : ConnectRecord<R>> : Transformation<R> {

    private lateinit var fieldName: String
    private var shiftAmount: Long = 0

    companion object {
        const val FIELD_CONFIG = "field"
        const val SHIFT_AMOUNT_CONFIG = "shift-amount"

        val CONFIG_DEF: ConfigDef =
            ConfigDef().define(FIELD_CONFIG, ConfigDef.Type.STRING, ConfigDef.Importance.HIGH, "The field to shift.")
                .define(SHIFT_AMOUNT_CONFIG, ConfigDef.Type.LONG, ConfigDef.Importance.HIGH, "Amount to shift.")
    }

    override fun configure(configs: Map<String, *>?) {
        val config = SimpleConfig(CONFIG_DEF, configs)
        fieldName = config.getString(FIELD_CONFIG)
        shiftAmount = config.getLong(SHIFT_AMOUNT_CONFIG)
    }

    override fun apply(record: R): R =
        if (record.value() !is Struct) {
            record
        } else {
            val valueStruct = record.value() as Struct
            val schema = record.valueSchema()

            schema.field(fieldName)?.let { field ->
                val intField = valueStruct.get(field) as? Int

                intField?.let { x ->
                    valueStruct.put(fieldName, x+shiftAmount)
                }
            }

            record.newRecord(
                record.topic(),
                record.kafkaPartition(),
                record.keySchema(),
                record.key(),
                schema,
                valueStruct,
                record.timestamp()
            )
        }

    override fun close() = Unit

    override fun config(): ConfigDef = CONFIG_DEF
}
