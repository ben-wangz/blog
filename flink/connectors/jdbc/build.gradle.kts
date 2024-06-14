plugins {
    java
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

val lombokDependency = "org.projectlombok:lombok:1.18.22"
val flinkVersion = "1.17.1"
val jacksonVersion = "2.13.4"
val slf4jVersion = "2.0.9"
val logbackVersion = "1.4.14"
dependencies {
    annotationProcessor(lombokDependency)
    implementation("com.google.guava:guava:32.1.1-jre")
    implementation("com.fasterxml.jackson.core:jackson-core:$jacksonVersion")
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation("org.slf4j:slf4j-api:$slf4jVersion")
    implementation("org.apache.flink:flink-connector-jdbc:3.1.0-1.17")
    implementation("com.clickhouse:clickhouse-jdbc:0.6.0")
    // for dependency of clickhouse
    implementation("org.apache.httpcomponents.client5:httpclient5:5.3.1")

    shadow(lombokDependency)
    shadow("org.slf4j:slf4j-simple:$slf4jVersion")
    shadow("org.apache.flink:flink-streaming-java:$flinkVersion")
    shadow("org.apache.flink:flink-clients:$flinkVersion")
    shadow("org.apache.flink:flink-connector-datagen:$flinkVersion")
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
