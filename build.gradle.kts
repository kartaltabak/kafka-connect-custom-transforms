plugins {
    java
}

group = "tr.name.tabak.kafka.connect"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.apache.kafka:connect-api:3.7.0")
    implementation("org.apache.kafka:connect-transforms:3.7.0")
    testImplementation("junit:junit:4.13.2")
}

