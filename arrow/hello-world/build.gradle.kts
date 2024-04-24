plugins {
    java
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

val lombokDependency = "org.projectlombok:lombok:1.18.22"
val jacksonVersion = "2.13.4"
var slf4jVersion = "2.0.9"
var logbackVersion = "1.4.14"
var arrowVersion = "15.0.2"
dependencies {
    annotationProcessor(lombokDependency)
    implementation("com.google.guava:guava:32.1.1-jre")
    implementation("com.fasterxml.jackson.core:jackson-core:$jacksonVersion")
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation("org.slf4j:slf4j-api:$slf4jVersion")
    implementation("org.apache.arrow:arrow-vector:$arrowVersion")
    implementation("org.apache.arrow:arrow-memory-netty:$arrowVersion")

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
    manifest.attributes["Main-Class"] = "tech.geekcity.arrow.hello.world.Hello"
}

tasks.shadowJar {
    relocate("com.google.common", "tech.geekcity.flink.shadow.com.google.common")
}
