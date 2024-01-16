# GeekCity Blog

## What's this?

* source code of [blog.geekcity.tech](https://blog.geekcity.tech)

## start locallly

1. dev mode with gradlew
    * ```shell
      # you should kill the process by command `kill`
      # find the process with command `ps aux | grep nuxi`
      # Ctrl + C is just kills the gradle process, not the node process
      ./gradlew :docs:dev
      ```
2. dev mode with docker
    * ```shell
      podman run --rm \
          -p 8080:8080 \
          -v $(pwd)/doc:/app \
          --workdir /app \
          -it docker.io/library/node:16.13.1-alpine npm run dev
      ```
3. build and host with container
    * ```shell
      podman build --ulimit nofile=4096:4096 -t geekcity-blog .
      pomdna run -d -p 8080:80 geekcity-blog
      ```
