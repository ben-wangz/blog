### docker commands

1. remove all `<none>` images
    + ```shell
      docker rmi `docker images | grep  '<none>' | awk '{print $3}'`
      ```
2. docker container with `host.docker.internal` point to host machine
    + ```shell
      docker run \
          ... \
          --add-host host.docker.internal:host-gateway \
          ...
      ```
