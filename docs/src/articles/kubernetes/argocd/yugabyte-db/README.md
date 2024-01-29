# yugabyte-db

## prepare

1. k8s is ready
2. argocd is ready and logged in

## installation

1. prepare `yugabyte-db.yaml`
    * ```yaml
      <!-- @include: yugabyte-db.yaml -->
      ```
2. apply to k8s
    * ```shell
      kubectl -n argocd apply -f yugabyte-db.yaml
      ```
3. sync by argocd
    * ```shell
      argocd app sync argocd/yugabyte-db
      ```