# docker registry

## main usage

* chart-museum for helm charts

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
* setup chart-museum
* test chart-museum

### do it

1. [create qemu machine for kind](../create.qemu.machine.for.kind.md)
2. setup [ingress-nginx](ingress.nginx.md)
3. setup [cert-manager](cert.manager.md)
4. configure self-signed issuer
    * `self-signed` issuer
        + prepare [self.signed.and.ca.issuer.yaml](resources/cert.manager/self.signed.and.ca.issuer.yaml.md)
        + ```shell
          ./bin/kubectl -n basic-components apply -f self.signed.and.ca.issuer.yaml
          ```
5. setup chart-museum
    * prepare [chart.museum.values.yaml](resources/chart.museum.values.yaml.md)
    * prepare images
        + ```shell
          for IMAGE in "ghcr.io/helm/chartmuseum:0.13.1" \
              "bitnami/minideb:buster"
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
              my-chart-museum \
              chartmuseum \
              --version 3.4.0 \
              --repo https://chartmuseum.github.io/charts \
              --values chart.museum.values.yaml \
              --atomic
          ```
6. test `helm push` and `helm pull` from `chart-museum`
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
