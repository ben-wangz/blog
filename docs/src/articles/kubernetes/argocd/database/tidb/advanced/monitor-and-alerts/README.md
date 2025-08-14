# TiDB monitor and alerts

## reference

* https://docs.pingcap.com/zh/tidb/stable/tidb-monitoring-framework
* https://docs.pingcap.com/zh/tidb-in-kubernetes/stable/monitor-a-tidb-cluster

## installation

1. prepare the `tidb-monitor.yaml`
    * ```yaml
      <!-- @include: tidb-monitor.yaml -->
      ```
2. apply TiDB monitor components
    * create a secret named `basic-grafana-credentials` to store the credentials of the grafana admin user
        + ```shell
          kubectl -n tidb-cluster create secret generic basic-grafana-credentials \
            --from-literal=username=admin \
            --from-literal=password=$(tr -dc A-Za-z0-9 </dev/urandom | head -c 16)
          ```
    * apply resources
        + ```shell
          kubectl -n tidb-cluster apply -f tidb-monitor.yaml
          ```

## check the metrics of the TiDB cluster

1. extract credentials
    + ```shell
      kubectl -n tidb-cluster get secret basic-grafana-credentials -o jsonpath="{.data.username}" | base64 -d && echo
      kubectl -n tidb-cluster get secret basic-grafana-credentials -o jsonpath="{.data.password}" | base64 -d && echo
      ```
2. visit with web browser
    + url: https://tidb-monitor.dev.geekcity.tech

## uninstallation

* ```shell
  kubectl -n tidb-cluster delete -f tidb-monitor.yaml
  ```
