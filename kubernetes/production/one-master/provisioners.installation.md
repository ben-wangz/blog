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

