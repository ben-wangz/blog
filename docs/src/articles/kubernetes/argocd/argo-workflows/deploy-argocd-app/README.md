# deploy argocd app

## prepare

1. argo workflows is ready
2. minio is ready for artifact repository
    * endpoint: minio.storage:9000

## demo

1. [configure s3 artifact repository](../configure-s3-artifact-repository/README.md)
2. prepare secret `argocd-login-credentials` which stores argocd username and password
    * ```shell
      ARGOCD_USERNAME=admin
      # change ARGOCD_PASSWORD to your argocd password
      ARGOCD_PASSWORD=$(kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" | base64 -d)
      kubectl -n business-workflows create secret generic argocd-login-credentials \
          --from-literal=username=${ARGOCD_USERNAME} \
          --from-literal=password=${ARGOCD_PASSWORD}
      ```
3. prepare `deploy-argocd-app-rbac.yaml` and apply it to k8s
    <!-- may be a bug: cannot import the whole file of deploy-argocd-app-rbac.yaml -->
    * ```yaml
      <!-- @include: deploy-argocd-app-rbac.yaml{1-12} -->
      <!-- @include: deploy-argocd-app-rbac.yaml{13-} -->
      ```
    * ```shell
      kubectl -n argocd apply -f deploy-argocd-app-rbac.yaml
      ```
4. prepare `deploy-argocd-app.yaml`
    * ```yaml
      <!-- @include: deploy-argocd-app.yaml -->
      ```
5. submit with argo workflow client
    * ```shell
      argo -n business-workflows submit deploy-argocd-app.yaml
      ```
6. check status
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
