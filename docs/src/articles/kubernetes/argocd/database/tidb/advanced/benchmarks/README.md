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
    * change the env `SYSBENCH_THREADS` to control the number of threads
2. apply the job
    * ```shell
      kubectl -n tidb-cluster apply -f sysbench.job.yaml
      ```
3. check logs
    * ```shell
      kubectl -n tidb-cluster logs -l job-name=sysbench-for-mysql-oltp
      ```
    * key logs example
        + ```text
          sysbench results:
          ...
          SQL statistics:
              queries performed:
                  read:                            958258
                  write:                           273670
                  other:                           136841
                  total:                           1368769
              transactions:                        68394  (113.96 per sec.)
              queries:                             1368769 (2280.72 per sec.)
              ignored errors:                      53     (0.09 per sec.)
              reconnects:                          0      (0.00 per sec.)
          
          General statistics:
              total time:                          600.1459s
              total number of events:              68394
          
          Latency (ms):
                   min:                                   26.18
                   avg:                                  140.38
                   max:                                 7127.99
                   95th percentile:                      211.60
                   sum:                              9601310.58
          
          Threads fairness:
              events (avg/stddev):           4274.6250/11.18
              execution time (avg/stddev):   600.0819/0.03
          ```
