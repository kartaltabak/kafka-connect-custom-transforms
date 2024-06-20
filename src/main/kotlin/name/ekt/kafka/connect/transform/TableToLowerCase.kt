package name.ekt.kafka.connect.transform

import org.apache.kafka.connect.connector.ConnectRecord
import java.util.Locale.ENGLISH

class TableToLowerCase<R : ConnectRecord<R>?> : TableTransformation<R>({ it.lowercase(ENGLISH) })
