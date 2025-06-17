# local path provisioner

## references

* https://github.com/rancher/local-path-provisioner
* https://github.com/rancher/local-path-provisioner/tree/master/deploy/chart/local-path-provisioner

## installation

1. prepare `local-path-provisioner.yaml`
    * ```yaml
      <!-- @include: local-path-provisioner.yaml -->
      ```
2. apply to k8s
    * ```shell
      kubectl -n argocd apply -f local-path-provisioner.yaml
      ```
3. sync by argocd
    * ```shell
      argocd app sync argocd/opt-local-path
      ```

## tests

1. prepare `busybox-pvc-test.yaml`
    * ```yaml
      <!-- @include: busybox-pvc-test.yaml -->
      ```
2. apply to k8s
    * ```shell
      kubectl -n default apply -f busybox-pvc-test.yaml
      ```
3. check log of the test job
    * ```shell
      kubectl -n default logs job/busybox-pvc-test-job
      ```
4. delete job and pvc
    * ```shell
      kubectl -n default delete job busybox-pvc-test-job
      kubectl -n default delete pvc opt-local-path-pvc
      ```
