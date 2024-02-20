# clickhouse

## prepare

1. k8s is ready
2. argocd is ready and logged in

## installation

1. prepare `clickhouse.yaml`
    * ```yaml
      <!-- @include: clickhouse.yaml -->
      ```
2. prepare admin credentials secret
    * ```shell
      kubectl -n database create secret generic clickhouse-admin-credentials \
          --from-literal=password=$(tr -dc A-Za-z0-9 </dev/urandom | head -c 16)
      ```
3. apply to k8s
    * ```shell
      kubectl -n argocd apply -f clickhouse.yaml
      ```
4. sync by argocd
    * ```shell
      argocd app sync argocd/clickhouse
      ```

## tests

1. extract clickhouse admin credentials
    * ```shell
      kubectl -n database get secret clickhouse-admin-credentials -o jsonpath='{.data.password}' | base64 -d && echo ""
      ```
2. with http
    * ```shell
      PASSWORD=$(kubectl -n database get secret clickhouse-admin-credentials -o jsonpath='{.data.password}' | base64 -d)
      echo 'SELECT version()' | curl -k "https://admin:${PASSWORD}@clickhouse.dev.geekcity.tech:32443/" --data-binary @-
      ```