# redis

## main usage

* an open source, advanced key-value store. It is often referred to as a data structure server since keys can contain
  strings, hashes, lists, sets and sorted sets

## conceptions

* none

## purpose

* prepare a kind cluster with basic components
* install `redis`

## installation

1. [prepare a kind cluster with basic components](../basic/kind.cluster.md)
2. download and load images to qemu machine(run command at the host of qemu machine)
    * run scripts
      in [download.and.load.function.sh](../resources/create.qemu.machine.for.kind/download.and.load.function.sh.md) to
      load function `download_and_load`
    * ```shell
      TOPIC_DIRECTORY="redis.software"
      BASE_URL="https://resource.geekcity.tech/kubernetes/docker-images/x86_64"
      download_and_load $TOPIC_DIRECTORY $BASE_URL \
          "docker.io_bitnami_redis_7.0.4-debian-11-r2.dim" \
          "docker.io_bitnami_redis-sentinel_7.0.4-debian-11-r0.dim" \
          "docker.io_bitnami_redis-exporter_1.43.0-debian-11-r9.dim" \
          "docker.io_bitnami_bitnami-shell_11-debian-11-r16.dim"
      ```
3. install `redis`
    * prepare [redis.values.yaml](resources/redis/redis.values.yaml.md)
    * prepare images
        + run scripts in [load.image.function.sh](../resources/load.image.function.sh.md) to load function `load_image`
        + ```shell
          load_image "docker.registry.local:443" \
              "docker.io/bitnami/redis:7.0.4-debian-11-r2" \
              "docker.io/bitnami/redis-sentinel:7.0.4-debian-11-r0" \
              "docker.io/bitnami/redis-exporter:1.43.0-debian-11-r9" \
              "docker.io/bitnami/bitnami-shell:11-debian-11-r16"
          ```
    * install with helm
        + ```shell
          helm install \
              --create-namespace --namespace application \
              my-redis \
              https://resource.geekcity.tech/kubernetes/charts/https/charts.bitnami.com/bitnami/redis-17.0.2.tgz \
              --values redis.values.yaml \
              --atomic
          ```
4. install `redis-tool`
    * prepare [redis.tool.yaml](resources/redis/redis.tool.yaml.md)
    * ```shell
      kubectl -n application apply -f redis.tool.yaml
      ```

## test with redis-tool

1. connect to `redis`
    * ```shell
      kubectl -n application exec -it deployment/redis-tool -- bash -c '\
          echo "ping" | redis-cli -c -h my-redis-master.application -a $REDIS_PASSWORD' \
          && kubectl -n application exec -it deployment/redis-tool -- bash -c '\
              echo "set mykey somevalue" | redis-cli -c -h my-redis-master.application -a $REDIS_PASSWORD' \
          && kubectl -n application exec -it deployment/redis-tool -- bash -c '\
              echo "get mykey" | redis-cli -c -h my-redis-master.application -a $REDIS_PASSWORD' \
          && kubectl -n application exec -it deployment/redis-tool -- bash -c '\
              echo "del mykey" | redis-cli -c -h my-redis-master.application -a $REDIS_PASSWORD' \
          && kubectl -n application exec -it deployment/redis-tool -- bash -c '\
              echo "get mykey" | redis-cli -c -h my-redis-master.application -a $REDIS_PASSWORD'
      ```

## uninstallation

1. uninstall `redis-tool`
    * ```shell
      kubectl -n application delete -f redis.tool.yaml
      ```
2. uninstall `redis`
    * ```shell
      helm -n application uninstall my-redis \
          && kubectl -n application delete pvc redis-data-my-redis-master-0
      ```
