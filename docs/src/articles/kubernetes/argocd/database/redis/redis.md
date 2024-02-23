# redis

## prepare

1. k8s is ready
2. argocd is ready and logged in

## installation

1. prepare `redis.yaml`
    * ```yaml
      <!-- @include: redis.yaml -->
      ```
2. prepare credentials secret
    * ```shell
      kubectl get namespaces database > /dev/null 2>&1 || kubectl create namespace database
      kubectl -n database create secret generic redis-credentials \
          --from-literal=redis-password=$(tr -dc A-Za-z0-9 </dev/urandom | head -c 16)
      ```
3. apply to k8s
    * ```shell
      kubectl -n argocd apply -f redis.yaml
      ```
4. sync by argocd
    * ```shell
      argocd app sync argocd/redis
      ```

## tests

1. prepare `redis-tool.yaml`
    + ```yaml
      <!-- @include: redis-tool.yaml -->
      ```
2. apply to k8s
    + ```shell
      kubectl -n database apply -f redis-tool.yaml
      ```
3. tests
    + ```shell
      kubectl -n database exec -it deployment/redis-tool -- \
          redis-cli -c -h redis-master.database ping
      ```
    + ```shell
      kubectl -n database exec -it deployment/redis-tool -- \
          redis-cli -c -h redis-master.database set mykey somevalue
      ```
    + ```shell
      kubectl -n database exec -it deployment/redis-tool -- \
          redis-cli -c -h redis-master.database get mykey
      ```
    + ```shell
      kubectl -n database exec -it deployment/redis-tool -- \
          redis-cli -c -h redis-master.database del mykey
      ```
    + ```shell
      kubectl -n database exec -it deployment/redis-tool -- \
          redis-cli -c -h redis-master.database get mykey
      ```
