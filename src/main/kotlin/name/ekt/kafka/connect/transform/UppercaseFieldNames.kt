package name.ekt.kafka.connect.transform

import org.apache.kafka.connect.connector.ConnectRecord
import java.util.Locale.ENGLISH

class UppercaseFieldNames<R : ConnectRecord<R>> : ProcessFieldNames<R>() {
    override fun transformFieldName(fieldName: String): String = fieldName.uppercase(ENGLISH)
}