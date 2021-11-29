# docker registry

## main usage

* a registry for docker

## conceptions

* none

## practise

### pre-requirements

* [a k8s cluster created by kind](../create.local.cluster.with.kind.md) have been read and practised
* [download kubernetes binary tools](../download.kubernetes.binary.tools.md)
    + kind
    + kubectl
    + helm
* we recommend to use [qemu machine](../../qemu/README.md)

### purpose

* create a kubernetes cluster by kind
* setup ingress
* setup cert-manager and self-signed issuer
* setup docker registry
* test with docker registry

### do it

1. [create local cluster for testing](local.cluster.for.testing.md)
2. setup [ingress-nginx](ingress.nginx.md)
3. setup [cert-manager](cert.manager.md)
4. configure self-signed issuer
    * `self-signed` issuer
        + prepare [self.signed.and.ca.issuer.yaml](resources/self.signed.and.ca.issuer.yaml.md)
        + ```shell
          ./bin/kubectl -n basic-components apply -f self.signed.and.ca.issuer.yaml
          ```
5. setup docker registry
    * prepare [docker.registry.values.yaml](resources/docker.registry.values.yaml.md)
    * prepare images
        + ```shell
          for IMAGE in "registry:2.7.1"
          do
              LOCAL_IMAGE="localhost:5000/$IMAGE"
              docker image inspect $IMAGE || docker pull $IMAGE
              docker image tag $IMAGE $LOCAL_IMAGE
              docker push $LOCAL_IMAGE
          done
          ```
    * install by helm
        + ```shell
          ./bin/helm install \
              --create-namespace --namespace basic-components \
              my-docker-registry \
              docker-registry \
              --version 1.14.0 \
              --repo https://helm.twun.io \
              --values $(pwd)/docker.registry.values.yaml \
              --atomic
          ```
    * configure ingress
        + NOTE: ingress in helm chart is not compatible enough for us, we have to install ingress manually
        + prepare [docker.registry.ingress.yaml](resources/docker.registry.ingress.yaml.md)
        + apply ingress
            * ```shell
              ./bin/kubectl -n basic-components apply -f docker.registry.ingress.yaml
              ```
6. test `docker push` and `docker pull` from `docker-registry`
    * configure `/etc/hosts` to point `` to the host
    * configure docker client as our tls is self-signed
        + ```shell
          mkdir -p /etc/docker/certs.d/docker.registry.lan:443 \
              && ./bin/kubectl -n basic-components get secret docker-registry-lan-tls -o jsonpath="{.data.tls\\.crt}" | base64 --decode > /etc/docker/certs.d/docker.registry.lan:443/ca.crt
          ```
    * tag and push
        + ```shell
          docker tag registry:2.7.1 docker.registry.lan:443/registry:2.7.1 \
              && docker push docker.registry.lan:443/registry:2.7.1
          ```
    * pull
        + ```shell
          docker image rm docker.registry.lan:443/registry:2.7.1 \
              && docker pull docker.registry.lan:443/registry:2.7.1
          ```
