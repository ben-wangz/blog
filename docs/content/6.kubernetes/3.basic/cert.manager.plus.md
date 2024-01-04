# cert manager plus

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
4. download and load images to qemu machine(run command at the host of qemu machine)
    * run scripts
      in [download.and.load.function.sh](../resources/create.qemu.machine.for.kind/download.and.load.function.sh.md) to
      load function `download_and_load`
    * ```shell
      TOPIC_DIRECTORY="cert.manager.plus.basic"
      BASE_URL="https://resource.geekcity.tech/kubernetes/docker-images/x86_64"
      download_and_load $TOPIC_DIRECTORY $BASE_URL \
          "ghcr.io_devmachine-fr_cert-manager-alidns-webhook_cert-manager-alidns-webhook_0.2.0.dim" \
          "docker.io_bitnami_nginx_1.21.3-debian-10-r29.dim"
      ```
5. install `alidns-webhook`
    * prepare [alidns.webhook.values.yaml](resources/cert.manager.plus/alidns.webhook.values.yaml.md)
    * prepare images
        + run scripts in [load.image.function.sh](../resources/load.image.function.sh.md) to load function `load_image`
        + ```shell
          load_image "localhost:5000" \
              "ghcr.io/devmachine-fr/cert-manager-alidns-webhook/cert-manager-alidns-webhook:0.2.0"
          ```
    * create `basic-components-plus` namespace if not exists
        + ```shell
          kubectl get namespace basic-components-plus > /dev/null 2>&1 || kubectl create namespace basic-components-plus
          ```
    * prepare `access-key-id` and `access-key-secret` to manage your domain
        + ```shell
          kubectl -n basic-components-plus create secret generic alidns-webhook-secrets \
              --from-literal="access-token=$YOUR_ACCESS_KEY_ID" \
              --from-literal="secret-key=$YOUR_ACCESS-KEY-SECRET"
          ```
    * add permissions to your RAM account
        + ```json
          {
            "Version": "1",
            "Statement": [
              {
                "Effect": "Allow",
                "Action": [
                  "alidns:AddDomainRecord",
                  "alidns:DeleteDomainRecord"
                ],
                "Resource": "acs:alidns:*:*:domain/geekcity.tech"
              }, {
                "Effect": "Allow",
                "Action": [
                  "alidns:DescribeDomains",
                  "alidns:DescribeDomainRecords"
                ],
                "Resource": "acs:alidns:*:*:domain/*"
              }
            ]
          }
          ```
    * ```shell
      helm install \
          --create-namespace --namespace basic-components-plus \
          my-alidns-webhook \
          https://resource.geekcity.tech/kubernetes/charts/https/github.com/DEVmachine-fr/cert-manager-alidns-webhook/releases/download/alidns-webhook-0.6.0/alidns-webhook-0.6.0.tgz \
          --values alidns.webhook.values.yaml \
          --atomic
      ```

## test

1. test with certificate
    * create issuer
        + prepare [alidns.webhook.staging.issuer.yaml](
          resources/cert.manager.plus/alidns.webhook.staging.issuer.yaml.md)
        + ```shell
          kubectl -n basic-components-plus apply -f alidns.webhook.staging.issuer.yaml
          ```
    * create certificate
        + prepare [test.certificate.yaml](resources/cert.manager.plus/test.certificate.yaml.md)
        + ```shell
          kubectl -n basic-components-plus apply -f test.certificate.yaml
          ```
    * certificate should have been `successfully issued` after a while
        + ```shell
          kubectl -n basic-components-plus describe certificate cm-plus-test
          ```
2. test with nginx
    * prepare [alidns.webhook.staging.nginx.values.yaml](
      resources/cert.manager.plus/alidns.webhook.staging.nginx.values.yaml.md)
    * prepare images
        + run scripts in [load.image.function.sh](../resources/load.image.function.sh.md) to load function `load_image`
        + ```shell
          load_image "localhost:5000" \
              "docker.io/bitnami/nginx:1.21.3-debian-10-r29"
          ```
    * ```shell
      helm install \
          --create-namespace --namespace basic-components-plus \
          alidns-letsencrypt-staging-nginx \
          https://resource.geekcity.tech/kubernetes/charts/https/charts.bitnami.com/bitnami/nginx-9.5.7.tgz \
          --values alidns.webhook.staging.nginx.values.yaml \
          --atomic
      ```
    * certificate should have been `successfully issued` after a while
        + ```shell
          kubectl -n basic-components-plus describe certificate alidns-letsencrypt-staging-nginx.geekcity.tech-tls
          ```
    * ```shell
      curl --insecure --header 'Host: alidns-letsencrypt-staging-nginx.geekcity.tech' https://localhost/my-nginx-prefix/
      ```