# clickhouse

## prepare

1. k8s is ready
2. argocd is ready and logged in

## installation

1. prepare `clickhouse.yaml`
    * ```yaml
      <!-- @include: clickhouse.yaml -->
      ```
2. apply to k8s
    * ```shell
      kubectl -n argocd apply -f clickhouse.yaml
      ```
3. sync by argocd
    * ```shell
      argocd app sync argocd/clickhouse
      ```