# kind cluster with basic components

## main usage

* kind cluster with basic components

## conceptions

* none

## purpose

* create a kubernetes cluster by kind
* setup ingress
* setup cert-manager and self-signed issuer
* setup docker-registry
* setup chart-museum
* test nginx whose endpoint exposed by ingress
* test docker image push and pull
* test helm chart push and pull

## installation

1. [create qemu machine for kind](../create.qemu.machine.for.kind.md)
2. setup [ingress-nginx](ingress.nginx.md)
3. setup [cert-manager](cert.manager.md)
4. setup [docker-registry](docker.registry.md)
5. download and load images to qemu machine(run command at the host of qemu machine)
    * run scripts
      in [download.and.load.function.sh](../resources/create.qemu.machine.for.kind/download.and.load.function.sh.md) to
      load function `download_and_load`
    * ```shell
      TOPIC_DIRECTORY="kind.cluster.basic"
      BASE_URL="https://resource.geekcity.tech/kubernetes/docker-images/x86_64"
      download_and_load $TOPIC_DIRECTORY $BASE_URL \
          "docker.io_bitnami_nginx_1.21.3-debian-10-r29.dim" \
          "docker.io_registry_2.7.1.dim"
      ```
6. configure docker client as tls of `docker.registry.local` is self-signed
    * ```shell
      DOCKER_REGISTRY_URL="docker.registry.local:443" \
          && CERT_DIRECTORY_PATH="/etc/docker/certs.d/$DOCKER_REGISTRY_URL" \
          && mkdir -p $CERT_DIRECTORY_PATH \
          && kubectl -n basic-components get secret docker.registry.local-tls \
          -o jsonpath="{.data.tls\\.crt}" \
          | base64 --decode > $CERT_DIRECTORY_PATH/ca.crt
      ```
7. configure `/etc/hosts` for `docker-registry` and `chart-museum`
    * ```shell
      echo 172.17.0.1 docker.registry.local >> /etc/hosts \
          && echo 172.17.0.1 insecure.docker.registry.local >> /etc/hosts \
          && echo 172.17.0.1 chart.museum.local >> /etc/hosts \
          && docker exec kind-control-plane bash -c 'echo 172.17.0.1 docker.registry.local >> /etc/hosts' \
          && docker exec kind-control-plane bash -c 'echo 172.17.0.1 insecure.docker.registry.local >> /etc/hosts' \
          && docker exec kind-control-plane bash -c 'echo 172.17.0.1 chart.museum.local >> /etc/hosts'
      ```

## test nginx whose endpoint exposed by ingress

1. install nginx service
    * prepare [nginx.values.yaml](resources/ingress.nginx/nginx.values.yaml.md)
    * prepare images
        + run scripts in [load.image.function.sh](../resources/load.image.function.sh.md) to load function `load_image`
        + ```shell
          load_image "localhost:5000" \
              "docker.io/bitnami/nginx:1.21.3-debian-10-r29"
          ```
    * ```shell
      helm install \
          --create-namespace --namespace test \
          my-nginx \
          https://resource.geekcity.tech/kubernetes/charts/https/charts.bitnami.com/bitnami/nginx-9.5.7.tgz \
          --values nginx.values.yaml \
          --atomic
      ```
2. access nginx service with ingress
    * ```shell
      curl --header 'Host: my.nginx.local' http://localhost/my-nginx-prefix/
      ```

## test docker image push and pull

1. push
    * ```shell
      for DOCKER_REGISTRY_URL in "docker.registry.local:443" \
          "insecure.docker.registry.local:80"
      do
          DOCKER_IMAGE="registry:2.7.1" \
              && docker tag $DOCKER_IMAGE $DOCKER_REGISTRY_URL/$DOCKER_IMAGE \
              && docker push $DOCKER_REGISTRY_URL/$DOCKER_IMAGE
      done
      ```
2. pull
    * ```shell
      for DOCKER_REGISTRY_URL in "docker.registry.local:443" \
          "insecure.docker.registry.local:80"
      do
          DOCKER_IMAGE="registry:2.7.1" \
              && docker image rm $DOCKER_REGISTRY_URL/$DOCKER_IMAGE \
              && docker pull $DOCKER_REGISTRY_URL/$DOCKER_IMAGE
      done
      ```
