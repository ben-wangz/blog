# Ceph

## prepare

1. k8s is ready
2. argocd is ready and logged in
3. Storage devices for Ceph are prepared on cluster nodes (refer to ceph-cluster.yaml for configuration)

## installation

### install rook-ceph-operator

1. prepare `rook-ceph-operator.app.yaml`
    * ```yaml
       <!-- @include: rook-ceph-operator.app.yaml -->
      ```
2. prepare `ceph-cluster.app.yaml`
    * ```yaml
       <!-- @include: ceph-cluster.app.yaml -->
      ```
3. apply rook-ceph-operator to k8s
    * ```shell
      kubectl -n argocd apply -f rook-ceph-operator.app.yaml
      ```
4. sync by argocd
    * ```shell
      argocd app sync rook-ceph-operator
      ```

### create a ceph-cluster and a filesystem

1. create a ceph-cluster
    * prepare `ceph-cluster.yaml`
        + ```yaml
          <!-- @include: ceph-cluster.yaml -->
          ```
    * ```shell
      kubectl -n rook-ceph apply -f ceph-cluster.yaml
      ```
    * wait for all pods to be running:
        + ```shell
          kubectl -n rook-ceph get pods -w
          ```
2. create a filesystem
    * prepare `filesystem.yaml`
        + ```yaml
          <!-- @include: filesystem.yaml -->
          ```
    * ```shell
      kubectl -n rook-ceph apply -f filesystem.yaml
      ```
3. verify ceph status
    * prepare `toolbox.yaml`
        + ```yaml
          <!-- @include: toolbox.yaml -->
          ```
    * apply toolbox to k8s
        + ```shell
          kubectl -n rook-ceph apply -f toolbox.yaml
          ```
    * check Ceph cluster status:
        + ```shell
          kubectl -n rook-ceph exec -it deployment/rook-ceph-tools -- ceph status
          ```
### create a storage class to use the filesystem

1. prepare `storage-class.yaml`
    * ```yaml
       <!-- @include: storage-class.yaml -->
      ```
2. apply storage-class to k8s
    * ```shell
      kubectl apply -f storage-class.yaml
      ```
3. check storage class
    * ```shell
      kubectl get storageclass ceph-filesystem
      ```
