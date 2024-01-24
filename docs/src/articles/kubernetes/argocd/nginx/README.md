# nginx

## prepare

1. k8s is ready
2. argocd is ready and logged in

## installation

1. prepare `nginx.yaml`
    * ```yaml
      <!-- @include: nginx.yaml -->
      ```
2. apply to k8s
    * ```shell
      kubectl -n argocd apply -f nginx.yaml
      ```
3. sync by argocd
    * ```shell
      argocd app sync argocd/nginx
      ```