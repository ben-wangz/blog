# GeekCity Blog

## What's this?

* source code of [blog.geekcity.tech](https://blog.geekcity.tech)

## develop with dev container and vscode

* open with vscode which contains .devcontainer

## start locallly

1. dev mode with gradlew
    * ```shell
      # you should kill the process by command `kill`
      # find the process with command `ps aux | grep vuepress`
      # Ctrl + C is just kills the gradle process, not the node process
      ./gradlew :docs:dev
      ```
2. dev mode with docker
    * ```shell
      podman run --rm \
          -p 8080:8080 \
          -v $(pwd)/docs:/app \
          --workdir /app \
          -it docker.io/library/node:21.4.0-alpine sh -c 'npm install && npm run dev'
      ```
3. build and host with container
    * ```shell
      podman build --ulimit nofile=4096:4096 -f docs/Dockerfile -t blog-docs .
      podman run --rm -p 8080:8080 -d localhost/blog-docs
      ```
