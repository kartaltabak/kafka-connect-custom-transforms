import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm") version "2.0.0"
    `maven-publish`
}

group = "name.tabak.kafka.connect"
version = "1.0.0"

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
    implementation("org.slf4j:slf4j-api:2.0.12")
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

tasks.withType<KotlinCompile> {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

tasks.withType<JavaCompile> {
    sourceCompatibility = "11"
    targetCompatibility = "11"
}

tasks.test {
    useJUnitPlatform()
}

val createManifest by tasks.creating(Copy::class) {
    from("manifest.json")
    into("$buildDir/component_archive/")
}

val createLibs by tasks.creating(Copy::class) {
    from(tasks.jar)
    into("$buildDir/component_archive/lib")
}

val copyDependencies by tasks.creating(Copy::class) {
    from(
        configurations.runtimeClasspath.get().filter { !it.isDirectory })
    into("$buildDir/component_archive/lib")
}

val createComponentArchive by tasks.creating(Zip::class) {
    dependsOn(createManifest, createLibs)
    archiveFileName.set("kafka-connect-common-transforms.zip")
    destinationDirectory.set(file("$buildDir"))
    from("$buildDir/component_archive/")
}

tasks.build {
    dependsOn(createComponentArchive)
}

tasks.register<Jar>("sourcesJar") {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            artifact(tasks.getByName("sourcesJar"))
        }
    }
    repositories {
        maven {
            name = "local"
            url = uri(findProperty("altDeploymentRepository") ?: "file://${System.getProperty("user.home")}/.m2/repository")
        }
    }}


