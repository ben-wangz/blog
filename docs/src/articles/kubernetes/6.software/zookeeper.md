# zookeeper

## main usage

* ZooKeeper is a centralized service for maintaining configuration information, naming, providing distributed
  synchronization, and providing group services.

## conceptions

* none

## purpose

* prepare a kind cluster with basic components
* install `zookeeper`

## installation

1. [prepare a kind cluster with basic components](../basic/kind.cluster.md)
2. download and load images to qemu machine(run command at the host of qemu machine)
    * run scripts
      in [download.and.load.function.sh](../resources/create.qemu.machine.for.kind/download.and.load.function.sh.md) to
      load function `download_and_load`
    * ```shell
      TOPIC_DIRECTORY="zookeeper.software"
      BASE_URL="https://resource.geekcity.tech/kubernetes/docker-images/x86_64"
      download_and_load $TOPIC_DIRECTORY $BASE_URL \
          "docker.io_bitnami_zookeeper_3.8.0-debian-10-r37.dim" \
          "docker.io_bitnami_bitnami-shell_10-debian-10-r40.dim"
      ```
3. install `zookeeper`
    * prepare [zookeeper.values.yaml](resources/zookeeper/zookeeper.values.yaml.md)
    * prepare images
        + run scripts in [load.image.function.sh](../resources/load.image.function.sh.md) to load function `load_image`
        + ```shell
          load_image "docker.registry.local:443" \
              "docker.io/bitnami/zookeeper:3.8.0-debian-10-r37" \
              "docker.io/bitnami/bitnami-shell:10-debian-10-r403"
          ```
    * install with helm
        + ```shell
          helm install \
              --create-namespace --namespace application \
              my-zookeeper \
              https://resource.geekcity.tech/kubernetes/charts/https/charts.bitnami.com/bitnami/zookeeper-9.1.1.tgz \
              --values zookeeper.values.yaml \
              --atomic
          ```
4. install `zookeeper-tool`
    * prepare [zookeeper.tool.yaml](resources/zookeeper/zookeeper.tool.yaml.md)
    * ```shell
      kubectl -n application apply -f zookeeper.tool.yaml
      ```

## test with zookeeper-tool

1. connect to `zookeeper`
    * ```shell
      kubectl -n application exec -it deployment/zookeeper-tool -- \
          zkCli.sh -server my-zookeeper.application:2181 ls /
      kubectl -n application exec -it deployment/zookeeper-tool -- \
          zkCli.sh -server my-zookeeper.application:2181 create /zk_test
      kubectl -n application exec -it deployment/zookeeper-tool -- \
          zkCli.sh -server my-zookeeper.application:2181 ls /
      kubectl -n application exec -it deployment/zookeeper-tool -- \
          zkCli.sh -server my-zookeeper.application:2181 get /zk_test
      kubectl -n application exec -it deployment/zookeeper-tool -- \
          zkCli.sh -server my-zookeeper.application:2181 delete /zk_test
      kubectl -n application exec -it deployment/zookeeper-tool -- \
          zkCli.sh -server my-zookeeper.application:2181 ls /
      ```

## uninstallation

1. uninstall `zookeeper-tool`
    * ```shell
      kubectl -n application delete -f zookeeper.tool.yaml
      ```
2. uninstall `zookeeper`
    * ```shell
      helm -n application uninstall my-zookeeper \
          && kubectl -n application delete pvc data-my-zookeeper-0 \
          && kubectl -n application delete pvc data-my-zookeeper-1 \
          && kubectl -n application delete pvc data-my-zookeeper-2
      ```
