# rook ceph

## main usage

* create a storage class for providing pv dynamically
    + bare metal machines
    + disk provided with LVM

## conceptions

* LVM
* local static provisioner
* rook

## practise

### pre-requirements

* [a k8s cluster created by kind](../create.local.cluster.with.kind.md) have been read and practised
* k8s binary tools
    + kubectl
    + helm
* [local static provisioner](../storage/local.static.provisioner.md) have been read and practised
* [rook ceph](../storage/rook.ceph.md) have been read and practised

### purpose

* create LVs by LVM
* create local storage provisioner by helm
    + one for rook monitor
    + one for rook data
* create rook ceph operator and rook ceph cluster
    + create a storage class for dynamically providing pvs
* create maria-db by helm to consume the storage provided by rook cluster
* check rook dashboard

### do it

1. create LVs in each worker node in kubernetes cluster
    + ```shell
      lvcreate -L 10G -n rook01 centos && mkfs.xfs /dev/mapper/centos-rook01
      mkdir -p /data/local-static-provisioner/rook-ceph/monitor/rook01
      echo /dev/mapper/centos-rook01 /data/local-static-provisioner/rook-ceph/monitor/rook01      xfs     defaults        0 0 >> /etc/fstab
      mount -a && df -h
      lvcreate -L 100G -n rook02 centos
      mkdir -p /data/local-static-provisioner/rook-ceph/data
      ln -s /dev/mapper/centos-rook02 /data/local-static-provisioner/rook-ceph/data/$(hostname)-rook02
      ```
2. create local storage for rook monitor
    + storage class is `rook-monitor`
    + [local.rook.monitor.values.yaml](../resources/storage-for-bare-metal-machine/local.rook.monitor.values.yaml.md)
    + ```shell
      git clone --single-branch --branch v2.4.0 https://github.com/kubernetes-sigs/sig-storage-local-static-provisioner.git
      ./helm install \
          --create-namespace --namespace local-disk \
          disk-rook-monitor \
          $(pwd)/sig-storage-local-static-provisioner/helm/provisioner/ \
          --values $(pwd)/local.rook.monitor.values.yaml \
          --atomic
      ```
3. create local storage for rook data
    + storage class is `rook-data`
    + [local.rook.data.values.yaml](../resources/storage-for-bare-metal-machine/local.rook.data.values.yaml.md)
    + ```shell
      # git clone --single-branch --branch v2.4.0 https://github.com/kubernetes-sigs/sig-storage-local-static-provisioner.git
      ./helm install \
          --create-namespace --namespace local-disk \
          disk-rook-data \
          $(pwd)/sig-storage-local-static-provisioner/helm/provisioner/ \
          --values $(pwd)/local.rook.data.values.yaml \
          --atomic
      ```
4. create rook operator by helm
    + [rook.ceph.operator.values.yaml](../resources/storage-for-bare-metal-machine/rook.ceph.operator.values.yaml.md)
    + ```shell
      ./helm install \
          --create-namespace --namespace rook-ceph \
          rook-ceph \
          https://resource.geekcity.tech/kubernetes/charts/https/charts.rook.io/release/rook-ceph-v1.7.3.tgz \
          --values rook.ceph.operator.values.yaml \
          --atomic
      ```
5. create rook cluster
    + [rook-cluster-on-pvc.yaml](../resources/storage-for-bare-metal-machine/rook-cluster-on-pvc.yaml.md)
    + ```shell
      ./kubectl -n rook-ceph apply -f rook-cluster-on-pvc.yaml
    ```