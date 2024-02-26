# clickhouse

## prepare

1. k8s is ready
2. argocd is ready and logged in
3. cert-manager is ready and the clusterissuer named `self-signed-ca-issuer` is ready

## installation

1. prepare `clickhouse.yaml`
    * ```yaml
      <!-- @include: clickhouse.yaml -->
      ```
2. prepare admin credentials secret
    * ```shell
      kubectl get namespaces database > /dev/null 2>&1 || kubectl create namespace database
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
5. expose postgresql interface
    1. prepare `postgresql-interface.yaml`
        * ```yaml
          <!-- @include: postgresql-interface.yaml -->
          ```
    2. apply to k8s
        * ```shell
          kubectl -n database apply -f postgresql-interface.yaml
          ```
6. expose mysql interface
    1. prepare `mysql-interface.yaml`
        * ```yaml
          <!-- @include: mysql-interface.yaml -->
          ```
    2. apply to k8s
        * ```shell
          kubectl -n database apply -f mysql-interface.yaml
          ```

## tests

1. extract clickhouse admin credentials
    * ```shell
      PASSWORD=$(kubectl -n database get secret clickhouse-admin-credentials -o jsonpath='{.data.password}' | base64 -d)
      ```
2. with http
    * clickhouse.dev.geekcity.tech should be resolved to nginx-ingress
        + for example, add `$K8S_MASTER_IP clickhouse.dev.geekcity.tech` to `/etc/hosts`
    * ```shell
      echo 'SELECT version()' | curl -k "https://admin:${PASSWORD}@clickhouse.dev.geekcity.tech:32443/" --data-binary @-
      ```
3. with postgresql client
    * ```shell
      podman run --rm \
          --env PGPASSWORD=${PASSWORD} \
          --entrypoint psql \
          -it docker.io/library/postgres:15.2-alpine3.17 \
          --host host.containers.internal \
          --port 32005 \
          --username admin \
          --dbname default \
          --command 'select version()'
      ```
4. with mysql client
    * ```shell
      podman run --rm \
          -e MYSQL_PWD=${PASSWORD} \
          -it docker.io/library/mariadb:11.2.2-jammy \
          mariadb \
          --host host.containers.internal \
          --port 32004 \
          --user admin \
          --database default \
          --execute 'select version()'
      ```