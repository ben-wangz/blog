# publish container image

## prepare

1. argo workflows is ready
2. the storage class named `local-disks` is ready

## demo

1. prepare `publish-container-image.yaml`
    * ```yaml
      <!-- @include: publish-container-image.yaml -->
      ```
2. prepare `docker-login-credentials` secret
    * ```shell
      kubectl -n business-workflows create secret generic docker-login-credentials \
        --from-literal="username=${DOCKER_LOGIN_USERNAME:-wangz2019}" \
        --from-literal="password=${DOCKER_LOGIN_PASSWORD}"
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