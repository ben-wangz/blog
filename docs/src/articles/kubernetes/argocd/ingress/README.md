# ingress

## prepare

1. k8s is ready
2. argocd is ready and logged in

## installation

1. prepare `ingress-nginx.yaml`
    * ```yaml
      <!-- @include: @src/articles/kubernetes/argocd/ingress/ingress-nginx.yaml -->
      ```
2. sync by argocd
    * ```shell
      argocd app sync argocd/ingress-nginx
      ```