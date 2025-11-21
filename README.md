# Kafka Connect Custom Transforms

This repository contains custom Kafka Connect transformations 
that can be used to modify Kafka records. 
The available transformations include:

* `AppendProcessingTime`
* `LowercaseFieldNames`
* `RemoveNullCharacters`
* `ReplaceRegexValue`
* `TableToLowerCase`
* `TableToUpperCase`
* `UppercaseFieldNames`
* `RenameFieldNamesRegEx`
*  `TimeShift`
* `WktToPostgresGeometry`


## Transformations

### AppendProcessingTime

Adds a processing timestamp field to the record's value. 
This transformation is useful for tracking when a record was processed by Kafka Connect. 
It appends a new field to the record's value schema, which contains the timestamp when the record was processed.


#### Configuration

- `field` (default: `processing_time`): The name of the field to store the processing timestamp.
- `is_optional` (default: `true`): The name of the field to store the processing timestamp.

#### Example

```json
"transforms": "AppendProcessingTime",
"transforms.AppendProcessingTime.type": "name.ekt.kafka.connect.transform.AppendProcessingTime",
"transforms.AppendProcessingTime.field": "processing_time",
"transforms.AppendProcessingTime.is_optional": true
```

### LowercaseFieldNames

Transforms all field names in the record's value to lowercase. 
This transformation ensures that all field names within a record are converted to lowercase, 
which can be useful for maintaining a consistent naming convention across different data sources and sinks.

This transformation is particularly useful in scenarios where integrating non-case-sensitive systems 
with case-sensitive systems can cause issues due to discrepancies in field name casing.

#### Example

```json
"transforms": "LowercaseFieldNames",
"transforms.LowercaseFieldNames.type": "name.ekt.kafka.connect.transform.LowercaseFieldNames"
```

### RemoveNullCharacters

Removes null characters from string fields in the record's value. 
A null character is a control character with the value `\u0000`. 
This transformation is helpful when dealing with data that may contain null characters, 
which can cause issues in downstream processing or storage systems.

#### Example

```json
"transforms": "RemoveNullCharacters",
"transforms.RemoveNullCharacters.type": "name.ekt.kafka.connect.transform.RemoveNullCharacters"
```

### ReplaceRegexValue

Replaces values in Kafka Connect records using a regular expression. This transformation allows for flexible 
text manipulation within specific fields of the records, based on regular expression matching and replacement.

#### Configuration

- `field`: The field to apply the replacement.
- `regex`: The regular expression pattern to replace.
- `replacement`: The replacement value.

#### Example

```json
"transforms": "ReplaceRegexValue",
"transforms.ReplaceRegexValue.type": "name.ekt.kafka.connect.transform.ReplaceRegexValue",
"transforms.ReplaceRegexValue.field": "myField",
"transforms.ReplaceRegexValue.regex": "\\s+",
"transforms.ReplaceRegexValue.replacement": "_"
```

### TableToLowerCase

Transforms the topic name to lowercase. 
This transformation is useful for ensuring consistent topic naming conventions, 
particularly when integrating non-case-sensitive systems with case-sensitive systems 
that require or expect lowercase topic names.

#### Example

```json
"transforms": "TableToLowerCase",
"transforms.TableToLowerCase.type": "name.ekt.kafka.connect.transform.TableToLowerCase"
```

### TableToUpperCase

Transforms the topic name to uppercase. 
This transformation is useful for ensuring consistent topic naming conventions, 
particularly when integrating non-case-sensitive systems with case-sensitive systems 
that require or expect uppercase topic names.

#### Example

```json
"transforms": "TableToUpperCase",
"transforms.TableToUpperCase.type": "name.ekt.kafka.connect.transform.TableToUpperCase"
```

### UppercaseFieldNames

Transforms all field names in the record's value to uppercase. 
This transformation ensures that all field names within a record are converted to uppercase, 
which can be useful for maintaining a consistent naming convention across different data sources and sinks.

This transformation is particularly useful in scenarios where 
integrating non-case-sensitive systems with case-sensitive systems can cause issues 
due to discrepancies in field name casing.

#### Example

```json
"transforms": "UppercaseFieldNames",
"transforms.UppercaseFieldNames.type": "name.ekt.kafka.connect.transform.UppercaseFieldNames"
```

### RenameFieldNamesRegEx

Transforms field names in Kafka Connect records by applying a regular expression pattern 
and replacing matching parts of the field names with a specified replacement string. 
This transformation is useful for renaming fields to follow specific conventions 
or to remove unwanted characters.

