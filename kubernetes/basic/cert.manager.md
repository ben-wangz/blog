# cert manager

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
    * combination of `self-signed` issuer and `CA` issuer
        + prepare [self.signed.and.ca.issuer.yaml](resources/self.signed.and.ca.issuer.yaml.md)
        + ```shell
          ./bin/kubectl -n test apply -f self.signed.and.ca.issuer.yaml
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
    * play with the combination of `self-signed` issuer and `CA` issuer
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
        + optional, use tls.crt with your browser: take `wget` as an example
            * NOTES
                + `curl` is too strict for ssl, we didn't make it...
                + auto generated tls is a `BadConfig` for the certificate is being created by a SelfSigned issuer which
                  has an empty Issuer DN
            * extract `tls.crt`
                + ```shell
                  kubectl -n test get secret self-signed.nginx.tech-tls -o jsonpath="{.data.tls\\.crt}" | base64 --decode > tls.crt
                  ```
                + `tls.crt` should be the same as `connect.service.crt`
                    * ```shell
                      openssl s_client \
                          -connect localhost:443 \
                          -servername self-signed.nginx.tech \
                          -showcerts </dev/null 2>/dev/null \
                          | sed -n '/^-----BEGIN CERT/,/^-----END CERT/p' > connect.service.crt
                      # the output should be empty
                      diff connect.service.crt tls.crt
                      ```
            * import `tls.crt` into your system
                + with mac/windows just double click them and modify strategy to trust them
                + for centos 8
                    * ```shell
                      cp tls.crt /etc/pki/ca-trust/source/anchors/
                      update-ca-trust extract
                      # how to delete it?
                      #rm /etc/pki/ca-trust/source/anchors/tls.crt
                      #update-ca-trust extract
                      ```
            * ```shell
              echo "127.0.0.1 self-signed.nginx.tech" >> /etc/hosts
              # you'll see the welcome page
              wget -O - https://self-signed.nginx.tech/my-nginx-prefix/
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
