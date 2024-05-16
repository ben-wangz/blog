# mirror container image

## prepare

1. argo workflows is ready

## demo

1. prepare `registry-credentials` secret
    * ```shell
      #REGISTRY_INSECURE=false
      #REGISTRY=REGISTRY
      #REGISTRY_USERNAME=your-username
      #REGISTRY_PASSWORD=your-password
      kubectl -n business-workflows create secret generic registry-credentials \
        --from-literal="insecure=${REGISTRY_INSECURE:-false}" \
        --from-literal="registry=${REGISTRY}" \
        --from-literal="username=${REGISTRY_USERNAME}" \
        --from-literal="password=${REGISTRY_PASSWORD}"
      ```
2. prepare `mirror-container-image.yaml`
    * ```yaml
      <!-- @include: mirror-container-image.yaml -->
      ```
3. submit with argo workflow client
    * ```shell
      argo -n business-workflows submit mirror-container-image.yaml
      ```
4. check status
    * ```shell
      argo -n business-workflows list
      ```
    * ```shell
      # argo -n business-workflows get mirror-container-image-2j5z2
      argo -n business-workflows get @lastest
      ```
    * ```shell
      # argo -n business-workflows logs mirror-container-image-2j5z2
      argo -n business-workflows logs @latest
      ```