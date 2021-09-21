# rook ceph

## main usage

* dynamically create and bind pv with pvc for general software

## conceptions

* what's rook
* what's ceph

## practise

### pre-requirements

* [a k8s cluster created by kind](create.local.cluster.with.kind.md) have been read and practised
* [download kubernetes binary tools](download.kubernetes.binary.tools.md)
    + kind
    + kubectl
    + helm
* [local static provisioner](local.static.provisioner.md) have been read and practised
* we recommend to use [qemu machine](../qemu/README.md) because we will modify the devices: /dev/loopX

### purpose

* combination of `local static provisioner` and `rook ceph`
* providing consistent storage cluster which can be consumed by a storage class
* create a storage class in kubernetes to dynamically providing pvs
* created pvs can be used by maria-db installed by helm
    + storage class provided by `rook` will be consumed by maria-db

### do it

1. optional, [create centos 8 with qemu](../qemu/create.centos.8.with.qemu.md)
    * ```shell
      qemu-system-x86_64 \
              -accel kvm \
              -cpu kvm64 -smp cpus=2 \
              -m 4G \
              -drive file=$(pwd)/centos.8.qcow2,if=virtio,index=0,media=disk,format=qcow2 \
              -rtc base=localtime \
              -pidfile $(pwd)/centos.8.qcow2.pid \
              -display none \
              -nic user,hostfwd=tcp::1022-:22 \
              -daemonize
      ssh -o "UserKnownHostsFile /dev/null" -p 1022 root@localhost dnf -y install tar git vim
      ```
    * login with ssh
        + ```shell
          ssh -o "UserKnownHostsFile /dev/null" -p 1022 root@localhost
          ```
    * [install docker engine](../docker/installation.md)
2. download kind, kubectl and helm binaries according
   to [download kubernetes binary tools](download.kubernetes.binary.tools.md)
3. setup kubernetes cluster with one master and two workers by `kind`
    + prepare [kind.cluster.yaml](resources/rook-ceph/kind.cluster.yaml.md)
        * we need three workers for setting the count of rook monitor count to 3
    + ```shell
      ./kind create cluster --config $(pwd)/kind.cluster.yaml
      ```
4. mount one "virtual disk" into discovery directory at each worker node
    * we need 6 pvs: 3 for monitors and 3 for data
    * ```shell
        for WORKER in "kind-worker" "kind-worker2" "kind-worker3"
        do
            docker exec -it $WORKER bash -c '\
                set -x && HOSTNAME=$(hostname) \
                    && mkdir -p /data/virtual-disks/monitor/$HOSTNAME-volume-1 \
                    && mkdir -p /data/local-static-provisioner/rook-monitor/$HOSTNAME-volume-1 \
                    && mount --bind /data/virtual-disks/monitor/$HOSTNAME-volume-1 /data/local-static-provisioner/rook-monitor/$HOSTNAME-volume-1 \
            '
            # TODO choose the rang 0..3 according to yor environment
            docker exec -it $WORKER bash -c '\
                set -x && set -e
                for i in {0..3}; do 
                    if [ -e /dev/loop$i ]; then 
                        continue
                    fi
                    mknod /dev/loop$i b 7 $i
                    chown root:disk /dev/loop$i
                    chmod 660 /dev/loop$i
                done \
            '
            docker exec -it $WORKER bash -c '\
                set -x && HOSTNAME=$(hostname) \
                    && mkdir -p /data/virtual-disks/data \
                    && mkdir -p /data/local-static-provisioner/rook-data/ \
                    && dd if=/dev/zero of=/data/virtual-disks/data/$HOSTNAME-volume-1 bs=1M count=1024 \
            '
        done
        docker exec -it kind-worker bash -c '\
            set -x && HOSTNAME=$(hostname)\
                && losetup -fP /data/virtual-disks/data/$HOSTNAME-volume-1 \
                && ln -s /dev/loop1 /data/local-static-provisioner/rook-data/$HOSTNAME-volume-1\
        '
        docker exec -it kind-worker2 bash -c '\
            set -x && HOSTNAME=$(hostname)\
                && losetup -fP /data/virtual-disks/data/$HOSTNAME-volume-1 \
                && ln -s /dev/loop2 /data/local-static-provisioner/rook-data/$HOSTNAME-volume-1\
        '
        docker exec -it kind-worker3 bash -c '\
            set -x && HOSTNAME=$(hostname)\
                && losetup -fP /data/virtual-disks/data/$HOSTNAME-volume-1 \
                && ln -s /dev/loop3 /data/local-static-provisioner/rook-data/$HOSTNAME-volume-1\
        '
        ```
