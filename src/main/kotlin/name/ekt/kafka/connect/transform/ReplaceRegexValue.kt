package name.ekt.kafka.connect.transform

import name.ekt.kafka.connect.transform.ReplaceRegexValue.ConfigName.Companion.FIELD
import name.ekt.kafka.connect.transform.ReplaceRegexValue.ConfigName.Companion.REGEX
import name.ekt.kafka.connect.transform.ReplaceRegexValue.ConfigName.Companion.REPLACEMENT
import org.apache.kafka.common.config.ConfigDef
import org.apache.kafka.connect.connector.ConnectRecord
import org.apache.kafka.connect.data.Struct
import org.apache.kafka.connect.transforms.Transformation
import org.apache.kafka.connect.transforms.util.SimpleConfig
import org.slf4j.LoggerFactory
import java.util.regex.Pattern

class ReplaceRegexValue<R : ConnectRecord<R>?> : Transformation<R> {
    private interface ConfigName {
        companion object {
            const val FIELD = "field"
            const val REGEX = "regex"
            const val REPLACEMENT = "replacement"
        }
    }

    private val logger = LoggerFactory.getLogger(ReplaceRegexValue::class.java)

    private lateinit var field: String
    private lateinit var replacement: String
    private lateinit var regexPattern: Pattern
    override fun configure(props: Map<String?, *>?) {
        val config = SimpleConfig(CONFIG_DEF, props)
        field = config.getString(FIELD)
        regexPattern =
            config.getString(REGEX)
                .let { Pattern.compile(it) }
        replacement = config.getString(REPLACEMENT)
        logger.info("Configured with field: {}, regex: {}, replacement: {}", field, regexPattern, replacement)
    }

    override fun apply(record: R): R {
        val value = record!!.value()
        if (value is Struct) {
            value.getString(field)
                ?.let { regexPattern.matcher(it).replaceAll(replacement) }
                ?.also { value.put(field, it) }
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
        const val OVERVIEW_DOC = "Replace values in Kafka Connect records."
        val CONFIG_DEF = ConfigDef()
            .define(
                FIELD,
                ConfigDef.Type.STRING,
                ConfigDef.NO_DEFAULT_VALUE,
                ConfigDef.Importance.HIGH,
                "Field to apply the replacement"
            )
            .define(
                REGEX,
                ConfigDef.Type.STRING,
                ConfigDef.NO_DEFAULT_VALUE,
                ConfigDef.Importance.HIGH,
                "Regex to be replaced"
            )
            .define(
                REPLACEMENT,
                ConfigDef.Type.STRING,
                ConfigDef.NO_DEFAULT_VALUE,
                ConfigDef.Importance.HIGH,
                "Value to replace with"
            )
    }
}