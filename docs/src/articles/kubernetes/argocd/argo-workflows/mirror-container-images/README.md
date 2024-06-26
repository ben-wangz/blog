# mirror container images

## prepare

1. argo workflows is ready
2. minio is ready for artifact repository
    * endpoint: minio.storage:9000

## demo

1. [configure s3 artifact repository](../configure-s3-artifact-repository/README.md)
2. prepare `images-to-mirror.yaml` which contains a configmap storing a list of images to mirror
    * ```yaml
      <!-- @include: images-to-mirror.yaml -->
      ```
3. apply `images-to-mirror.yaml` to k8s
    * ```shell
      kubectl -n business-workflows apply -f images-to-mirror.yaml
      ```
4. prepare `registry-credentials` secret
    * ```shell
      <!-- @include: ../create-registry-credentials-secret.sh -->
      ```
5. prepare `mirror-container-images.yaml`
    * ::: code-tabs#shell
      @tab with-docker
      ```yaml
      <!-- @include: mirror-with-docker.yaml -->
      ```
      @tab with-seopeo
      ```yaml
      <!-- @include: mirror-with-skopeo.yaml -->
      ```
      :::
6. submit with argo workflow client
    * ```shell
      argo -n business-workflows submit mirror-container-images.yaml
      ```
7. check status
    * ```shell
      argo -n business-workflows list
      ```
    * ```shell
      # argo -n business-workflows get mirror-container-images-2j5z2
      argo -n business-workflows get @lastest
      ```
    * ```shell
      # argo -n business-workflows logs mirror-container-images-2j5z2
      argo -n business-workflows logs @latest
      ```