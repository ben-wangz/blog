# rook cephfs

## main usage

* dynamically create and bind pv with pvc for general software

## conceptions

* what's rook
* what's cephfs

## practise

### pre-requirements

* [a k8s cluster created by kind](create.local.cluster.with.kind.md) have been read and practised
* k8s binary tools
    + kind
    + kubectl
    + helm
* [local static provisioner](local.static.provisioner.md) have been read and practised

### purpose

* combination of `local static provisioner` and `rook cephfs`
* providing consistent storage cluster which can be consumed by a storage class
* create a storage class in kubernetes to dynamically providing pvs
* created pvs can be used by maria-db installed by helm
    + storage class provided by `rook` will be consumed by maria-db

### do it

1. setup kubernetes cluster with one master and two workers by `kind`
    + prepare [kind.cluster.yaml](resources/rook-cephfs/kind.cluster.yaml.md)
        * we need three workers for setting the count of rook monitor count to 3
    + ```shell
      ./kind create cluster --config $(pwd)/kind.cluster.yaml
      ```
2. setup `local static provisioner` provide one pv from each node, and create a storage class named `rook-local-storage`
   which will only be used by `rook cluster`
    + prepare [local.static.provisioner.values.yaml](resources/rook-cephfs/local.static.provisioner.values.yaml.md)
    + installation
        * ```shell
          git clone --single-branch --branch v2.4.0 https://github.com/kubernetes-sigs/sig-storage-local-static-provisioner.git
          docker pull k8s.gcr.io/sig-storage/local-volume-provisioner:v2.4.0
          ./kind load docker-image k8s.gcr.io/sig-storage/local-volume-provisioner:v2.4.0
          ./helm install \
              --create-namespace --namespace storage \
              local-static-provisioner \
              $(pwd)/sig-storage-local-static-provisioner/helm/provisioner/ \
              --values $(pwd)/local.static.provisioner.values.yaml \
              --atomic
          ```
    + check pods ready
        * ```shell
          ./kubectl -n storage wait --for=condition=ready pod --all
          ```
    + mount one "virtual disk" into discovery directory at each worker node
        * we need 6 pvs: 3 for monitors and 3 for data
        * ```shell
          for WORKER in "kind-worker" "kind-worker2" "kind-worker3"
          do
              docker exec -it $WORKER \
                  bash -c '\
                      for VOLUME_INDEX in "01" "02"; do \
                          mkdir -p /data/virtual-disks/rook-cephfs/$(hostname)-volume-$VOLUME_INDEX \
                          && mkdir -p /data/local-static-provisioner/rook-cephfs/$(hostname)-volume-$VOLUME_INDEX \
                          && mount --bind /data/virtual-disks/rook-cephfs/$(hostname)-volume-$VOLUME_INDEX /data/local-static-provisioner/rook-cephfs/$(hostname)-volume-$VOLUME_INDEX \
                      ;done'
          done
          ```
    + check pvs created by `local static provisioner`
        * ```shell
          ./kubectl get pv
          ```
        * expected output is something like
            + ```text
              NAME                CAPACITY   ACCESS MODES   RECLAIM POLICY   STATUS      CLAIM   STORAGECLASS   REASON   AGE
              local-pv-140fc177   368Gi      RWO            Delete           Available           local-disks             3s
              local-pv-6fb33d11   368Gi      RWO            Delete           Available           local-disks             3s
              local-pv-a3bd362    368Gi      RWO            Delete           Available           local-disks             3s
              local-pv-ab80f249   368Gi      RWO            Delete           Available           local-disks             3s
              local-pv-b0b78bee   368Gi      RWO            Delete           Available           local-disks             3s
              local-pv-eb1d9042   368Gi      RWO            Delete           Available           local-disks             3s
              ```
3. install `rook cephfs operator` by helm
    * prepare [values.yaml](resources/rook-cephfs/values.yaml.md)
    * ```shell
      docker pull rook/ceph:v1.7.3
      ./kind load docker-image rook/ceph:v1.7.3
      ./helm install \
          --create-namespace --namespace rook-ceph \
          my-rook-ceph-operator \
          rook-ceph \
          --repo https://charts.rook.io/release \
          --version 1.7.3 \
          --values values.yaml \
          --atomic
      ```
4. install `rook cluster`
    * prepare [cluster-on-pvc.yaml](resources/rook-cephfs/cluster-on-pvc.yaml.md)
        + full configuration can be found
          at [github](https://github.com/rook/rook/blob/v1.7.3/cluster/examples/kubernetes/ceph/cluster-on-pvc.yaml)
    * apply to k8s cluster
        + ```shell
          for IMAGE in "quay.io/ceph/ceph:v16.2.5" \
              "k8s.gcr.io/sig-storage/csi-attacher:v3.2.1" \
              "k8s.gcr.io/sig-storage/csi-node-driver-registrar:v2.2.0" \
              "k8s.gcr.io/sig-storage/csi-provisioner:v2.2.2" \
              "k8s.gcr.io/sig-storage/csi-resizer:v1.2.0" \
              "k8s.gcr.io/sig-storage/csi-snapshotter:v4.1.1" \
              "quay.io/ceph/ceph:v16.2.5" \
              "quay.io/cephcsi/cephcsi:v3.4.0"
          do
              docker pull $IMAGE
              ./kind load docker-image $IMAGE
          done
          ./kubectl -n rook-ceph apply -f cluster-on-pvc.yaml
          ```
5. install maria-db by helm
    * prepare [values.maria.db.yaml](resources/rook-cephfs/maria.db.values.yaml.md)
    * helm install maria-db
        + ```shell
          ./helm install \
              --create-namespace --namespace database \
              maria-db-test \
              mariadb \
              --version 9.5.1 \
              --repo https://charts.bitnami.com/bitnami \
              --values maria.db.values.yaml \
              --atomic \
              --timeout 600s
          ```
6. connect to maria-db and check the provisioner of `rook`
7. clean up
    * uninstall maria-db by helm
    * uninstall `rook cluster`
    * uninstall `rook cephfs operator` by helm
    * uninstall kubernetes cluster
        + ```shell
          ./kind delete cluster
          ```
