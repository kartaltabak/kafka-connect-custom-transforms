plugins {
    java
}

group = "name.tabak.kafka.connect"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven {
        url = uri("https://packages.confluent.io/maven/")
    }
}

val kafkaConnectVersion = "7.5.3-ccs"
val junitVersion = "5.10.2"
val testcontainersVersion = "1.19.8"
val mssqlJdbcVersion = "12.6.2.jre11"
val postgresqlJdbcVersion = "42.7.3"


dependencies {
    implementation("org.apache.kafka:connect-api:$kafkaConnectVersion")
    implementation("org.apache.kafka:connect-transforms:$kafkaConnectVersion")
    testImplementation("com.microsoft.sqlserver:mssql-jdbc:$mssqlJdbcVersion")
    testImplementation("org.postgresql:postgresql:$postgresqlJdbcVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testImplementation("org.testcontainers:testcontainers:$testcontainersVersion")
    testImplementation("org.testcontainers:kafka:$testcontainersVersion")
    testImplementation("org.testcontainers:junit-jupiter:$testcontainersVersion")
    testImplementation("org.testcontainers:mssqlserver:$testcontainersVersion")
    testImplementation("org.testcontainers:postgresql:$testcontainersVersion")
    testImplementation("com.squareup.okhttp3:okhttp:4.9.3")
    testImplementation("com.github.spullara.mustache.java:compiler:0.9.13")
}

tasks.test {
    useJUnitPlatform()
}

