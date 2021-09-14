# local static provisioner

## main usage

* dynamically create and bind pv with pvc

## conceptions

* `pv` and `pvc`: [reference of pv and pvc](https://kubernetes.io/docs/concepts/storage/persistent-volumes)
* `hostPath` volume: [reference of hostPath](https://kubernetes.io/docs/concepts/storage/volumes/#hostpath)
* `local` volume: [reference of local](https://kubernetes.io/docs/concepts/storage/volumes/#local)

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