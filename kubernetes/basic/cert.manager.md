# ingress nginx

## main usage

* ingress nginx to expose http/https endpoints

## conceptions

* none

## practise

### pre-requirements

* none

### purpose

* create a kubernetes cluster by kind
* setup ingress-nginx
* setup cert-manager
* install nginx service and access it with https

### do it

1. [create local cluster for testing](local.cluster.for.testing.md)
2. setup [ingress-nginx](ingress.nginx.md)
3. install cert manager
    * prepare [cert.manager.values.yaml](resources/cert.manager.values.yaml.md)
    * prepare images
        + ```shell
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
    * ```shell
      ./bin/helm install \
          --create-namespace --namespace basic-components \
          my-cert-manager \
          cert-manager \
          --version 1.5.4 \
          --repo https://charts.jetstack.io \
          --values $(pwd)/cert.manager.values.yaml \
          --atomic
      ```
4. config issuer(s)
    * create `test` namespace if not exists
        + ```shell
          ./bin/kubectl create namespace test --dry-run=client -o yaml | ./bin/kubectl apply -f -
          ```
    * `self-signed` issuer
        + prepare [self.signed.issuer.yaml](resources/self.signed.issuer.yaml.md)
        + ```shell
          ./bin/kubectl -n test apply -f self.signed.issuer.yaml
          ```
    * `letsencrypt-staging` issuer
        + prepare [letsencrypt.staging.issuer.yaml](resources/letsencrypt.staging.issuer.yaml.md)
        + ```shell
          ./bin/kubectl -n test apply -f letsencrypt.staging.issuer.yaml
          ```
    * `letsencrypt-prod` issuer
        + NOTE: a domain name is needed. and in this case `letsencrypt-prod.nginx.geekcity.tech` will be used
        + NOTE: take care of [limits for requests](https://letsencrypt.org/docs/rate-limits/)
        + NOTE: `letsencrypt-prod.nginx.geekcity.tech` needs point to ingress port which can be accessed from network
        + prepare [letsencrypt.prod.issuer.yaml](resources/letsencrypt.prod.issuer.yaml.md)
        + ```shell
          IMAGE="quay.io/jetstack/cert-manager-acmesolver:v1.5.4"
          docker image inspect $IMAGE || docker pull $IMAGE
          ./bin/kind load docker-image $IMAGE
          ./bin/kubectl -n test apply -f letsencrypt.prod.issuer.yaml
          ```
5. checking with nginx service
    * prepare images
        + ```shell
          IMAGE="docker.io/bitnami/nginx:1.21.3-debian-10-r29"
          docker image inspect $IMAGE || docker pull $IMAGE
          docker image tag $IMAGE localhost:5000/$IMAGE
          docker push localhost:5000/$IMAGE
          ```
    * play with `self-signed` issuer
        + prepare [self.signed.nginx.values.yaml](resources/self.signed.nginx.values.yaml.md)
        + ```shell
          ./bin/helm install \
              --create-namespace --namespace test \
              self-signed-nginx \
              nginx \
              --version 9.5.7 \
              --repo https://charts.bitnami.com/bitnami \
              --values $(pwd)/self.signed.nginx.values.yaml \
              --atomic
          ```
        + ```shell
          curl --insecure --header 'Host: self-signed.nginx.tech' https://localhost/my-nginx-prefix/
          ```
        + expected output is something like
            * ```html
              <!DOCTYPE html>
              <html>
              <head>
              <title>Welcome to nginx!</title>
              <style>
              html { color-scheme: light dark; }
              body { width: 35em; margin: 0 auto;
              font-family: Tahoma, Verdana, Arial, sans-serif; }
              </style>
              </head>
              <body>
              <h1>Welcome to nginx!</h1>
              ...
              </body>
              </html>
              ```
    * play with `letsencrypt-staging`
        + prepare [letsencrypt.staging.nginx.values.yaml](resources/letsencrypt.staging.nginx.values.yaml.md)
        + ```shell
          ./bin/helm install \
              --create-namespace --namespace test \
              letsencrypt-staging-nginx \
              nginx \
              --version 9.5.7 \
              --repo https://charts.bitnami.com/bitnami \
              --values $(pwd)/letsencrypt.staging.nginx.values.yaml \
              --atomic
          ```
        + ```shell
          curl --insecure --header 'Host: letsencrypt-staging.nginx.tech' https://localhost/my-nginx-prefix/
          ```
    * play with `letsencrypt-prod`
        + prepare [letsencrypt.prod.nginx.values.yaml](resources/letsencrypt.prod.nginx.values.yaml.md)
        + ```shell
          ./bin/helm install \
              --create-namespace --namespace test \
              letsencrypt-prod-nginx \
              nginx \
              --version 9.5.7 \
              --repo https://charts.bitnami.com/bitnami \
              --values $(pwd)/letsencrypt.prod.nginx.values.yaml \
              --atomic
          ```
        + ```shell
          curl https://letsencrypt-prod.nginx.geekcity.tech/my-nginx-prefix/
          ```
        + access from you browser: https://letsencrypt-prod.nginx.geekcity.tech/my-nginx-prefix/
