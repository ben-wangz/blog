# deploy argocd app

## prepare

1. k8s is ready
2. argo workflows is ready
3. minio is ready for artifact repository
    * endpoint: minio.storage:9000

## demo

1. prepare bucket for s3 artifact repository
    * ```shell
      # change K8S_MASTER_IP to your k8s master ip
      K8S_MASTER_IP=192.168.1.107
      ACCESS_SECRET=$(kubectl -n storage get secret minio-secret -o jsonpath='{.data.rootPassword}' | base64 -d)
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
      ACCESS_KEY=$(kubectl -n storage get secret minio-secret -o jsonpath='{.data.rootUser}' | base64 -d)
      SECRET_KEY=$(kubectl -n storage get secret minio-secret -o jsonpath='{.data.rootPassword}' | base64 -d)
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
4. prepare secret `argocd-login-credentials` which stores argocd username and password
    * ```shell
      ARGOCD_USERNAME=admin
      # change ARGOCD_PASSWORD to your argocd password
      ARGOCD_PASSWORD=$(kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" | base64 -d)
      kubectl -n business-workflows create secret generic argocd-login-credentials \
          --from-literal=username=${ARGOCD_USERNAME} \
          --from-literal=password=${ARGOCD_PASSWORD}
      ```
5. prepare `deploy-argocd-app.yaml`
    * ```yaml
      <!-- @include: deploy-argocd-app.yaml -->
      ```
6. submit with argo workflow client
    * ```shell
      argo -n business-workflows submit deploy-argocd-app.yaml
      ```
7. check status
    * ```shell
      argo -n business-workflows list
      ```
    * ```shell
      # argo -n business-workflows get deploy-argocd-app-2j5z2
      argo -n business-workflows get @lastest
      ```
    * ```shell
      # argo -n business-workflows logs deploy-argocd-app-2j5z2
      argo -n business-workflows logs @latest
      ```
