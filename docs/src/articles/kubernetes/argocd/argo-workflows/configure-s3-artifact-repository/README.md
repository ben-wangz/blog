# configure-s3-artifact-repository

## prepare

1. argo workflows is ready
2. minio is ready for artifact repository
    * endpoint: minio.storage:9000

## configuration

1. prepare bucket for s3 artifact repository
    * ```shell
      K8S_MASTER_IP=$(kubectl get node -l node-role.kubernetes.io/control-plane -o jsonpath='{.items[0].status.addresses[?(@.type=="InternalIP")].address}')
      ACCESS_SECRET=$(kubectl -n storage get secret minio-credentials -o jsonpath='{.data.rootPassword}' | base64 -d)
      podman run --rm \
          --entrypoint bash \
          --add-host=minio-api.dev.geekcity.tech:${K8S_MASTER_IP} \
          -it docker.io/minio/mc:latest \
          -c "mc alias set minio http://minio-api.dev.geekcity.tech:32080 admin ${ACCESS_SECRET} \
              && mc ls minio \
              && mc mb --ignore-existing minio/argo-workflows-artifacts"
      ```
2. prepare secret `s3-artifact-repository-credentials` which stores s3 access-key and secret-key
    * ```shell
      ACCESS_KEY=$(kubectl -n storage get secret minio-credentials -o jsonpath='{.data.rootUser}' | base64 -d)
      SECRET_KEY=$(kubectl -n storage get secret minio-credentials -o jsonpath='{.data.rootPassword}' | base64 -d)
      kubectl -n business-workflows create secret generic s3-artifact-repository-credentials \
          --from-literal=accessKey=${ACCESS_KEY} \
          --from-literal=secretKey=${SECRET_KEY}
      ```
3. prepare `artifact-repositories.yaml` and apply it to k8s
    * ```yaml
      <!-- @include: artifact-repositories.yaml -->
      ```
    * ```shell
      kubectl -n business-workflows apply -f artifact-repositories.yaml
      ```
