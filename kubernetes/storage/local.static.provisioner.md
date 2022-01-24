# local static provisioner

## main usage

* dynamically create and bind pv with pvc
* limitations
    + not fully automatically
    + bind with `local` storage
* better in production for general software: [rook ceph](rook.ceph.md)
* but it's recommend for software, which implement data replication and recovery, like [TiDB]() // TODO

## conceptions

* `pv` and `pvc`: [reference of pv and pvc](https://kubernetes.io/docs/concepts/storage/persistent-volumes)
* `hostPath` volume: [reference of hostPath](https://kubernetes.io/docs/concepts/storage/volumes/#hostpath)
* `local` volume: [reference of local](https://kubernetes.io/docs/concepts/storage/volumes/#local)
* `mount --bind $path-source $path-target`
    + instead of mounting a device to a path, `mount --bind` can mount a path to another path
    + in this example: $path-source is mounted to $path-target
* loop mount can mount a file virtualized device as a filesystem to a path
    + ```shell
      dd if=/dev/zero of=/data/virtual-disks/file.fs bs=1024 count=1024000
      mount /data/virtual-disks/file.fs /data/local-static-provisioner/file.fs/
      ```

## purpose

* setup configured local static provisioner with helm
    + discovery path is `/data/local-static-provisioner`
* pv will be created when we mount any storage device into the discovery path
* created pvs can be bind with pvc
* created pvs for maria-db installed by helm
    + **NOTE**: DO NOT use this case in production as maria-db pod will not able to move to other nodes except the
      initial one
    + in a word, `local` volume is not suitable for maria-db
    + but `local` volume recommend for ti-db

## installation

1. [create qemu machine for kind](../create.qemu.machine.for.kind.md)
    * modify configuration of kind to [kind.cluster.yaml](resources/local.static.provisioner/kind.cluster.yaml.md)
    * we recommend to use [qemu machine](../../qemu/README.md) because we will modify the devices: /dev/loopX
2. download and load images to qemu machine(run command at the host of qemu machine)
    * run scripts
      in [download.and.load.function.sh](../resources/create.qemu.machine.for.kind/download.and.load.function.sh.md) to
      load function `download_and_load`
    * ```shell
      TOPIC_DIRECTORY="local.static.provisioner.storage"
      BASE_URL="https://resource.geekcity.tech/kubernetes/docker-images/x86_64"
      download_and_load $TOPIC_DIRECTORY $BASE_URL \
          "docker.io_k8s.gcr.io_sig-storage_local-volume-provisioner_v2.4.0.dim" \
          "docker.io_busybox_1.33.1-uclibc.dim" \
          "docker.io_bitnami_mariadb_10.5.12-debian-10-r0.dim" \
          "docker.io_bitnami_bitnami-shell_10-debian-10-r153.dim" \
          "docker.io_bitnami_mysqld-exporter_0.13.0-debian-10-r56.dim"
      ```
3. install local static provisioner
    * prepare
      [local.static.provisioner.values.yaml](resources/local.static.provisioner/local.static.provisioner.values.yaml.md)
        + reference to
          [values.yaml](https://github.com/kubernetes-sigs/sig-storage-local-static-provisioner/blob/v2.4.0/helm/provisioner/values.yaml)
          in kubernetes-sigs/sig-storage-local-static-provisioner
    * prepare images
        + run scripts in [load.image.function.sh](../resources/load.image.function.sh.md) to load function `load_image`
        + ```shell
          load_image "localhost:5000" \
              "docker.io/k8s.gcr.io/sig-storage/local-volume-provisioner:v2.4.0"
          ```
    * ```shell
      helm install \
          --create-namespace --namespace storage \
          local-static-provisioner \
          https://resource.geekcity.tech/kubernetes/charts/https/github.com/kubernetes-sigs/sig-storage-local-static-provisioner/helm/provisioner/sig-storage-local-static-provisioner.v2.4.0.tar.gz \
          --values local.static.provisioner.values.yaml \
          --atomic
      ```
    * waiting for ready
        + ```shell
          kubectl -n storage wait --for=condition=ready pod --all
          ```
4. check if pvs will be created automatically when we mount any storage device into the discovery path
    * mount device with `mount --bind`
        1. jump into a worker node
            + ```shell
              docker exec -it kind-worker bash
              ```
        2. mounting `devices`
            + ```shell
              HOSTNAME=$(hostname)
              for index in $(seq 1 3)
              do
                  mkdir -p /data/virtual-disks/$HOSTNAME-volume-$index
                  mkdir -p /data/local-static-provisioner/$HOSTNAME-volume-$index
                  mount --bind /data/virtual-disks/$HOSTNAME-volume-$index /data/local-static-provisioner/$HOSTNAME-volume-$index
              done
              ```
        3. exit from worker node
            + ```shell
              exit
              ```
        4. check whether pvs created or not
            + ```shell
              kubectl get pv
              ```
        5. output expected is something like
            + ```text
              NAME                CAPACITY   ACCESS MODES   RECLAIM POLICY   STATUS      CLAIM   STORAGECLASS   REASON   AGE
              local-pv-282ff4ad   36Gi       RWO            Delete           Available           local-disks             2s
              local-pv-da68ee83   36Gi       RWO            Delete           Available           local-disks             2s
              local-pv-fcdea680   36Gi       RWO            Delete           Available           local-disks             2s
              ```
    * mount device with loop mount
        1. jump into another worker node
            + ```shell
              docker exec -it kind-worker2 bash
              ```
        2. mount 'devices'
            + ```shell
              HOSTNAME=$(hostname)
              mkdir -p /data/virtual-disks/device
              for index in $(seq 1 3)
              do
                  dd if=/dev/zero of=/data/virtual-disks/$HOSTNAME-volume-$index bs=1024 count=1024000
                  mkfs.ext4 /data/virtual-disks/$HOSTNAME-volume-$index
                  # TODO risk of mknod major/minor, this is just for testing
                  mknod -m 0660 /dev/loop80$index b 7 80$index
                  losetup /dev/loop80$index /data/virtual-disks/$HOSTNAME-volume-$index
                  mkdir -p /data/local-static-provisioner/$HOSTNAME-volume-$index
                  mount /data/virtual-disks/$HOSTNAME-volume-$index /data/local-static-provisioner/$HOSTNAME-volume-$index
              done
              ```
        3. exit from worker node
            + ```shell
              exit
              ```
        4. exit from the worker node and check whether pvs created or not
            + ```shell
              kubectl get pv
              ```
        5. output expected is something like
            + ```text
              NAME                CAPACITY   ACCESS MODES   RECLAIM POLICY   STATUS      CLAIM   STORAGECLASS   REASON   AGE
              ...
              local-pv-85d17cde   968Mi      RWO            Delete           Available           local-disks             14s
              local-pv-c7257780   968Mi      RWO            Delete           Available           local-disks             4s
              local-pv-cbcd4e25   968Mi      RWO            Delete           Available           local-disks             4s
              ```
5. bind pvc with created pv
    * prepare [pvc.test.with.job.yaml](resources/local.static.provisioner/pvc.test.with.job.yaml.md)
    * load images
        + ```shell
          load_image "localhost:5000" \
              "docker.io/busybox:1.33.1-uclibc"
          ```
    * apply a pvc
        + ```shell
          kubectl -n default apply -f pvc.test.with.job.yaml
          ```
    * check binding
        + ```shell
          kubectl -n default wait --for=condition=complete job job-test-pvc
          ```
    * clean up jobs
        + ```shell
          kubectl -n default delete -f pvc.test.with.job.yaml
          ```
6. helm install maria-db whose storage is provided by the local-static-provisioner according to storage class specified
    * prepare [maria.db.values.yaml](resources/local.static.provisioner/maria.db.values.yaml.md)
    * helm install maria-db
        + ```shell
          load_image "localhost:5000" \
              "docker.io/bitnami/mariadb:10.5.12-debian-10-r0" \
              "docker.io/bitnami/bitnami-shell:10-debian-10-r153" \
              "docker.io/bitnami/mysqld-exporter:0.13.0-debian-10-r56"
          ```
        + ```shell
          helm install \
              --create-namespace --namespace database \
              maria-db-test \
              https://resource.geekcity.tech/kubernetes/charts/https/charts.bitnami.com/bitnami/mariadb-9.4.2.tgz \
              --values maria.db.values.yaml \
              --atomic \
              --timeout 600s
          ```
    * connect to maria-db
        + start a pod to run mysql client
            * ```shell
              ROOT_PASSWORD=$(kubectl get secret --namespace database \
                  maria-db-test-mariadb \
                  -o jsonpath="{.data.mariadb-root-password}" \
                  | base64 --decode \
              ) && kubectl run maria-db-test-mariadb-client \
                  --rm --tty -i --restart='Never' \
                  --image localhost:5000/docker.io/bitnami/mariadb:10.5.12-debian-10-r0 \
                  --env ROOT_PASSWORD=$ROOT_PASSWORD \
                  --namespace database \
                  --command \
                  -- bash
              ```
        + connect to maria-db and show databases
            * ```shell
              echo 'show databases;' \
                  | mysql -h maria-db-test-mariadb.database.svc.cluster.local -uroot -p$ROOT_PASSWORD my_database
              ```
            * expected output is something like
                + ```text
                  Database
                  information_schema
                  my_database
                  mysql
                  performance_schema
                  test
                  ```
            * exit from mysql client and the pod
        + check pv bind with pvc
            * ```shell
              kubectl get pv
              ```
            * expected output is something like
                + ```text
                  NAME                CAPACITY   ACCESS MODES   RECLAIM POLICY   STATUS      CLAIM                                   STORAGECLASS   REASON   AGE
                  local-pv-282ff4ad   36Gi       RWO            Delete           Bound       database/data-maria-db-test-mariadb-0   local-disks             10m
                  ...
                  ```
            * use commands to check data in the k8s nodes, you will find `data/` created by maria-db
                + ```shell
                  for host in "kind-worker" "kind-worker2"
                  do
                      for index in $(seq 1 3)
                      do
                          docker exec $host ls -l /data/local-static-provisioner/$host-volume-$index
                      done
                  done
                  ```
    * helm uninstall maria-db
        + ```shell
          helm --namespace database uninstall maria-db-test
          kubectl -n database get pvc
          # may change pvc name
          kubectl -n database delete pvc data-maria-db-test-mariadb-0
          ```
7. uninstall `local-static-provisioner` with helm
    * ```shell
      helm --namespace storage uninstall local-static-provisioner
      ```
8. clean pv created by `local-static-provisioner`
    * ```shell
      kubectl get pv
      # may change pv names
      kubectl delete pv local-pv-282ff4ad local-pv-85d17cde local-pv-c7257780 local-pv-cbcd4e25 local-pv-da68ee83 local-pv-fcdea680
      ```
9. clean the loop devices made by `mknod`
    * ```shell
      for index in $(seq 1 3)
      do 
          docker exec -it kind-worker2 losetup --detach /dev/loop80$index 
          docker exec -it kind-worker2 rm /dev/loop80$index
          docker exec -it kind-worker2 umount /data/local-static-provisioner/kind-worker2-volume-$index 
          docker exec -it kind-worker2 rm /data/virtual-disks/kind-worker2-volume-$index 
          rm -f /dev/loop80$index 
      done
      ```
10. delete kubernetes cluster
    * ```shell
      kind delete cluster
      ```
