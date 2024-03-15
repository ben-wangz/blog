# blog

## prepare

1. k8s is ready
2. argocd is ready and logged in
3. ingress is ready
4. cert-manager is ready
    * the clusterissuer named `alidns-webhook-letsencrypt` is ready

## installation

1. prepare `blog.yaml`
    * ```yaml
      <!-- @include: blog.yaml -->
      ```
2. apply to k8s
    * ```shell
      kubectl -n argocd apply -f blog.yaml
      ```
3. sync by argocd
    * ```shell
      argocd app sync argocd/blog
      ```