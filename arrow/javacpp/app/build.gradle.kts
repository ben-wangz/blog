plugins {
    java
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

version = "0.1.0"

val lombokDependency = "org.projectlombok:lombok:1.18.22"
val jacksonVersion = "2.13.4"
val slf4jVersion = "2.0.9"
dependencies {
    implementation(project(":arrow:javacpp:geekmath"))

    annotationProcessor(lombokDependency)
    implementation("com.google.guava:guava:32.1.1-jre")
    implementation("com.fasterxml.jackson.core:jackson-core:$jacksonVersion")
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation("org.slf4j:slf4j-api:$slf4jVersion")

    shadow(lombokDependency)
    shadow("org.slf4j:slf4j-simple:$slf4jVersion")

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

tasks.jar {
    manifest.attributes["Main-Class"] = "tech.geekcity.javacpp.app.Application"
}

tasks.shadowJar {
    relocate("com.google.common", "tech.geekcity.flink.shadow.com.google.common")
}
