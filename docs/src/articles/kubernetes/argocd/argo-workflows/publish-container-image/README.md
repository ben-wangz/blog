# publish container image

## prepare

1. argo workflows is ready
2. the storage class named `local-disks` is ready

## demo

1. prepare `publish-container-image.yaml`
    * ::: code-tabs#shell
      @tab dind
      ```yaml
      <!-- @include: publish-container-image-with-dind.yaml -->
      ```
      @tab buildah
      ```yaml
      <!-- @include: publish-container-image-with-buildah.yaml -->
      ```
      :::
2. prepare `registry-credentials` secret
    * ```shell
      <!-- @include: ../create-registry-credentials-secret.sh -->
      ```
3. submit with argo workflow client
    * ```shell
      argo -n business-workflows submit publish-container-image.yaml
      ```
4. check status
    * ```shell
      argo -n business-workflows list
      ```
    * ```shell
      # argo -n business-workflows get publish-container-image-2j5z2
      argo -n business-workflows get @lastest
      ```
    * ```shell
      # argo -n business-workflows logs publish-container-image-2j5z2
      argo -n business-workflows logs @latest
      ```