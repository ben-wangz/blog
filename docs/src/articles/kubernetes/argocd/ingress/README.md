# ingress

## prepare

1. k8s is ready
2. argocd is ready and logged in

## installation

1. prepare `ingress-nginx.yaml`
    * ```yaml
      <!-- @include: ingress-nginx.yaml -->
      ```
2. apply to k8s
    * ```shell
      kubectl -n argocd apply -f ingress-nginx.yaml
      ```
3. sync by argocd
    * ```shell
      argocd app sync argocd/ingress-nginx
      ```