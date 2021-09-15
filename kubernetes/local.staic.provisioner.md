# local static provisioner

## main usage

* dynamically create and bind pv with pvc

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
    * [local static provisioner values](resources/local.static.provisioner.values.yaml.md)
    * reference
      to [values.yaml](https://github.com/kubernetes-sigs/sig-storage-local-static-provisioner/blob/v2.4.0/helm/provisioner/values.yaml)
      in kubernetes-sigs/sig-storage-local-static-provisioner
3. install with helm
    * ```shell
      ./helm install --create-namespace --namespace storage local-static-provisioner sig-storage-local-static-provisioner/helm/provisioner/ --values values.yaml --atomic
      ```
4. check if pvs will be created automatically when we mount any storage device into the discovery path
    * jump into a worker node
        + ```shell
          docker exec -it kind-worker bash
          ```
    * mount device with `mount --bind`
        1. jump into a worker node with `docker exec -it kind-worker bash`
        2. mounting 'devices'
            + ```shell
              for index in $(seq 1 3)
              do
                  # TODO change to unique name
                  mkdir -p /data/virtual-disks/volume${index}
                  mkdir -p /data/local-static-provisioner/volume${index}
                  mount --bind /data/virtual-disks/volume${index} /data/local-static-provisioner/volume${index}
              done
              ```
        3. exit from the worker node and check whether pvs created or not by `./kubectl`
            + ```shell
              ./kubectl get pv
              ```
        4. output expected is something like
            + ```text
              TODO
              ```
    * mount device with loop mount
        1. jump into another worker node with `docker exec -it kind-worker2 bash`
        2. mount 'devices'
            + ```shell
              for index in $(seq 1 3)
              do
                  # TODO change to unique name
                  dd if=/dev/zero of=/data/virtual-disks/volume${index} bs=1024 count=1024000
                  mkfs.ext4 /data/virtual-disks/volume${index}
                  mkdir -p /data/local-static-provisioner/volume${index}
                  mount --bind /data/virtual-disks/volume${index} /data/local-static-provisioner/volume${index}
              done
              ```
        3. exit from the worker node and check whether pvs created or not by `./kubectl`
            + ```shell
              ./kubectl get pv
              ```
        4. output expected is something like
            + ```text
              TODO
              ```
5. bind pvc with created pv
6. helm install maria-db whose storage is provided by the local-static-provisioner according to storage class specified