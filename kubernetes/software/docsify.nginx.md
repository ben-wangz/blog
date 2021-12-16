# docsify-nginx

## main usage

* a markdown doc system

## conceptions

* none

## purpose

* prepare a kind cluster with basic components
* install `docsify-nginx`

## installation

1. [prepare a kind cluster with basic components](../basic/kind.cluster.md)
2. download and load images to qemu machine(run command at the host of qemu machine)
    * run scripts
      in [download.and.load.function.sh](../resources/create.qemu.machine.for.kind/download.and.load.function.sh.md) to
      load function `download_and_load`
    * ```shell
      TOPIC_DIRECTORY="docsify.nginx.software"
      BASE_URL="https://resource.geekcity.tech/kubernetes/docker-images/x86_64"
      download_and_load $TOPIC_DIRECTORY $BASE_URL \
          "docker.io_bitnami_nginx_1.21.3-debian-10-r29.dim" \
          "docker.io_bitnami_git_2.33.0-debian-10-r53.dim"
      ```
3. prepare ssh-key-secret
    * create `rsa keys` by `ssh-keygen` if not generated before
        + ```shell
          mkdir -p ssh-keys/ \
              && ssh-keygen -t rsa -b 4096 -N "" -f ssh-keys/id_rsa
          ```
    * create namespace `application` if not exists
        + ```shell
          kubectl get namespace application \
              || kubectl create namespace application
          ```
    * generate `git-ssh-key-secret`
        + ```shell
          kubectl -n application create secret generic git-ssh-key-secret --from-file=ssh-keys/
          ```
    * add `ssh-keys/id_rsa.pub` to git repo server as deploy key
4. install
    * prepare [blog.values.yaml](resources/docsify.nginx/blog.values.yaml.md)
    * prepare images
        + run scripts in [load.image.function.sh](../resources/load.image.function.sh.md) to load function `load_image`
        + ```shell
          load_image "docker.registry.local:443" \
              "docker.io/bitnami/nginx:1.21.3-debian-10-r29" \
              "docker.io/bitnami/git:2.33.0-debian-10-r53"
          ```
    * install by helm
        * ```shell
          helm install \
              --create-namespace --namespace application \
              blog \
              nginx \
              --version 9.5.7 \
              --repo https://charts.bitnami.com/bitnami \
              --values blog.values.yaml \
              --atomic
          ```

## test

1. test connection
    * ```shell
      curl --insecure --header 'Host: blog.local' https://localhost
      ```

## uninstallation

1. uninstall
    * ```shell
      helm -n application uninstall blog
      ```
2. delete secret `git-ssh-key-secret`
    * ```shell
      kubectl -n application delete secret git-ssh-key-secret
      ```
