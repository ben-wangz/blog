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
* install nginx service and access it with ingress

### do it

1. [create qemu machine for kind](../create.qemu.machine.for.kind.md)
2. install ingress nginx
    * prepare [ingress.nginx.values.yaml](resources/ingress.nginx.values.yaml.md)
    * prepare images
        + ```shell
          for IMAGE in "k8s.gcr.io/ingress-nginx/controller:v1.0.3" "k8s.gcr.io/ingress-nginx/kube-webhook-certgen:v1.0"
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
          my-ingress-nginx \
          ingress-nginx \
          --version 4.0.5 \
          --repo https://kubernetes.github.io/ingress-nginx \
          --values $(pwd)/ingress.nginx.values.yaml \
          --atomic
      ```
3. install nginx service
    * prepare [nginx.values.yaml](resources/nginx.values.yaml.md)
    * prepare images
        + ```shell
          IMAGE="docker.io/bitnami/nginx:1.21.3-debian-10-r29"
          docker image inspect $IMAGE || docker pull $IMAGE
          docker image tag $IMAGE localhost:5000/$IMAGE
          docker push localhost:5000/$IMAGE
          ```
    * ```shell
      ./bin/helm install \
          --create-namespace --namespace test \
          my-nginx \
          nginx \
          --version 9.5.7 \
          --repo https://charts.bitnami.com/bitnami \
          --values $(pwd)/nginx.values.yaml \
          --atomic
      ```
4. access nginx service with ingress
    + ```shell
      curl --header 'Host: my.nginx.tech' http://localhost/my-nginx-prefix/
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
          <p>If you see this page, the nginx web server is successfully installed and
          working. Further configuration is required.</p>

          <p>For online documentation and support please refer to
          <a href="http://nginx.org/">nginx.org</a>.<br/>
          Commercial support is available at
          <a href="http://nginx.com/">nginx.com</a>.</p>

          <p><em>Thank you for using nginx.</em></p>
          </body>
          </html>
          ```
5. NOTES
    * `ingress-nginx` use `NodePort` as serviceType, whose nodePorts contains 32080(http) and 32443(https)
    * 32080(http) and 32443(https) mapped to 80 and 443 at localhost(the host machine of kind cluster) by kind
      configuration
    * therefore, access localhost:80 is equal to accessing `ingress-nginx` service
    * curl can modify the header by --header
    * the ingress will locate the application service(`my-nginx` in this case) according to the `Host` header and the
      path `/my-nginx-prefix/`
