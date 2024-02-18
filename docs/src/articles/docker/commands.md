# docker commands

1. remove all `<none>` images
    * ```shell
      podman rmi `docker images | grep  '<none>' | awk '{print $3}'`
      ```
2. remove all stopped containers
    * ```shell
      podman container prune
      # podman container prune -a
      ```
3. remove all docker images not used
    * ```shell
      podman image prune
      #crictl rmi --prune
      ```
4. remove all docker images not referenced by any container
    * ```shell
      podman image prune -a
      ```
5. generate htpasswd file
    * ```shell
      docker run --rm wangz2019/jiuying-htpasswd:1.2.0 my-username my-password > htpasswd
      ```
6. generate gradle projects
    * ```shell
      docker run --rm \
          -v $(pwd):/app \
          -w /app \
          docker.io/gradle:8.4.0-jdk11-focal \
          gradle init \
              --dsl kotlin \
              --type java-application \
              --project-name hello-world \
              --package com.example.helloworld \
              --test-framework junit-jupiter
      ```
7. find ip address of a container
    * ```shell
      podman inspect --format='{{.NetworkSettings.IPAddress}}' minio-server
      ```
