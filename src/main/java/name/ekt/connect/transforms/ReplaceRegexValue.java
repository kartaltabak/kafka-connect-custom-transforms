package name.ekt.connect.transforms;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.ConnectRecord;
import org.apache.kafka.connect.transforms.Transformation;
import org.apache.kafka.connect.transforms.util.SimpleConfig;


import java.util.Map;
import java.util.regex.Pattern;

public class ReplaceRegexValue<R extends ConnectRecord<R>> implements Transformation<R> {
    public static final String OVERVIEW_DOC = "Replace values in Kafka Connect records.";

    private interface ConfigName {
        String FIELD = "field";
        String REGEX = "regex";
        String REPLACEMENT = "replacement";
    }

    public static final ConfigDef CONFIG_DEF = new ConfigDef()
            .define(ConfigName.FIELD, ConfigDef.Type.STRING, ConfigDef.NO_DEFAULT_VALUE, ConfigDef.Importance.HIGH, "Field to apply the replacement")
            .define(ConfigName.REGEX, ConfigDef.Type.STRING, ConfigDef.NO_DEFAULT_VALUE, ConfigDef.Importance.HIGH, "Regex to be replaced")
            .define(ConfigName.REPLACEMENT, ConfigDef.Type.STRING, ConfigDef.NO_DEFAULT_VALUE, ConfigDef.Importance.HIGH, "Value to replace with");

    private String field;
    private String replacement;

    private Pattern regexPattern;

    @Override
    public void configure(Map<String, ?> props) {
        SimpleConfig config = new SimpleConfig(CONFIG_DEF, props);
        field = config.getString(ConfigName.FIELD);

        String regex = config.getString(ConfigName.REGEX);
        regexPattern = Pattern.compile(regex);

        replacement = config.getString(ConfigName.REPLACEMENT);
    }

    @Override
    public R apply(R record) {
        Object value = record.value();
        if (value instanceof Map) {
            Map<String, Object> valueMap = (Map<String, Object>) value;
            if (valueMap.containsKey(field)) {
                Object fieldValue = valueMap.get(field);
                if (fieldValue instanceof String fieldValueString) {
                    String newFieldValue = regexPattern.matcher(fieldValueString).replaceAll(replacement);
                    valueMap.put(field, newFieldValue);
                }
            }
        }
        return record.newRecord(
                record.topic(), record.kafkaPartition(),
                record.keySchema(), record.key(),
                record.valueSchema(), value,
                record.timestamp());
    }

    @Override
    public ConfigDef config() {
        return CONFIG_DEF;
    }

    @Override
    public void close() {
    }
}