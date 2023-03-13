# install applications

## install minio

1. prepare [minio.for.provisioner.values.yaml](resources/application/minio.for.provisioner.values.yaml.md)
2. prepare images
    * ```shell
      DOCKER_IMAGE_PATH=/root/data/docker-images
      for IMAGE in "docker.io_bitnami_minio_2022.8.22-debian-11-r0.dim" \
          "docker.io_bitnami_minio-client_2022.8.11-debian-11-r3.dim" \
          "docker.io_bitnami_bitnami-shell_11-debian-11-r28.dim"
      do
          IMAGE_FILE=$DOCKER_IMAGE_PATH/$IMAGE \
              && docker image load -i $IMAGE_FILE
      done
      ```
3. install by helm
    * ```shell
      helm install \
          --create-namespace --namespace application \
          my-minio-for-provisioner \
          /root/data/charts/minio-11.9.2.tgz \
          --values minio.for.provisioner.values.yaml \
          --atomic
      ```
4. find access id and access secret
    * ```shell
      kubectl -n application get secret my-minio-for-provisioner \
          -o jsonpath="{.data.root-user}" | base64 -d && echo
      kubectl -n application get secret my-minio-for-provisioner \
          -o jsonpath="{.data.root-password}" | base64 -d && echo
      ```
5. check `https://web-minio.geekcity.tech`
