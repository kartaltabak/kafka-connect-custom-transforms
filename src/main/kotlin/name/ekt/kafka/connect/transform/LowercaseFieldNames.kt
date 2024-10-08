package name.ekt.kafka.connect.transform

import org.apache.kafka.connect.connector.ConnectRecord
import java.util.Locale.ENGLISH

class LowercaseFieldNames<R : ConnectRecord<R>> : ProcessFieldNames<R>() {
    override fun transformFieldName(fieldName: String): String = fieldName.lowercase(ENGLISH)
}