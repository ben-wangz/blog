# install static websites

## prepare `ssh-key-secret`

1. create `rsa keys` by `ssh-keygen` if not generated before
    * ```shell
      mkdir -p ssh-keys/ \
          && ssh-keygen -t rsa -b 4096 -N "" -f ssh-keys/id_rsa
      ```
2. generate `git-ssh-key-secret`
    * ```shell
      kubectl -n application create secret generic git-ssh-key-secret --from-file=ssh-keys/
      ```

## install nginx to serve the doc of this project

1. prepare [blog.values.yaml](resources/static-websites/blog.values.yaml.md)
2. prepare images
    * ```shell
      DOCKER_IMAGE_PATH=/root/data/docker-images
      for IMAGE in "docker.io_bitnami_nginx_1.23.3-debian-11-r33.dim" \
          "docker.io_bitnami_git_2.39.2-debian-11-r9.dim"
      do
          IMAGE_FILE=$DOCKER_IMAGE_PATH/$IMAGE \
              && docker image load -i $IMAGE_FILE
      done
      ```
3. install by helm
    * ```shell
      helm install \
          --create-namespace --namespace application \
          blog \
          /root/data/charts/nginx-13.2.29.tgz \
          --values blog.values.yaml \
          --atomic
      ```

## install nginx to service ant-doc

1. prepare [ant.doc.values.yaml](resources/static-websites/ant.doc.values.yaml.md)
2. prepare images
    * ```shell
      DOCKER_IMAGE_PATH=/root/data/docker-images
      for IMAGE in "docker.io_bitnami_nginx_1.23.3-debian-11-r33.dim" \
          "docker.io_bitnami_git_2.39.2-debian-11-r9.dim"
      do
          IMAGE_FILE=$DOCKER_IMAGE_PATH/$IMAGE \
              && docker image load -i $IMAGE_FILE
      done
      ```
3. install by helm
    * ```shell
      helm install \
          --create-namespace --namespace application \
          ant-doc \
          /root/data/charts/nginx-13.2.29.tgz \
          --values ant.doc.values.yaml \
          --atomic
      ```