package name.ekt.kafka.connect.transform

import org.apache.kafka.common.config.ConfigDef
import org.apache.kafka.connect.connector.ConnectRecord
import org.apache.kafka.connect.transforms.util.SimpleConfig
import java.util.regex.Pattern

class RenameFieldNamesRegEx<R : ConnectRecord<R>>
    : ProcessFieldNames<R>() {
    private interface ConfigName {
        companion object {
            const val REGEX = "regex"
            const val REPLACEMENT = "replacement"
        }
    }

    private lateinit var replacement: String
    private lateinit var regexPattern: Pattern
    override fun configure(configs: Map<String, *>?) {
        val config = SimpleConfig(CONFIG_DEF, configs)
        regexPattern =
            config.getString(ConfigName.REGEX)
                .let { Pattern.compile(it) }
        replacement = config.getString(ConfigName.REPLACEMENT)
    }

    override fun transformFieldName(fieldName: String): String =
        regexPattern.matcher(fieldName).replaceAll(replacement)

    companion object {
        const val OVERVIEW_DOC = "Renames fields in Kafka Connect records."
        val CONFIG_DEF = ConfigDef()
            .define(
                ConfigName.REGEX,
                ConfigDef.Type.STRING,
                ConfigDef.NO_DEFAULT_VALUE,
                ConfigDef.Importance.HIGH,
                "Regex to be replaced"
            )
            .define(
                ConfigName.REPLACEMENT,
                ConfigDef.Type.STRING,
                ConfigDef.NO_DEFAULT_VALUE,
                ConfigDef.Importance.HIGH,
                "Value to replace with"
            )
    }
}