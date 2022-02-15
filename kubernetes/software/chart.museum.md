# docker registry

## main usage

* chart-museum for helm charts

## conceptions

* none

## purpose

* prepare a kind cluster with basic components
* setup chart-museum
* test chart-museum

## installation

1. [prepare a kind cluster with basic components](../basic/kind.cluster.md)
4. download and load images to qemu machine(run command at the host of qemu machine)
    * run scripts
      in [download.and.load.function.sh](../resources/create.qemu.machine.for.kind/download.and.load.function.sh.md) to
      load function `download_and_load`
    * ```shell
      TOPIC_DIRECTORY="chart.museum.software"
      BASE_URL="https://resource.geekcity.tech/kubernetes/docker-images/x86_64"
      download_and_load $TOPIC_DIRECTORY $BASE_URL \
          "ghcr.io_helm_chartmuseum_v0.13.1.dim" \
          "bitnami_minideb_buster.dim"
      ```
5. configure self-signed issuer
    * `self-signed` issuer
        + prepare [self.signed.and.ca.issuer.yaml](../basic/resources/cert.manager/self.signed.and.ca.issuer.yaml.md)
        + ```shell
          kubectl -n application apply -f self.signed.and.ca.issuer.yaml
          ```
6. setup chart-museum
    * prepare [chart.museum.values.yaml](resources/chart.museum/chart.museum.values.yaml.md)
    * prepare images
        + run scripts in [load.image.function.sh](../resources/load.image.function.sh.md) to load function `load_image`
        + ```shell
          load_image "docker.registry.local:443" \
              "ghcr.io/helm/chartmuseum:v0.13.1" \
              "bitnami/minideb:buster"
          ```
    * install by helm
        + ```shell
          helm install \
              --create-namespace --namespace application \
              my-chart-museum \
              https://resource.geekcity.tech/kubernetes/charts/https/chartmuseum.github.io/charts/chartmuseum-3.4.0.tgz \
              --values chart.museum.values.yaml \
              --atomic
          ```

## test

1. test `push`
    * configure `/etc/hosts` to point `chart.museum.local` to the host
        + ```shell
          echo 127.0.0.1 chart.museum.local >> /etc/hosts
          ```
    * push
        + ```shell
          helm create mychart \
              && helm package mychart \
              && curl --insecure --data-binary "@mychart-0.1.0.tgz" https://chart.museum.local/api/charts
          ```
2. test `pull`
    * pull
        + ```shell
          rm -rf mychart mychart-0.1.0.tgz \
              && helm pull mychart \
                  --version 0.1.0 \
                  --repo https://chart.museum.local \
                  --insecure-skip-tls-verify
          ```
