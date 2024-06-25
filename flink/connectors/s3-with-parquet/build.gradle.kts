plugins {
    java
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

val lombokDependency = "org.projectlombok:lombok:1.18.22"
var flinkVersion = "1.19.1"
val jacksonVersion = "2.13.4"
var slf4jVersion = "2.0.9"
var logbackVersion = "1.4.14"
dependencies {
    annotationProcessor(lombokDependency)
    implementation("com.google.guava:guava:32.1.1-jre")
    implementation("com.fasterxml.jackson.core:jackson-core:$jacksonVersion")
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation("org.slf4j:slf4j-api:$slf4jVersion")

    implementation("org.apache.parquet:parquet-avro:1.12.2") {
        // exclude(group = "org.apache.hadoop", module = "hadoop-client")
        exclude(group = "it.unimi.dsi", module = "fastutil")
    }
    // dependency for parquet format sink
    implementation("org.apache.hadoop:hadoop-mapreduce-client-core:3.3.6")
    // dependency for parquet format source
    implementation("org.apache.flink:flink-avro:$flinkVersion")
    implementation("org.apache.flink:flink-connector-datagen:$flinkVersion")
    implementation("org.apache.flink:flink-parquet:$flinkVersion")

    shadow(lombokDependency)
    shadow("org.apache.flink:flink-s3-fs-hadoop:$flinkVersion")
    shadow("org.slf4j:slf4j-simple:$slf4jVersion")
    shadow("org.apache.flink:flink-streaming-java:$flinkVersion")
    shadow("org.apache.flink:flink-clients:$flinkVersion")
    shadow("org.apache.flink:flink-connector-files:$flinkVersion")
    shadow("org.apache.flink:flink-runtime-web:$flinkVersion")

    testImplementation("org.junit.jupiter:junit-jupiter:5.9.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

tasks.shadowJar {
    relocate("com.google.common", "tech.geekcity.flink.shadow.com.google.common")
}

tasks.register("buildBinary") {
    doLast {
        val resultMessagePathPropertyName = "RESULT_MESSAGE_PATH"
        if (!project.hasProperty(resultMessagePathPropertyName)) {
            throw RuntimeException("$resultMessagePathPropertyName not found in project property")
        }
        val resultMessageFile = project.file(project.property(resultMessagePathPropertyName) as String)
        resultMessageFile.writeText(tasks.shadowJar.get().archiveFile.get().asFile.absolutePath)
    }
    dependsOn(tasks.shadowJar)
}
