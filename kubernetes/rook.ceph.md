# rook ceph

## main usage

* dynamically create and bind pv with pvc for general software

## conceptions

* [what's rook](conceptions/rook.md)
* [what's ceph](conceptions/ceph.md)

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
          -smp cpus=3 \
          -m 5G \
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
2. prepare LVs for data sets
    * NOTE: cannot create LVs in docker container
    * ```shell
      for MINOR in "1" "2" "3"
      do
          mkdir -p /data/virtual-disks/data \
              && if [ ! -e /dev/loop$MINOR ]; then mknod /dev/loop$MINOR b 7 $MINOR; fi \
              && dd if=/dev/zero of=/data/virtual-disks/data/$HOSTNAME-volume-$MINOR bs=1M count=512 \
              && losetup /dev/loop$MINOR /data/virtual-disks/data/$HOSTNAME-volume-$MINOR \
              && vgcreate vgtest$MINOR /dev/loop$MINOR \
              && lvcreate -L 512M -n data$MINOR vgtest$MINOR
      done
      ```
3. download kind, kubectl and helm binaries according
   to [download kubernetes binary tools](download.kubernetes.binary.tools.md)
4. setup kubernetes cluster with one master and two workers by `kind`
    * prepare [kind.cluster.yaml](resources/rook-ceph/kind.cluster.yaml.md)
        * we need three workers for setting the count of rook monitor count to 3
    * ```shell
      ./kind create cluster --config $(pwd)/kind.cluster.yaml --image kindest/node:v1.22.1
      ```
5. mount one "virtual disk" into discovery directory at each worker node
    * we need 6 pvs: 3 for monitors and 3 for data sets
    * for monitors
        + ```shell
          for WORKER in "kind-worker" "kind-worker2" "kind-worker3"
          do
              docker exec -it $WORKER bash -c '\
                  set -x && HOSTNAME=$(hostname) \
                      && mkdir -p /data/virtual-disks/monitor/$HOSTNAME-volume-1 \
                      && mkdir -p /data/local-static-provisioner/rook-monitor/$HOSTNAME-volume-1 \
                      && mount --bind /data/virtual-disks/monitor/$HOSTNAME-volume-1 /data/local-static-provisioner/rook-monitor/$HOSTNAME-volume-1 \
              '
          done
          ```
    * for data sets
        + ```shell
          for WORKER in "kind-worker" "kind-worker2" "kind-worker3"
          do
              docker exec -it $WORKER bash -c '\
                  set -x && HOSTNAME=$(hostname) \
                      && PREFIX=kind-worker \
                      && INDEX=${HOSTNAME:${#PREFIX}:1} \
                      && MINOR=${INDEX:-1} \
                      && mkdir -p /data/local-static-provisioner/rook-data \
                      && ln -s /dev/mapper/vgtest$MINOR-data$MINOR /data/local-static-provisioner/rook-data/$HOSTNAME-volume-1
              '
          done
          ```
6. setup `local static provisioner` provide one pv from each node, and create a storage class named `rook-local-storage`
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
              local-pv-19114d56   300Mi      RWO            Delete           Available           rook-data               18s
              local-pv-3a76903e   36Gi       RWO            Delete           Available           rook-monitor            22s
              local-pv-8dad2f78   36Gi       RWO            Delete           Available           rook-monitor            22s
              local-pv-c0bd8dc8   36Gi       RWO            Delete           Available           rook-monitor            22s
              local-pv-de34da66   300Mi      RWO            Delete           Available           rook-data               18s
              local-pv-ed8026b0   300Mi      RWO            Delete           Available           rook-data               18s
              ```
7. install `rook ceph operator` by helm
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
8. install `rook cluster`
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
          # /dev/loop0 will be needed to setup osd
          for WORKER in "kind-worker" "kind-worker2" "kind-worker3"
          do
              docker exec -it $WORKER mknod /dev/loop0 b 7 0
              # 3 more loop back devices for rook-ceph
              docker exec -it $WORKER mknod /dev/loop4 b 7 4
              docker exec -it $WORKER mknod /dev/loop5 b 7 5
              docker exec -it $WORKER mknod /dev/loop6 b 7 6
          done
          ./kubectl -n rook-ceph apply -f cluster-on-pvc.yaml
          # waiting for osd(s) to be ready, 3 pod named rook-ceph-osd-$index-... are expected to be Running
          ./kubectl -n rook-ceph get pod -w
          ```
9. install rook-ceph toolbox
    * prepare [toolbox.yaml](resources/rook-ceph/toolbox.yaml.md)
    * apply to k8s cluster
        + ```shell
          ./kubectl -n rook-ceph apply -f toolbox.yaml
          ```
    * check ceph status
        + ```shell
          # 3 osd(s), 3 mon(s) and 1 pool are expected
          # pgs(if exists any) should be active and clean
          ./kubectl -n rook-ceph exec -it deploy/rook-ceph-tools -- ceph status
          ```
10. create ceph filesystem and storage class
    * prepare [ceph.filesystem.yaml](resources/rook-ceph/ceph.filesystem.yaml.md)
    * apply ceph filesystem to k8s cluster
        + ```shell
          ./kubectl -n rook-ceph apply -f ceph.filesystem.yaml
          ```
    * prepare [ceph.storage.class.yaml](resources/rook-ceph/ceph.storage.class.yaml.md)
    * apply ceph storage class to k8s cluster
        + ```shell
          ./kubectl -n rook-ceph apply -f ceph.storage.class.yaml
          ```
    * check ceph status
        + ```shell
          # pgs(if exists any) should be active and clean
          ./kubectl -n rook-ceph exec -it deploy/rook-ceph-tools -- ceph status
          ```
        + ```shell
          # one file system is expected
          ./kubectl -n rook-ceph exec -it deploy/rook-ceph-tools -- ceph fs status
          ```
            + expected output is something like
                * ```text
                  ceph-filesystem-01 - 0 clients
                  ==================
                  POOL               TYPE     USED  AVAIL
                  ceph-filesystem-01-metadata  metadata     0    277M
                  ceph-filesystem-01-data0     data       0    277M
                  ```
11. install maria-db by helm
    * prepare [maria.db.values.yaml](resources/rook-ceph/maria.db.values.yaml.md)
    * helm install maria-db
        + ```shell
          docker pull docker.io/bitnami/mariadb:10.5.12-debian-10-r32
          ./kind load docker-image docker.io/bitnami/mariadb:10.5.12-debian-10-r32
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
    * describe pvc may see "subvolume group 'csi' does not exist"
    * how to solve
        + ```shell
          kubectl -n rook-ceph exec -it deploy/rook-ceph-tools -- ceph fs subvolumegroup create ceph-filesystem-01 csi
          ```
12. connect to maria-db and check the provisioner of `rook`
    * ```shell
      MYSQL_ROOT_PASSWORD=$(./kubectl get secret --namespace database maria-db-test-mariadb -o jsonpath="{.data.mariadb-root-password}" | base64 --decode)
      ./kubectl run maria-db-test-mariadb-client \
          --rm --tty -i \
          --restart='Never' \
          --image docker.io/bitnami/mariadb:10.5.12-debian-10-r32 \
          --namespace database \
          --env MYSQL_ROOT_PASSWORD=$MYSQL_ROOT_PASSWORD \
          --command -- bash
      ```
    * connect to maria-db with in the pod
        + ```shell
          echo "show databases;" | mysql -h maria-db-test-mariadb.database.svc.cluster.local -uroot -p$MYSQL_ROOT_PASSWORD my_database
          ```
    * checking pvc and pv
        + ```shell
          ./kubectl -n database get pvc
          ./kubectl get pv
          ```
13. clean up
    * uninstall maria-db by helm
        + ```shell
          ./helm -n database uninstall maria-db-test
          # pvc won't be deleted automatically
          ./kubectl -n database delete pvc data-maria-db-test-mariadb-0
          ```
    * uninstall `rook cluster`
        + ```shell
          ./kubectl -n rook-ceph delete -f ceph.storage.class.yaml
          ./kubectl -n rook-ceph delete -f ceph.filesystem.yaml
          ./kubectl -n rook-ceph delete -f cluster-on-pvc.yaml
          ```
    * delete dataDirHostPath(`/var/lib/rook`), which is defined by `cluster-on-pvc.yaml`, at each node
        + ```shell
          for WORKER in "kind-worker" "kind-worker2" "kind-worker3"
          do
              docker exec -it $WORKER rm -rf /var/lib/rook
          done
          ```
    * uninstall `toolbox`
        + ```shell
          ./kubectl -n rook-ceph delete -f toolbox.yaml
          ```
    * uninstall `rook ceph operator` by helm
        + ```shell
          ./helm -n rook-ceph uninstall my-rook-ceph-operator
          ```
    * delete pvs in namespace named `storage`
    * uninstall `local-rook-data`
        + ```shell
          ./helm -n storage delete local-rook-data
          ```
    * uninstall `local-rook-monitor`
        + ```shell
          ./helm -n storage delete local-rook-monitor
          ```
    * uninstall kubernetes cluster
        + ```shell
          ./kind delete cluster
          ```
