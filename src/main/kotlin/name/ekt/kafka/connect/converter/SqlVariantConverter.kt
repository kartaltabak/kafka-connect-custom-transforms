package name.ekt.kafka.connect.converter

import io.debezium.spi.converter.CustomConverter
import io.debezium.spi.converter.RelationalColumn
import org.apache.kafka.connect.data.SchemaBuilder
import java.util.Properties


class SqlVariantConverter : CustomConverter<SchemaBuilder?, RelationalColumn> {
    companion object {
        private const val SQL_VARIANT_NAME = "sql_variant"
        private const val FIELD_PROPERTY = "field";
    }

    private lateinit var field: String
    override fun configure(props: Properties){
        field = props.getProperty(FIELD_PROPERTY)?:""
    }

    override fun converterFor(column: RelationalColumn, registration: CustomConverter.ConverterRegistration<SchemaBuilder?>) {
        if (field == column.name() && SQL_VARIANT_NAME.equals(column.typeName(), ignoreCase = true)) {
            registration.register(SchemaBuilder.string(), ::convert)
        }
    }

    private fun convert(input: Any?): Any? {
        return input?.toString()
    }
}