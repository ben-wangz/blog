# docker registry

## main usage

* chart-museum for helm charts

## conceptions

* none

## purpose

* create a kubernetes cluster by kind
* setup ingress
* setup cert-manager and self-signed issuer
* setup chart-museum
* test chart-museum

## installation

1. [create qemu machine for kind](../create.qemu.machine.for.kind.md)
2. setup [ingress-nginx](ingress.nginx.md)
3. setup [cert-manager](cert.manager.md)
4. download and load images to qemu machine(run command at the host of qemu machine)
    * run scripts
      in [download.and.load.function.sh](../resources/create.qemu.machine.for.kind/download.and.load.function.sh.md) to
      load function `download_and_load`
    * ```shell
      TOPIC_DIRECTORY="chart.museum.basic"
      BASE_URL="https://nginx.geekcity.tech/proxy/docker-images/x86_64"
      download_and_load $TOPIC_DIRECTORY $BASE_URL \
          "ghcr.io_helm_chartmuseum_v0.13.1.dim" \
          "bitnami_minideb_buster.dim"
      ```
5. configure self-signed issuer
    * `self-signed` issuer
        + prepare [self.signed.and.ca.issuer.yaml](resources/cert.manager/self.signed.and.ca.issuer.yaml.md)
        + ```shell
          kubectl -n basic-components apply -f self.signed.and.ca.issuer.yaml
          ```
6. setup chart-museum
    * prepare [chart.museum.values.yaml](resources/chart.museum/chart.museum.values.yaml.md)
    * prepare images
        + run scripts in [load.image.function.sh](../resources/load.image.function.sh.md) to load function `load_image`
        + ```shell
          load_image "localhost:5000" \
              "ghcr.io/helm/chartmuseum:v0.13.1" \
              "bitnami/minideb:buster"
          ```
    * install by helm
        + ```shell
          helm install \
              --create-namespace --namespace basic-components \
              my-chart-museum \
              chartmuseum \
              --version 3.4.0 \
              --repo https://chartmuseum.github.io/charts \
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
