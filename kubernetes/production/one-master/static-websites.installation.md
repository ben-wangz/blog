# install static websites
## install nginx to service the doc of this project

1. prepare ssh-key-secret
    * create `rsa keys` by `ssh-keygen` if not generated before
        + ```shell
          mkdir -p ssh-keys/ \
              && ssh-keygen -t rsa -b 4096 -N "" -f ssh-keys/id_rsa
          ```
    * generate `git-ssh-key-secret`
        + ```shell
          ./bin/kubectl -n application create secret generic git-ssh-key-secret --from-file=ssh-keys/
          ```
2. prepare [blog.values.yaml](resources/blog.values.yaml.md)
3. prepare images
    * ```shell
      for IMAGE in "docker.io/bitnami/nginx:1.21.3-debian-10-r29" \
          "docker.io/bitnami/git:2.33.0-debian-10-r53"
      do
          LOCAL_IMAGE="localhost:5000/$IMAGE"
          docker inspect $IMAGE > /dev/null 2>&1 || docker pull $IMAGE
          docker image tag $IMAGE $LOCAL_IMAGE
          docker push $LOCAL_IMAGE
      done
      ```
4. install by helm
    * ```shell
      ./bin/helm install \
          --create-namespace --namespace application \
          blog \
          https://resource.geekcity.tech/kubernetes/charts/https/charts.bitnami.com/bitnami/nginx-9.5.7.tgz \
          --values blog.values.yaml \
          --atomic
      ```

## install nginx to service ant-doc

1. prepare [ant.doc.values.yaml](resources/ant.doc.values.yaml.md)
2. prepare images
    * ```shell
      for IMAGE in "docker.io/bitnami/nginx:1.21.3-debian-10-r29" \
          "docker.io/bitnami/git:2.33.0-debian-10-r53"
      do
          LOCAL_IMAGE="localhost:5000/$IMAGE"
          docker inspect $IMAGE > /dev/null 2>&1 || docker pull $IMAGE
          docker image tag $IMAGE $LOCAL_IMAGE
          docker push $LOCAL_IMAGE
      done
      ```
3. install by helm
    * ```shell
      ./bin/helm install \
          --create-namespace --namespace application \
          ant-doc \
          https://resource.geekcity.tech/kubernetes/charts/https/charts.bitnami.com/bitnami/nginx-9.5.7.tgz \
          --values ant.doc.values.yaml \
          --atomic
      ```