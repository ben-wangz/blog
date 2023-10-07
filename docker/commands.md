### docker commands

1. remove all `<none>` images
    * ```shell
      docker rmi `docker images | grep  '<none>' | awk '{print $3}'`
      ```
2. docker container with `host.docker.internal` point to host machine
    * ```shell
      docker run \
          ... \
          --add-host host.docker.internal:host-gateway \
          ...
      ```
3. remove all stopped containers
    * ```shell
      docker container prune
      ```
4. remove all docker images not used
    * ```shell
      docker image prune
      #crictl rmi --prune
      ```
5. remove all docker images not referenced by any container
    * ```shell
      docker image prune -a
      ```
6. generate htpasswd file
    * ```shell
      docker run --rm wangz2019/jiuying-htpasswd:1.2.0 my-username my-password > htpasswd
      ```