#### Configuration

- `regex`: The regular expression to be applied to field names.
- `replacement`: The string to replace the regex matches with.

#### Example

```json
"transforms": "RenameFieldNamesRegEx",
"transforms.RenameFieldNamesRegEx.type": "name.ekt.kafka.connect.transform.RenameFieldNamesRegEx",
"transforms.RenameFieldNamesRegEx.regex": "\\",
"transforms.RenameFieldNamesRegEx.replacement": "_"
```

### TimeShift

Shifts the date and time of a specified field by a given number of hours. This transformation is useful when you need to adjust the time zone or apply a time offset to a date field in your Kafka records.

#### Configuration

- `field`: The name of the date field to be shifted. This field must be of type `TIMESTAMP`.
- `hours`: The number of hours to shift the field. Positive values move the time forward, and negative values move it backward.

#### Example

```json
"transforms": "TimeShift",
"transforms.TimeShift.type": "name.ekt.kafka.connect.transform.TimeShift",
"transforms.TimeShift.field": "myDateField",
"transforms.TimeShift.hours": 5
```        
        
### WktToPostgresGeometry

Converts a WKT string field to PostGIS-compatible WKB binary for PostgreSQL geometry columns.

#### Configuration

* `field`: The name of the WKT field to convert.
* `srid` (default: `0`): The spatial reference ID (e.g., 4326 or 0). 

#### Example

```json
"transforms": "WKT",
"transforms.WKT.type": "name.ekt.kafka.connect.transform.WktToPostgresGeometry",
"transforms.WKT.field": "shape",
"transforms.WKT.srid": "4326"
```

## Usage

To use the custom transformations in your Kafka Connect setup, follow these steps:

   1. **Download the Artifact**: Download the latest release of the artifact JAR from 
[Releases](https://github.com/kartaltabak/kafka-connect-custom-transforms/releases).

   2. **Unzip the ZIP**: Copy the downloaded ZIP file and unzip it into your Kafka Connect plugins directory. 
This directory is typically located at `/usr/share/java`, `/usr/local/share/java`,  
`/app/confluent/connect/plugins`, or a similar path, depending on your installation.
    The unzipped directory should contain the necessary JAR files for the transformations. 

   3. **Restart Kafka Connect**: Restart the Kafka Connect service to load the new transformations. 
You can restart the service using a command like `sudo systemctl restart confluent-kafka-connect`
or `sudo service kafka-connect restart`, depending on your system setup.

   4. **Verify the Installation**: To ensure the transformations have been loaded correctly, 
check the Kafka Connect worker logs for messages indicating that the new plugins were discovered. 
Look for log entries similar to:

```plaintext
[INFO] Scanning for plugin paths
[INFO] Loading plugin from: /path/to/plugins/directory/kafka-connect-custom-transforms.jar
```

   5. **Configure Your Connectors**: Update your connector configurations to include the desired transformations 
as described in the examples above.

By following these steps, you should be able to seamlessly integrate and use the custom Kafka Connect transformations.

## Example Connector Configuration

```json
{
  "name": "my-connector",
  "config": {
    "connector.class": "io.confluent.connect.jdbc.JdbcSinkConnector",
    "tasks.max": "1",
    "topics": "my-topic",
    "connection.url": "jdbc:postgresql://localhost:5432/mydb",
    "connection.user": "myuser",
    "connection.password": "mypassword",
    "auto.create": "true",
    "transforms": "AppendProcessingTime,LowercaseFieldNames",
    "transforms.AppendProcessingTime.type": "name.ekt.kafka.connect.transform.AppendProcessingTime",
    "transforms.AppendProcessingTime.field.name": "processing_time",
    "transforms.LowercaseFieldNames.type": "name.ekt.kafka.connect.transform.LowercaseFieldNames"
  }
}
```

## Building and Testing

To build and test the transformations, follow these steps:

1. **Clone the Repository**: `git clone https://github.com/kartaltabak/kafka-connect-custom-transforms.git`
2. **Navigate to the Project Directory**: `cd kafka-connect-custom-transforms`
3. **Build the Project**: `./gradlew clean package`
4. **Deploy the JAR**: Copy the resulting JAR file to your Kafka Connect plugins path.

## Contributing

Contributions are welcome! 
If you have a feature request or bug report, please open an issue. 
For code contributions, create a pull request with your changes.

## License

This project is licensed under the MIT License. See the LICENSE file for details.

This plugin includes jts-core (Java Topology Suite) from the LocationTech project,
licensed under the Eclipse Public License 2.0.
