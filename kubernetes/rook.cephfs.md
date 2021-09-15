# rook cephfs

## main usage

* dynamically create and bind pv with pvc for general software

## conceptions

* what's rook
* what's cephfs

## practise

### pre-requirements

* [a k8s cluster created by kind](create.local.cluster.with.kind.md)
    + one master
    + two workers
* kubectl
* helm
* [local static provisioner](local.static.provisioner.md) have been read and practised

### purpose

* combination of `local static provisioner` and `rook cephfs`
* providing consistent storage cluster which can be consumed by a storage class
* create a storage class in kubernetes to dynamically providing pvs
* created pvs can be used by maria-db installed by helm
    + storage class provided by `rook` will be consumed by maria-db

### do it

1. setup `local static provisioner` provide one pv from each node, and create a storage class named `rook-local-storage`
   which will only be used by `rook cluster`
2. install `rook cephfs operator` by helm
3. install `rook cluster`
4. install maria-db by helm
5. connect to maria-db and check the provisioner of `rook`
6. clean up
    * uninstall maria-db by helm
    * uninstall `rook cluster`
    * uninstall `rook cephfs operator` by helm
