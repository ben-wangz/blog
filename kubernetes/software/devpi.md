# devpi

## main usage

* PyPI server and packaging/testing/release tool

## conceptions

* none

## purpose

* prepare a kind cluster with basic components
* install `devpi`

## installation

1. [prepare a kind cluster with basic components](../basic/kind.cluster.md)
2. download and load images to qemu machine(run command at the host of qemu machine)
    * run scripts
      in [download.and.load.function.sh](../resources/create.qemu.machine.for.kind/download.and.load.function.sh.md) to
      load function `download_and_load`
    * ```shell
      TOPIC_DIRECTORY="php.my.admin.software"
      BASE_URL="https://resource.geekcity.tech/kubernetes/docker-images/x86_64"
      download_and_load $TOPIC_DIRECTORY $BASE_URL \
          "docker.io_bitnami_phpmyadmin_5.1.1-debian-10-r147.dim" \
              "docker.io_bitnami_apache-exporter_0.10.1-debian-10-r54.dim"
      ```
3. install chart-museum
    * reference to [software/chart-museum](chart.museum.md)
    * change values
        + `ingress.hosts.[0].tls=tls`
        + `ingress.hosts.[0].host=insecure.chart.museum.local`
4. helm charts and docker images from [my github](https://github.com/ben-wangz/nvwa/tree/main/devpi)