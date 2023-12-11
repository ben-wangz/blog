val docsPath = file(".")
val containerName = "blog"
val dockerCommand = "podman"
val port = 8083

tasks.register<Exec>("startDocsNginxDocker") {
    doFirst {
        println("Executing command: ${commandLine.joinToString(" ")}")
    }
    executable = dockerCommand
    args =
        listOf(
            "run", "--rm",
            "--name", containerName,
            "-p", "$port:80",
            "-v", "$docsPath:/usr/share/nginx/html:ro",
            "-v", "$docsPath/offline-docsify/default.conf:/etc/nginx/conf.d/default.conf:ro",
            "-d", "docker.io/nginx:1.19.9-alpine",
        )
}

tasks.register<Exec>("stopDocsNginxDocker") {
    doFirst {
        println("Executing command: ${commandLine.joinToString(" ")}")
    }
    executable = dockerCommand
    args = listOf("kill", containerName)
}
