# backup and restore

## reference

* https://docs.pingcap.com/zh/tidb-in-kubernetes/stable/backup-restore-cr
* https://github.com/pingcap/tidb-operator/blob/master/pkg/apis/pingcap/v1alpha1/types.go#L1912
* https://asktug.com/t/topic/1042360

## introduction

## prepare

0. optional: only for local storage
1. requirements
    * for backup by br, `local://` protocol requires a local path in tikv to store the backup files
    * therefore, we need to create a pvc to mount the local path in tikv
2. prepare `backup.pvc.yaml`
    * ```yaml
      <!-- @include: backup.pvc.yaml -->
      ```
    * apply to the cluster
        + ```shell
          kubectl -n tidb-cluster apply -f backup.pvc.yaml
          ```
3. patch the tidb cluster
    * ```shell
      kubectl -n tidb-cluster patch tidbcluster basic --type=merge -p '{"spec":{"tikv":{"additionalVolumes":[{"name":"backup","persistentVolumeClaim":{"claimName":"tidb-cluster-backup-pvc"}}],"additionalVolumeMounts":[{"name":"backup","mountPath":"/backup"}]}}}'
      ```
    * check and waiting for the cluster status to be ready
        + ```shell
          kubectl -n tidb-cluster get tidbcluster basic -w
          ```

## backup and restore full database via pvc

1. apply the job
    * prepare `backup-full-pvc.yaml`
        + ```yaml
          <!-- @include: backup-full-pvc.yaml -->
          ```
    * apply to the cluster
        + ```shell
          kubectl -n tidb-cluster apply -f backup-full-pvc.yaml
          ```
2. delete database
    * prepare `delete-database.job.yaml`
        + ```yaml
          <!-- @include: delete-database.job.yaml -->
          ```
    * apply to the cluster
        + ```shell
          kubectl -n tidb-cluster apply -f delete-database.job.yaml
          kubectl -n tidb-cluster wait --for=condition=complete job/mysql-delete-database-job
          kubectl -n tidb-cluster logs -l job-name=mysql-delete-database-job
          ```
3. restore the database
    * prepare `restore-full-pvc.yaml`
        + ```yaml
          <!-- @include: restore-full-pvc.yaml -->
          ```
    * apply to the cluster
        + ```shell
          kubectl -n tidb-cluster apply -f restore-full-pvc.yaml
          ```

## backup and resstore specific db via pvc

## backup and restore specific table via pvc

## backup and restore specific table via s3
