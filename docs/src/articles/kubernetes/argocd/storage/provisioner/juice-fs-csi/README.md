# juicefs-csi

## references

* https://github.com/juicedata/juicefs-csi-driver
* https://github.com/juicedata/charts/tree/main/charts/juicefs-csi-driver

## prepare

* [minio](../../minio/README.md)
* [tidb](../../../database/tidb/README.md)

## installation

1. prepare `juicefs-csi.yaml`
    * ```yaml
      <!-- @include: juicefs-csi.yaml -->
      ```
2. apply to k8s
    * ```shell
      kubectl -n argocd apply -f juicefs-csi.yaml
      ```
3. sync by argocd
    * ```shell
      argocd app sync argocd/juicefs-csi
      ```

## pvc

1. create a secret to store juice fs credentials
    * ```shell
      MINIO_ACCESS_KEY=$(kubectl -n storage get secret minio-credentials -o jsonpath='{.data.rootUser}' | base64 -d)
      MINIO_SECRET_KEY=$(kubectl -n storage get secret minio-credentials -o jsonpath='{.data.rootPassword}' | base64 -d)
      kubectl -n storage create secret generic juice-fs-tidb-minio-credential \
        --from-literal=name=juice-fs-tidb-minio \
        --from-literal=metaurl=tikv://basic-pd.tidb-cluster:2379/juice-fs-tidb-minio \
        --from-literal=storage=minio \
        --from-literal=bucket=http://minio-headless.storage:9000/juice-fs-tidb-minio \
        --from-literal=access-key=${MINIO_ACCESS_KEY} \
        --from-literal=secret-key=${MINIO_SECRET_KEY}
      kubectl -n storage patch secret juice-fs-tidb-minio-credential -p '{"metadata":{"labels":{"juicefs.com/validate-secret":"true"}}}'
      ```
2. create pvc and test with a deployment
    * prepare `busybox-juice-fs-pvc-test.yaml`
        + ```yaml
          <!-- @include: busybox-juice-fs-pvc-test.yaml -->
          ```
    * apply to k8s
        + ```shell
          kubectl -n default apply -f busybox-juice-fs-pvc-test.yaml
          ```

## storage class

1. create storage class
    * prepare `juice-fs-tidb-minio-test.storageclass.yaml`
        + ```yaml
          <!-- @include: juice-fs-tidb-minio-test.storageclass.yaml -->
          ```
    * apply to k8s
        + ```shell
          kubectl -n default apply -f juice-fs-tidb-minio-test.storageclass.yaml
          ```
2. create pvc and test with a deployment
    * prepare `busybox-storage-class-test.yaml`
        + ```yaml
          <!-- @include: busybox-storage-class-test.yaml -->
          ```
    * apply to k8s
        + ```shell
          kubectl -n default apply -f busybox-storage-class-test.yaml
          ```
