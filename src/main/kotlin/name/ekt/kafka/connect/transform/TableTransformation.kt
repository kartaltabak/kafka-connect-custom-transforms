package name.ekt.kafka.connect.transform

import org.apache.kafka.common.config.ConfigDef
import org.apache.kafka.connect.connector.ConnectRecord
import org.apache.kafka.connect.transforms.Transformation
import org.slf4j.LoggerFactory

abstract class TableTransformation<R : ConnectRecord<R>?>(
    private val transformFunc: (String) -> String
) : Transformation<R> {

    override fun configure(props: Map<String?, *>?) = Unit

    override fun apply(record: R): R {
        return when (val topic = transformFunc(record!!.topic())) {
            record.topic() -> {
                log.trace("Not rerouting topic '{}' as original and rerouted ones are the same", record.topic())
                record
            }
            else -> {
                log.trace("Rerouting from topic '{}' to new topic '{}'", record.topic(), topic)
                record.newRecord(
                    topic,
                    record.kafkaPartition(),
                    record.keySchema(),
                    record.key(),
                    record.valueSchema(),
                    record.value(),
                    record.timestamp()
                )
            }
        }
    }

    override fun close() = Unit

    override fun config(): ConfigDef = CONFIG_DEF

    companion object {
        private val log = LoggerFactory.getLogger(TableTransformation::class.java)
        val CONFIG_DEF = ConfigDef()
    }
}