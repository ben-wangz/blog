# build and push container image

## prepare

1. k8s is ready
2. argo workflows is ready

## demo

1. prepare `build-and-push-container-image.yaml`
    * ```yaml
      <!-- @include: build-and-push-container-image.yaml -->
      ```
2. submit with argo workflow client
    * ```shell
      argo -n business-workflows submit build-and-push-container-image.yaml
      ```
3. check status
    * ```shell
      argo -n business-workflows list
      ```
    * ```shell
      # argo -n business-workflows get build-and-push-container-image-2j5z2
      argo -n business-workflows get @lastest
      ```
    * ```shell
      # argo -n business-workflows logs build-and-push-container-image-2j5z2
      argo -n business-workflows logs @latest
      ```