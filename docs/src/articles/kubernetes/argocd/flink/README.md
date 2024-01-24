# flink-operator

## prepare

1. k8s is ready
2. argocd is ready and logged in
3. cert-manager is ready
4. nginx-ingress is ready

## installation

1. prepare `flink-operator.yaml`
    * ```yaml
      <!-- @include: flink-operator.yaml -->
      ```
2. apply to k8s
    * ```shell
      kubectl -n argocd apply -f flink-operator.yaml
      ```
3. sync by argocd
    * ```shell
      argocd app sync argocd/flink-operator
      ```

## deploy flink application

<!-- TODO -->