# tidb scale in and out

## reference

* https://docs.pingcap.com/zh/tidb-in-kubernetes/stable/scale-a-tidb-cluster/

## introduction

* Horizontal scaling of TiDB is achieved by increasing or decreasing the number of Pods, and operations on PD, TiKV, and TiDB will be carried out in the order of the replicas value.
    + Scaling In: Decrease the replicas value of the component, and delete component Pods in descending order of the Pod number.
    + Scaling Out: Increase the replicas value of the component, and add component Pods in ascending order of the Pod number.

## Horizontal Scaling of PD, TiKV and TiDB

* for PD
    + ```shell
      kubectl patch -n tidb-cluster TidbCluster basic --type merge --patch '{"spec":{"pd":{"replicas":5}}}'
      ```
    + ```shell
      kubectl patch -n tidb-cluster TidbCluster basic --type merge --patch '{"spec":{"pd":{"replicas":3}}}'
      ```
* for TiKV
    + ```shell
      kubectl patch -n tidb-cluster TidbCluster basic --type merge --patch '{"spec":{"tikv":{"replicas":5}}}'
      ```
    + ```shell
      kubectl patch -n tidb-cluster TidbCluster basic --type merge --patch '{"spec":{"tikv":{"replicas":3}}}'
      ```
* for TiDB
    + ```shell
      kubectl patch -n tidb-cluster TidbCluster basic --type merge --patch '{"spec":{"tidb":{"replicas":5}}}'
      ```
    + ```shell
      kubectl patch -n tidb-cluster TidbCluster basic --type merge --patch '{"spec":{"tidb":{"replicas":3}}}'
      ```

## Horizontal Scaling of TiFlash

* please refer to docs of TiDB

## Horizontal Scaling of TiCDC

* please refer to docs of TiDB

