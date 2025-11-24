package name.ekt.kafka.connect.predicate

import org.apache.kafka.common.config.AbstractConfig
import org.apache.kafka.common.config.ConfigDef
import org.apache.kafka.connect.connector.ConnectRecord
import org.apache.kafka.connect.transforms.predicates.Predicate
import org.slf4j.LoggerFactory

/**
 * A predicate that checks if the record value is NOT an instance of a specific type.
 * Uses Class.forName() to load the target type and isInstance() to check the type.
 */
class ValueTypeIsNotInstance<R : ConnectRecord<R>> : Predicate<R> {
    private companion object {
        const val CLASS_NAME_CONFIG = "class.name"

        val CONFIG_DEF: ConfigDef = ConfigDef()
            .define(
                CLASS_NAME_CONFIG,
                ConfigDef.Type.STRING,
                ConfigDef.NO_DEFAULT_VALUE,
                ConfigDef.Importance.HIGH,
                "The fully qualified class name to check against (e.g., java.lang.String, " +
                        "org.apache.kafka.connect.data.Struct)"
            )
    }

    private val logger = LoggerFactory.getLogger(ValueTypeIsNotInstance::class.java)

    private lateinit var targetType: Class<*>

    override fun configure(configs: Map<String, *>?) {
        val config = AbstractConfig(CONFIG_DEF, configs)
        val typeName = config.getString(CLASS_NAME_CONFIG)

        if (typeName.isNullOrEmpty()) {
            throw IllegalArgumentException("`$CLASS_NAME_CONFIG` must be provided")
        }

        try {
            targetType = Class.forName(typeName)
            logger.info("Configured ValueTypeIsNotInstance predicate with type: {}", typeName)
        } catch (e: ClassNotFoundException) {
            throw IllegalArgumentException("Could not load class $typeName", e)
        }
    }

    override fun test(record: R): Boolean =
        record.value()
            ?.let { !targetType.isInstance(it) }
            ?: true

    override fun close() = Unit

    override fun config(): ConfigDef = CONFIG_DEF
}

