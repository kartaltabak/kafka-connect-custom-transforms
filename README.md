# Kafka Connect Custom Transforms

This project provides custom transformation classes for Kafka Connect. These transformations can be used to modify records as they flow through Kafka Connect pipelines, allowing for data manipulation and enhancement before reaching the destination system.

## Features

- **LowercaseFieldNames**: A transformation to convert all field names in a record to lowercase, ensuring consistency across different systems.
- **UppercaseFieldNames**: A transformation to convert all field names in a record to uppercase, ensuring consistency across different systems.
- **RemoveNullCharacters**: A transformation to remove null characters from string fields in a record, which can cause issues in certain systems.
- **ReplaceRegexValue**: A transformation to replace the value of a field using a regular expression pattern, allowing for data cleansing and normalization.
- **TableToLowerCase**: A transformation to convert the table name in the topic name to lowercase, ensuring consistency in naming conventions.
- **TableToUpperCase**: A transformation to convert the table name in the topic name to uppercase, ensuring consistency in naming conventions.


## Getting Started

### Prerequisites

- Java 11 or higher
- Kafka Connect

### Installation

Copy the artifact JAR into your Kafka Connect's `plugins` directory and restart the Kafka Connect service.

### Usage

To use the custom transformations in your Kafka Connect pipeline, add the following configuration to your connector:

```properties
transforms=lowercaseFieldNames
transforms.lowercaseFieldNames.type=name.ekt.kafka.connect.transform.LowercaseFieldNames
```

## Contributing

Contributions are welcome! Please feel free to submit a pull request.

## License

This project is licensed under GNU General Public License - see the LICENSE file for details.
