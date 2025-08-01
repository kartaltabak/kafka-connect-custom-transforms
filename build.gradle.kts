import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm") version "2.0.0"
}

group = "name.tabak.kafka.connect"
version = "1.6"

repositories {
    mavenCentral()
    maven {
        url = uri("https://packages.confluent.io/maven/")
    }
    maven {
        url = uri("https://repo.osgeo.org/repository/release/")
    }
}

val kafkaConnectVersion = "7.5.3-ccs"
val junitVersion = "5.11.2"
val slf4jVersion = "2.0.12"
val jtsCoreVersion = "1.20.0"

dependencies {
    compileOnly("org.apache.kafka:connect-transforms:$kafkaConnectVersion")
    compileOnly("org.slf4j:slf4j-api:$slf4jVersion")
    implementation("org.locationtech.jts:jts-core:$jtsCoreVersion")

    testImplementation("org.apache.kafka:connect-transforms:${kafkaConnectVersion}")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}

sourceSets {
    main {
        java {
            setSrcDirs(listOf("src/main/java", "src/main/kotlin"))
        }
    }
    test {
        java {
            setSrcDirs(listOf("src/test/java", "src/test/kotlin"))
        }
    }
}

tasks.withType<Test> {
    dependsOn(tasks.named("jar"))
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

tasks.withType<JavaCompile> {
    sourceCompatibility = "11"
    targetCompatibility = "11"
}

tasks.register<Zip>("distWithDeps") {
    archiveBaseName.set("kafka-connect-custom-transforms")
    archiveClassifier.set("with-deps")
    destinationDirectory.set(layout.buildDirectory.dir("distributions"))

    from(tasks.named("jar"))
    from(configurations.runtimeClasspath.get())
    from("licenses") {
        into("licenses")
    }
    from("LICENSE") {
        into("licenses")
    }
}

tasks.test {
    useJUnitPlatform()
}

