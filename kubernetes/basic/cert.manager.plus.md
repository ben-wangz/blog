# cert manager

## main usage

* create certification of ssl automatically by cert-manager
* use dns01 method

## conceptions

* none

## purpose

* create a kubernetes cluster by kind
* setup cert-manager
* setup alidns-webhook
* install nginx service and access it with https

## installation
1. [create qemu machine for kind](../create.qemu.machine.for.kind.md)
2. setup [ingress-nginx](ingress.nginx.md)
3. setup [cert-manager](cert.manager.md)
3. download and load images to qemu machine(run command at the host of qemu machine)
    * run scripts
      in [download.and.load.function.sh](../resources/create.qemu.machine.for.kind/download.and.load.function.sh.md) to
      load function `download_and_load`
    * ```shell
      TOPIC_DIRECTORY="cert.manager.basic"
      BASE_URL="https://resource.geekcity.tech/kubernetes/docker-images/x86_64"
      download_and_load $TOPIC_DIRECTORY $BASE_URL \
          "quay.io_jetstack_cert-manager-controller_v1.5.4.dim" \
          "quay.io_jetstack_cert-manager-webhook_v1.5.4.dim" \
          "quay.io_jetstack_cert-manager-cainjector_v1.5.4.dim" \
          "quay.io_jetstack_cert-manager-ctl_v1.5.4.dim" \
          "quay.io_jetstack_cert-manager-acmesolver_v1.5.4.dim" \
          "docker.io_bitnami_nginx_1.21.3-debian-10-r29.dim"
      ```
4. install cert manager
    * prepare [cert.manager.values.yaml](resources/cert.manager/cert.manager.values.yaml.md)
    * prepare images
        + run scripts in [load.image.function.sh](../resources/load.image.function.sh.md) to load function `load_image`
        + ```shell
          load_image "localhost:5000" \
              "quay.io/jetstack/cert-manager-controller:v1.5.4" \
              "quay.io/jetstack/cert-manager-webhook:v1.5.4" \
              "quay.io/jetstack/cert-manager-cainjector:v1.5.4" \
              "quay.io/jetstack/cert-manager-ctl:v1.5.4" \
              "quay.io/jetstack/cert-manager-acmesolver:v1.5.4"
          ```
    * ```shell
      helm install \
          --create-namespace --namespace basic-components \
          my-cert-manager \
          https://resource.geekcity.tech/kubernetes/charts/https/charts.jetstack.io/cert-manager-v1.5.4.tgz \
          --values cert.manager.values.yaml \
          --atomic
      ```