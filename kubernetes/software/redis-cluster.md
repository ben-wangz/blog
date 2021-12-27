# redis-cluster

## main usage

* an in-memory data structure store, used as a database, cache, and message broker

## conceptions

* none

## purpose

* prepare a kind cluster with basic components
* install `redis-cluster`

## installation

1. [prepare a kind cluster with basic components](../basic/kind.cluster.md)
2. download and load images to qemu machine(run command at the host of qemu machine)
    * run scripts
      in [download.and.load.function.sh](../resources/create.qemu.machine.for.kind/download.and.load.function.sh.md) to
      load function `download_and_load`
    * ```shell
      TOPIC_DIRECTORY="redis.cluster.software"
      BASE_URL="https://resource.geekcity.tech/kubernetes/docker-images/x86_64"
      download_and_load $TOPIC_DIRECTORY $BASE_URL \
          "docker.io_bitnami_redis-cluster_6.2.2-debian-10-r0.dim" \
          "docker.io_bitnami_redis-exporter_1.20.0-debian-10-r27.dim" \
          "docker.io_bitnami_bitnami-shell_10.dim"
      ```
3. install `redis-cluster`
    * prepare [redis.cluster.values.yaml](resources/redis.cluster/redis.cluster.values.yaml.md)
    * prepare images
        + run scripts in [load.image.function.sh](../resources/load.image.function.sh.md) to load function `load_image`
        + ```shell
          load_image "docker.registry.local:443" \
              "docker.io/bitnami/redis-cluster:6.2.2-debian-10-r0" \
              "docker.io/bitnami/redis-exporter:1.20.0-debian-10-r27" \
              "docker.io/bitnami/bitnami-shell:10"
          ```
    * install with helm
        + ```shell
          helm install \
              --create-namespace --namespace application \
              my-redis-cluster \
              https://resource.geekcity.tech/kubernetes/charts/https/charts.bitnami.com/bitnami/redis-cluster-5.0.1.tgz \
              --values redis.cluster.values.yaml \
              --atomic
          ```
4. install `redis-cluster-tool`
    * prepare [redis.cluster.tool.yaml](resources/redis.cluster/redis.cluster.tool.yaml.md)
    * ```shell
      kubectl -n application apply -f redis.cluster.tool.yaml
      ```

## test

1. check connection
    * ```shell
      curl --insecure --header 'Host: redis-cluster.local' https://localhost
      ```
2. test with `redis-cluster-tool`
    * find `POD_NAME`
        + ```shell
          POD_NAME=$(kubectl get pod -n application \
              -l "app.kubernetes.io/name=redis-cluster-tool" \
              -o jsonpath="{.items[0].metadata.name}")
          ```
    * add config for server
        + ```shell
          kubectl -n application exec -it $POD_NAME \
              -- bash -c ''
          ```

## uninstallation

1. uninstall `redis-cluster-tool`
    * ```shell
      kubectl -n application delete -f redis.cluster.tool.yaml
      ```
2. uninstall `redis-cluster`
    * ```shell
      helm -n application uninstall my-redis-cluster \
          && kubectl -n application delete pvc my-redis-cluster
      ```
