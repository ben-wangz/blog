# postgresql

## prepare

1. k8s is ready
2. argocd is ready and logged in
3. ingress is ready
    * only required by `pgadmin4` in the tests
4. cert-manager is ready
    * the clusterissuer named `self-signed-ca-issuer` is ready
    * only required by `pgadmin4` in the tests

## installation

1. prepare `postgresql.yaml`
    * ```yaml
      <!-- @include: postgresql.yaml -->
      ```
2. prepare credentials secret
    * ```shell
      kubectl get namespaces database > /dev/null 2>&1 || kubectl create namespace database
      kubectl -n database create secret generic postgresql-credentials \
          --from-literal=postgres-password=$(tr -dc A-Za-z0-9 </dev/urandom | head -c 16) \
          --from-literal=password=$(tr -dc A-Za-z0-9 </dev/urandom | head -c 16) \
          --from-literal=replication-password=$(tr -dc A-Za-z0-9 </dev/urandom | head -c 16)
      ```
3. apply to k8s
    * ```shell
      kubectl -n argocd apply -f postgresql.yaml
      ```
4. sync by argocd
    * ```shell
      argocd app sync argocd/postgresql
      ```
5. expose interface
    1. prepare `postgresql-expose.yaml`
        * ```yaml
          <!-- @include: postgresql-expose.yaml -->
          ```
    2. apply to k8s
        * ```shell
          kubectl -n database apply -f postgresql-expose.yaml
          ```

## tests

1. with root user
    * ```shell
      POSTGRES_PASSWORD=$(kubectl -n database get secret postgresql-credentials -o jsonpath='{.data.postgres-password}' | base64 -d)
      podman run --rm \
          --env PGPASSWORD=${POSTGRES_PASSWORD} \
          --entrypoint psql \
          -it docker.io/library/postgres:15.2-alpine3.17 \
          --host host.containers.internal \
          --port 32543 \
          --username postgres \
          --dbname postgres \
          --command 'SELECT datname FROM pg_database;'
      ```
2. with normal user
    * ```shell
      POSTGRES_PASSWORD=$(kubectl -n database get secret postgresql-credentials -o jsonpath='{.data.password}' | base64 -d)
      podman run --rm \
          --env PGPASSWORD=${POSTGRES_PASSWORD} \
          --entrypoint psql \
          -it docker.io/library/postgres:15.2-alpine3.17 \
          --host host.containers.internal \
          --port 32543 \
          --username ben.wangz \
          --dbname geekcity \
          --command 'SELECT datname FROM pg_database;'
      ```
## test with pgadmin4

1. prepare `pgadmin4.yaml`
    * ```yaml
      <!-- @include: pgadmin4.yaml -->
      ```
2. prepare credentials secret
    * ```shell
      kubectl -n database create secret generic pgadmin4-credentials \
          --from-literal=password=$(tr -dc A-Za-z0-9 </dev/urandom | head -c 16)
      ```
3. apply to k8s
    * ```shell
      kubectl -n argocd apply -f pgadmin4.yaml
      ```
4. sync by argocd
    * ```shell
      argocd app sync argocd/pgadmin4
      ```
5. open with browser: https://pgadmin4.dev.geekcity.tech:32443
    * pgadmin4.dev.geekcity.tech should be resolved to nginx-ingress
        + for example, add `$K8S_MASTER_IP pgadmin4.dev.geekcity.tech` to `/etc/hosts`
6. login
    * email: `pgadmin@mail.geekcity.tech`
    * password
        + ```shell
          kubectl -n database get secret pgadmin4-credentials -o jsonpath='{.data.password}' | base64 -d
          ```
    * connecting to postgresql database
        + host: `postgresql.database`
        + port: 5432
        + username: `postgres`
            * password
                * ```shell
                  kubectl -n database get secret postgresql-credentials -o jsonpath='{.data.postgres-password}' | base64 -d
                  ```
        + username: `ben.wangz`
            + password
                * ```shell
                  kubectl -n database get secret postgresql-credentials -o jsonpath='{.data.password}' | base64 -d
                  ```
