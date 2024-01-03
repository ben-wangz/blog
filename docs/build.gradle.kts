plugins {
    id("com.github.node-gradle.node") version ("3.0.1")
}

node {
    download.set(true)
    version.set("21.4.0")
    distBaseUrl.set("https://npmmirror.com/mirrors/node/")
}

tasks.register("dev") {
    dependsOn("npm_run_dev")
}
