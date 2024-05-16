# mirror container image

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
5. prepare `mirror-container-image.yaml`
    * ```yaml
      <!-- @include: mirror-container-image.yaml -->
      ```
6. submit with argo workflow client
    * ```shell
      argo -n business-workflows submit mirror-container-image.yaml
      ```
7. check status
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