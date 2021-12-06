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
2. download and load images to qemu machine(run command at the host of qemu machine)
    * ```shell
      function download_and_load()
      {
          TOPIC_DIRECTORY=$1
          BASE_URL=$2
          IMAGE_LIST="${@:3}"
          
          # prepare directories
          IMAGE_FILE_DIRECTORY_AT_HOST=docker-images/$TOPIC_DIRECTORY
          IMAGE_FILE_DIRECTORY_AT_QEMU_MACHINE=/root/docker-images/$TOPIC_DIRECTORY
          mkdir -p $IMAGE_FILE_DIRECTORY_AT_HOST
          SSH_OPTIONS="-o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no"
          ssh $SSH_OPTIONS -p 10022 root@localhost "mkdir -p $IMAGE_FILE_DIRECTORY_AT_QEMU_MACHINE"
      
          for IMAGE_FILE in $IMAGE_LIST
          do
              IMAGE_FILE_AT_HOST=docker-images/$TOPIC_DIRECTORY/$IMAGE_FILE
              IMAGE_FILE_AT_QEMU_MACHINE=$IMAGE_FILE_DIRECTORY_AT_QEMU_MACHINE/$IMAGE_FILE
              if [ ! -f $IMAGE_FILE_AT_HOST ]; then
                  TMP_FILE=$IMAGE_FILE_AT_HOST.tmp
                  curl -o $TMP_FILE -L ${BASE_URL}/$TOPIC_DIRECTORY/$IMAGE_FILE
                  mv $TMP_FILE $IMAGE_FILE_AT_HOST
              fi
              scp $SSH_OPTIONS -P 10022 $IMAGE_FILE_AT_HOST root@localhost:$IMAGE_FILE_AT_QEMU_MACHINE \
                  && ssh $SSH_OPTIONS -p 10022 root@localhost "docker image load -i $IMAGE_FILE_AT_QEMU_MACHINE"
          done
      }
      TOPIC_DIRECTORY="ingress.nginx.basic"
      BASE_URL="https://nginx.geekcity.tech/proxy/docker-images/x86_64"
      download_and_load $TOPIC_DIRECTORY $BASE_URL \
          "k8s.gcr.io_ingress-nginx_controller_v1.0.3.dim" \
          "k8s.gcr.io_ingress-nginx_kube-webhook-certgen_v1.0.dim" \
          "docker.io_bitnami_nginx_1.21.3-debian-10-r29.dim"
      ```
3. install ingress nginx
    * prepare [ingress.nginx.values.yaml](resources/ingress.nginx.values.yaml.md)
    * prepare images
        + ```shell
          for IMAGE in "k8s.gcr.io/ingress-nginx/controller:v1.0.3" \
              "k8s.gcr.io/ingress-nginx/kube-webhook-certgen:v1.0"
          do
              LOCAL_IMAGE="localhost:5000/$IMAGE"
              docker image inspect $IMAGE > /dev/null 2>&1 || docker pull $IMAGE
              docker image tag $IMAGE $LOCAL_IMAGE
              docker push $LOCAL_IMAGE
          done
          ```
    * ```shell
      helm install \
          --create-namespace --namespace basic-components \
          my-ingress-nginx \
          ingress-nginx \
          --version 4.0.5 \
          --repo https://kubernetes.github.io/ingress-nginx \
          --values ingress.nginx.values.yaml \
          --atomic
      ```
4. install nginx service
    * prepare [nginx.values.yaml](resources/nginx.values.yaml.md)
    * prepare images
        + ```shell
          for IMAGE in "docker.io/bitnami/nginx:1.21.3-debian-10-r29"
          do
              LOCAL_IMAGE="localhost:5000/$IMAGE"
              docker image inspect $IMAGE > /dev/null 2>&1 || docker pull $IMAGE
              docker image tag $IMAGE $LOCAL_IMAGE
              docker push $LOCAL_IMAGE
          done
          ```
    * ```shell
      helm install \
          --create-namespace --namespace test \
          my-nginx \
          nginx \
          --version 9.5.7 \
          --repo https://charts.bitnami.com/bitnami \
          --values nginx.values.yaml \
          --atomic
      ```
5. access nginx service with ingress
    + ```shell
      curl --header 'Host: my.nginx.local' http://localhost/my-nginx-prefix/
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
6. NOTES
    * `ingress-nginx` use `NodePort` as serviceType, whose nodePorts contains 32080(http) and 32443(https)
    * 32080(http) and 32443(https) mapped to 80 and 443 at localhost(the host machine of kind cluster) by kind
      configuration
    * therefore, access localhost:80 is equal to accessing `ingress-nginx` service
    * curl can modify the header by --header
    * the ingress will locate the application service(`my-nginx` in this case) according to the `Host` header and the
      path `/my-nginx-prefix/`
