# docker registry

## main usage

* a registry for docker

## conceptions

* none

## purpose

* create a kubernetes cluster by kind
* setup ingress
* setup cert-manager and self-signed issuer
* setup docker registry
* test docker registry

## installation

1. [create qemu machine for kind](../create.qemu.machine.for.kind.md)
2. setup [ingress-nginx](ingress.nginx.md)
3. setup [cert-manager](cert.manager.md)
4. download and load images to qemu machine(run command at the host of qemu machine)
    * run scripts
      in [download.and.load.function.sh](../resources/create.qemu.machine.for.kind/download.and.load.function.sh.md) to
      load function `download_and_load`
    * ```shell
      TOPIC_DIRECTORY="docker.registry.basic"
      BASE_URL="https://nginx.geekcity.tech/proxy/docker-images/x86_64"
      download_and_load $TOPIC_DIRECTORY $BASE_URL \
          "docker.io_registry_2.7.1.dim"
      ```
5. configure self-signed issuer
    * `self-signed` issuer
        + prepare [self.signed.and.ca.issuer.yaml](resources/cert.manager/self.signed.and.ca.issuer.yaml.md)
        + ```shell
          kubectl -n basic-components apply -f self.signed.and.ca.issuer.yaml
          ```
6. install docker registry
    * prepare [docker.registry.values.yaml](resources/docker.registry/docker.registry.values.yaml.md)
    * prepare images
        + run scripts in [load.image.function.sh](../resources/load.image.function.sh.md) to load function `load_image`
        + ```shell
          load_image "localhost:5000" \
              "docker.io/registry:2.7.1"
          ```
    * install by helm
        + ```shell
          helm install \
              --create-namespace --namespace basic-components \
              my-docker-registry \
              docker-registry \
              --version 1.14.0 \
              --repo https://helm.twun.io \
              --values docker.registry.values.yaml \
              --atomic
          ```
    * configure ingress with tls
        + NOTE: ingress in helm chart is not compatible enough for us, we have to install ingress manually
        + prepare [docker.registry.ingress.yaml](resources/docker.registry/docker.registry.ingress.yaml.md)
        + apply ingress
            * ```shell
              kubectl -n basic-components apply -f docker.registry.ingress.yaml
              ```
    * configure ingress without tls
        + NOTE: ingress in helm chart is not compatible enough for us, we have to install ingress manually
        +
      prepare [insecure.docker.registry.ingress.yaml](resources/docker.registry/insecure.docker.registry.ingress.yaml.md)
        + apply ingress
            * ```shell
              kubectl -n basic-components apply -f insecure.docker.registry.ingress.yaml
              ```

## test

1. test `docker push` and `docker pull` from `docker-registry` with tls
    * configure `/etc/hosts` to point `docker.registry.local` to the host
        + ```shell
          echo 127.0.0.1 docker.registry.local >> /etc/hosts
          ```
    * configure docker client as our tls is self-signed
        + ```shell
          DOCKER_REGISTRY_URL="docker.registry.local:443" \
              && CERT_DIRECTORY_PATH="/etc/docker/certs.d/$DOCKER_REGISTRY_URL" \
              && mkdir -p $CERT_DIRECTORY_PATH \
              && kubectl -n basic-components get secret docker.registry.local-tls \
                  -o jsonpath="{.data.tls\\.crt}" \
                  | base64 --decode > $CERT_DIRECTORY_PATH/ca.crt
          ```
    * tag and push
        + ```shell
          DOCKER_REGISTRY_URL="docker.registry.local:443" \
              && DOCKER_IMAGE="registry:2.7.1" \
              && docker tag $DOCKER_IMAGE $DOCKER_REGISTRY_URL/$DOCKER_IMAGE \
              && docker push $DOCKER_REGISTRY_URL/$DOCKER_IMAGE
          ```
    * pull
        + ```shell
          DOCKER_REGISTRY_URL="docker.registry.local:443" \
              && DOCKER_IMAGE="registry:2.7.1" \
              && docker image rm $DOCKER_REGISTRY_URL/$DOCKER_IMAGE \
              && docker pull $DOCKER_REGISTRY_URL/$DOCKER_IMAGE
          ```
2. test `docker push` and `docker pull` from `docker-registry` without tls
    * NOTE: docker client is pre-configured to support `insecure.docker.registry.local:80` before creating kind cluster
    * configure `/etc/hosts` to point `insecure.docker.registry.local` to the host
        + ```shell
          echo 127.0.0.1 insecure.docker.registry.local >> /etc/hosts
          ```
    * tag and push
        + ```shell
          DOCKER_REGISTRY_URL="insecure.docker.registry.local:80" \
              && DOCKER_IMAGE="registry:2.7.1" \
              && docker tag $DOCKER_IMAGE $DOCKER_REGISTRY_URL/$DOCKER_IMAGE \
              && docker push $DOCKER_REGISTRY_URL/$DOCKER_IMAGE
          ```
    * pull
        + ```shell
          DOCKER_REGISTRY_URL="insecure.docker.registry.local:80" \
              && DOCKER_IMAGE="registry:2.7.1" \
              && docker image rm $DOCKER_REGISTRY_URL/$DOCKER_IMAGE \
              && docker pull $DOCKER_REGISTRY_URL/$DOCKER_IMAGE
          ```
