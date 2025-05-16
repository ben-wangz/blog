# TiDB

## prepare

1. k8s is ready
2. argocd is ready and logged in
3. local storage class is ready

## installation

1. prepare resources
    * `tidb-operator-crd.yaml`
        + ```yaml
          <!-- @include: tidb-operator-crd.yaml -->
          ```
    * `tidb-operator.yaml`
        + ```yaml
          <!-- @include: tidb-operator.yaml -->
          ```
    * `tidb-cluster.yaml`
        + ```yaml
          <!-- @include: tidb-cluster.yaml -->
          ```
    * `tidb-dashboard.yaml`
        + ```yaml
          <!-- @include: tidb-dashboard.yaml -->
          ```
    * `tidb-monitor.yaml`
        + ```yaml
          <!-- @include: tidb-monitor.yaml -->
          ```
2. Create a namespace
    ```shell
    kubectl get namespace tidb-cluster > /dev/null 2>&1 \
      || kubectl create namespace tidb-cluster
    ```

3. Apply TiDB Operator CRD and sync
    ```shell
    kubectl -n argocd apply -f tidb-operator-crd.yaml
    argocd app sync argocd/tidb-operator-crd
    argocd app wait argocd/tidb-operator-crd
    ```

4. Apply TiDB Operator and sync
    ```shell
    kubectl -n argocd apply -f tidb-operator.yaml
    argocd app sync argocd/tidb-operator
    argocd app wait argocd/tidb-operator
    ```

5. Apply TiDB cluster, Dashboard and monitoring components
    ```shell
    kubectl -n tidb-cluster apply -f tidb-cluster.yaml
    kubectl -n tidb-cluster apply -f tidb-dashboard.yaml
    kubectl -n tidb-cluster apply -f tidb-monitor.yaml
    ```

## tests

1. Check the status of the TiDB cluster
    ```shell
    kubectl -n tidb-cluster get pods
    ```

2. Access the TiDB Dashboard
    ```shell
    kubectl -n tidb-cluster port-forward svc/basic-tidb-dashboard-exposed 12333:12333 --address 0.0.0.0
    ```
    Then open `http://localhost:1234` in your browser. Please replace `<tidb-dashboard-service-name>` with the actual TiDB Dashboard service name.
