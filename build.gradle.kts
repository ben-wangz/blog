plugins {
    id("com.diffplug.spotless") version "6.23.3"
}
configure<com.diffplug.gradle.spotless.SpotlessExtension> {
    kotlinGradle {
        target("**/*.kts")
        ktlint()
    }
    java {
        target("**/*.java")
        googleJavaFormat()
            .reflowLongStrings()
            .skipJavadocFormatting()
            .reorderImports(false)
    }
    yaml {
        target("**/*.yaml")
        targetExclude("**/.gradle/**")
        jackson()
            .feature("ORDER_MAP_ENTRIES_BY_KEYS", false)
            .yamlFeature("MINIMIZE_QUOTES", true)
            .yamlFeature("ALWAYS_QUOTE_NUMBERS_AS_STRINGS", true)
            .yamlFeature("SPLIT_LINES", false)
            .yamlFeature("LITERAL_BLOCK_STYLE", true)
    }
    json {
        target("**/*.json")
        targetExclude("**/.gradle/**")
        jackson()
            .feature("ORDER_MAP_ENTRIES_BY_KEYS", true)
    }
}

allprojects {
    repositories {
        maven { setUrl("https://maven.aliyun.com/repository/public") }
        maven { setUrl("https://maven.aliyun.com/repository/spring") }
        maven { setUrl("https://maven.aliyun.com/repository/mapr-public") }
        maven { setUrl("https://maven.aliyun.com/repository/spring-plugin") }
        maven { setUrl("https://maven.aliyun.com/repository/gradle-plugin") }
        maven { setUrl("https://maven.aliyun.com/repository/google") }
        maven { setUrl("https://maven.aliyun.com/repository/jcenter") }
        mavenCentral()
    }
}
