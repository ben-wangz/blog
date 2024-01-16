# resource-nginx

## main usage

* a static resource sharing service

## conceptions

* none

## purpose

* prepare a kind cluster with basic components
* install `resource-nginx`

## installation

1. [prepare a kind cluster with basic components](../basic/kind.cluster.md)
2. download and load images to qemu machine(run command at the host of qemu machine)
    * run scripts
      in [download.and.load.function.sh](../resources/create.qemu.machine.for.kind/download.and.load.function.sh.md) to
      load function `download_and_load`
    * ```shell
      TOPIC_DIRECTORY="resource.nginx.software"
      BASE_URL="https://resource.geekcity.tech/kubernetes/docker-images/x86_64"
      download_and_load $TOPIC_DIRECTORY $BASE_URL \
          "docker.io_bitnami_nginx_1.21.3-debian-10-r29.dim" \
          "docker.io_busybox_1.33.1-uclibc.dim"
      ```
3. create pvc named `resource-nginx-pvc`
    * prepare [resource.nginx.pvc.yaml](resources/resource.nginx/resource.nginx.pvc.yaml.md)
    * ```shell
      kubectl -n application apply -f resource.nginx.pvc.yaml
      ```
4. install
    * prepare [resource.nginx.values.yaml](resources/resource.nginx/resource.nginx.values.yaml.md)
    * prepare images
        + run scripts in [load.image.function.sh](../resources/load.image.function.sh.md) to load function `load_image`
        + ```shell
          load_image "docker.registry.local:443" \
              "docker.io/bitnami/nginx:1.21.3-debian-10-r29" \
              "docker.io/busybox:1.33.1-uclibc"
          ```
    * install by helm
        * ```shell
          helm install \
              --create-namespace --namespace application \
              resource-nginx \
              https://resource.geekcity.tech/kubernetes/charts/https/charts.bitnami.com/bitnami/nginx-9.5.7.tgz \
              --values resource.nginx.values.yaml \
              --atomic
          ```

## test

1. test connection
    * ```shell
      curl --insecure --header 'Host: resource.nginx.local' https://localhost
      ```
2. add resource
    * ```shell
      export POD_NAME=$(kubectl -n application get pod \
          -l "app.kubernetes.io/instance=resource-nginx" \
          -o jsonpath="{.items[0].metadata.name}") \
          && kubectl -n application exec -it $POD_NAME -c busybox \
              -- sh -c "echo this is a test > /root/data/file-created-by-busybox"
      ```
3. check resource by `curl`
    * ```shell
      curl --insecure --header 'Host: resource.nginx.local' https://localhost
      ```

## uninstallation

1. uninstall
    * ```shell
      helm -n application uninstall resource-nginx
      ```
2. delete pvc `resource-nginx-pvc`
    * ```shell
      kubectl -n application delete -f resource.nginx.pvc.yaml
      ```
