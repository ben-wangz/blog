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

2. build and host with container
    * ```shell
      podman build -t geekcity-blog .
      ```
