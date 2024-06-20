package name.ekt.kafka.connect.transform

import org.apache.kafka.connect.connector.ConnectRecord
import java.util.Locale.ENGLISH

class TableToUpperCase<R : ConnectRecord<R>?> : TableTransformation<R>({ it.uppercase(ENGLISH) })
