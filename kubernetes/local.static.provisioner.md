# local static provisioner

## main usage

* dynamically create and bind pv with pvc
* limitations
    + not fully automatically
    + bind with `local` storage
* better in production for general software: [rook cephfs](rook.cephfs.md)
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

## practise

### pre-requirements

* [a k8s cluster created by kind](create.local.cluster.with.kind.md)
    + one master
    + two workers
* kubectl
* helm

### purpose

* setup configured local static provisioner with helm
    + discovery path is `/data/local-static-provisioner`
* pv will be created when we mount any storage device into the discovery path
* created pvs can be bind with pvc
* created pvs can be used by maria-db installed by helm
    + **NOTE**: DO NOT use this case in production as maria-db pod will not able to move to other nodes except the
      initial one
    + in a word, `local` volume is not suitable for maria-db
    + but `local` volume recommend for ti-db

### do it

1. clone
    * ```shell
      git clone --single-branch --branch v2.4.0 https://github.com/kubernetes-sigs/sig-storage-local-static-provisioner.git
      ```
2. prepare values for local-static-provisioner
    * [local static provisioner values](resources/local-static-provisioner/values.yaml.md)
    * reference
      to [values.yaml](https://github.com/kubernetes-sigs/sig-storage-local-static-provisioner/blob/v2.4.0/helm/provisioner/values.yaml)
      in kubernetes-sigs/sig-storage-local-static-provisioner
3. install `local-static-provisioner` with helm
    * ```shell
      docker pull k8s.gcr.io/sig-storage/csi-node-driver-registrar:v2.2.0
      ./kind load docker-image k8s.gcr.io/sig-storage/csi-node-driver-registrar:v2.2.0
      ```
    * ```shell
      ./helm install \
          --create-namespace --namespace storage \
          local-static-provisioner \
          $(pwd)/sig-storage-local-static-provisioner/helm/provisioner/ \
          --values $(pwd)/values.yaml \
          --atomic
      ```

    + ```shell
      ./kubectl -n storage wait --for=condition=ready pod --all
      ```
4. check if pvs will be created automatically when we mount any storage device into the discovery path
    * mount device with `mount --bind`
        1. jump into a worker node
            + ```shell
              docker exec -it kind-worker bash
              ```
        2. mounting 'devices'
            + ```shell
              HOSTNAME=$(hostname)
              for index in $(seq 1 3)
              do
                  mkdir -p /data/virtual-disks/$HOSTNAME-volume${index}
                  mkdir -p /data/local-static-provisioner/$HOSTNAME-volume${index}
                  mount --bind /data/virtual-disks/$HOSTNAME-volume${index} /data/local-static-provisioner/$HOSTNAME-volume${index}
              done
              ```
        3. exit from worker node
            + ```shell
              exit
              ```
        4. check whether pvs created or not by `./kubectl`
            + ```shell
              ./kubectl get pv
              ```
        5. output expected is something like
            + ```text
              NAME                CAPACITY   ACCESS MODES   RECLAIM POLICY   STATUS      CLAIM   STORAGECLASS   REASON   AGE
              local-pv-30ff6d55   368Gi      RWO            Delete           Available           local-disks             1s
              local-pv-7a24af4b   368Gi      RWO            Delete           Available           local-disks             1s
              local-pv-847bacd2   368Gi      RWO            Delete           Available           local-disks             1s
              ```
    * mount device with loop mount
        1. jump into another worker node
            + ```shell
              docker exec -it kind-worker2 bash
              ```
        2. mount 'devices'
            + ```shell
              HOSTNAME=$(hostname)
              mkdir -p /data/virtual-disks
              dd if=/dev/zero of=/data/virtual-disks/$HOSTNAME-volume-1 bs=1024 count=1024000
              mkfs.ext4 /data/virtual-disks/$HOSTNAME-volume-1
              mkdir -p /data/local-static-provisioner/$HOSTNAME-volume-1
              mount -o loop /data/virtual-disks/$HOSTNAME-volume-1 /data/local-static-provisioner/$HOSTNAME-volume-1
              // TODO mount another disk will fail: needs to find reason
              ```
        3. exit from worker node
            + ```shell
              exit
              ```
        4. exit from the worker node and check whether pvs created or not by `./kubectl`
            + ```shell
              ./kubectl get pv
              ```
        5. output expected is something like
            + ```text
              NAME                CAPACITY   ACCESS MODES   RECLAIM POLICY   STATUS      CLAIM   STORAGECLASS   REASON   AGE
              ...
              local-pv-9f5472c0   968Mi      RWO            Delete           Available           local-disks             1s
              ```
5. bind pvc with created pv
    + prepare [pvc.test.with.job.yaml](resources/local-static-provisioner/pvc.test.with.job.yaml.md)
    + apply a pvc
        * ```shell
          ./kubectl -n default apply -f $(pwd)/pvc.test.with.job.yaml
          ```
    + check binding
        * ```shell
          ./kubectl -n default wait --for=condition=complete job job-test-pvc
          ```
    + clean up jobs
        * ```shell
          ./kubectl -n default delete -f $(pwd)/pvc.test.with.job.yaml
          ```
6. helm install maria-db whose storage is provided by the local-static-provisioner according to storage class specified
    + prepare [values.maria.db.yaml](resources/local-static-provisioner/maria.db.values.yaml.md)
    + helm install maria-db
        * ```shell
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
    + connect to maria-db
        * start a pod to run mysql client
            + ```shell
              ROOT_PASSWORD=$(./kubectl get secret --namespace database \
                  maria-db-test-mariadb \
                  -o jsonpath="{.data.mariadb-root-password}" \
                  | base64 --decode \
              ) && ./kubectl run maria-db-test-mariadb-client \
                  --rm --tty -i --restart='Never' \
                  --image docker.io/bitnami/mariadb:10.5.12-debian-10-r32 \
                  --env ROOT_PASSWORD=$ROOT_PASSWORD \
                  --namespace database \
                  --command \
                  -- bash
              ```
        * connect to maria-db and show databases
            + ```shell
              echo 'show databases;' \
                  | mysql -h maria-db-test-mariadb.database.svc.cluster.local -uroot -p$ROOT_PASSWORD my_database 
              ```
            + expected output is something like
                * ```text
                  Database
                  information_schema
                  my_database
                  mysql
                  performance_schema
                  test
                  ```
        * check pv bind with pvc
            + ```shell
              ./kubectl get pv
              ```
            + expected output is something like
                * ```text
                  NAME                CAPACITY   ACCESS MODES   RECLAIM POLICY   STATUS      CLAIM                                   STORAGECLASS   REASON   AGE
                  local-pv-282ff4ad   368Gi      RWO            Delete           Bound       database/data-maria-db-test-mariadb-0   local-disks             21m
                  local-pv-85d17cde   968Mi      RWO            Delete           Available                                           local-disks             163m
                  local-pv-da68ee83   368Gi      RWO            Delete           Available                                           local-disks             164m
                  local-pv-fcdea680   368Gi      RWO            Delete           Available                                           local-disks             120m
                  ```
            + use commands to check data in the k8s nodes, you will find `data/` created by maria-db
                * ```shell
                  docker exec kind-worker ls -l /data/local-static-provisioner/kind-worker-volume1
                  docker exec kind-worker ls -l /data/local-static-provisioner/kind-worker-volume2
                  docker exec kind-worker ls -l /data/local-static-provisioner/kind-worker-volume3
                  docker exec kind-worker2 ls -l /data/local-static-provisioner/kind-worker2-volume-1
                  ```
    + helm uninstall maria-db
        * ```shell
          ./helm --namespace database uninstall maria-db-test
          ./kubectl -n database get pvc
          # may change pvc name
          ./kubectl -n database delete pvc data-maria-db-test-mariadb-0
          ```
7. uninstall `local-static-provisioner` with helm
    + ```shell
      ./helm --namespace storage uninstall local-static-provisioner
      ```
8. clean pv created by `local-static-provisioner`
    * ```shell
      ./kubectl get pv
      # may change pv names
      ./kubectl delete pv local-pv-282ff4ad local-pv-85d17cde local-pv-da68ee83 local-pv-fcdea680
      ```
