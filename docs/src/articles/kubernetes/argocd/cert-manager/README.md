# cert-manager

## prepare

1. k8s is ready
2. argocd is ready and logged in

## installation

1. prepare `cert-manager.yaml`
    * ```yaml
      <!-- @include: @src/articles/kubernetes/argocd/cert-manager/cert-manager.yaml -->
      ```
2. apply `cert-manager.yaml` to k8s
    * ```shell
      kubectl -n argocd apply -f cert-manager.yaml
      ```
3. sync by argocd
    * ```shell
      argocd app sync argocd/cert-manager
      ```