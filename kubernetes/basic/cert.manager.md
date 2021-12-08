# cert manager

## main usage

* create certification of ssl automatically by cert-manager

## conceptions

* none

## purpose

* create a kubernetes cluster by kind
* setup ingress-nginx
* setup cert-manager
* install nginx service and access it with https

## do it

1. [create qemu machine for kind](../create.qemu.machine.for.kind.md)
2. setup [ingress-nginx](ingress.nginx.md)
3. download and load images to qemu machine(run command at the host of qemu machine)
    * run scripts
      in [download.and.load.function.sh](../resources/create.qemu.machine.for.kind/download.and.load.function.sh.md) to
      load function `download_and_load`
    * ```shell
      TOPIC_DIRECTORY="cert.manager.basic"
      BASE_URL="https://nginx.geekcity.tech/proxy/docker-images/x86_64"
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
          cert-manager \
          --version 1.5.4 \
          --repo https://charts.jetstack.io \
          --values cert.manager.values.yaml \
          --atomic
      ```

## checking with nginx service

1. config issuer(s)
    * create `test` namespace if not exists
        + ```shell
          kubectl get namespace test > /dev/null 2>&1 || kubectl create namespace test
          ```
    * combination of `self-signed` issuer and `CA` issuer
        + prepare [self.signed.and.ca.issuer.yaml](resources/cert.manager/self.signed.and.ca.issuer.yaml.md)
        + ```shell
          kubectl -n test apply -f self.signed.and.ca.issuer.yaml
          ```
    * `letsencrypt-staging` issuer
        + prepare [letsencrypt.staging.issuer.yaml](resources/cert.manager/letsencrypt.staging.issuer.yaml.md)
        + ```shell
          kubectl -n test apply -f letsencrypt.staging.issuer.yaml
          ```
    * `letsencrypt-prod` issuer
        + NOTE: a domain name is needed. and in this case `letsencrypt-prod.nginx.geekcity.tech` will be used
        + NOTE: take care of [limits for requests](https://letsencrypt.org/docs/rate-limits/)
        + NOTE: `letsencrypt-prod.nginx.geekcity.tech` needs point to ingress port which can be accessed from network
        + prepare [letsencrypt.prod.issuer.yaml](resources/cert.manager/letsencrypt.prod.issuer.yaml.md)
        + ```shell
          kubectl -n test apply -f letsencrypt.prod.issuer.yaml
          ```
2. prepare images
    * run scripts in [load.image.function.sh](../resources/load.image.function.sh.md) to load function `load_image`
    * ```shell
      load_image "localhost:5000" \
          "docker.io/bitnami/nginx:1.21.3-debian-10-r29"
      ```
3. play with the combination of `self-signed` issuer and `CA` issuer
    + prepare [self.signed.nginx.values.yaml](resources/cert.manager/self.signed.nginx.values.yaml.md)
    + ```shell
      helm install \
          --create-namespace --namespace test \
          self-signed-nginx \
          nginx \
          --version 9.5.7 \
          --repo https://charts.bitnami.com/bitnami \
          --values self.signed.nginx.values.yaml \
          --atomic
      ```
    + ```shell
      curl --insecure --header 'Host: self-signed.nginx.local' https://localhost/my-nginx-prefix/
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
            + auto generated tls is a `BadConfig` for the certificate is being created by a SelfSigned issuer which has
              an empty Issuer DN
        * extract `tls.crt`
            + ```shell
              kubectl -n test get secret self-signed.nginx.local-tls -o jsonpath="{.data.tls\\.crt}" \
                  | base64 --decode > tls.crt
              ```
            + `tls.crt` should be the same as `connect.service.crt`
                * ```shell
                  openssl s_client \
                      -connect localhost:443 \
                      -servername self-signed.nginx.local \
                      -showcerts </dev/null 2>/dev/null \
                      | sed -n '/^-----BEGIN CERT/,/^-----END CERT/p' > connect.service.crt
                  # the output should be empty
                  diff connect.service.crt tls.crt
                  ```
        * import `tls.crt` into your system
            + with mac/windows just double click them and modify strategy to trust them
            + for centos stream 8
                * ```shell
                  cp tls.crt /etc/pki/ca-trust/source/anchors/ \
                      && update-ca-trust extract
                  # how to delete it?
                  #rm /etc/pki/ca-trust/source/anchors/tls.crt \
                  #    && update-ca-trust extract
                  ```
        * test with `wget`
            + ```shell
              echo "127.0.0.1 self-signed.nginx.local" >> /etc/hosts
              # you'll see the welcome page
              dnf -y install wget \
                  && wget -O - https://self-signed.nginx.local/my-nginx-prefix/
              ```
4. play with `letsencrypt-staging`
    + prepare [letsencrypt.staging.nginx.values.yaml](resources/cert.manager/letsencrypt.staging.nginx.values.yaml.md)
    + ```shell
      helm install \
          --create-namespace --namespace test \
          letsencrypt-staging-nginx \
          nginx \
          --version 9.5.7 \
          --repo https://charts.bitnami.com/bitnami \
          --values letsencrypt.staging.nginx.values.yaml \
          --atomic
      ```
    + ```shell
      curl --insecure --header 'Host: letsencrypt-staging.nginx.local' https://localhost/my-nginx-prefix/
      ```
5. play with `letsencrypt-prod`
    + prepare [letsencrypt.prod.nginx.values.yaml](resources/cert.manager/letsencrypt.prod.nginx.values.yaml.md)
    + NOTE: `letsencrypt-prod.nginx.geekcity.tech` should be pointed to your host, otherwise `letsencrypt` can not find
      the `acmesolver`
    + ```shell
      helm install \
          --create-namespace --namespace test \
          letsencrypt-prod-nginx \
          nginx \
          --version 9.5.7 \
          --repo https://charts.bitnami.com/bitnami \
          --values letsencrypt.prod.nginx.values.yaml \
          --atomic
      ```
    + ```shell
      curl https://letsencrypt-prod.nginx.geekcity.tech/my-nginx-prefix/
      ```
    + access from you browser: https://letsencrypt-prod.nginx.geekcity.tech/my-nginx-prefix/