5. setup `local static provisioner` provide one pv from each node, and create a storage class named `rook-local-storage`
   which will only be used by `rook cluster`
    + prepare [local.rook.monitor.values.yaml](resources/rook-ceph/local.rook.monitor.values.yaml.md)
    + prepare [local.rook.data.values.yaml](resources/rook-ceph/local.rook.data.values.yaml.md)
    + installation
        * ```shell
          git clone --single-branch --branch v2.4.0 https://github.com/kubernetes-sigs/sig-storage-local-static-provisioner.git
          docker pull k8s.gcr.io/sig-storage/local-volume-provisioner:v2.4.0
          ./kind load docker-image k8s.gcr.io/sig-storage/local-volume-provisioner:v2.4.0
          ./helm install \
              --create-namespace --namespace storage \
              local-rook-monitor \
              $(pwd)/sig-storage-local-static-provisioner/helm/provisioner/ \
              --values $(pwd)/local.rook.monitor.values.yaml \
              --atomic
          ./helm install \
              --create-namespace --namespace storage \
              local-rook-data \
              $(pwd)/sig-storage-local-static-provisioner/helm/provisioner/ \
              --values $(pwd)/local.rook.data.values.yaml \
              --atomic
          ```
    + check pods ready
        * ```shell
          ./kubectl -n storage wait --for=condition=ready pod --all
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
6. install `rook ceph operator` by helm
    * prepare [values.yaml](resources/rook-ceph/values.yaml.md)
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
7. install `rook cluster`
    * prepare [cluster-on-pvc.yaml](resources/rook-ceph/cluster-on-pvc.yaml.md)
        + full configuration can be found
          at [github](https://github.com/rook/rook/blob/v1.7.3/cluster/examples/kubernetes/ceph/cluster-on-pvc.yaml)
    * apply to k8s cluster
        + ```shell
          for IMAGE in  "k8s.gcr.io/sig-storage/csi-attacher:v3.2.1" \
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
8. install maria-db by helm
    * prepare [values.maria.db.yaml](resources/rook-ceph/maria.db.values.yaml.md)
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
9. connect to maria-db and check the provisioner of `rook`
10. clean up
    * uninstall maria-db by helm
    * uninstall `rook cluster`
    * uninstall `rook ceph operator` by helm
    * uninstall `local-rook-data`
        + ```shell
          ./helm -n storage delete local-rook-data
          ```
    * uninstall `local-rook-monitor`
        + ```shell
          ./helm -n storage delete local-rook-monitor
          ```
    * clean the loop devices made by `mknod`
        + ```shell
          for WORKER in "kind-worker" "kind-worker2" "kind-worker3"
          do
              MINOR=${WORKER:11}
              docker exec -it $WORKER umount /data/local-static-provisioner/rook-monitor/$WORKER-volume-monitor
              docker exec -it $WORKER losetup --detach /dev/loop80$MINOR
              docker exec -it $WORKER rm /dev/loop80$MINOR
              rm -f /dev/loop80$MINOR
              docker exec -it $WORKER losetup --detach /dev/loop81$MINOR
              docker exec -it $WORKER rm /dev/loop81$MINOR
              rm -f /dev/loop81$MINOR
          done
          ```
    * uninstall kubernetes cluster
        + ```shell
          ./kind delete cluster
          ```
