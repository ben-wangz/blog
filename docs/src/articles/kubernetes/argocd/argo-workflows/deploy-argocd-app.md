# deploy argocd app

## prepare

1. k8s is ready
2. argo workflows is ready

## demo

1. prepare `deploy-argocd-app.yaml`
    * ```yaml
      <!-- @include: deploy-argocd-app.yaml -->
      ```
2. submit with argo workflow client
    * ```shell
      argo -n business-workflows submit deploy-argocd-app.yaml
      ```
3. check status
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