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
        + prepare [self.signed.issuer.yaml](resources/self.signed.issuer.yaml.md)
        + ```shell
          ./bin/kubectl -n basic-components apply -f self.signed.issuer.yaml
          ```
5. setup harbor
    * prepare [harbor.values.yaml](resources/harbor.values.yaml.md)
    * prepare images
        + ```shell
          for IMAGE in "docker.io/bitnami/bitnami-shell:10-debian-10-r173" \
              "docker.io/bitnami/nginx:1.21.1-debian-10-r46" \
              "docker.io/bitnami/harbor-portal:2.3.2-debian-10-r2" \
              "docker.io/bitnami/harbor-core:2.3.2-debian-10-r2" \
              "docker.io/bitnami/harbor-jobservice:2.3.2-debian-10-r2" \
              "docker.io/bitnami/harbor-registry:2.3.2-debian-10-r2" \
              "docker.io/bitnami/harbor-registryctl:2.3.2-debian-10-r2" \
              "docker.io/bitnami/chartmuseum:0.13.1-debian-10-r149" \
              "docker.io/bitnami/harbor-clair:2.3.2-debian-10-r2" \
              "docker.io/bitnami/harbor-adapter-clair:2.3.2-debian-10-r2" \
              "docker.io/bitnami/harbor-notary-server:2.3.2-debian-10-r2" \
              "docker.io/bitnami/harbor-notary-signer:2.3.2-debian-10-r2" \
              "docker.io/bitnami/harbor-adapter-trivy:2.3.2-debian-10-r2" \
              "docker.io/bitnami/postgresql:11.13.0-debian-10-r12" \
              "docker.io/bitnami/bitnami-shell:10-debian-10-r172" \
              "docker.io/bitnami/postgres-exporter:0.10.0-debian-10-r40" \
              "docker.io/bitnami/redis:6.2.5-debian-10-r11" \
              "docker.io/bitnami/redis-sentinel:6.2.5-debian-10-r11" \
              "docker.io/bitnami/redis-exporter:1.24.0-debian-10-r48" \
              "docker.io/bitnami/bitnami-shell:10-debian-10-r151" \
              "docker.io/bitnami/bitnami-shell:10-debian-10-r151"
          do
              LOCAL_IMAGE="localhost:5000/$IMAGE"
              docker image inspect $IMAGE || docker pull $IMAGE
              docker image tag $IMAGE $LOCAL_IMAGE
              docker push $LOCAL_IMAGE
          done
          ```
    * ```shell
      ./bin/helm install \
          --create-namespace --namespace basic-components \
          my-harbor \
          harbor \
          --version 11.0.5 \
          --repo https://charts.bitnami.com/bitnami \
          --values $(pwd)/harbor.values.yaml \
          --atomic
      ```
