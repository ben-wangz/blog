# juicefs-csi

## references

* https://github.com/juicedata/juicefs-csi-driver
* https://github.com/juicedata/charts/tree/main/charts/juicefs-csi-driver

## prepare

* [minio](../../minio/README.md)
* [tidb](../../../database/tidb/README.md)

## installation

1. prepare namespace `storage`
    * ```shell
      kubectl get namespace storage > /dev/null 2>&1 || kubectl create namespace storage
      ```
2. prepare dashboard secret
    * ```shell
      kubectl -n storage create secret generic juicefs-dashboard-secret \
        --from-literal=username=admin \
        --from-literal=password=$(tr -dc A-Za-z0-9 </dev/urandom | head -c 16)
      ```
3. prepare `juicefs-csi.yaml`
    * ```yaml
      <!-- @include: juicefs-csi.yaml -->
      ```
4. apply to k8s
    * ```shell
      kubectl -n argocd apply -f juicefs-csi.yaml
      ```
5. sync by argocd
    * ```shell
      argocd app sync argocd/juicefs-csi
      ```
6. visit juice-fs-dashboard by broswer
    * `https://juice-fs-dashboard.dev.geekcity.tech`
    * credentials
        + ```shell
          # username
          kubectl -n storage get secret juicefs-dashboard-secret -o jsonpath='{.data.username}' | base64 -d && echo
          ```
        + ```shell
          # password
          kubectl -n storage get secret juicefs-dashboard-secret -o jsonpath='{.data.password}' | base64 -d && echo
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

## advanced topics

1. [monitor with prometheus and grafana](advanced/monitor-with-prometheus-and-grafana/README.md)
2. [configure mount options](advanced/configure-mount-options/README.md)
3. [benchmark](advanced/benchmark/README.md)
4. [sync](advanced/sync/README.md)
