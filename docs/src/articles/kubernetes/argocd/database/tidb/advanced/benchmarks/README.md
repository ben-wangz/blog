# benchmark for tidb mysql interface

## reference

* https://docs.pingcap.com/zh/tidb/stable/benchmark-tidb-using-sysbench

## introduction

1. What's TiDB MySQL interface
    * TiDB is a distributed SQL database that is compatible with the MySQL protocol. The TiDB MySQL interface allows users to interact with the TiDB database using familiar MySQL commands and tools. This compatibility enables seamless migration of existing MySQL applications to TiDB without significant code changes, providing users with a consistent experience while leveraging TiDB's scalability, high availability, and distributed capabilities.
2. What's SysBench
    * SysBench is an open-source, cross-platform, and multi-threaded benchmarking tool designed to evaluate system performance under various load conditions. It supports a wide range of database systems, including MySQL, PostgreSQL, and TiDB. SysBench can simulate different types of database workloads, such as OLTP (Online Transaction Processing) and OLAP (Online Analytical Processing), helping users to measure the performance of their database systems, identify bottlenecks, and optimize configurations.

## benchmark with SysBench via k8s Job

1. prepare `sysbench.job.yaml`
    * ```yaml
      <!-- @include: sysbench.job.yaml -->
      ```
2. apply the job
    * ```shell
      kubectl -n tidb-cluster apply -f sysbench.job.yaml
      ```
