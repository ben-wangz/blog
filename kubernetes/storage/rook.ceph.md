# rook ceph

## main usage

* dynamically create and bind pv with pvc for general software

## conceptions

* [what's ceph](../conceptions/ceph.md)
* [what's rook](../conceptions/rook.md)

## purpose

* combination of `local static provisioner` and `rook ceph`
* providing consistent storage cluster which can be consumed by a storage class
* create a storage class in kubernetes to dynamically providing pvs
* created pvs can be used by maria-db installed by helm
    + storage class provided by `rook` will be consumed by maria-db

## pre-requirements

* [a k8s cluster created by kind](../create.local.cluster.with.kind.md) have been read and practised
* [local static provisioner](local.static.provisioner.md) have been read and practised
* we recommend to use [qemu machine](../../qemu/README.md) because we will modify the devices: /dev/loopX

## installation

1. prepare LVs for data sets
    * NOTE: cannot create LVs in docker container
    * NOTE: losetup may fail at first time
    * ```shell
      for MINOR in "1" "2" "3"
      do
          mkdir -p /data/virtual-disks/data \
              && if [ ! -e /dev/loop$MINOR ]; then mknod /dev/loop$MINOR b 7 $MINOR; fi \
              && dd if=/dev/zero of=/data/virtual-disks/data/$HOSTNAME-volume-$MINOR bs=1M count=512 \
              && ( \
                  losetup /dev/loop$MINOR /data/virtual-disks/data/$HOSTNAME-volume-$MINOR \
                      || losetup /dev/loop$MINOR /data/virtual-disks/data/$HOSTNAME-volume-$MINOR \
              ) \
              && vgcreate vgtest$MINOR /dev/loop$MINOR \
              && lvcreate -L 500M -n data$MINOR vgtest$MINOR
      done
      ```
2. [create qemu machine for kind](../create.qemu.machine.for.kind.md)
    * modify configuration of kind to [kind.cluster.yaml](resources/rook.ceph/kind.cluster.yaml.md)
    * we recommend to use [qemu machine](../../qemu/README.md) because we will modify the devices: /dev/loopX
3. mount one "virtual disk" into discovery directory at each worker node
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
4. download and load images to qemu machine(run command at the host of qemu machine)
    * run scripts
      in [download.and.load.function.sh](../resources/create.qemu.machine.for.kind/download.and.load.function.sh.md) to
      load function `download_and_load`
    * ```shell
      TOPIC_DIRECTORY="rook.ceph.storage"
      BASE_URL="https://resource.geekcity.tech/kubernetes/docker-images/x86_64"
      download_and_load $TOPIC_DIRECTORY $BASE_URL \
          "docker.io_k8s.gcr.io_sig-storage_local-volume-provisioner_v2.4.0.dim" \
          "docker.io_rook_ceph_v1.7.3.dim" \
          "docker.io_quay.io_cephcsi_cephcsi_v3.4.0.dim" \
          "docker.io_k8s.gcr.io_sig-storage_csi-node-driver-registrar_v2.2.0.dim" \
          "docker.io_k8s.gcr.io_sig-storage_csi-provisioner_v2.2.2.dim" \
          "docker.io_k8s.gcr.io_sig-storage_csi-snapshotter_v4.1.1.dim" \
          "docker.io_k8s.gcr.io_sig-storage_csi-attacher_v3.2.1.dim" \
          "docker.io_k8s.gcr.io_sig-storage_csi-resizer_v1.2.0.dim" \
          "docker.io_quay.io_csiaddons_volumereplication-operator_v0.1.0.dim" \
          "docker.io_bitnami_mariadb_10.5.12-debian-10-r0.dim" \
          "docker.io_bitnami_bitnami-shell_10-debian-10-r153.dim" \
          "docker.io_bitnami_mysqld-exporter_0.13.0-debian-10-r56.dim"
      ```

5. setup `local static provisioner` provide one pv from each node, and create a storage class named `rook-local-storage`
   which will only be used by `rook cluster`
    * prepare [local.rook.monitor.values.yaml](resources/rook.ceph/local.rook.monitor.values.yaml.md)
    * prepare [local.rook.data.values.yaml](resources/rook.ceph/local.rook.data.values.yaml.md)
    * prepare images
        + run scripts in [load.image.function.sh](../resources/load.image.function.sh.md) to load function `load_image`
        + ```shell
          load_image "localhost:5000" \
              "docker.io/k8s.gcr.io/sig-storage/local-volume-provisioner:v2.4.0"
          ```
    * installation
        + ```shell
          helm install \
              --create-namespace --namespace storage \
              local-rook-monitor \
              https://resource.geekcity.tech/kubernetes/charts/https/github.com/kubernetes-sigs/sig-storage-local-static-provisioner/helm/provisioner/sig-storage-local-static-provisioner.v2.4.0.tar.gz \
              --values local.rook.monitor.values.yaml \
              --atomic
          helm install \
              --create-namespace --namespace storage \
              local-rook-data \
              https://resource.geekcity.tech/kubernetes/charts/https/github.com/kubernetes-sigs/sig-storage-local-static-provisioner/helm/provisioner/sig-storage-local-static-provisioner.v2.4.0.tar.gz \
              --values local.rook.data.values.yaml \
              --atomic
          ```
    * check pods ready
        + ```shell
          kubectl -n storage wait --for=condition=ready pod --all
          ```
    * check pvs created by `local static provisioner`
        + ```shell
          kubectl get pv
          ```
        + expected output is something like
            + ```text
              NAME                CAPACITY   ACCESS MODES   RECLAIM POLICY   STATUS      CLAIM   STORAGECLASS   REASON   AGE
              local-pv-19114d56   500Mi      RWO            Delete           Available           rook-data               8s
              local-pv-3a76903e   36Gi       RWO            Delete           Available           rook-monitor            10s
              local-pv-8dad2f78   36Gi       RWO            Delete           Available           rook-monitor            9s
              local-pv-c0bd8dc8   36Gi       RWO            Delete           Available           rook-monitor            10s
              local-pv-de34da66   500Mi      RWO            Delete           Available           rook-data               8s
              local-pv-ed8026b0   500Mi      RWO            Delete           Available           rook-data               8s
              ```
6. install `rook ceph operator` by helm
    * prepare [rook.ceph.values.yaml](resources/rook.ceph/rook.ceph.values.yaml.md)
    * prepare images
        + run scripts in [load.image.function.sh](../resources/load.image.function.sh.md) to load function `load_image`
        + ```shell
          load_image "localhost:5000" \
              "docker.io/rook/ceph:v1.7.3" \
              "docker.io/quay.io/cephcsi/cephcsi:v3.4.0" \
              "docker.io/k8s.gcr.io/sig-storage/csi-node-driver-registrar:v2.2.0" \
              "docker.io/k8s.gcr.io/sig-storage/csi-provisioner:v2.2.2" \
              "docker.io/k8s.gcr.io/sig-storage/csi-snapshotter:v4.1.1" \
              "docker.io/k8s.gcr.io/sig-storage/csi-attacher:v3.2.1" \
              "docker.io/k8s.gcr.io/sig-storage/csi-resizer:v1.2.0" \
              "docker.io/quay.io/csiaddons/volumereplication-operator:v0.1.0"
          ```
    * ```shell
      helm install \
          --create-namespace --namespace rook-ceph \
          my-rook-ceph-operator \
          https://resource.geekcity.tech/kubernetes/charts/https/charts.rook.io/release/rook-ceph-v1.7.3.tgz \
          --values rook.ceph.values.yaml \
          --atomic
      ```
7. install `rook cluster`
    * prepare [cluster-on-pvc.yaml](resources/rook.ceph/cluster-on-pvc.yaml.md)
        + full configuration can be found
          at [github](https://github.com/rook/rook/blob/v1.7.3/cluster/examples/kubernetes/ceph/cluster-on-pvc.yaml)
    * prepare loop devices
        + ```shell
          # /dev/loop0 will be needed to setup osd
          for WORKER in "kind-worker" "kind-worker2" "kind-worker3"
          do
              docker exec -it $WORKER mknod /dev/loop0 b 7 0
              # 3 more loop back devices for rook-ceph
              docker exec -it $WORKER mknod /dev/loop4 b 7 4
              docker exec -it $WORKER mknod /dev/loop5 b 7 5
              docker exec -it $WORKER mknod /dev/loop6 b 7 6
          done
          ```
    * apply to k8s cluster
        + ```shell
          kubectl -n rook-ceph apply -f cluster-on-pvc.yaml
          # waiting for osd(s) to be ready, 3 pod named rook-ceph-osd-$index-... are expected to be Running
          kubectl -n rook-ceph get pod -w
          ```
8. install rook-ceph toolbox
    * prepare [toolbox.yaml](resources/rook.ceph/toolbox.yaml.md)
    * apply to k8s cluster
        + ```shell
          kubectl -n rook-ceph apply -f toolbox.yaml
          ```
    * check ceph status
        + ```shell
          # 3 osd(s) and 3 mon(s) are expected
          # pgs(if exists any) should be active and clean
          kubectl -n rook-ceph exec -it deploy/rook-ceph-tools -- ceph status
          ```
9. create ceph filesystem and storage class
    * prepare [ceph.filesystem.yaml](resources/rook.ceph/ceph.filesystem.yaml.md)
    * apply ceph filesystem to k8s cluster
        + ```shell
      kubectl -n rook-ceph apply -f ceph.filesystem.yaml
         ```
    * prepare [ceph.storage.class.yaml](resources/rook.ceph/ceph.storage.class.yaml.md)
    * apply ceph storage class to k8s cluster
        + ```shell
      kubectl -n rook-ceph apply -f ceph.storage.class.yaml
         ```
    * check ceph status
        + ```shell
      # pgs(if exists any) should be active and clean
      kubectl -n rook-ceph exec -it deploy/rook-ceph-tools -- ceph status
         ```
        + ```shell
         # one file system is expected
         kubectl -n rook-ceph exec -it deploy/rook-ceph-tools -- ceph fs status
         ```
            + expected output is something like
                * ```text
                 ceph-filesystem-01 - 1 clients
                 ==================
                 RANK      STATE               MDS              ACTIVITY     DNS    INOS   DIRS   CAPS
                 0        active      ceph-filesystem-01-a  Reqs:    0 /s    10     13     12      1
                 0-s   standby-replay  ceph-filesystem-01-b  Evts:    0 /s     0      3      2      0
                 POOL               TYPE     USED  AVAIL
                 ceph-filesystem-01-metadata  metadata  96.0k   469M
                 ceph-filesystem-01-data0     data       0    469M
                 MDS version: ceph version 16.2.5 (0883bdea7337b95e4b611c768c0279868462204a) pacific (stable)
                 ```
10. install maria-db by helm
    * prepare [maria.db.values.yaml](resources/rook.ceph/maria.db.values.yaml.md)
    * prepare images
        + run scripts in [load.image.function.sh](../resources/load.image.function.sh.md) to load function `load_image`
        + ```shell
          load_image "localhost:5000" \
              "docker.io/bitnami/mariadb:10.5.12-debian-10-r0" \
              "docker.io/bitnami/bitnami-shell:10-debian-10-r153" \
              "docker.io/bitnami/mysqld-exporter:0.13.0-debian-10-r56"
          ```
    * helm install maria-db
        + ```shell
          helm install \
              --create-namespace --namespace database \
              maria-db-test \
              https://resource.geekcity.tech/kubernetes/charts/https/charts.bitnami.com/bitnami/mariadb-9.4.2.tgz \
              --values maria.db.values.yaml \
              --atomic \
              --timeout 600s
          ```
    * describe pvc may see "subvolume group 'csi' does not exist"
    * how to solve
        + ```shell
          kubectl -n rook-ceph exec -it deploy/rook-ceph-tools -- ceph fs subvolumegroup create ceph-filesystem-01 csi
          ```
11. connect to maria-db and check the provisioner of `rook`
    * ```shell
      MYSQL_ROOT_PASSWORD=$(kubectl get secret --namespace database maria-db-test-mariadb -o jsonpath="{.data.mariadb-root-password}" | base64 --decode)
      kubectl run maria-db-test-mariadb-client \
          --rm --tty -i \
          --restart='Never' \
          --image localhost:5000/docker.io/bitnami/mariadb:10.5.12-debian-10-r0 \
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
          kubectl -n database get pvc
          kubectl get pv
          ```
12. clean up
    * uninstall maria-db by helm
        + ```shell
          helm -n database uninstall maria-db-test
          # pvc won't be deleted automatically
          kubectl -n database delete pvc data-maria-db-test-mariadb-0
          ```
    * uninstall `rook cluster`
        + ```shell
          kubectl -n rook-ceph delete -f ceph.storage.class.yaml
          kubectl -n rook-ceph delete -f ceph.filesystem.yaml
          kubectl -n rook-ceph patch cephcluster rook-ceph \
              --type merge \
              -p '{"spec":{"cleanupPolicy":{"confirmation":"yes-really-destroy-data"}}}'
          kubectl -n rook-ceph delete -f cluster-on-pvc.yaml
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
          kubectl -n rook-ceph delete -f toolbox.yaml
          ```
    * uninstall `rook ceph operator` by helm
        + ```shell
          helm -n rook-ceph uninstall my-rook-ceph-operator
          ```
    * uninstall `local-rook-data`
        + ```shell
          helm -n storage delete local-rook-data
          ```
    * uninstall `local-rook-monitor`
        + ```shell
          helm -n storage delete local-rook-monitor
          ```
    * delete pvs in namespace named `storage`
    * uninstall kubernetes cluster
        + ```shell
          kind delete cluster
          ```
