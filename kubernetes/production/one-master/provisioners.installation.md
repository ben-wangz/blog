# install provisioners

## install nfs provisioner

1. prepare nfs service
    * use NAS from aliyun
        + host: `051c649500-wdc91.cn-hangzhou.nas.aliyuncs.com`
        + path: `/`
        + options: `vers=4,minorversion=0,rsize=1048576,wsize=1048576,hard,timeo=600,retrans=2,noresvport`
2.

prepare [nfs.subdir.external.provisioner.values.yaml](resources/provisioners/nfs.subdir.external.provisioner.values.yaml.md)

3. prepare images
    * ```shell
      DOCKER_IMAGE_PATH=/root/data/docker-images
      for IMAGE in "k8s.gcr.io_sig-storage_nfs-subdir-external-provisioner_v4.0.2.dim" \
          "docker.io_bitnami_mariadb_10.5.12-debian-10-r0.dim"
      do
          IMAGE_FILE=$DOCKER_IMAGE_PATH/$IMAGE
          docker image load -i $IMAGE_FILE
      done
      ```
4. install by helm
    * ```shell
      helm install \
          --create-namespace --namespace nfs-provisioner \
          my-nfs-subdir-external-provisioner \
          /root/data/charts/nfs-subdir-external-provisioner-4.0.14.tgz \
          --values nfs.subdir.external.provisioner.values.yaml \
          --atomic
      ```

## install minio

1. prepare [minio.for.provisioner.values.yaml](resources/provisioners/minio.for.provisioner.values.yaml.md)
2. prepare images
    * ```shell
      DOCKER_IMAGE_PATH=/root/data/docker-images
      for IMAGE in "docker.io_bitnami_minio_2022.8.22-debian-11-r0.dim" \
          "docker.io_bitnami_minio-client_2022.8.11-debian-11-r3.dim" \
          "docker.io_bitnami_bitnami-shell_11-debian-11-r28.dim"
      do
          IMAGE_FILE=$DOCKER_IMAGE_PATH/$IMAGE
          docker image load -i $IMAGE_FILE
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

## install minio provisioner
