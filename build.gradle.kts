plugins {
    java
}

group = "tr.name.tabak.kafka.connect"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.apache.kafka:connect-api:2.8.0")
    implementation("org.apache.kafka:connect-transforms:3.7.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.1")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.7.1")
    testImplementation("org.testcontainers:testcontainers:1.16.2")
    testImplementation("org.testcontainers:kafka:1.16.2")
    testImplementation("org.testcontainers:junit-jupiter:1.16.2")
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    manifest {
        attributes(
            "Main-Class" to "com.example.kafka.connect.UppercaseTableName",
            "Implementation-Title" to project.name,
            "Implementation-Version" to project.version,
            "Implementation-Vendor" to group,
            "Specification-Title" to project.name,
            "Specification-Version" to project.version,
            "Specification-Vendor" to group
        )
    }
    from("src/main/resources") {
        include("META-INF/services/**")
    }
}
