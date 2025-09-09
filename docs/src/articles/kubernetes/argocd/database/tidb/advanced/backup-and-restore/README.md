# backup and restore

## reference

* https://docs.pingcap.com/zh/tidb-in-kubernetes/stable/backup-restore-cr
* https://github.com/pingcap/tidb-operator/blob/master/pkg/apis/pingcap/v1alpha1/types.go#L1912
* https://asktug.com/t/topic/1042360

## introduction

* two types of backup
    * full backup: backup full data of a cluster at a specific point in time
    * log backup(log refers to KV changes in TiKV): backup data changes in TiDB
* restore
    * restore the full backup
    * restore the target cluster to any specific point in time of the source backup cluster by integrating full backup with log backup, a capability formally referred to as Point-in-Time Recovery (PITR)

## prepare

0. optional: only for local storage
1. requirements
    * for backup/restore by br, `local://` protocol requires a local path in tikv to store the backup/restore files
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
4. perpare default service account `tidb-backup-manager`
    * ```yaml
      <!-- @include: tidb-backup-manager.serviceaccount.yaml -->
      ```

## backup and restore full database via pvc

1. apply backup
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

## backup and resstore specific database/table via pvc

## backup and restore specific table via s3

## backup schedule and restore full database via pvc

1. apply backup schedule
    * prepare `backup-schedule-full-pvc.yaml`
        + ```yaml
          <!-- @include: backup-schedule-full-pvc.yaml -->
          ```
    * apply to the cluster
        + ```shell
          kubectl -n tidb-cluster apply -f backup-schedule-full-pvc.yaml
          ```
2. pause the backup schedule
3. delete database
4. restore the database

## backup log and PITR (Point-in-time recovery)
