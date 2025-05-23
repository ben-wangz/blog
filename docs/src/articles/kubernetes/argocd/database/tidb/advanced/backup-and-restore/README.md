# backup and restore

## reference

* https://docs.pingcap.com/zh/tidb-in-kubernetes/stable/backup-restore-cr
* https://github.com/pingcap/tidb-operator/blob/master/pkg/apis/pingcap/v1alpha1/types.go#L1912

## introduction


## backup and restore full database via pvc

1. prepare `backup-full-pvc.yaml`
    * ```yaml
      <!-- @include: backup-full-pvc.yaml -->
      ```
2. apply the job
    * ```shell
      kubectl -n tidb-cluster apply -f backup-full-pvc.yaml
      ```
