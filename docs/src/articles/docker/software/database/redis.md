# redis

## server

* ```shell
  mkdir -p $(pwd)/redis/data
  podman run --rm \
      --name redis \
      -p 6379:6379 \
      -d docker.io/library/redis:7.2.4-alpine
  ```

## client

* ```shell
  podman run --rm \
      -it docker.io/library/redis:7.2.4-alpine \
      redis-cli \
      -h host.containers.internal \
      PING
  ```
* ```shell
  podman run --rm \
      -it docker.io/library/redis:7.2.4-alpine \
      redis-cli \
      -h host.containers.internal \
      set mykey somevalue
  ```
* ```shell
  podman run --rm \
      -it docker.io/library/redis:7.2.4-alpine \
      redis-cli \
      -h host.containers.internal \
      get mykey
  ```
* ```shell
  podman run --rm \
      -it docker.io/library/redis:7.2.4-alpine \
      redis-cli \
      -h host.containers.internal \
      del mykey
  ```
* ```shell
  podman run --rm \
      -it docker.io/library/redis:7.2.4-alpine \
      redis-cli \
      -h host.containers.internal \
      get mykey
  ```
