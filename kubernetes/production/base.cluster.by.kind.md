# base cluster by kind

## create k8s cluster by kind

1. prepare [kind.cluster.yaml](resources/kind.cluster.yaml.md)
2. prepare [kind.with.registry.sh](resources/kind.with.registry.sh.md)
3. download kind, kubectl and helm binaries to `bin/` according
   to [download kubernetes binary tools](../download.kubernetes.binary.tools.md)
4. install k8s cluster
    * ```shell
      bash kind.with.registry.sh kind.cluster.yaml bin/kind bin/kubectl
      ```

## install ingress-nginx

1. prepare [ingress.nginx.values.yaml](resources/ingress.nginx.values.yaml.md)
2. prepare images
    * ```shell
      for IMAGE in "k8s.gcr.io/ingress-nginx/controller:v1.0.3" "k8s.gcr.io/ingress-nginx/kube-webhook-certgen:v1.0"
      do
          LOCAL_IMAGE="localhost:5000/$IMAGE"
          docker image inspect $IMAGE || docker pull $IMAGE
          docker image tag $IMAGE $LOCAL_IMAGE
          docker push $LOCAL_IMAGE
      done
      ```
3. install ingress-nginx with helm
    * ```shell
      ./bin/helm install \
          --create-namespace --namespace basic-components \
          my-ingress-nginx \
          ingress-nginx \
          --version 4.0.5 \
          --repo https://kubernetes.github.io/ingress-nginx \
          --values ingress.nginx.values.yaml \
          --atomic
      ```

## install cert-manager

1. prepare [cert.manager.values.yaml](resources/cert.manager.values.yaml.md)
2. prepare images
    * ```shell
      for IMAGE in "quay.io/jetstack/cert-manager-controller:v1.5.4" \
          "quay.io/jetstack/cert-manager-webhook:v1.5.4" \
          "quay.io/jetstack/cert-manager-cainjector:v1.5.4" \
          "quay.io/jetstack/cert-manager-ctl:v1.5.4"
      do
          LOCAL_IMAGE="localhost:5000/$IMAGE"
          docker image inspect $IMAGE || docker pull $IMAGE
          docker image tag $IMAGE $LOCAL_IMAGE
          docker push $LOCAL_IMAGE
      done
      ```
3. install cert-manager with helm
    * ```shell
      ./bin/helm install \
          --create-namespace --namespace basic-components \
          my-cert-manager \
          cert-manager \
          --version 1.5.4 \
          --repo https://charts.jetstack.io \
          --values cert.manager.values.yaml \
          --atomic
      ```
4. config letsencrypt issuer
    * prepare [letsencrypt.prod.issuer.yaml](resources/letsencrypt.prod.issuer.yaml.md)
    * ```shell
      IMAGE="quay.io/jetstack/cert-manager-acmesolver:v1.5.4"
      docker image inspect $IMAGE || docker pull $IMAGE
      ./bin/kind load docker-image $IMAGE
      ./bin/kubectl -n basic-components apply -f letsencrypt.prod.issuer.yaml
      ```

## install docker-registry

1. prepare [docker.registry.values.yaml](resources/docker.registry.values.yaml.md)
2. prepare images
    * ```shell
      for IMAGE in "registry:2.7.1"
      do
          LOCAL_IMAGE="localhost:5000/$IMAGE"
          docker image inspect $IMAGE || docker pull $IMAGE
          docker image tag $IMAGE $LOCAL_IMAGE
          docker push $LOCAL_IMAGE
      done
      ```
3. install by helm
    * ```shell
      ./bin/helm install \
          --create-namespace --namespace basic-components \
          my-docker-registry \
          docker-registry \
          --version 1.14.0 \
          --repo https://helm.twun.io \
          --values $(pwd)/docker.registry.values.yaml \
          --atomic
      ```
4. configure ingress
    * NOTE: ingress in helm chart is not compatible enough for us, we have to install ingress manually
    * prepare [docker.registry.ingress.yaml](resources/docker.registry.ingress.yaml.md)
    * apply ingress
        + ```shell
          ./bin/kubectl -n basic-components apply -f docker.registry.ingress.yaml
          ```

## install nginx to service the doc of this project

## install grafana

## install k8s dashboard
